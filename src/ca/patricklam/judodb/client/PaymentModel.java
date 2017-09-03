// -*-  indent-tabs-mode:nil; c-basic-offset:4; -*-
package ca.patricklam.judodb.client;

import com.google.gwt.core.client.JsonUtils;
import java.util.Date;

public class PaymentModel implements Comparable<PaymentModel> {
    public final String getId() { return this.id; }
    public final String getClientId() { return this.client_id != null ? this.client_id : ""; }
    public final void setClientId(String client_id) { this.client_id = client_id; }
    public final String getServiceId() { return this.service_id != null ? this.service_id : ""; }
    public final void setServiceId(String service_id) { this.service_id = service_id; }
    public final String getNumber() { return this.number != null ? this.number : ""; }
    public final void setNumber(String number) { this.number = number; }
    public final String getMode() { return this.mode; }
    public final void setMode(String mode) { this.mode = mode; }
    public final String getChqno() { return this.chqno != null ? this.chqno : ""; }
    public final void setChqno(String chqno) { this.chqno = chqno; }

    public final String getPaiementDate() { return this.paiement_date; }
    public final void setPaiementDate(String paiement_date) { this.paiement_date = paiement_date; }

    public final String getMontant() { return this.montant != null ? this.montant : ""; }
    public final void setMontant(String montant) { this.montant = montant; }
    public final boolean getIsAdd() { return this.is_add; }
    public final void setIsAdd(boolean is_add) { this.is_add = is_add; }

    public final String get(String key) {
	if (key.equals("id")) return getId();
	if (key.equals("client_id")) return getClientId();
	if (key.equals("service_id")) return getServiceId();
	if (key.equals("number")) return getNumber();
	if (key.equals("mode")) return getMode();
	if (key.equals("chqno")) return getChqno();
	if (key.equals("paiement_date")) return getPaiementDate();
	if (key.equals("montant")) return getMontant();
	throw new RuntimeException("get: bad key " + key);
    }

    public final void set(String key, String value) {
	// id read-only
	// if (key.equals("id")) setId(value);
	if (key.equals("client_id")) setClientId(value);
	if (key.equals("service_id")) setServiceId(value);
	else if (key.equals("number")) setNumber(value);
	else if (key.equals("mode")) setMode(value);
	else if (key.equals("chqno")) setChqno(value);
	else if (key.equals("paiement_date")) setPaiementDate(value);
	else if (key.equals("montant")) setMontant(value);
	else throw new RuntimeException("set: bad key " + key);
    }

    public final int compareTo(PaymentModel o) {
        if (getIsAdd() || o == null) return -1;
        if (o.getIsAdd()) return 1;

        Date thisDate = null, otherDate = null;
        try {
            thisDate = Constants.STD_DATE_FORMAT.parse(getPaiementDate());
            otherDate = Constants.STD_DATE_FORMAT.parse(o.getPaiementDate());
        } catch (IllegalArgumentException e) {}
        if (thisDate == null) return -1;
        if (otherDate == null) return 1;

        return thisDate.compareTo(otherDate);
    }

    public PaymentData toPaymentData() {
	PaymentData pd =
	    JsonUtils.<PaymentData>safeEval
	    ("{\"is_add\":\"0\",\"id\":\""+getId()+"\"}");
        pd.setClientId(getClientId());
	pd.setServiceId(getServiceId());
        pd.setNumber(getNumber());
	pd.setMode(getMode());
	pd.setChqno(getChqno());
	pd.setPaiementDate(getPaiementDate());
	pd.setMontant(getMontant());
        return pd;
    }

    PaymentModel(String id) { this.id = id; }

    PaymentModel(PaymentData pd) {
        this.id = pd.getId();
        this.client_id = pd.getClientId();
        this.service_id = pd.getServiceId();
        this.number = pd.getNumber();
        this.mode = pd.getMode();
        this.chqno = pd.getChqno();
        this.paiement_date = pd.getPaiementDate();
        this.montant = pd.getMontant();
    }

    PaymentModel(PaymentModel pm) {
        this.id = pm.getId();
        this.client_id = pm.getClientId();
        this.service_id = pm.getServiceId();
        this.number = pm.getNumber();
        this.mode = pm.getMode();
        this.chqno = pm.getChqno();
        this.paiement_date = pm.getPaiementDate();
        this.montant = pm.getMontant();
        this.is_add = pm.getIsAdd();
    }

    private String id, client_id, service_id, number, mode, chqno, paiement_date, montant;
    private boolean is_add;
}
