package ca.patricklam.judodb.client;

import com.google.gwt.core.client.JavaScriptObject;

public class ServiceData extends JavaScriptObject {
	protected ServiceData() {}

	public final native String getID() /*-{ return this.id; }-*/;
	public final native String getDateInscription() /*-{ return this.date_inscription; }-*/;
	public final native String getSaisons() /*-{ return this.saisons; }-*/;
	public final native boolean getSansAffiliation() /*-{ return this.sans_affiliation != '0'; }-*/;
	public final native int getCours() /*-{ return parseInt(this.cours); }-*/;
	public final native int getSessions() /*-{ return this.sessions; }-*/;
	public final native boolean getPasseport() /*-{ return this.passeport != '0'; }-*/;
	public final native boolean getNonAnjou() /*-{ return this.non_anjou != '0'; }-*/;
	public final native int getEscompte() /*-{ return parseInt(this.escompte); }-*/;
	public final native String getCasSpecialNote() /*-{ return this.cas_special_note; }-*/;
	public final native String getEscompteSpecial() /*-{ return this.escompte_special; }-*/;
	public final native String getJudogi() /*-{ return this.judogi; }-*/;
	public final native String getCategorieFrais() /*-{ return this.categorie_frais; }-*/;
	public final native String getAffiliationFrais() /*-{ return this.affiliation_frais; }-*/;
	public final native String getSuppFrais() /*-{ return this.supp_frais; }-*/;
	public final native String getFrais() /*-{ return this.frais; }-*/;
	public final native boolean getVerification() /*-{ return this.verification != '0'; }-*/;
}
