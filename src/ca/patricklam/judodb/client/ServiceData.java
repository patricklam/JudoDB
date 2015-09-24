// -*-  indent-tabs-mode:nil; c-basic-offset:4; -*-
package ca.patricklam.judodb.client;

import java.util.Date;

import com.google.gwt.core.client.JavaScriptObject;

public class ServiceData extends JavaScriptObject {
    protected ServiceData() {}

    public final native String getID() /*-{ return this.id; }-*/;
    /** returns date d'inscription in DB format */
    public final native String getDateInscription() /*-{ return this.date_inscription == null ? "" : this.date_inscription; }-*/;
    public final native void setDateInscription(String date_inscription) /*-{ this.date_inscription = date_inscription; }-*/;
    public final native String getSaisons() /*-{ return this.saisons == null ? "" : this.saisons; }-*/;
    public final native void setSaisons(String saisons) /*-{ this.saisons = saisons; }-*/;
    public final native String getClubID() /*-{ return this.club_id; }-*/;
    public final native void setClubID(String club_id) /*-{ this.club_id = club_id; }-*/;
    public final native boolean getSansAffiliation() /*-{ return this.sans_affiliation != '0'; }-*/;
    public final native void setSansAffiliation(boolean sans_affiliation) /*-{ this.sans_affiliation = sans_affiliation ? "1" : "0"; }-*/;
    public final native boolean getAffiliationInitiation() /*-{ return this.affiliation_initiation != '0'; }-*/;
    public final native void setAffiliationInitiation(boolean affiliation_initiation) /*-{ this.affiliation_initiation = affiliation_initiation ? "1" : "0"; }-*/;
    public final native boolean getAffiliationEcole() /*-{ return this.affiliation_ecole != '0'; }-*/;
    public final native void setAffiliationEcole(boolean affiliation_ecole) /*-{ this.affiliation_ecole = affiliation_ecole ? "1" : "0"; }-*/;
    public final native String getCours() /*-{ return this.cours; }-*/;
    public final native void setCours(String cours) /*-{ this.cours = cours; }-*/;
    public final native int getSessionCount() /*-{ return (this.no_sessions == null || this.no_sessions == "") ? 2 : parseInt(this.no_sessions); }-*/;
    public final native void setSessionCount(int no_sessions) /*-{ this.no_sessions = no_sessions.toString(); }-*/;
    public final native boolean getPasseport() /*-{ return this.passeport != '0'; }-*/;
    public final native void setPasseport(boolean passeport) /*-{ this.passeport = passeport ? "1" : "0"; }-*/;
    public final native boolean getResident() /*-{ return this.resident != '0'; }-*/;
    public final native void setResident(boolean resident) /*-{ this.resident = resident ? "1" : "0"; }-*/;
    public final native boolean getPaypal() /*-{ return this.paypal != '0'; }-*/;
    public final native void setPaypal(boolean paypal) /*-{ this.paypal = paypal ? "1" : "0"; }-*/;
    public final native String getEscompteId() /*-{ return this.escompte == null ? "0" : this.escompte; }-*/;
    public final native void setEscompteId(String id) /*-{ this.escompte = id; }-*/;
    public final native String getCasSpecialNote() /*-{ return this.cas_special_note; }-*/;
    public final native void setCasSpecialNote(String cas_special_note) /*-{ this.cas_special_note = cas_special_note; }-*/;
    /* note: cas_special_pct is not in-db; escompte_special is the source of truth from db */
    public final native String getCasSpecialPct() /*-{ return this.cas_special_pct == null ? "-1" : this.cas_special_pct; }-*/;
    public final native void setCasSpecialPct(String cas_special_pct) /*-{ this.cas_special_pct = cas_special_pct; }-*/;
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
    public final native boolean getVerification() /*-{ return this.verification != '0'; }-*/;
    public final native void setVerification(boolean verification) /*-{ this.verification = verification ? "1" : "0"; }-*/;
    public final native boolean getSolde() /*-{ return this.solde!= '0'; }-*/;
    public final native void setSolde(boolean solde) /*-{ this.solde = solde ? "1" : "0"; }-*/;

    public final void inscrireAujourdhui() {
        setDateInscription(Constants.DB_DATE_FORMAT.format(new Date()));
    }

    public static final native ServiceData newServiceData() /*-{
        return {
            id: null,
            club_id: "",
            date_inscription: "",
            saisons: "",
            sans_affiliation: "0",
            affiliation_initiation: "0",
            affiliation_ecole: "0",
            cours: "0",
            no_sessions: "2",
            passeport: "0",
            resident: "0",
            judogi: "0.0",
            escompte: "0",
            categorie_frais: "0.0",
            affiliation_frais: "0.0",
            supp_frais: "0.0",
            frais: "0.0",
            cas_special_note: "",
            escompte_special: "",
            verification: "0",
            solde: "0"
        };
    }-*/;
}
