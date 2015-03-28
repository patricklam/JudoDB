package ca.patricklam.judodb.client;

import com.google.gwt.core.client.JavaScriptObject;

public class ClubPrix extends JavaScriptObject {
    protected ClubPrix() { }

    public final native String getDivisionAbbrev() /*-{ return this.division_abbrev; }-*/;
    public final native double getFrais1Session() /*-{ return parseFloat(this.frais_1_session); }-*/;
    public final native double getFrais2Session() /*-{ return parseFloat(this.frais_2_session); }-*/;
    public final native double getFraisJudoQC() /*-{ return parseFloat(this.frais_judo_qc); }-*/;
}
