// -*-  indent-tabs-mode:nil; c-basic-offset:4; -*-
package ca.patricklam.judodb.client;

import java.util.Date;
import com.google.gwt.core.client.JavaScriptObject;

public class PaymentData extends JavaScriptObject {
    protected PaymentData() { }

    public final native String getId() /*-{ return this.id; }-*/;
    public final native String getClientId() /*-{ return this.client_id != null ? this.client_id : ""; }-*/;
    public final native void setClientId(String client_id) /*-{ this.client_id = client_id; }-*/;
    public final native String getServiceId() /*-{ return this.service_id != null ? this.service_id : ""; }-*/;
    public final native void setServiceId(String service_id) /*-{ this.service_id = service_id; }-*/;
    public final native String getNumber() /*-{ return this.number != null ? this.number : ""; }-*/;
    public final native void setNumber(String number) /*-{ this.number = number; }-*/;
    public final native String getMode() /*-{ return this.mode; }-*/;
    public final native void setMode(String mode) /*-{ this.mode = mode; }-*/;
    public final native String getChqno() /*-{ return this.chqno != null ? this.chqno : ""; }-*/;
    public final native void setChqno(String chqno) /*-{ this.chqno = chqno; }-*/;

    public final native String getPaiementDate() /*-{ return this.paiement_date; }-*/;
    public final native void setPaiementDate(String paiement_date) /*-{ this.paiement_date = paiement_date; }-*/;
    public final Date getDate() {
        if (getPaiementDate() == null) return null;
        try {
            return Constants.DB_DATE_FORMAT.parse(getPaiementDate());
        } catch (IllegalArgumentException e) { return null; }
    }

    public final native String getMontant() /*-{ return this.montant != null ? this.montant : ""; }-*/;
    public final native void setMontant(String montant) /*-{ this.montant = montant; }-*/;
    public final native String getIsAdd() /*-{ return this == null ? "0" : this.is_add; }-*/;
    public final native void setIsAdd(String is_add) /*-{ this.is_add = is_add; }-*/;
}
