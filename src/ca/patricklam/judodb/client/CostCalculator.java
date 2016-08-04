// -*-  indent-tabs-mode:nil; c-basic-offset:4; -*-
package ca.patricklam.judodb.client;

import java.util.Collection;
import java.util.List;
import java.util.LinkedList;
import java.util.Date;
import java.util.Collections;
import java.util.HashMap;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.datepicker.client.CalendarUtil;

public class CostCalculator {
    // constants for Prix
    public final static String JUDO_QC = null;
    public final static String ALL_DIVISIONS = "*";
    public final static String ALL_COURS = "-1";

    static int remainingWeeks(ServiceData sd, SessionSummary ss, Date dateInscription, List<SessionSummary> sessionSummaries) {
        try {
            if (ss == null) return 0;
            Date sessionEnd = Constants.DB_DATE_FORMAT.parse(ss.getLastClassDate());

            double remainingWeeks = (CalendarUtil.getDaysBetween(dateInscription, sessionEnd) + 6) / 7.0;

            if (sd.getSessionCount() == 2) {
                for (SessionSummary ssp : sessionSummaries) {
                    if (ssp.getSeqno().equals(ss.getLinkedSeqno())) {
                        remainingWeeks += totalWeeks(ssp);
                    }
                }
            }

            return (int)(remainingWeeks+0.5);
        }
        catch (NullPointerException e) { return 1; }
        catch (IllegalArgumentException e) { return 1; }
    }

    static int totalWeeks(SessionSummary ss) {
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

    static int totalWeeksBothSessions(ServiceData sd, SessionSummary ss, List<SessionSummary> sessionSummaries) {
        int totalWeeks = totalWeeks(ss);
        if (ss != null && sd.getSessionCount() == 2) {
            for (SessionSummary ssp : sessionSummaries) {
                if (ssp.getSeqno().equals(ss.getLinkedSeqno())) {
                    totalWeeks += totalWeeks(ssp);
                }
            }
        }
        return totalWeeks;
    }

    static double fraisCours(ClientData cd, ServiceData sd, ClubSummary cs, List<SessionSummary> sessionSummaries, List<CoursSummary> coursSummaries, List<Prix> prixSummaries) {
        Constants.Division d = null;
        String[] sessionsByName = sd.getSessions().split(" ");
        StringBuilder sessions = new StringBuilder();
        for (String s : sessionsByName) {
            for (SessionSummary ss : sessionSummaries) {
                if (ss.getAbbrev().equals(s)) {
                    d = cd.getDivision(ss.getYear());
                    break;
                }
            }
        }
        if (d == null) return -1.0;
        return Double.parseDouble(getFrais(prixSummaries, cs,
                                           JudoDB.sessionSeqnosFromAbbrevs(sd.getSessions(), sessionSummaries),
                                           d.abbrev,
                                           sd.getCours()));
    }

    static double proratedFraisCours(ClientData cd, ServiceData sd, ClubSummary cs, SessionSummary ss, List<SessionSummary> sessionSummaries, List<CoursSummary> coursSummaries, List<Prix> prixSummaries) {
        double baseCost = fraisCours(cd, sd, cs, sessionSummaries, coursSummaries, prixSummaries);
        if (sd == null || sd.getDateInscription() == null || sd.getDateInscription() == Constants.DB_DUMMY_DATE)
            return baseCost;

        Date dateInscription = null;
        try { dateInscription = Constants.DB_DATE_FORMAT.parse(sd.getDateInscription()); } catch (Exception e) { return baseCost; }

        // calculate number of weeks between start of session and dateInscription
        // calculate total number of weeks
        // divide, then add Constants.PRORATA_PENALITE
        // but only use the prorated frais if ew < tw - 4

        int rw = remainingWeeks(sd, ss, dateInscription, sessionSummaries);
        int tw = totalWeeksBothSessions(sd, ss, sessionSummaries);
        double supplement_prorata = 0;
        if (cs.getSupplementProrata() != null && !cs.getSupplementProrata().equals(""))
            supplement_prorata = Double.parseDouble(cs.getSupplementProrata());
        double prorataCost = baseCost * ((double)rw / (double)tw) + supplement_prorata;
        if (rw < tw - 4)
            return Math.min(baseCost, prorataCost);
        else
            return baseCost;
    }

    private static final double getFraisJudoQC(SessionSummary ss, Constants.Division c, List<SessionSummary> sessionSummaries, List<Prix> prixSummaries) {
        // xxx fix ss.getSeqno().
        StringBuilder seqnoPair = new StringBuilder();
        if (ss.isPrimary()) {
            seqnoPair.append(ss.getSeqno());
            SessionSummary linked = JudoDB.getLinkedSession(ss, sessionSummaries);
            if (ss != linked) {
                seqnoPair.append(" ");
                seqnoPair.append(linked.getSeqno());
            }
        } else {
            SessionSummary linked = JudoDB.getLinkedSession(ss, sessionSummaries);
            seqnoPair.append(linked.getSeqno());
            if (ss != linked) {
                seqnoPair.append(" ");
                seqnoPair.append(ss.getSeqno());
            }
        }
        return Double.parseDouble(getFrais(prixSummaries, null,
                                           seqnoPair.toString(), c.abbrev,
                                           ALL_COURS));
    }

    static double affiliationFrais(ClientData cd, ServiceData sd, SessionSummary ss, List<SessionSummary> sessionSummaries, List<Prix> prixSummaries) {
        if (sd == null) return 0.0;

        boolean sans_affiliation = sd.getSansAffiliation();
        boolean affiliation_initiation = sd.getAffiliationInitiation();
        boolean affiliation_ecole = sd.getAffiliationEcole();
        boolean affiliation_parascolaire = sd.getAffiliationParascolaire();
        Constants.Division c = cd.getDivision(ss.getYear());

        double dAffiliationFrais = 0.0;
        if (!sans_affiliation) {
            if (affiliation_initiation)
                dAffiliationFrais = Constants.COUT_JUDOQC_INITIATION;
            else if (affiliation_ecole)
                dAffiliationFrais = Constants.COUT_JUDOQC_ECOLE;
            else if (affiliation_parascolaire)
                dAffiliationFrais = Constants.COUT_JUDOQC_PARASCOLAIRE;
            else
                dAffiliationFrais = getFraisJudoQC(ss, c, sessionSummaries, prixSummaries);
        }
        return dAffiliationFrais;
    }

    static double suppFrais(ServiceData sd, ClubSummary cs, Collection<ProduitSummary> ps, double fraisSoFar) {
        if (sd == null) return 0.0;

        double judogiFrais = 0.0;
        for (ProduitSummary p : ps) {
            try { judogiFrais += Double.parseDouble(p.getMontant()); } catch (Exception e) {}
        }
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

    static Collection<ProduitSummary> getApplicableProduits(ServiceData sd,
						List<ProduitSummary> produitSummaries) {
        Collection<ProduitSummary> ps = new java.util.ArrayList<ProduitSummary>();
        if (sd.getJudogi() == null) return ps;

        List<String> produits = java.util.Arrays.asList(sd.getJudogi().split(";"));
        for (ProduitSummary p : produitSummaries) {
            if (produits.contains(p.getId()))
                ps.add(p);
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
            return Double.parseDouble(sd.getEscompteFrais());
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

    static String getWeeksSummary(ServiceData sd, SessionSummary ss, Date dateInscription, List<SessionSummary> sessionSummaries) {
        int rw = remainingWeeks(sd, ss, dateInscription, sessionSummaries);
        int tw = totalWeeksBothSessions(sd, ss, sessionSummaries);
        if (rw < tw)
            return " ("+rw+"/"+tw+")";
        else
            return "";
    }


    public static String getFrais(List<Prix> applicablePrix, ClubSummary cs, String session_seqno, String division_abbrev, String cours_id) {
        String club_id = (cs == null) ? JUDO_QC : cs.getId();

        if (cs != null && !cs.getAjustableCours())
            cours_id = ALL_COURS;

        if (cs != null && !cs.getAjustableDivision())
            division_abbrev = ALL_DIVISIONS;

	for (Prix p : applicablePrix) {
	    if (p.getClubId().equals(club_id) &&
		p.getSessionSeqno().equals(session_seqno) &&
		p.getDivisionAbbrev().equals(division_abbrev) &&
		p.getCoursId().equals(cours_id))
		return p.getFrais();
	}
	return "0";
    }

    // extract data from Prix objects and lists thereof...
    public static List<Prix> getPrixForClubSessionCours(List<Prix> applicablePrix, String club_id, String session_seqno, String cours_id, boolean isUnidivision) {
        if (isUnidivision) {
            for (Prix p : applicablePrix) {
                if (p.getClubId().equals(club_id) &&
                    p.getSessionSeqno().equals(session_seqno) &&
                    p.getCoursId().equals(cours_id) &&
                    p.getDivisionAbbrev().equals(ALL_DIVISIONS))
                    return Collections.singletonList(p);
            }
            Prix np = JsonUtils.<Prix>safeEval
                ("{\"id\":\"0\", \"club_id\":\""+club_id+"\", \"session_seqno\":\""+session_seqno+"\","+
                 "\"division_abbrev\":\""+ALL_DIVISIONS+"\",\"cours_id\":\""+cours_id+"\",\"frais\":\"0\"}");
            return Collections.singletonList(np);
        }

	HashMap<Constants.Division, Prix> rv = new HashMap<>();
	for (Prix p : applicablePrix) {
	    if (p.getClubId().equals(club_id) &&
		p.getSessionSeqno().equals(session_seqno) &&
		p.getCoursId().equals(cours_id))
		rv.put(Constants.getDivisionByAbbrev(p.getDivisionAbbrev()), p);
	}

	List<Prix> rvSorted = new LinkedList<>();
	for (Constants.Division d : Constants.DIVISIONS) {
	    if (rv.containsKey(d))
		rvSorted.add(rv.get(d));
	    else {
		Prix np = JsonUtils.<Prix>safeEval
		    ("{\"id\":\"0\", \"club_id\":\""+club_id+"\", \"session_seqno\":\""+session_seqno+"\","+
		     "\"division_abbrev\":\""+d.abbrev+"\",\"cours_id\":\""+cours_id+"\",\"frais\":\"0\"}");
		rvSorted.add(np);
	    }
	}
	return rvSorted;
    }

    /** Model-level method to recompute costs. */
    public static void recompute(SessionSummary ss, ClientData cd, ServiceData sd, ClubSummary cs, List<SessionSummary> sessionSummaries, List<CoursSummary> coursSummaries, Collection<ProduitSummary> ps, boolean prorataOverride, List<Prix> prixSummaries, List<EscompteSummary> escompteSummaries) {
      if (prixSummaries == null) return;
      if (ss == null) return;

      double dCategorieFrais = proratedFraisCours(cd, sd, cs, ss, sessionSummaries, coursSummaries, prixSummaries);
      if (!prorataOverride) dCategorieFrais = fraisCours(cd, sd, cs, sessionSummaries, coursSummaries, prixSummaries);
      double dEscompteFrais = escompteFrais(sd, dCategorieFrais, escompteSummaries);
      double dAffiliationFrais = affiliationFrais(cd, sd, ss, sessionSummaries, prixSummaries);
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
