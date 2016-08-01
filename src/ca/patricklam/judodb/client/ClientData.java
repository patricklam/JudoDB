// -*-  indent-tabs-mode:nil; c-basic-offset:4; -*-
package ca.patricklam.judodb.client;

import java.util.Date;

import ca.patricklam.judodb.client.Constants.Division;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.i18n.client.DateTimeFormat;

public class ClientData extends JavaScriptObject {
    protected ClientData() {}

    public final native String getID() /*-{ return this.id; }-*/;
    public final native void setID(String id) /*-{ this.id = id; }-*/;

    public final native String getNom() /*-{ return this.nom; }-*/;
    public final native void setNom(String nom) /*-{ this.nom = nom; }-*/;
    public final native String getPrenom() /*-{ return this.prenom; }-*/;
    public final native void setPrenom(String prenom) /*-{ this.prenom = prenom; }-*/;
    public final native String getDDNString() /*-{ return this.ddn; }-*/;
    public final native void setDDNString(String ddn) /*-{ this.ddn = ddn; }-*/;
    public final native String getSexe() /*-{ return this.sexe; }-*/;
    public final native void setSexe(String sexe) /*-{ this.sexe = sexe; }-*/;

    public final native String getAdresse() /*-{ return this.adresse; }-*/;
    public final native void setAdresse(String adresse) /*-{ this.adresse = adresse; }-*/;
    public final native String getVille() /*-{ return this.ville; }-*/;
    public final native void setVille(String ville) /*-{ this.ville = ville; }-*/;
    public final native String getCodePostal() /*-{ return this.code_postal; }-*/;
    public final native void setCodePostal(String codePostal) /*-{ this.code_postal = codePostal; }-*/;
    public final native String getTel() /*-{ return this.tel; }-*/;
    public final native void setTel(String tel) /*-{ this.tel = tel; }-*/;
    public final native String getCourriel() /*-{ return this.courriel; }-*/;
    public final native void setCourriel(String courriel) /*-{ this.courriel = courriel; }-*/;

    public final native String getJudoQC() /*-{ return this.affiliation; }-*/;
    public final native void setJudoQC(String judoQC) /*-{ this.affiliation = judoQC; }-*/;

    public final native JsArray<GradeData> getGrades() /*-{ return this.grades; }-*/;
    public final native void setGrades(JsArray<GradeData> grades) /*-{ this.grades = grades; }-*/;

    public final native String getCarteResident() /*-{ return this.carte_resident; }-*/;
    public final native void setCarteResident(String carte_resident) /*-{ this.carte_resident = carte_resident; }-*/;
    public final native String getNomRecuImpot() /*-{ return this.nom_recu_impot; }-*/;
    public final native void setNomRecuImpot(String nom_recu_impot) /*-{ this.nom_recu_impot = nom_recu_impot; }-*/;

    public final native String getNomContactUrgence() /*-{ return this.nom_contact_urgence; }-*/;
    public final native void setNomContactUrgence(String nom_contact_urgence) /*-{ this.nom_contact_urgence = nom_contact_urgence; }-*/;
    public final native String getTelContactUrgence() /*-{ return this.tel_contact_urgence; }-*/;
    public final native void setTelContactUrgence(String tel_contact_urgence) /*-{ this.tel_contact_urgence = tel_contact_urgence; }-*/;

    public final native JsArray<ServiceData> getServices() /*-{ return this.services; }-*/;
    public final native void setServices(JsArray<ServiceData> services) /*-{ this.services = services; }-*/;

    public final int getMostRecentServiceNumber() {
        JsArray<ServiceData> services = getServices();
        if (services == null || services.length() == 0) return -1;

        int r = -1;
        for (int i = 0; i < services.length(); i++) {
            if (r == -1 || services.get(i).getDateInscription().compareTo(services.get(r).getDateInscription()) > 0)
                r = i;
        }
        return r;
    }

    public final ServiceData getServiceFor(SessionSummary s) {
        JsArray<ServiceData> services = getServices();
        if (services == null) return null;
        if (s == null) return services.get(getMostRecentServiceNumber());

        for (int i = 0; i < services.length(); i++) {
            if (services.get(i).getSessions().contains(s.getAbbrev()))
                return services.get(i);
        }
        return null;
    }

    public final String getAllActiveSaisons() {
        String r = "";
        JsArray<ServiceData> services = getServices();
        if (services == null || services.length() == 0) return r;

        for (int i = 0; i < services.length(); i++) {
            if (i > 0) r += " ";
            r += services.get(i).getSessions();
        }
        return r;

    }

    public final GradeData getMostRecentGrade() {
        JsArray<GradeData> grades = getGrades();
        if (grades == null || grades.length() == 0) return null;

        GradeData m = grades.get(0);
        for (int i = 0; i < grades.length(); i++) {
            String dg = grades.get(i).getDateGrade();
            if (dg != null && dg.compareTo(m.getDateGrade()) > 0)
                m = grades.get(i);
        }
        return m;
    }

    public final String getGrade() {
        GradeData m = getMostRecentGrade();
        return m == null ? "" : m.getGrade();
    }

    public final String getDateGrade() {
        GradeData m = getMostRecentGrade();
        return m == null ? "" : m.getDateGrade();
    }

    public final Date getDDN() {
        if (getDDNString() == null) return null;
        try {
            return Constants.DB_DATE_FORMAT.parse(getDDNString());
        } catch (IllegalArgumentException e) { return null; }
    }

    public final boolean isNoire() {
        return getGrade() != null && getGrade().endsWith("D");
    }

    public final Division getDivision(String cy) {
	int current_year = Integer.parseInt(cy);
        Date d = getDDN();
        if (d == null) return Constants.EMPTY_DIVISION;

        int year = Integer.parseInt(DateTimeFormat.getFormat("yyyy").format(d));

        for (int i = 0; i < Constants.DIVISIONS.length; i++) {
            if (isNoire() == Constants.DIVISIONS[i].noire) {
                if (Constants.DIVISIONS[i].years_ago == 0)
                    return Constants.DIVISIONS[i];
                if (Constants.DIVISIONS[i].years_ago < 0 &&
                    current_year - (-Constants.DIVISIONS[i].years_ago) >= year)
                    return Constants.DIVISIONS[i];
                if (current_year - Constants.DIVISIONS[i].years_ago < year)
                    return Constants.DIVISIONS[i];
            }
        }
        return Constants.EMPTY_DIVISION;
    }

    /* deprecated */
    public final native String getRAMQ() /*-{ return this.RAMQ; }-*/;

    public final void setDefaultsPerClub(ClubSummary cs) {
        setVille(cs.getVille());
        setCodePostal(cs.getPrefixCodepostale());
        setTel(cs.getIndicatifRegional());
        setTelContactUrgence(cs.getIndicatifRegional());
    }

    public final boolean isDefault(ClubSummary cs) {
        return getAdresse().equals("") &&
            getVille().equals(cs.getVille()) &&
            getCodePostal().equals(cs.getPrefixCodepostale()) &&
            getTel().equals(cs.getIndicatifRegional()) &&
            getTelContactUrgence().equals(cs.getIndicatifRegional()) &&
            getCourriel().equals("");
    }
}
