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
    public final native String getPersonneContact() /*-{ return this.personne_contact; }-*/;
    public final native void setPersonneContact(String personne_contact) /*-{ this.personne_contact = personne_contact; }-*/;
    public final native String getPersonneContactCourriel() /*-{ return this.personne_contact_courriel; }-*/;
    public final native void setPersonneContactCourriel(String personne_contact_courriel) /*-{ this.personne_contact_courriel = personne_contact_courriel; }-*/;
    public final native String getPersonneContactAdresse() /*-{ return this.personne_contact_adresse; }-*/;
    public final native void setPersonneContactAdresse(String personne_contact_adresse) /*-{ this.personne_contact_adresse = personne_contact_adresse; }-*/;
    public final native String getPersonneContactTel() /*-{ return this.personne_contact_tel; }-*/;
    public final native void setPersonneContactTel(String personne_contact_tel) /*-{ this.personne_contact_tel = personne_contact_tel; }-*/;
    public final native String getPrefixCodepostale() /*-{ return this.prefix_codepostale; }-*/;
    public final native void setPrefixCodepostale(String prefix_codepostale) /*-{ this.prefix_codepostale = prefix_codepostale; }-*/;
    public final native String getEscompteResident() /*-{ return this.escompte_resident == null ? '0' : this.escompte_resident; }-*/;
    public final native void setEscompteResident(String escompte_resident) /*-{ this.escompte_resident = escompte_resident; }-*/;
    public final native String getIndicatifRegional() /*-{ return this.indicatif_regional; }-*/;
    public final native void setIndicatifRegional(String indicatif_regional) /*-{ this.indicatif_regional = indicatif_regional; }-*/;
    public final native String getSupplementProrata() /*-{ return this.supplement_prorata; }-*/;
    public final native void setSupplementProrata(String supplement_paypal) /*-{ this.supplement_prorata = supplement_prorata; }-*/;
    // should be an enum, but anyways, false = tarif, true = cours
    public final native boolean getFraisCoursTarif() /*-{ return this.frais_cours_tarif != '0'; }-*/;
    public final native void setFraisCoursTarif(boolean frais_cours_tarif) /*-{ this.frais_cours_tarif = frais_cours_tarif ? '1' : '0'; }-*/;
    public final native boolean getEnableProrata() /*-{ return this.pro_rata != '0'; }-*/;
    public final native void setEnableProrata(boolean pro_rata) /*-{ this.pro_rata = pro_rata ? '1' : '0'; }-*/;
    public final native String getAfficherPaypal() /*-{ return this.afficher_paypal; }-*/;
    public final native void setAfficherPaypal(String afficher_paypal) /*-{ this.afficher_paypal = afficher_paypal; }-*/;
    public final native String getMontantPaypal() /*-{ return this.montant_paypal; }-*/;
    public final native void setMontantPaypal(String afficher_paypal) /*-{ this.montant_paypal = montant_paypal; }-*/;
    public final native boolean getAjustableCours() /*-{ return this.ajustable_cours != '0'; }-*/;
    public final native void setAjustableCours(boolean ajustable_cours) /*-{ this.ajustable_cours = ajustable_cours ? '1' : '0'; }-*/;
    public final native boolean getAjustableDivision() /*-{ return this.ajustable_division != '0'; }-*/;
    public final native void setAjustableDivision(boolean ajustable_division) /*-{ this.ajustable_division = ajustable_division ? '1' : '0'; }-*/;
    public final native String getTresorier() /*-{ return this.tresorier; }-*/;
    public final native void setTresorier(String tresorier) /*-{ this.tresorier = tresorier; }-*/;
    public final native String getCoords() /*-{ return (this.coords == null) ? null : this.coords.replace(/_/g,","); }-*/;
    public final native void setCoords(String coords) /*-{ this.coords = coords; }-*/;

    public final String getClubText() {
        return "[" + getNumeroClub() + "] " + getNom();
    }
}
