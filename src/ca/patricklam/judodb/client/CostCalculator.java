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
    
    static double proratedFraisCours(ClientData cd, ServiceData sd) {
        int sessionId = Constants.currentSessionNo();
        Constants.Division c = cd.getDivision(Constants.currentSession().effective_year);
        int sessionCount = sd.getSessionCount();
        Date dateInscription = Constants.DB_DATE_FORMAT.parse(sd.getDateInscription());

        // calculate number of weeks between start of session and dateInscription
        // calculate total number of weeks
        // divide, then add Constants.PRORATA_PENALITE
        // but only use the prorated frais if ew < tw - 4
        
        int ew = elapsedWeeks(sessionId, dateInscription);
        int tw = totalWeeks(sessionId, dateInscription);
        double baseCost = Constants.getFraisCours(sessionId, c, sessionCount);
        double prorataCost = baseCost * ((double)ew / (double)tw) + Constants.PRORATA_PENALITE;
        if (ew < tw - 4)
            return Math.min(baseCost, prorataCost);
        else
            return baseCost;
    }
    
    static double affiliationFrais(ClientData cd, ServiceData sd) {
        int sessionId = Constants.currentSessionNo();
        boolean sans_affiliation = sd.getSansAffiliation();
        Constants.Division c = cd.getDivision(Constants.currentSession().effective_year);
        
        double dAffiliationFrais = 0.0;
        if (!sans_affiliation)
            dAffiliationFrais = Constants.getFraisJudoQC(sessionId, c);
        return dAffiliationFrais;
    }
    
    static double suppFrais(ServiceData sd) {
        double judogiFrais = Double.parseDouble(sd.getJudogi());
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
}
