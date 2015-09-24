// -*-  indent-tabs-mode:nil; c-basic-offset:4; -*-
package ca.patricklam.judodb.client;

import com.google.gwt.core.client.JavaScriptObject;

public class ClubPrix extends JavaScriptObject {
    protected ClubPrix() { }

    public final native String getId() /*-{ return this.id; }-*/;
    public final native void setId(String id) /*-{ this.id = id; }-*/;

    public final native String getDivisionAbbrev() /*-{ return this.division_abbrev; }-*/;
    public final native void setDivisionAbbrev(String division_abbrev) /*-{ this.division_abbrev = division_abbrev; }-*/;
    public final native String getFrais1Session() /*-{ return this.frais_1_session; }-*/;
    public final native void setFrais1Session(String frais_1_session) /*-{ this.frais_1_session = frais_1_session; }-*/;
    public final native String getFrais2Session() /*-{ return this.frais_2_session; }-*/;
    public final native void setFrais2Session(String frais_2_session) /*-{ this.frais_2_session = frais_2_session; }-*/;
    public final native String getFraisJudoQC() /*-{ return this.frais_judo_qc; }-*/;
    public final native void setFraisJudoQC(String frais_judo_qc) /*-{ this.frais_judo_qc = frais_judo_qc; }-*/;
    public final native String getSession() /*-{ return this.session_seqno; }-*/;
    public final native void setSession(String session_seqno) /*-{ this.session_seqno = session_seqno; }-*/;

    public final String get(String key) {
	if (key.equals("session")) return getSession();
	if (key.equals("id")) return getId();
	if (key.equals("division_abbrev")) return getDivisionAbbrev();
	if (key.equals("frais_1_session")) return getFrais1Session();
	if (key.equals("frais_2_session")) return getFrais2Session();
	if (key.equals("frais_judo_qc")) return getFraisJudoQC();
	throw new RuntimeException("get: bad key " + key);
    }

    public final void set(String key, String value) {
	if (key.equals("session")) setSession(value);
	else if (key.equals("division_abbrev")) setDivisionAbbrev(value);
	else if (key.equals("frais_1_session")) setFrais1Session(value);
	else if (key.equals("frais_2_session")) setFrais2Session(value);
	else if (key.equals("frais_judo_qc")) setFraisJudoQC(value);
	else throw new RuntimeException("set: bad key " + key);
    }

    public final String getSignature() {
	StringBuffer sb = new StringBuffer();
	sb.append(getDivisionAbbrev()); sb.append("|");
	sb.append(getFrais1Session()); sb.append("|");
	sb.append(getFrais2Session()); sb.append("|");
	sb.append(getFraisJudoQC()); sb.append("|");
	return sb.toString();
    }
}
