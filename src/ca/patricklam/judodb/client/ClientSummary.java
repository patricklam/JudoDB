// -*-  indent-tabs-mode:nil; c-basic-offset:4; -*-
package ca.patricklam.judodb.client;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArrayString;

public class ClientSummary extends JavaScriptObject {
    protected ClientSummary() { }

    public final native String getId() /*-{ return this.id; }-*/;
    public final native String getNom() /*-{ return this.nom; }-*/;
    public final native String getPrenom() /*-{ return this.prenom; }-*/;
    public final native String getSaisons() /*-{ return this.saisons; }-*/;
    // getClubs() returns an array of Club.getId()s.
    public final native JsArrayString getClubs() /*-{ return this.clubs; }-*/;
}
