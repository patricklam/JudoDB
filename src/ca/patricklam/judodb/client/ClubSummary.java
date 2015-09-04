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
    public final native String getEscompteResident() /*-{ return this.escompte_resident; }-*/;
    public final native void setEscompteResident(String escompte_resident) /*-{ this.escompte_resident = escompte_resident; }-*/;
    public final native String getIndicatifRegional() /*-{ return this.indicatif_regional; }-*/;
    public final native void setIndicatifRegional(String indicatif_regional) /*-{ this.indicatif_regional = indicatif_regional; }-*/;
    public final native boolean getDefaultProrata() /*-{ return this.pro_rata != '0'; }-*/;
    public final native void setDefaultProrata(boolean pro_rata) /*-{ this.pro_rata = pro_rata ? '1' : '0'; }-*/;
}
