// -*-  indent-tabs-mode:nil; c-basic-offset:4; -*-
package ca.patricklam.judodb.client;

import com.google.gwt.core.client.JavaScriptObject;

public class EscompteSummary extends JavaScriptObject {
    protected EscompteSummary() { }

    public final native String getId() /*-{ return this.id; }-*/;
    public final native String getClubId() /*-{ return this.club_id; }-*/;
    public final native void setClubId(String club_id) /*-{ this.club_id = club_id; }-*/;
    public final native String getNom() /*-{ return this.nom; }-*/;
    public final native void setNom(String nom) /*-{ this.nom = nom; }-*/;
    public final native String getAmountPercent() /*-{ return this.amount_percent; }-*/;
    public final native void setAmountPercent(String amount_percent) /*-{ this.amount_percent = amount_percent; }-*/;
    public final native String getAmountAbsolute() /*-{ return this.amount_absolute; }-*/;
    public final native void setAmountAbsolute(String amount_absolute) /*-{ this.amount_absolute = amount_absolute; }-*/;
    public final native String getIsAdd() /*-{ return this == null ? "0" : this.is_add; }-*/;

    public final String get(String key) {
	if (key.equals("id")) return getId();
	if (key.equals("club_id")) return getClubId();
	if (key.equals("nom")) return getNom();
	if (key.equals("amount_percent")) return getAmountPercent();
	if (key.equals("amount_absolute")) return getAmountAbsolute();
	throw new RuntimeException("get: bad key " + key);
    }

    public final void set(String key, String value) {
	if (key.equals("club_id")) setClubId(value);
	else if (key.equals("nom")) setNom(value);
	else if (key.equals("amount_percent")) setAmountPercent(value);
	else if (key.equals("amount_absolute")) setAmountAbsolute(value);
	else throw new RuntimeException("set: bad key " + key);
    }
}
