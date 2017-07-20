// -*-  indent-tabs-mode:nil; c-basic-offset:4; -*-
package ca.patricklam.judodb.client;

import com.google.gwt.core.client.JavaScriptObject;

public class NomTarif extends JavaScriptObject {
    protected NomTarif() { }

    public final native String getId() /*-{ return this.id; }-*/;
    public final native void setId(String id) /*-{ this.id = id; }-*/;

    public final native String getNomTarif() /*-{ return this.nom_tarif; }-*/;
    public final native void setNomTarif(String nom_tarif) /*-{ this.nom_tarif = nom_tarif; }-*/;

    public final String get(String key) {
	if (key.equals("id")) return getId();
	if (key.equals("nom_tarif")) return getNomTarif();
	throw new RuntimeException("get: bad key " + key);
    }

    public final void set(String key, String value) {
	if (key.equals("id")) setId(value);
	else if (key.equals("nom_tarif")) setNomTarif(value);
	else throw new RuntimeException("set: bad key " + key);
    }
}
