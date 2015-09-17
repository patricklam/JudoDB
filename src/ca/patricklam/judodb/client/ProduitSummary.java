package ca.patricklam.judodb.client;

import com.google.gwt.core.client.JavaScriptObject;

public class ProduitSummary extends JavaScriptObject {
    protected ProduitSummary() { }

    public final native String getId() /*-{ return this.id; }-*/;
    public final native String getClubId() /*-{ return this.club_id; }-*/;
    public final native void setClubId(String club_id) /*-{ this.club_id = club_id; }-*/;
    public final native String getNom() /*-{ return this.nom; }-*/;
    public final native void setNom(String nom) /*-{ this.nom = nom; }-*/;
    public final native String getMontant() /*-{ return this.montant; }-*/;
    public final native void setMontant(String montant) /*-{ this.montant = montant; }-*/;

    public final String get(String key) {
	if (key.equals("id")) return getId();
	if (key.equals("club_id")) return getClubId();
	if (key.equals("nom")) return getNom();
	if (key.equals("montant")) return getMontant();
	throw new RuntimeException("get: bad key " + key);
    }

    public final void set(String key, String value) {
	if (key.equals("club_id")) setClubId(value);
	else if (key.equals("nom")) setNom(value);
	else if (key.equals("montant")) setMontant(value);
	else throw new RuntimeException("set: bad key " + key);
    }
}

