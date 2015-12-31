// -*-  indent-tabs-mode:nil; c-basic-offset:4; -*-
package ca.patricklam.judodb.client;

import com.google.gwt.core.client.JavaScriptObject;

public class ClubSummary extends JavaScriptObject {
    protected ClubSummary() { }

    public final native String getId() /*-{ return this.id; }-*/;
    public final native String getNom() /*-{ return this.nom; }-*/;
    public final native void setNom(String nom) /*-{ this.nom = nom; }-*/;
    public final native String getNomShort() /*-{ return this.nom_short; }-*/;
    public final native void setNomShort(String nom_short) /*-{ this.nom_short = nom_short; }-*/;
    public final native String getNumeroClub() /*-{ return this.numero_club; }-*/;
    public final native void setNumeroClub(String numero_club) /*-{ this.numero_club = numero_club; }-*/;
    public final native String getVille() /*-{ return this.ville; }-*/;
    public final native void setVille(String ville) /*-{ this.ville = ville; }-*/;
    public final native String getPrefixCodepostale() /*-{ return this.prefix_codepostale; }-*/;
    public final native void setPrefixCodepostale(String prefix_codepostale) /*-{ this.prefix_codepostale = prefix_codepostale; }-*/;
    public final native String getEscompteResident() /*-{ return this.escompte_resident == null ? '0' : this.escompte_resident; }-*/;
    public final native void setEscompteResident(String escompte_resident) /*-{ this.escompte_resident = escompte_resident; }-*/;
    public final native String getIndicatifRegional() /*-{ return this.indicatif_regional; }-*/;
    public final native void setIndicatifRegional(String indicatif_regional) /*-{ this.indicatif_regional = indicatif_regional; }-*/;
    public final native String getSupplementProrata() /*-{ return this.supplement_prorata; }-*/;
    public final native void setSupplementProrata(String supplement_paypal) /*-{ this.supplement_prorata = supplement_prorata; }-*/;
    public final native boolean getEnableProrata() /*-{ return this.pro_rata != '0'; }-*/;
    public final native void setEnableProrata(boolean pro_rata) /*-{ this.pro_rata = pro_rata ? '1' : '0'; }-*/;
    public final native boolean getAfficherPaypal() /*-{ return this.afficher_paypal != '0'; }-*/;
    public final native void setAfficherPaypal(boolean afficher_paypal) /*-{ this.afficher_paypal = afficher_paypal ? '1' : '0'; }-*/;
    public final native boolean getAjustableCours() /*-{ return this.ajustable_cours != '0'; }-*/;
    public final native void setAjustableCours(boolean ajustable_cours) /*-{ this.ajustable_cours = ajustable_cours ? '1' : '0'; }-*/;
    public final native boolean getAjustableDivision() /*-{ return this.ajustable_division != '0'; }-*/;
    public final native void setAjustableDivision(boolean ajustable_division) /*-{ this.ajustable_division = ajustable_division ? '1' : '0'; }-*/;

    public final String getClubText() {
        return "[" + getNumeroClub() + "] " + getNom();
    }
}
