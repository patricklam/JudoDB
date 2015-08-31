package ca.patricklam.judodb.client;

import java.util.Date;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;

public class Constants {
    public static boolean ENABLE_PRORATA = false;

    public static int currentSessionNo() { return 10; }

    public static final String STD_DUMMY_DATE = "01/01/0000";
    public static final String DB_DUMMY_DATE = "0000-00-00";
    public static final String STD_DATE_FORMAT_STRING = "dd/MM/yyyy";
    public static final DateTimeFormat STD_DATE_FORMAT = DateTimeFormat.getFormat(Constants.STD_DATE_FORMAT_STRING);
    public static final String DB_DATE_FORMAT_STRING = "yyyy-MM-dd";
    public static final DateTimeFormat DB_DATE_FORMAT = DateTimeFormat.getFormat(Constants.DB_DATE_FORMAT_STRING);
    public static final String dbToStdDate(String d) {
        if (d.equals(DB_DUMMY_DATE) || d.equals("")) return STD_DUMMY_DATE;
        return STD_DATE_FORMAT.format(DB_DATE_FORMAT.parse(d));
    }
    public static final String stdToDbDate(String d) {
        if (d.equals(STD_DUMMY_DATE) || d.equals("")) return DB_DUMMY_DATE;
        return DB_DATE_FORMAT.format(STD_DATE_FORMAT.parse(d));
    }
    public static final NumberFormat currencyFormat = NumberFormat.getFormat("0.00");

    public static final double PASSEPORT_JUDO_QC = 5.0;
    public static final double RESIDENT = 5.0;
    public static final int VETERAN = 35;
    public static final int PRORATA_PENALITE = 5;
    public static final int COUT_JUDOQC_INITIATION = 10;
    public static final int COUT_JUDOQC_ECOLE = 5;

    static class Session {
        final int seqno;
        final String abbrev;
        final int effective_year;
        final Date debut_cours;
        final Date fin_cours;

        public Session(int seqno, String abbrev, int effective_year, Date debut_cours, Date fin_cours) {
            this.seqno = seqno; this.abbrev = abbrev;
            this.effective_year = effective_year;
            this.debut_cours = debut_cours; this.fin_cours = fin_cours;
        }
    }

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

    static class Cours {
        final String seqno;
        final String name;
        final String short_desc;
        final String entraineur;

        public Cours(String seqno, String name, String short_desc, String entraineur) {
            this.seqno = seqno;
            this.name = name; this.short_desc = short_desc;
            this.entraineur = entraineur;
        }
    }

    static class Escompte {
        final String seqno;
        final String name;
        final int amount; // in percent

        public Escompte(String seqno, String name, int amount) {
            this.seqno = seqno; this.name = name; this.amount = amount;
        }
    }

    public static final int escompte(String seqno) {
        for (Escompte e : ESCOMPTES)
            if (e.seqno.equals(seqno))
                return e.amount;
        return 0;
    }

    static class Judogi {
        final String seqno;
        final String name;
        final String amount;

        public Judogi(String seqno, String name, String amount) {
            this.seqno = seqno; this.name = name; this.amount = amount;
        }
    }

    public static final String judogi(String seqno) {
        for (Judogi j : JUDOGIS)
            if (j.seqno.equals(seqno))
                return j.amount;
        return "0";
    }

    static class CategorieSession {
        final int session_seqno_min, session_seqno_max;
        final String categorie_abbrev;
        final double frais_1_session, frais_2_session;
        final double frais_judo_qc;

        public CategorieSession(int session_seqno_min, int session_seqno_max, String categorie_abbrev,
                double frais_1_session, double frais_2_session,
                double frais_judo_qc) {
            this.session_seqno_min = session_seqno_min;
            this.session_seqno_max = session_seqno_max;
            this.categorie_abbrev = categorie_abbrev;
            this.frais_1_session = frais_1_session;
            this.frais_2_session = frais_2_session;
            this.frais_judo_qc = frais_judo_qc;
        }
    }

    static class Grade {
        final String name; final String n3;
        final int order;
        public Grade(String name, String n3, int order) {
            this.name = name; this.n3 = n3; this.order = order;
        }
        public Grade(String name, int order) {
            this.name = name; this.n3 = name; this.order = order;
        }
    }

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
        new Grade("5D", 50)
    };

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

    public static final Division[] DIVISIONS = new Division[] {
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

    public static final Cours[] COURS = new Cours[] {
        new Cours("0", "Adultes (LM2015-2145, V2000-2145)", "LM2015 V2000", ""),
        new Cours("1", "Équipe compétition (LM1830-2015, V2000-2145)", "LM1830 V2000", ""),
        new Cours("2", "Intérmediares 7-12 ans (L1730-1830, V1830-2000)", "L1730 V1830", ""),
        new Cours("3", "Débutants 7-12 ans (MaJ1730-1830)", "MaJ1730", ""),
        new Cours("4", "Débutants 5-6 ans (S900-1000)", "S900", ""),
        new Cours("5", "Anciens 5-6 ans (S1000-1130)", "S1000", ""),
        new Cours("6", "Anciens 7-8 ans (S1130-1300)", "S1130", ""),
        new Cours("7", "Anciens 7-9 ans (S1300-1430)", "S1300", ""),
        new Cours("8", "Anciens 7-10 ans (S1430-1600)", "S1430", ""),
        new Cours("9", "Débutants (D930-1030)", "D930", ""),
        new Cours("10", "Anciens (D1030-1130)", "D1030", ""),
        new Cours("11", "Débutants 7-10 ans (MV1700-1800)", "MV1700", ""),
        new Cours("12", "Anciens (D1130-1300)", "D1130", ""),
        new Cours("13", "Débutants 7-10 ans (S1600-1730)", "S1600", ""),
        new Cours("14", "Débutants (D1300-1400)", "D1300", ""),
        new Cours("15", "Anciens 7-10 ans (MV1800-1900)", "MV1800", ""),
        new Cours("16", "Débutants (D1400-1500)", "D1400", ""),
        new Cours("17", "Autre", "X0000", ""),
    };

    public static final Escompte[] ESCOMPTES = new Escompte[] {
        new Escompte("0", "Aucun", 0),
        new Escompte("1", "2e membre", 10),
        new Escompte("2", "3e membre", 15),
        new Escompte("3", "4e membre", 20),
        new Escompte("4", "5e membre", 25),
        new Escompte("5", "Nouvel(le) ami(e)", 10),
        new Escompte("6", "Membre du CA", 50),
        new Escompte("7", "Cas spécial", -1)
    };

    public static final Judogi[] JUDOGIS = new Judogi[] {
        new Judogi("0", "Aucun", "0"),
        new Judogi("1", "000/00 (55$)", "55"),
        new Judogi("2", "0/1 (60$)", "60"),
        new Judogi("3", "2/3 (65$)", "65"),
        new Judogi("4", "4/5/6 (75$)", "75")
    };

}
