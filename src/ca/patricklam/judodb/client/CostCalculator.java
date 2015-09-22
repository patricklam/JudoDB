package ca.patricklam.judodb.client;

import java.util.List;
import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.datepicker.client.CalendarUtil;

public class CostCalculator {
    static int remainingWeeks(SessionSummary ss, Date dateInscription) {
        try {
            Date sessionEnd = Constants.DB_DATE_FORMAT.parse(ss.getLastClassDate());

            double remainingWeeks = (CalendarUtil.getDaysBetween(dateInscription, sessionEnd) + 6) / 7.0;
            return (int)(remainingWeeks+0.5);
        }
        catch (NullPointerException e) { return 1; }
        catch (IllegalArgumentException e) { return 1; }
    }

    static int totalWeeks(SessionSummary ss, Date dateInscription) {
        try {
            if (ss == null) return 1;
            Date sessionStart = Constants.DB_DATE_FORMAT.parse(ss.getFirstClassDate());
            Date sessionEnd = Constants.DB_DATE_FORMAT.parse(ss.getLastClassDate());
            double totalWeeks = (CalendarUtil.getDaysBetween(sessionStart, sessionEnd) + 6) / 7.0;
            return (int)(totalWeeks+0.5);
        }
        catch (NullPointerException e) { return 1; }
        catch (IllegalArgumentException e) { return 1; }
    }

    private static final double getFraisCours(SessionSummary ss, Constants.Division c, int sessionCount, ClubPrix[] cpA) {
        for (ClubPrix cp : cpA) {
            if (cp.getDivisionAbbrev().equals(c.abbrev)) {
                if (sessionCount == 2)
                    return Double.parseDouble(cp.getFrais2Session());
                return Double.parseDouble(cp.getFrais1Session());
            }
        }
        return 0.0;
    }

    private static final double getFraisJudoQC(SessionSummary ss, Constants.Division c, ClubPrix[] cpA) {
        for (ClubPrix cp : cpA) {
            if (cp.getDivisionAbbrev().equals(c.abbrev)) {
                return Double.parseDouble(cp.getFraisJudoQC());
            }
        }
        return 0.0;
    }

    static double fraisCours(SessionSummary ss, ClientData cd, ServiceData sd, ClubPrix[] cpA) {
        Constants.Division c = cd.getDivision(ss.getYear());
        int sessionCount = 2; try { sessionCount = sd.getSessionCount(); } catch (Exception e) {}
        return getFraisCours(ss, c, sessionCount, cpA);
    }

    static double proratedFraisCours(SessionSummary ss, ClientData cd, ServiceData sd, ClubSummary cs, ClubPrix[] cpA) {
        double baseCost = fraisCours(ss, cd, sd, cpA);
        boolean enableProrata = cs.getDefaultProrata();
        if (!enableProrata || sd == null || sd.getDateInscription() == null || sd.getDateInscription() == "0000-00-00")
            return baseCost;

        Date dateInscription = null;
        try { dateInscription = Constants.DB_DATE_FORMAT.parse(sd.getDateInscription()); } catch (Exception e) { return baseCost; }

        // calculate number of weeks between start of session and dateInscription
        // calculate total number of weeks
        // divide, then add Constants.PRORATA_PENALITE
        // but only use the prorated frais if ew < tw - 4

        int rw = remainingWeeks(ss, dateInscription);
        int tw = totalWeeks(ss, dateInscription);
        double supplement_prorata = 0;
        if (cs.getSupplementProrata() != null && !cs.getSupplementProrata().equals(""))
            supplement_prorata = Double.parseDouble(cs.getSupplementProrata());
        double prorataCost = baseCost * ((double)rw / (double)tw) + supplement_prorata;
        if (rw < tw - 4)
            return Math.min(baseCost, prorataCost);
        else
            return baseCost;
    }

    static double affiliationFrais(SessionSummary ss, ClientData cd, ServiceData sd, ClubPrix[] cpA) {
        if (sd == null) return 0.0;

        boolean sans_affiliation = sd.getSansAffiliation();
        boolean affiliation_initiation = sd.getAffiliationInitiation();
        boolean affiliation_ecole = sd.getAffiliationEcole();
        Constants.Division c = cd.getDivision(ss.getYear());

        double dAffiliationFrais = 0.0;
        if (!sans_affiliation) {
            if (affiliation_initiation)
                dAffiliationFrais = Constants.COUT_JUDOQC_INITIATION;
            else if (affiliation_ecole)
                dAffiliationFrais = Constants.COUT_JUDOQC_ECOLE;
            else
                dAffiliationFrais = getFraisJudoQC(ss, c, cpA);
        }
        return dAffiliationFrais;
    }

    static double suppFrais(ServiceData sd, ClubSummary cs, ProduitSummary ps, double fraisSoFar) {
        if (sd == null) return 0.0;

        double judogiFrais = 0.0;
        try { judogiFrais = Double.parseDouble(ps.getMontant()); } catch (Exception e) {}
        boolean passeport = sd.getPasseport();
        boolean resident = sd.getResident();
        boolean paypal = sd.getPaypal();

        double dSuppFrais = judogiFrais;
        if (passeport)
            dSuppFrais += Constants.PASSEPORT_JUDO_QC;
        if (resident)
            dSuppFrais -= Double.parseDouble(cs.getEscompteResident());

        if (paypal)
            dSuppFrais += Constants.PAYPAL_PCT / 100.0 * (dSuppFrais + fraisSoFar);
        return dSuppFrais;
    }

    static boolean isCasSpecial(ServiceData sd, EscompteSummary es) {
        return es != null && es.getAmountPercent().equals("-1");
    }

    static EscompteSummary getApplicableEscompte(ServiceData sd,
                                                 List<EscompteSummary> escompteSummaries) {
        EscompteSummary es = null;
        for (EscompteSummary e : escompteSummaries) {
            if (e.getId().equals(sd.getEscompteId())) {
                es = e; break;
            }
        }
        return es;
    }

    static ProduitSummary getApplicableProduit(ServiceData sd,
						List<ProduitSummary> produitSummaries) {
        ProduitSummary ps = null;
        for (ProduitSummary p : produitSummaries) {
            if (p.getId().equals(sd.getJudogi())) {
                ps = p; break;
            }
        }
        return ps;
    }

    static double escompteFrais(ServiceData sd, double dCategorieFrais,
                                List<EscompteSummary> escompteSummaries) {
        if (sd == null) return 0.0;

        EscompteSummary es = getApplicableEscompte(sd, escompteSummaries);
        if (es == null) return 0.0;

        double escomptePct = 0.0;
        boolean emptyPct = false;
        if (isCasSpecial(sd, es)) {
	    // cas special, use amount stored in sd
	    if (sd.getCasSpecialPct().equals("-1"))
		return Double.parseDouble(sd.getEscompteFrais());

            escomptePct = Double.parseDouble(sd.getCasSpecialPct());
        } else {
            if (es.getAmountPercent().equals(""))
                emptyPct = true;
            else
                escomptePct = Double.parseDouble(es.getAmountPercent());
        }
        if (emptyPct)
            return es.getAmountAbsolute().equals("") ? 0.0 : -Double.parseDouble(es.getAmountAbsolute());
        return -dCategorieFrais * (escomptePct / 100.0);
    }

    static String getWeeksSummary(SessionSummary ss, Date dateInscription) {
        int rw = remainingWeeks(ss, dateInscription);
        int tw = totalWeeks(ss, dateInscription);
        if (rw < tw)
            return " ("+rw+"/"+tw+")";
        else
            return "";
    }

    /** Model-level method to recompute costs. */
    public static void recompute(SessionSummary ss, ClientData cd, ServiceData sd, ClubSummary cs, ProduitSummary ps, boolean prorataOverride, ClubPrix[] cpA, List<EscompteSummary> escompteSummaries) {
      if (cpA == null) return;
      if (ss == null) return;

      double dCategorieFrais = proratedFraisCours(ss, cd, sd, cs, cpA);
      if (!prorataOverride) dCategorieFrais = fraisCours(ss, cd, sd, cpA);
      double dEscompteFrais = escompteFrais(sd, dCategorieFrais, escompteSummaries);
      double dAffiliationFrais = affiliationFrais(ss, cd, sd, cpA);
      double dSuppFrais = suppFrais(sd, cs, ps, dCategorieFrais + dEscompteFrais + dAffiliationFrais);

      if (sd != null) {
          sd.setCategorieFrais(Double.toString(dCategorieFrais));
          sd.setEscompteFrais(Double.toString(dEscompteFrais));
          sd.setAffiliationFrais(Double.toString(dAffiliationFrais));
          sd.setSuppFrais(Double.toString(dSuppFrais));
          sd.setFrais(Double.toString(dCategorieFrais + dAffiliationFrais + dEscompteFrais + dSuppFrais));
      }
    }
}
