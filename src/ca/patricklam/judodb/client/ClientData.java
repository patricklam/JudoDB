package ca.patricklam.judodb.client;

import java.util.Date;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.i18n.client.DateTimeFormat;

public class ClientData extends JavaScriptObject {
	protected ClientData() {}
	
	public final native String getID() /*-{ return this.id; }-*/;

	public final native String getNom() /*-{ return this.nom; }-*/;
	public final native void setNom(String nom) /*-{ this.nom = nom; }-*/;
	public final native String getPrenom() /*-{ return this.prenom; }-*/;
	public final native void setPrenom(String prenom) /*-{ this.prenom = prenom; }-*/;
	public final native String getDDNString() /*-{ return this.ddn; }-*/;
	public final native void setDDNString(String ddn) /*-{ this.ddn = ddn; }-*/;
	public final native String getSexe() /*-{ return this.sexe; }-*/;
	public final native void setSexe(String sexe) /*-{ this.sexe = sexe; }-*/;
	
	public final native String getAdresse() /*-{ return this.adresse; }-*/;
	public final native void setAdresse(String adresse) /*-{ this.adresse = adresse; }-*/;
	public final native String getVille() /*-{ return this.ville; }-*/;
	public final native void setVille(String ville) /*-{ this.ville = ville; }-*/;
	public final native String getCodePostal() /*-{ return this.code_postal; }-*/;
	public final native void setCodePostal(String codePostal) /*-{ this.code_postal = codePostal; }-*/;
	public final native String getTel() /*-{ return this.tel; }-*/;
	public final native void setTel(String tel) /*-{ this.tel = tel; }-*/;
	public final native String getCourriel() /*-{ return this.courriel; }-*/;
	public final native void setCourriel(String courriel) /*-{ this.courriel = courriel; }-*/;

	public final native String getJudoQC() /*-{ return this.affiliation; }-*/;
	public final native void setJudoQC(String judoQC) /*-{ this.judoQC = judoQC; }-*/;
	
	public final native JsArray<GradeData> getGrades() /*-{ return this.grades; }-*/;
	public final native void setGrades(JsArray<GradeData> grades) /*-{ this.grades = grades; }-*/;
	
	public final native String getCarteAnjou() /*-{ return this.carte_anjou; }-*/;
	public final native void setCarteAnjou(String carte_anjou) /*-{ this.carte_anjou = carte_anjou; }-*/;
	public final native String getNomRecuImpot() /*-{ return this.nom_recu_impot; }-*/;
	public final native void setNomRecuImpot(String nom_recu_impot) /*-{ this.nom_recu_impot = nom_recu_impot; }-*/;
	
	public final native String getNomContactUrgence() /*-{ return this.nom_contact_urgence; }-*/;
	public final native void setNomContactUrgence(String nom_contact_urgence) /*-{ this.nom_contact_urgence = nom_contact_urgence; }-*/;
	public final native String getTelContactUrgence() /*-{ return this.tel_contact_urgence; }-*/;
	public final native void setTelContactUrgence(String tel_contact_urgence) /*-{ this.tel_contact_urgence = tel_contact_urgence; }-*/;

	public final native ServiceData getServices() /*-{ return this.services; }-*/;
	public final native void setServices(ServiceData services) /*-{ this.services = services; }-*/;
	
	public final GradeData getMostRecentGrade() {
		JsArray<GradeData> grades = getGrades();
		if (grades == null || grades.length() == 0) return null;
		
		GradeData m = grades.get(0); 
		for (int i = 0; i < grades.length(); i++) {
			if (grades.get(i).getDateGrade().compareTo(m.getDateGrade()) > 0)
				m = grades.get(i);
		}
		return m;
	}
	
	public final String getGrade() {
		GradeData m = getMostRecentGrade();
		return m == null ? "" : m.getGrade();
	}

	public final String getDateGrade() { 
		GradeData m = getMostRecentGrade();
		return m == null ? "" : m.getDateGrade();
	}
	
	public final Date getDDN() {
		return DateTimeFormat.getFormat("yyyy-MM-dd").parse(getDDNString());
	}
	
	public final boolean isNoire() {
		return getGrade().endsWith("D");
	}
		
	public final String getCategorieAbbrev() {
		Date d = getDDN();
		int year = Integer.parseInt(DateTimeFormat.getFormat("yyyy").format(d));
		
		for (int i = 0; i < Constants.CATEGORIES.length; i++) {
			if (isNoire() == Constants.CATEGORIES[i].noire &&
					((Constants.CATEGORIES[i].years_ago == 0) ||
					 (Constants.CURRENT_YEAR - Constants.CATEGORIES[i].years_ago < year)))
				return Constants.CATEGORIES[i].abbrev;
		}
		return "";
	}
	
	/* deprecated */
	public final native String getRAMQ() /*-{ return this.RAMQ; }-*/;	
}