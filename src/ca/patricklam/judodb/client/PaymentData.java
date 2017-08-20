// -*-  indent-tabs-mode:nil; c-basic-offset:4; -*-
package ca.patricklam.judodb.client;

import java.util.Date;
import com.google.gwt.core.client.JavaScriptObject;

public class PaymentData extends JavaScriptObject {
    protected PaymentData() { }

    public final native String getId() /*-{ return this.id; }-*/;
    public final native String getServiceId() /*-{ return this.service_id != null ? this.service_id : ""; }-*/;
    public final native void setServiceId(String service_id) /*-{ this.service_id = service_id; }-*/;
    public final native String getNumber() /*-{ return this.number != null ? this.number : ""; }-*/;
    public final native void setNumber(String number) /*-{ this.number = number; }-*/;
    public final native String getMode() /*-{ return this.mode; }-*/;
    public final native void setMode(String mode) /*-{ this.mode = mode; }-*/;
    public final native String getChqno() /*-{ return this.chqno != null ? this.chqno : ""; }-*/;
    public final native void setChqno(String chqno) /*-{ this.chqno = chqno; }-*/;

    public final native String getDateString() /*-{ return this.date; }-*/;
    public final native void setDateString(String date) /*-{ this.date = date; }-*/;
    public final Date getDate() {
        if (getDateString() == null) return null;
        try {
            return Constants.DB_DATE_FORMAT.parse(getDateString());
        } catch (IllegalArgumentException e) { return null; }
    }

    public final native String getMontant() /*-{ return this.montant != null ? this.montant : ""; }-*/;
    public final native void setMontant(String montant) /*-{ this.montant = montant; }-*/;
    public final native String getIsAdd() /*-{ return this.is_add; }-*/;

    public final String get(String key) {
	if (key.equals("id")) return getId();
	if (key.equals("service_id")) return getServiceId();
	if (key.equals("number")) return getNumber();
	if (key.equals("mode")) return getMode();
	if (key.equals("chqno")) return getChqno();
	if (key.equals("date")) return getDateString();
	if (key.equals("montant")) return getMontant();
	throw new RuntimeException("get: bad key " + key);
    }

    public final void set(String key, String value) {
	// id read-only
	// if (key.equals("id")) setId(value);
	if (key.equals("service_id")) setServiceId(value);
	else if (key.equals("number")) setNumber(value);
	else if (key.equals("mode")) setMode(value);
	else if (key.equals("chqno")) setChqno(value);
	else if (key.equals("date")) setDateString(value);
	else if (key.equals("montant")) setMontant(value);
	else throw new RuntimeException("set: bad key " + key);
    }

    public final int compareTo(PaymentData o) {
	int s = Integer.parseInt(getId()), os = Integer.parseInt(o.getId());
	return s - os;
    }
}
