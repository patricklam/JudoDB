// -*-  indent-tabs-mode:nil; c-basic-offset:4; -*-
package ca.patricklam.judodb.client;

import java.util.Date;
import java.util.HashMap;

import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.TakesValue;

public class Constants {
    public static final String STD_DUMMY_DATE = "01/01/0000";
    public static final String DB_DUMMY_DATE = "0000-00-00";
    public static final String STD_DATE_FORMAT_STRING = "dd/MM/yyyy";
    public static final DateTimeFormat STD_DATE_FORMAT = DateTimeFormat.getFormat(Constants.STD_DATE_FORMAT_STRING);
    public static final String DB_DATE_FORMAT_STRING = "yyyy-MM-dd";
    public static final DateTimeFormat DB_DATE_FORMAT = DateTimeFormat.getFormat(Constants.DB_DATE_FORMAT_STRING);
    public static final String dbToStdDate(String d) {
        if (d.equals(DB_DUMMY_DATE) || d.equals("")) return STD_DUMMY_DATE;
        try {
            return STD_DATE_FORMAT.format(DB_DATE_FORMAT.parse(d));
        } catch (IllegalArgumentException e) {
            return STD_DUMMY_DATE;
        }
    }
    public static final String stdToDbDate(String d) {
        if (d.equals(STD_DUMMY_DATE) || d.equals("")) return DB_DUMMY_DATE;
        try {
            return DB_DATE_FORMAT.format(STD_DATE_FORMAT.parse(d));
        } catch (IllegalArgumentException e) {
            return DB_DUMMY_DATE;
        }
    }
    public static final NumberFormat currencyFormat = NumberFormat.getFormat("0.00");

    static class Division {
        final String name;
        final String abbrev;
        final int years_ago;
        final boolean noire;
        final String aka; // e.g. for "U20N", this is "U20"
        public Division(String name, String abbrev, int years_ago, boolean noire, String aka) {
            this.name = name; this.abbrev = abbrev;
            this.years_ago = years_ago; this.noire = noire;
            this.aka = aka;
        }
    }

    static class Grade implements TakesValue<String> {
        final String name; final String n3;
        final int order;
        public Grade(String name, String n3, int order) {
            this.name = name; this.n3 = n3; this.order = order;
        }
        public Grade(String name, int order) {
            this.name = name; this.n3 = name; this.order = order;
        }

        @Override public String getValue() { return name; }
        @Override public void setValue(String v) {};
    }

    /** Case-insensitively assigns grade to one of GRADES.
     * Truncate input to 3 letters.
     * Then, look first for exact case-insensitive matches.
     * Otherwise, look for a first-letter match between input and one of the grades (e.g. M and Marron).
     */
    public static Grade stringToGrade(String grade) {
        if (grade == null) return null;

        if (grade.length() >= 3) grade = grade.substring(0, 3);
        for (Grade g : GRADES) {
            if (g.n3.equalsIgnoreCase(grade))
                return g;
        }

        if (grade.length() == 1) {
            for (Grade g : GRADES)
                if (g.name.startsWith(grade))
                    return g;
        }

        return null;
    }

    public static native final String webDateToMilliSec(String webDate) /*-{
        var longDate = Date.parse(webDate);
        return longDate.toString();
    }-*/;

    public static Date newDate(String s) {
        long longDate = Long.parseLong(webDateToMilliSec(s));
        return new Date(longDate);
    }

    /* constant data which should be refactored */
    public static final double PASSEPORT_JUDO_QC = 5.0;
    public static final int VETERAN = 35;
    public static final int COUT_JUDOQC_INITIATION = 10;
    public static final int COUT_JUDOQC_ECOLE = 5;
    public static final int COUT_JUDOQC_PARASCOLAIRE = 8;
    public static final int PAYPAL_PCT = 3;

    /* constant data */
    public static final Grade[] GRADES = new Grade[] {
        new Grade("Blanche", "Bla", -60),
        new Grade("B/J", -55),
        new Grade("Jaune", "Jau", -50),
        new Grade("J/O", -45),
        new Grade("Orange", "Ora", -40),
        new Grade("O/V", -35),
        new Grade("Verte", "Ver", -30),
        new Grade("V/B", -25),
        new Grade("Bleue", "Ble", -20),
        new Grade("B/M", -15),
        new Grade("Marron", "Mar", -10),
        new Grade("1D", 10),
        new Grade("2D", 20),
        new Grade("3D", 30),
        new Grade("4D", 40),
        new Grade("5D", 50),
        new Grade("6D", 60),
        new Grade("7D", 70)
    };

    public static final Division[] DIVISIONS = new Division[] {
        new Division("O65", "O65", -65, false, null),
        new Division("O65N", "O65N", -65, true, "O65"),
        new Division("U6", "U6", 6, false, null),
        new Division("Mini-Poussin", "U8", 8, false, null),
        new Division("Poussin", "U10", 10, false, null),
        new Division("Benjamin", "U12", 12, false, null),
        new Division("Minime", "U14", 14, false, null),
        new Division("Juvénile", "U16", 16, false, null),
        new Division("Cadet", "U18", 18, false, null),
        new Division("Junior", "U21", 21, false, null),
        new Division("Senior", "S", 0, false, null),
        new Division("Cadet Noire", "U18N", 18, true, "U18"),
        new Division("Junior Noire", "U21N", 21, true, "U21"),
        new Division("Senior Noire", "SN", 0, true, "S"),
    };
    public static final Division EMPTY_DIVISION = new Division("", "", 0, false, null);

    private static HashMap<String, Division> divisions;
    public static Division getDivisionByAbbrev(String abbrev) {
        if (divisions == null) {
            divisions = new HashMap<>();
            for (Division d : DIVISIONS) {
                divisions.put(d.abbrev, d);
            }
        }
        return divisions.get(abbrev);
    }

    public static final Grade EMPTY_GRADE = new Grade("---", "---", 0);

    public static final EscompteSummary EMPTY_ESCOMPTE =
	JsonUtils.<EscompteSummary>safeEval
	("{\"id\":\"0\", \"club_id\":\"0\", \"nom\":\"Aucun\","+
	 "\"amount_percent\":\"0\",\"amount_absolute\":\"0\"}");

    public static final ProduitSummary EMPTY_PRODUIT =
	JsonUtils.<ProduitSummary>safeEval
	("{\"id\":\"0\", \"club_id\":\"0\", \"nom\":\"Aucun\","+
	 "\"montant\":\"0\"}");
}
