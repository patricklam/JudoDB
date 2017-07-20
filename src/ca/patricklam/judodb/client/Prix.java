// -*-  indent-tabs-mode:nil; c-basic-offset:4; -*-
package ca.patricklam.judodb.client;

import com.google.gwt.core.client.JavaScriptObject;

public class Prix extends JavaScriptObject {
    protected Prix() { }

    public final native String getId() /*-{ return this.id; }-*/;
    public final native void setId(String id) /*-{ this.id = id; }-*/;

    // prix cours is a function of: club, session, division, cours
    // NULL = frais Judo QC = FraisCostCalculator.JUDO_QC
    public final native String getClubId() /*-{ return this.club_id; }-*/;
    public final native void setClubId(String club_id) /*-{ this.club_id = club_id; }-*/;

    // may also be a pair of space-separated seqnos, i.e. "13 14"
    public final native String getSessionSeqno() /*-{ return this.session_seqno; }-*/;
    public final native void setSessionSeqno(String session_seqno) /*-{ this.session_seqno = session_seqno; }-*/;

    // "*" means match all divisions
    public final native String getDivisionAbbrev() /*-{ return this.division_abbrev; }-*/;
    public final native void setDivisionAbbrev(String division_abbrev) /*-{ this.division_abbrev = division_abbrev; }-*/;

    // -1 means: applicable to all cours
    public final native String getCoursId() /*-{ return this.cours_id; }-*/;
    public final native void setCoursId(String id) /*-{ this.cours_id = id; }-*/;

    public final native String getFrais() /*-{ return this.frais; }-*/;
    public final native void setFrais(String frais) /*-{ this.frais = frais; }-*/;

    public final native String getNomTarifId() /*-{ return this.nom_tarif_id == null ? "" : this.nom_tarif_id; }-*/;
    public final native void setNomTarifId(String nom_tarif_id) /*-{ this.nom_tarif_id = nom_tarif_id; }-*/;

    public final native String getNomTarif() /*-{ return this.nom_tarif == null ? "" : this.nom_tarif; }-*/;
    public final native void setNomTarif(String nom_tarif) /*-{ this.nom_tarif = nom_tarif; }-*/;

    public final native String getIsAdd() /*-{ return this.is_add; }-*/;

    public final String get(String key) {
	if (key.equals("id")) return getId();
	if (key.equals("club_id")) return getClubId();
	if (key.equals("session_seqno")) return getSessionSeqno();
	if (key.equals("division_abbrev")) return getDivisionAbbrev();
	if (key.equals("cours_id")) return getCoursId();
	if (key.equals("frais")) return getFrais();
	if (key.equals("nom_tarif_id")) return getNomTarifId();
	if (key.equals("is_add")) return getIsAdd();
	throw new RuntimeException("get: bad key " + key);
    }

    public final void set(String key, String value) {
	if (key.equals("id")) setId(value);
	else if (key.equals("club_id")) setClubId(value);
	else if (key.equals("session_seqno")) setSessionSeqno(value);
	else if (key.equals("division_abbrev")) setDivisionAbbrev(value);
	else if (key.equals("cours_id")) setCoursId(value);
	else if (key.equals("frais")) setFrais(value);
	else if (key.equals("nom_tarif_id")) setNomTarifId(value);
	else throw new RuntimeException("set: bad key " + key);
    }
}
