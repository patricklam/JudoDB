package ca.patricklam.judodb.client;

import com.google.gwt.core.client.JavaScriptObject;

public class ClubPrix extends JavaScriptObject {
    protected ClubPrix() { }

    public final native String getId() /*-{ return this.id; }-*/;
    public final native String getDivisionAbbrev() /*-{ return this.division_abbrev; }-*/;
    public final native String setDivisionAbbrev(String division_abbrev) /*-{ return this.division_abbrev; }-*/;
    public final native String getFrais1Session() /*-{ return this.frais_1_session; }-*/;
    public final native String setFrais1Session(String frais_1_session) /*-{ this.frais_1_session = frais_1_session; }-*/;
    public final native String getFrais2Session() /*-{ return this.frais_2_session; }-*/;
    public final native String setFrais2Session(String frais_2_session) /*-{ this.frais_2_session = frais_2_session; }-*/;
    public final native String getFraisJudoQC() /*-{ return this.frais_judo_qc; }-*/;
    public final native String setFraisJudoQC(String frais_judo_qc) /*-{ this.frais_judo_qc = frais_judo_qc; }-*/;

    public final String get(String key) {
	if (key.equals("id")) return getId();
	if (key.equals("div")) return getDivisionAbbrev();
	if (key.equals("frais_1")) return getFrais1Session();
	if (key.equals("frais_2")) return getFrais2Session();
	if (key.equals("frais_judo_qc")) return getFraisJudoQC();
	throw new RuntimeException("get: bad key " + key);
    }

    public final void set(String key, String value) {
	if (key.equals("div")) setDivisionAbbrev(value);
	else if (key.equals("frais_1")) setFrais1Session(value);
	else if (key.equals("frais_2")) setFrais2Session(value);
	else if (key.equals("frais_judo_qc")) setFraisJudoQC(value);
	else throw new RuntimeException("set: bad key " + key);
    }
}
