package ca.patricklam.judodb.client;

import com.google.gwt.core.client.JavaScriptObject;

public class ServiceData extends JavaScriptObject {
	protected ServiceData() {}

	public final native String getID() /*-{ return this.id; }-*/;
	public final native String getDateInscription() /*-{ return this.date_inscription == null ? "" : this.date_inscription; }-*/;
	public final native void setDateInscription(String date_inscription) /*-{ this.date_inscription = date_inscription; }-*/;
	public final native String getSaisons() /*-{ return this.saisons; }-*/;
	public final native void setSaisons(String saisons) /*-{ this.saisons = saisons; }-*/;
	public final native boolean getSansAffiliation() /*-{ return this.sans_affiliation != '0'; }-*/;
	public final native void setSansAffiliation(boolean sans_affiliation) /*-{ this.sans_affiliation = boolToZeroOne(sans_affiliation); }-*/;
	public final native int getCours() /*-{ return parseInt(this.cours); }-*/;
	public final native void setCours(int cours) /*-{ this.cours = cours.toString(); }-*/;
	public final native int getSessions() /*-{ return this.sessions == null ? 2 : parseInt(this.sessions); }-*/;
	public final native void setSessions(int sessions) /*-{ this.sessions = Integer.toString(sessions); }-*/;
	public final native boolean getPasseport() /*-{ return this.passeport != '0'; }-*/;
	public final native void setPasseport(boolean passeport) /*-{ this.passport = boolToZeroOne(passeport); }-*/;
	public final native boolean getNonAnjou() /*-{ return this.non_anjou != '0'; }-*/;
	public final native boolean setNonAnjou(boolean non_anjou) /*-{ this.non_anjou = boolToZeroOne(non_anjou); }-*/;
	public final native int getEscompte() /*-{ return this.escompte == null ? 0 : parseInt(this.escompte); }-*/;
	public final native void setEscompte(int escompte) /*-{ this.escompte = escompte.toString(); }-*/;
	public final native String getCasSpecialNote() /*-{ return this.cas_special_note; }-*/;
	public final native void setCasSpecialNote(String cas_special_note) /*-{ this.cas_special_note = cas_special_note; }-*/;
	public final native String getEscompteSpecial() /*-{ return this.escompte_special; }-*/;
	public final native void setEscompteSpecial(String escompte_special) /*-{ this.escompte_special = escompte_special; }-*/;
	public final native String getJudogi() /*-{ return this.judogi; }-*/;
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
	public final native void setVerification(boolean verification) /*-{ this.verification = boolToZeroOne(verification); }-*/;
	
	public final String boolToZeroOne(boolean b) { return b ? "1" : "0"; }

	public static final native ServiceData newServiceData() /*-{
		return { 
			id: null,
    		date_inscription: "",
    		saisons: "",
    		sans_affiliation: "0",
    		cours: "0",
    		sessions: "2",
    		passeport: "0",
    		non_anjou: "0",
    		judogi: "0.0",
    		escompte: "0",
    		categorie_frais: "0.0",
    		affiliation_frais: "0.0",
    		supp_frais: "0.0",
    		frais: "0.0",
    		cas_special_note: "",
    		escompte_special: "",
    		verification: "0"
		};
	}-*/;
}
