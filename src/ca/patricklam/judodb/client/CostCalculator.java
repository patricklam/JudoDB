package ca.patricklam.judodb.client;

import java.util.Date;

import com.google.gwt.user.datepicker.client.CalendarUtil;

public class CostCalculator {
    static int elapsedWeeks(int sessionId, Date dateInscription) {
        Date sessionEnd = Constants.session(sessionId).fin_cours;
        double elapsedWeeks = (CalendarUtil.getDaysBetween(dateInscription, sessionEnd) + 6) / 7.0;
        return (int)(elapsedWeeks+0.5);        
    }
    
    static int totalWeeks(int sessionId, Date dateInscription) {
        Date sessionStart = Constants.session(sessionId).debut_cours;
        Date sessionEnd = Constants.session(sessionId).fin_cours;
        double totalWeeks = (CalendarUtil.getDaysBetween(sessionStart, sessionEnd) + 6) / 7.0;
        return (int)(totalWeeks+0.5);
    }
    
    static double fraisCours(ClientData cd, ServiceData sd) {
        int sessionId = Constants.currentSessionNo();
        Constants.Division c = cd.getDivision(Constants.currentSession().effective_year);
        int sessionCount = 2; try { sessionCount = sd.getSessionCount(); } catch (Exception e) {}
        return Constants.getFraisCours(sessionId, c, sessionCount);
    }
    
    static double proratedFraisCours(ClientData cd, ServiceData sd) {
        int sessionId = Constants.currentSessionNo();
        double baseCost = fraisCours(cd, sd);
        if (!Constants.ENABLE_PRORATA || sd == null || sd.getDateInscription() == null)
        	return baseCost;
        
        Date dateInscription = null;
        try { dateInscription = Constants.DB_DATE_FORMAT.parse(sd.getDateInscription()); } catch (Exception e) { return baseCost; }

        // calculate number of weeks between start of session and dateInscription
        // calculate total number of weeks
        // divide, then add Constants.PRORATA_PENALITE
        // but only use the prorated frais if ew < tw - 4
        
        int ew = elapsedWeeks(sessionId, dateInscription);
        int tw = totalWeeks(sessionId, dateInscription);
        double prorataCost = baseCost * ((double)ew / (double)tw) + Constants.PRORATA_PENALITE;
        if (ew < tw - 4)
            return Math.min(baseCost, prorataCost);
        else
            return baseCost;
    }
    
    static double affiliationFrais(ClientData cd, ServiceData sd) {
    	if (sd == null) return 0.0;
    	
        int sessionId = Constants.currentSessionNo();
        boolean sans_affiliation = sd.getSansAffiliation();
        boolean affiliation_initiation = sd.getAffiliationInitiation();
        Constants.Division c = cd.getDivision(Constants.currentSession().effective_year);
        
        double dAffiliationFrais = 0.0;
        if (!sans_affiliation) {
            if (affiliation_initiation) 
                dAffiliationFrais = Constants.COUT_JUDOQC_INITIATION;
            else
                dAffiliationFrais = Constants.getFraisJudoQC(sessionId, c);
        }
        return dAffiliationFrais;
    }
    
    static double suppFrais(ServiceData sd) {
    	if (sd == null) return 0.0;

    	double judogiFrais = 0.0;
        try { judogiFrais = Double.parseDouble(sd.getJudogi()); } catch (Exception e) {}
        boolean passeport = sd.getPasseport();
        boolean non_anjou = sd.getNonAnjou();

        double dSuppFrais = judogiFrais;
        if (passeport)
            dSuppFrais += Constants.PASSEPORT_JUDO_QC;
        if (non_anjou)
            dSuppFrais += Constants.NON_ANJOU;
        return dSuppFrais;
    }

    static boolean isCasSpecial(ServiceData sd) {
        return Constants.escompte(Integer.toString(sd.getEscompteType())) == -1;
    }
    
    static double escompteFrais(ServiceData sd, double dCategorieFrais) {
    	if (sd == null) return 0.0;
    	
        double escomptePct;
        if (isCasSpecial(sd)) {
            escomptePct = Double.parseDouble(sd.getCasSpecialPct());
        } else {
            escomptePct = Constants.escompte(Integer.toString(sd.getEscompteType()));
        }
        return -dCategorieFrais * (escomptePct / 100.0);
    }
    
    static String getWeeksSummary(int sessionId, Date dateInscription) {
        int ew = elapsedWeeks(sessionId, dateInscription);
        int tw = totalWeeks(sessionId, dateInscription);
        if (ew < tw)
            return " ("+ew+"/"+tw+")";
        else
            return "";
    }
    
    /** Model-level method to recompute costs. */
    public static void recompute(ClientData cd, ServiceData sd, boolean prorata) {
      double dCategorieFrais = proratedFraisCours(cd, sd);
      if (!prorata) dCategorieFrais = fraisCours(cd, sd);
      double dEscompteFrais = escompteFrais(sd, dCategorieFrais);
      double dAffiliationFrais = affiliationFrais(cd, sd);
      double dSuppFrais = suppFrais(sd);
      
      if (sd != null) {
    	  sd.setCategorieFrais(Double.toString(dCategorieFrais));
    	  sd.setEscompteFrais(Double.toString(dEscompteFrais));
    	  sd.setAffiliationFrais(Double.toString(dAffiliationFrais));
    	  sd.setSuppFrais(Double.toString(dSuppFrais));
    	  sd.setFrais(Double.toString(dCategorieFrais + dAffiliationFrais + dEscompteFrais + dSuppFrais));
      }
    }
}
