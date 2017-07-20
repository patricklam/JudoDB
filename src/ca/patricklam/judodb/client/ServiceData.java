// -*-  indent-tabs-mode:nil; c-basic-offset:4; -*-
package ca.patricklam.judodb.client;

import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.JavaScriptObject;

public class ServiceData extends JavaScriptObject {
    protected ServiceData() {}

    public final native String getID() /*-{ return this.id; }-*/;
    /** returns date d'inscription in DB format */
    public final native String getDateInscription() /*-{ return this.date_inscription == null ? "" : this.date_inscription; }-*/;
    public final native void setDateInscription(String date_inscription) /*-{ this.date_inscription = date_inscription; }-*/;
    public final native String getSessions() /*-{ return this.saisons == null ? "" : this.saisons; }-*/;
    public final native void setSessions(String sessions) /*-{ this.saisons = sessions; }-*/;
    public final native String getClubID() /*-{ return this.club_id; }-*/;
    public final native void setClubID(String club_id) /*-{ this.club_id = club_id; }-*/;
    public final native String getDAEString() /*-{ return this.date_affiliation_envoye == null ? "" : this.date_affiliation_envoye; }-*/;
    public final Date getDateAffiliationEnvoye() {
        if (getDAEString() == null) return Constants.DB_DATE_FORMAT.parse(Constants.DB_DUMMY_DATE_ACTUAL);
        try {
            return Constants.DB_DATE_FORMAT.parse(getDAEString());
        } catch (IllegalArgumentException e) { return Constants.DB_DATE_FORMAT.parse(Constants.DB_DUMMY_DATE_ACTUAL); }
    }
    public final native void setDAEString(String date_affiliation_envoye) /*-{ this.date_affiliation_envoye = date_affiliation_envoye; }-*/;
    public final native boolean getCarteJudocaRecu() /*-{ return this.carte_judoca_recu != '0'; }-*/;
    public final native void setCarteJudocaRecu(boolean carte_judoca_recu) /*-{ this.carte_judoca_recu = carte_judoca_recu ? "1" : "0"; }-*/;
    public final native boolean getSansAffiliation() /*-{ return this.sans_affiliation != '0'; }-*/;
    public final native void setSansAffiliation(boolean sans_affiliation) /*-{ this.sans_affiliation = sans_affiliation ? "1" : "0"; }-*/;
    public final native boolean getAffiliationInitiation() /*-{ return this.affiliation_initiation != '0'; }-*/;
    public final native void setAffiliationInitiation(boolean affiliation_initiation) /*-{ this.affiliation_initiation = affiliation_initiation ? "1" : "0"; }-*/;
    public final native boolean getAffiliationEcole() /*-{ return this.affiliation_ecole != '0'; }-*/;
    public final native void setAffiliationEcole(boolean affiliation_ecole) /*-{ this.affiliation_ecole = affiliation_ecole ? "1" : "0"; }-*/;
    public final native boolean getAffiliationParascolaire() /*-{ return this.affiliation_parascolaire != '0'; }-*/;
    public final native void setAffiliationParascolaire(boolean affiliation_parascolaire) /*-{ this.affiliation_parascolaire = affiliation_parascolaire ? "1" : "0"; }-*/;
    public final native String getCours() /*-{ return this.cours; }-*/;
    public final native void setCours(String cours) /*-{ this.cours = cours; }-*/;
    public final native String getNomTarifId() /*-{ return this.nom_tarif_id; }-*/;
    public final native void setNomTarifId(String cours) /*-{ this.nom_tarif_id = nom_tarif_id; }-*/;
    public final native int getSessionCount() /*-{ return this.saisons == null ? 0 : this.saisons.split(" ").length; }-*/;
    public final native boolean getResident() /*-{ return this.resident != '0'; }-*/;
    public final native void setResident(boolean resident) /*-{ this.resident = resident ? "1" : "0"; }-*/;
    public final native boolean getPaypal() /*-{ return this.paypal != '' && this.paypal != '0'; }-*/;
    public final native void setPaypal(boolean paypal) /*-{ this.paypal = paypal ? "1" : "0"; }-*/;
    public final native String getEscompteId() /*-{ return this.escompte == null ? "0" : this.escompte; }-*/;
    public final native void setEscompteId(String id) /*-{ this.escompte = id; }-*/;
    public final native String getCasSpecialNote() /*-{ return this.cas_special_note; }-*/;
    public final native void setCasSpecialNote(String cas_special_note) /*-{ this.cas_special_note = cas_special_note; }-*/;
    public final native String getEscompteFrais() /*-{ return (this.escompte_special == null || this.escompte_special == "") ? "0" : this.escompte_special; }-*/;
    public final native void setEscompteFrais(String escompteFrais) /*-{ this.escompte_special = escompteFrais; }-*/;
    public final native String getJudogi() /*-{ return this.judogi == "" ? "0" : this.judogi; }-*/;
    public final native void setJudogi(String judogi) /*-{ this.judogi = judogi; }-*/;
    public final native String getCategorieFrais() /*-{ return this.categorie_frais == null ? "0" : this.categorie_frais; }-*/;
    public final native void setCategorieFrais(String categorie_frais) /*-{ this.categorie_frais = categorie_frais; }-*/;
    public final native String getAffiliationFrais() /*-{ return this.affiliation_frais == null ? "0" : this.affiliation_frais; }-*/;
    public final native void setAffiliationFrais(String affiliation_frais) /*-{ this.affiliation_frais = affiliation_frais; }-*/;
    public final native String getSuppFrais() /*-{ return this.supp_frais == null ? "0" : this.supp_frais; }-*/;
    public final native void setSuppFrais(String supp_frais) /*-{ this.supp_frais = supp_frais; }-*/;
    public final native String getFrais() /*-{ return this.frais == null ? "0" : this.frais; }-*/;
    public final native void setFrais(String frais) /*-{ this.frais = frais; }-*/;
    public final native boolean getAffiliationEnvoye() /*-{ return this.affiliation_envoye != '0'; }-*/;
    public final native void setAffiliationEnvoye(boolean affiliation_envoye) /*-{ this.affiliation_envoye = affiliation_envoye ? "1" : "0"; }-*/;
    public final native boolean getSolde() /*-{ return this.solde!= '0'; }-*/;
    public final native void setSolde(boolean solde) /*-{ this.solde = solde ? "1" : "0"; }-*/;

    public final void inscrireAujourdhui(List<SessionSummary> sessionSummaries) {
        Date today = new Date();
        setDateInscription(Constants.DB_DATE_FORMAT.format(today));
        SessionSummary ts = JudoDB.getSessionForDate(today, sessionSummaries);
        if (ts.isPrimary()) {
            setSessions(JudoDB.getSessionIds(today, 2, sessionSummaries));
        } else {
            setSessions(JudoDB.getSessionIds(today, 1, sessionSummaries));
        }
    }

    public static final native ServiceData newServiceData() /*-{
        return {
            id: null,
            club_id: "",
            nom_tarif_id: "",
            date_inscription: "",
            saisons: "",
            date_affiliation_envoye: "",
            carte_judoca_recu: "0",
            sans_affiliation: "0",
            affiliation_initiation: "0",
            affiliation_ecole: "0",
            affiliation_parascolaire: "0",
            cours: "0",
            no_sessions: "2",
            passeport: "0",
            resident: "0",
            paypal: "0",
            judogi: "0.0",
            escompte: "0",
            categorie_frais: "0.0",
            affiliation_frais: "0.0",
            supp_frais: "0.0",
            frais: "0.0",
            cas_special_note: "",
            escompte_special: "",
            verification: "0",
            affiliation_envoye: "0",
            solde: "0"
        };
    }-*/;
}
