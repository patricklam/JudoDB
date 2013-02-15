package ca.patricklam.judodb.client;

import java.util.Date;

import com.google.gwt.user.datepicker.client.CalendarUtil;

public class CostCalculator {
    static int getElapsedWeeks(int sessionId, Date dateInscription) {
        Date sessionEnd = Constants.session(sessionId).fin_cours;
        double elapsedWeeks = (CalendarUtil.getDaysBetween(dateInscription, sessionEnd) + 6) / 7.0;
        return (int)(elapsedWeeks+0.5);        
    }
    
    static int getTotalWeeks(int sessionId, Date dateInscription) {
        Date sessionStart = Constants.session(sessionId).debut_cours;
        Date sessionEnd = Constants.session(sessionId).fin_cours;
        double totalWeeks = (CalendarUtil.getDaysBetween(sessionStart, sessionEnd) + 6) / 7.0;
        return (int)(totalWeeks+0.5);
    }
    
    static String getWeeksSummary(int sessionId, Date dateInscription) {
        int ew = getElapsedWeeks(sessionId, dateInscription);
        int tw = getTotalWeeks(sessionId, dateInscription);
        if (ew < tw)
            return " ("+ew+"/"+tw+")";
        else
            return "";
        
    }
    
    static double proratedFrais(int sessionId, Constants.Division c, int sessionCount, Date dateInscription) {
        // calculate number of weeks between start of session and dateInscription
        // calculate total number of weeks
        // divide, then add Constants.PRORATA_PENALITE
        // but only use the prorated frais if ew < tw - 4
        int ew = getElapsedWeeks(sessionId, dateInscription);
        int tw = getTotalWeeks(sessionId, dateInscription);
        double baseCost = Constants.getFraisCours(sessionId, c, sessionCount);
        double prorataCost = baseCost * ((double)ew / (double)tw) + Constants.PRORATA_PENALITE;
        if (ew < tw - 4)
            return Math.min(baseCost, prorataCost);
        else
            return baseCost;
    }

}
