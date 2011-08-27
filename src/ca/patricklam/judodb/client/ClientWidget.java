package ca.patricklam.judodb.client;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.Widget;

public class ClientWidget extends Composite {
	interface MyUiBinder extends UiBinder<Widget, ClientWidget> {}
	public static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

	private final JudoDB jdb;

	@UiField DivElement cid;

	@UiField TextBox nom;
	@UiField TextBox prenom;
	@UiField TextBox ddn;
	@UiField TextBox sexe;

	@UiField TextBox adresse;
	@UiField TextBox ville;
	@UiField TextBox codePostal;
	@UiField TextBox tel;
	@UiField TextBox courriel;
	
	@UiField TextBox affiliation;
	@UiField TextBox grade;
	@UiField Anchor showgrades;
	@UiField TextBox date_grade;
	@UiField TextBox carte_anjou;
	@UiField TextBox nom_recu_impot;

	@UiField TextBox tel_contact_urgence;

	@UiField ListBox date_inscription;
	@UiField Anchor inscrire;
	@UiField Anchor modifier;
	@UiField Anchor desinscrire;
	@UiField TextBox saisons;
	@UiField CheckBox verification;

	@UiField TextBox categorie;
	@UiField TextBox categorieFrais;

	@UiField ListBox cours;
	@UiField ListBox sessions;

	@UiField ListBox escompte;
	@UiField TextBox cas_special_note;
	@UiField TextBox cas_special_pct;
	@UiField TextBox escompteFrais;

	@UiField CheckBox sans_affiliation;
	@UiField TextBox affiliationFrais;

	@UiField TextBox judogi;
	@UiField CheckBox passeport;
	@UiField CheckBox non_anjou;
	@UiField TextBox suppFrais;

	@UiField CheckBox solde;
	@UiField TextBox frais;
	
	@UiField Button saveClientButton;
	@UiField Button saveAndReturnClientButton;
	@UiField Button discardClientButton;

	@UiField HTMLPanel blurb;
	@UiField HTMLPanel gradeHistory;
	@UiField Grid gradeTable;
	@UiField Anchor saveGrades;
	@UiField Anchor annulerGrades;
	
	private static final String PULL_ONE_CLIENT_URL = JudoDB.BASE_URL + "pull_one_client.php?id=";
	private static final String CALLBACK_URL_SUFFIX = "&callback=";
	public interface BlurbTemplate extends SafeHtmlTemplates {
		@Template 
	      ("<p>Je {0} certifie que les informations inscrites sur ce formulaire sont véridiques. "+
	       "J'adhère au Club Judo Anjou. J'accepte tous les risques d'accident liés à la pratique du "+
	       "judo qui pourraient survenir dans les locaux ou lors d'activités extérieurs organisées par le Club. "+
	       "J'accepte de respecter les règlements du Club.</p>"+
	       "<h4>Politique de remboursement</h4>"+
	       "<p>Aucun remboursement ne sera accordé sans présentation d'un certificat médical du participant. "+
	       "Seuls les frais de cours correspondant à la période restante sont remboursables. "+
	       "En cas d'annulation par le participant, un crédit pourra être émis par le Club, "+
	       "pour les frais de cours correspondant à la période restante.</p>"+
	       "<p>Signature {1}: <span style='float:right'>Date: _________</span></p>"+
	       "<p>Signature résponsable du club:  <span style='float:right'>Date: {2}</span></p>")
	       SafeHtml blurb(String nom, String membreOuParent, String today);
	}
	private static final BlurbTemplate BLURB = GWT.create(BlurbTemplate.class);
		
	private ClientData cd;
	private int currentServiceNumber;
	public int getCurrentServiceNumber() { return currentServiceNumber; }
	
	public ClientWidget(JudoDB jdb, int cid) {
		this.jdb = jdb;
		initWidget(uiBinder.createAndBindUi(this));
		jdb.pleaseWait();
		
		for (Constants.Cours c : Constants.COURS) {
			cours.addItem(c.name, c.seqno);
		}
		sessions.addItem("1");
		sessions.addItem("2");
		for (Constants.Escompte e : Constants.ESCOMPTES) {
			escompte.addItem(e.name, Integer.toString(e.amount));
		}
		
		gradeHistory.setVisible(false);
		categorie.setReadOnly(true);
		saisons.setReadOnly(true);
		categorieFrais.setReadOnly(true); categorieFrais.setAlignment(ValueBoxBase.TextAlignment.RIGHT);
		cas_special_pct.setAlignment(ValueBoxBase.TextAlignment.RIGHT);
		escompteFrais.setReadOnly(true); escompteFrais.setAlignment(ValueBoxBase.TextAlignment.RIGHT);
		judogi.setAlignment(ValueBoxBase.TextAlignment.RIGHT);
		affiliationFrais.setReadOnly(true); affiliationFrais.setAlignment(ValueBoxBase.TextAlignment.RIGHT);
		suppFrais.setReadOnly(true); suppFrais.setAlignment(ValueBoxBase.TextAlignment.RIGHT);
		frais.setReadOnly(true); frais.setAlignment(ValueBoxBase.TextAlignment.RIGHT);
		((Element)cas_special_note.getElement().getParentNode()).getStyle().setDisplay(Display.NONE);
		((Element)cas_special_pct.getElement().getParentNode()).getStyle().setDisplay(Display.NONE);

		sessions.setItemSelected(1, true);
		
		inscrire.addClickHandler(inscrireClickHandler);
		modifier.addClickHandler(modifierClickHandler);
		desinscrire.addClickHandler(desinscrireClickHandler);
		showgrades.addClickHandler(new ClickHandler() { 
			public void onClick(ClickEvent e) { gradeHistory.setVisible(true); } ;
		});
		saveGrades.addClickHandler(saveGradesHandler);
		annulerGrades.addClickHandler(annulerGradesHandler);
		
		ddn.addChangeHandler(recomputeHandler);
		grade.addChangeHandler(recomputeHandler);
		date_inscription.addChangeHandler(changeSaisonHandler);
		sessions.addChangeHandler(recomputeHandler);
		escompte.addChangeHandler(recomputeHandler);
		cas_special_pct.addChangeHandler(clearEscompteAmtAndRecomputeHandler);
		escompteFrais.addChangeHandler(clearEscomptePctAndRecomputeHandler);
		sans_affiliation.addValueChangeHandler(recomputeValueHandler);
		judogi.addChangeHandler(recomputeHandler);
		passeport.addValueChangeHandler(recomputeValueHandler);
		non_anjou.addValueChangeHandler(recomputeValueHandler);
		
		saveAndReturnClientButton.addClickHandler(new ClickHandler() { 
			public void onClick(ClickEvent e) {
				pushClientDataToServer();
				ClientWidget.this.jdb.popMode(); }
		});
		saveClientButton.addClickHandler(new ClickHandler() { 
			public void onClick(ClickEvent e) {
				pushClientDataToServer();
			}
		});
		discardClientButton.addClickHandler(new ClickHandler() { 
			public void onClick(ClickEvent e) {
				ClientWidget.this.jdb.clearStatus();
				ClientWidget.this.jdb.popMode(); 
			}
		});

		if (cid != -1)
			getJson(jdb.jsonRequestId++, PULL_ONE_CLIENT_URL + cid + CALLBACK_URL_SUFFIX, this);
		else {
			this.cd = JavaScriptObject.createObject().cast();
			JsArray<ServiceData> sa = JavaScriptObject.createArray().cast();
			ServiceData sd = ServiceData.newServiceData();
			sd.inscrireAujourdhui();
			sa.set(0, sd);
			this.cd.setServices(sa);
			currentServiceNumber = 0;
			
			JsArray<GradeData> ga = JavaScriptObject.createArray().cast();
			this.cd.setGrades(ga);
			loadClientData();
			jdb.clearStatus();
		}
	}
	
	@SuppressWarnings("deprecation")
	private void updateBlurb() {
		if (cd.getDDNString() == null) return;
		Date ddn = cd.getDDN(), today = new Date();
		if (cd.getDDN() == null) return;
		
		int by = ddn.getYear(), bm = ddn.getMonth(), bd = ddn.getDay(), 
			ny = today.getYear(), nm = today.getMonth(), nd = ddn.getDay();
		int y = ny - by; if (bm > nm || (bm == nm && bd > nd)) y--;
		String nom = cd.getPrenom() + " " + cd.getNom();
		String nn, mm;
		if (y >= 18) {
			nn = nom; mm = "membre";
		} else {
			nn = "__________________________, parent ou tuteur du membre,";
			mm = "parent ou tuteur";
		}
		
		SafeHtml blurbContents = BLURB.blurb(nn, mm, DateTimeFormat.getFormat("yyyy-MM-dd").format(today));
		
		blurb.clear();
		blurb.add(new HTMLPanel(blurbContents));
	}
		
	/** Takes data from ClientData into the form. */
	private void loadClientData () {
		cid.setInnerText(cd.getID());
		nom.setText(cd.getNom());
		prenom.setText(cd.getPrenom());
		ddn.setText(cd.getDDNString());
		sexe.setText(cd.getSexe());
		adresse.setText(cd.getAdresse());
		ville.setText(cd.getVille());
		codePostal.setText(cd.getCodePostal());
		tel.setText(cd.getTel());
		courriel.setText(cd.getCourriel());

		affiliation.setText(cd.getJudoQC());
		grade.setText(cd.getGrade());
		date_grade.setText(cd.getDateGrade());
		carte_anjou.setText(cd.getCarteAnjou());
		nom_recu_impot.setText(cd.getNomRecuImpot());

		tel_contact_urgence.setText(cd.getTelContactUrgence());
		
		ServiceData sd;
		if (currentServiceNumber == -1) {
			sd = ServiceData.newServiceData();
			sd.inscrireAujourdhui();
		}
		else
			sd = cd.getServices().get(currentServiceNumber);

		loadGradesData();
		
		date_inscription.clear();
		boolean hasToday = false;
		boolean hasThisSession = cd.getServiceFor(Constants.currentSession()) != null; 
		String todayString = DateTimeFormat.getFormat("yyyy-MM-dd").format(new Date()); 
		
		for (int i = 0; i < cd.getServices().length(); i++) {
			ServiceData ssd = cd.getServices().get(i);
			if (todayString.equals(ssd.getDateInscription())) hasToday = true;
			date_inscription.addItem(ssd.getDateInscription(), Integer.toString(i));
		}
		date_inscription.setSelectedIndex(currentServiceNumber);
		inscrire.setVisible(!hasToday && !hasThisSession);
		modifier.setVisible(!hasToday && hasThisSession);
		desinscrire.setVisible(cd.getServices().length() > 0);
		
		// categories is set in recompute().
		saisons.setText(sd.getSaisons());
		verification.setValue(sd.getVerification());
		cours.setItemSelected(Integer.parseInt(sd.getCours()), true);
		sessions.setItemSelected(sd.getSessionCount()-1, true);
		categorieFrais.setText(sd.getCategorieFrais());
		
		sans_affiliation.setValue(sd.getSansAffiliation());
		affiliationFrais.setText(sd.getAffiliationFrais());

		escompte.setSelectedIndex(sd.getEscompteType());
		cas_special_note.setText(sd.getCasSpecialNote());
		cas_special_pct.setValue(sd.getCasSpecialPct());
		escompteFrais.setText(sd.getEscompteFrais());
		
		judogi.setText(sd.getJudogi());
		passeport.setValue(sd.getPasseport());
		non_anjou.setValue(sd.getNonAnjou());
		suppFrais.setText(sd.getSuppFrais());	
		
		frais.setText(sd.getFrais());
		solde.setValue(sd.getSolde());
		
		recompute();
	}

	/** Puts data from the form back onto ClientData. */
	private void saveClientData() {
		cd.setNom(nom.getText());
		cd.setPrenom(prenom.getText());
		cd.setDDNString(ddn.getText());
		cd.setSexe(sexe.getText());
		
		cd.setAdresse(adresse.getText());
		cd.setVille(ville.getText());
		cd.setCodePostal(codePostal.getText());
		cd.setTel(tel.getText());
		cd.setCourriel(courriel.getText());

		cd.setJudoQC(affiliation.getText());
		// TODO update grade
		cd.setCarteAnjou(carte_anjou.getText());
		cd.setNomRecuImpot(nom_recu_impot.getText());

		cd.setTelContactUrgence(tel_contact_urgence.getText());
		
		if (currentServiceNumber == -1) return;
		
		ServiceData sd = cd.getServices().get(currentServiceNumber);
		sd.setDateInscription(date_inscription.getItemText(currentServiceNumber));
		sd.setSaisons(saisons.getText());
		sd.setVerification(verification.getValue());
		sd.setCours(Integer.toString(cours.getSelectedIndex()));
		sd.setSessionCount(sessions.getSelectedIndex()+1);
		sd.setCategorieFrais(stripDollars(categorieFrais.getText()));
		
		sd.setSansAffiliation(sans_affiliation.getValue());
		sd.setAffiliationFrais(stripDollars(affiliationFrais.getText()));
		
		sd.setEscompteType(escompte.getSelectedIndex());
		sd.setCasSpecialNote(cas_special_note.getText());
		sd.setCasSpecialPct(cas_special_pct.getText());
		sd.setEscompteFrais(escompteFrais.getText());
		
		sd.setJudogi(judogi.getText());
		sd.setPasseport(passeport.getValue());
		sd.setNonAnjou(non_anjou.getValue());
		sd.setSuppFrais(suppFrais.getText());
		
		sd.setFrais(frais.getText());
		sd.setSolde(solde.getValue());
	}
	
	/** Load grades data from ClientData into the gradeTable. */
	private void loadGradesData() {
		gradeTable.clear();
		gradeTable.resize(cd.getGrades().length()+2, 2);
		gradeTable.setText(0, 0, "Grade"); 
		gradeTable.getCellFormatter().setStyleName(0, 0, "{style.lwp}");
		gradeTable.setText(0, 1, "Date");
		gradeTable.getCellFormatter().setStyleName(0, 1, "{style.lwp}");

		JsArray<GradeData> grades_ = cd.getGrades();
		GradeData[] grades = new GradeData[grades_.length()];
		for (int i = 0; i < grades_.length(); i++)
			grades[i] = grades_.get(i);
		
		Arrays.sort(grades, new Comparator<GradeData>() {
			// note: this comparator is reversed!
			@SuppressWarnings("deprecation")
			public final int compare(GradeData g1, GradeData g0) {
				DateTimeFormat df = DateTimeFormat.getFormat("yyyy-MM-dd");
				
				Date d0 = df.parse(g0.getDateGrade());
				Date d1 = df.parse(g1.getDateGrade());
				
				if (d0.getYear() != d1.getYear())
					return d0.getYear() - d1.getYear();
				
				if (d0.getMonth() != d1.getMonth())
					return d0.getMonth() - d1.getMonth();
				
				if (d0.getDay() != d1.getDay())
					return d0.getDay() - d1.getDay();
				
				return 0;
			}
		});
		
		for (int i = 0; i < grades.length; i++) {
			setGradesTableRow(i+1, grades[i].getGrade(), grades[i].getDateGrade());
		}
		setGradesTableRow(grades.length+1, "", "");
	}

	private void setGradesTableRow(int row, String grade, String dateGrade) {
		TextBox g = new TextBox(); 
		TextBox gd = new TextBox(); 

		g.setValue(grade); g.setVisibleLength(5);
		gd.setValue(dateGrade); gd.setVisibleLength(10);
		gradeTable.setWidget(row, 0, g);
		gradeTable.setWidget(row, 1, gd);
		
	}
	
	private final ChangeHandler recomputeHandler = new ChangeHandler() {
		public void onChange(ChangeEvent e) { recompute(); }
	};
	private final ChangeHandler changeSaisonHandler = new ChangeHandler() {
		public void onChange(ChangeEvent e) { 
			saveClientData();
			currentServiceNumber = Integer.parseInt(date_inscription.getValue(date_inscription.getSelectedIndex()));
			loadClientData();
		}
	};
	private final ValueChangeHandler<Boolean> recomputeValueHandler = new ValueChangeHandler<Boolean>() {
		public void onValueChange(ValueChangeEvent<Boolean> e) { recompute(); }
	};

	private final ChangeHandler clearEscomptePctAndRecomputeHandler = new ChangeHandler() {
		public void onChange(ChangeEvent e) { 
			cas_special_pct.setValue("-1");
			recompute(); 
		}
	};

	private final ChangeHandler clearEscompteAmtAndRecomputeHandler = new ChangeHandler() {
		public void onChange(ChangeEvent e) { 
			escompteFrais.setValue("-1");
			recompute(); 
		}
	};

	/** Create a new inscription for the current session. */
	private final ClickHandler inscrireClickHandler = new ClickHandler() {
		public void onClick(ClickEvent e) { 
			saveClientData();

			ServiceData sd = cd.getServiceFor(Constants.currentSession());
			if (sd == null) {
				sd = ServiceData.newServiceData();
				cd.getServices().push(sd);
			}  // actually, sd != null should not occur; that should be modify.
			sd.inscrireAujourdhui();
			currentServiceNumber = cd.getMostRecentServiceNumber();
			loadClientData(); 
			recompute(); 
		}
	};

	/** Resets the date d'inscription for the current seesion to today's date. */
	private final ClickHandler modifierClickHandler = new ClickHandler() {
		public void onClick(ClickEvent e) { 
			ServiceData sd = cd.getServiceFor(Constants.currentSession());

			// shouldn't happen, actually.
			if (sd == null)
				return;

			saveClientData();
			sd.inscrireAujourdhui();
			currentServiceNumber = cd.getMostRecentServiceNumber();
			loadClientData(); 
			recompute(); 
		}
	};
	
	private final ClickHandler desinscrireClickHandler = new ClickHandler() {
		public void onClick(ClickEvent e) { 
			saveClientData();

			JsArray<ServiceData> newServices = JavaScriptObject.createArray().cast();
			for (int i = 0, j = 0; i < cd.getServices().length(); i++) {
				if (i != currentServiceNumber)
					newServices.set(j++, cd.getServices().get(i));
			}
			cd.setServices(newServices);
			currentServiceNumber = cd.getMostRecentServiceNumber();
			loadClientData(); 
			recompute(); 
		}
	};
	
	private final ClickHandler saveGradesHandler = new ClickHandler() {
		public void onClick(ClickEvent e) {
			// TODO
			gradeHistory.setVisible(false);			
		}
	};

	private final ClickHandler annulerGradesHandler = new ClickHandler() {
		public void onClick(ClickEvent e) {
			loadGradesData();
			gradeHistory.setVisible(false);
		}
	};
	
	// argh NumberFormat doesn't work for me at all!
	// stupidly, it says that you have to use ',' as the decimal separator, but people use both '.' and ','.
	private String stripDollars(String s) {
		StringBuffer ss = new StringBuffer("");
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == '$' || s.charAt(i) == ' ' || s.charAt(i) == ')') 
				continue;

			if (s.charAt(i) == ',')
				ss.append(".");
			else if (s.charAt(i) == '(')
				ss.append("-");
			else
				ss.append(s.charAt(i));
		}
		return ss.toString();
	}

	private static native float parseFloat(String s) /*-{ return parseFloat(s); }-*/;
	
	@SuppressWarnings("deprecation")
	private boolean sameDate(Date d1, Date d2) {
		return d1.getYear() == d2.getYear() && d1.getMonth() == d2.getMonth() && d1.getDay() == d2.getDay();
	}
	
	private void updateFrais() {
		ServiceData sd = cd.getServices().get(currentServiceNumber);
		Date dateInscription = DateTimeFormat.getFormat("yyyy-MM-dd").parse(sd.getDateInscription());
		
		// do not update frais for previous inscriptions
		if (!sameDate(dateInscription, new Date()))
			return;
		
		NumberFormat cf = NumberFormat.getCurrencyFormat("CAD");

		int sessionCount = 1;
		if (sessions.getValue(sessions.getSelectedIndex()).equals("2")) {
			sessionCount = 2;
		}

		saisons.setText(Constants.getCurrentSessionIds(sessionCount));

		Constants.Categorie c = cd.getCategorie(Constants.currentSession().effective_year);
		
		double dCategorieFrais = Constants.getFraisCours
			(Constants.currentSessionNo(), c, sessionCount);
		
		double dEscompteFrais = 0.0;
		escompteFrais.setReadOnly(true); 
		if (escompte.getValue(escompte.getSelectedIndex()).equals("-1")) {
			NumberFormat nf = NumberFormat.getDecimalFormat();
			if (cas_special_pct.getValue().equals("-1")) {
				String ef = stripDollars(escompteFrais.getValue());
				float fEf = parseFloat(ef);
				cas_special_pct.setValue(nf.format(-100 * fEf / dCategorieFrais));
				if (fEf > 0) {
					fEf *= -1;
					escompteFrais.setValue(nf.format(fEf));
				}
			} else if (escompteFrais.getValue().equals("-1")) {
				String cpct = stripDollars(cas_special_pct.getValue());
				float fCpct = parseFloat(cpct); 
				if (fCpct < 0) {
					fCpct *= -1; 
					cas_special_pct.setValue(nf.format(fCpct));
				}
				escompteFrais.setValue(nf.format(-fCpct * dCategorieFrais / 100.0));				
			}
			dEscompteFrais = parseFloat(stripDollars(escompteFrais.getValue()));
			escompteFrais.setReadOnly(false); 
		} else {
			dEscompteFrais = -dCategorieFrais * parseFloat(escompte.getValue(escompte.getSelectedIndex())) / 100; 
		}
		
		double dAffiliationFrais = 0.0;
		if (!sans_affiliation.getValue())
			dAffiliationFrais = Constants.getFraisJudoQC(Constants.currentSessionNo(), c);

		double dSuppFrais = parseFloat(stripDollars(judogi.getText()));
		if (passeport.getValue())
			dSuppFrais += Constants.PASSEPORT_JUDO_QC;
		if (non_anjou.getValue())
			dSuppFrais += Constants.NON_ANJOU;

		categorieFrais.setText (cf.format(dCategorieFrais));
		affiliationFrais.setText (cf.format(dAffiliationFrais));
		escompteFrais.setValue(cf.format(dEscompteFrais));
		suppFrais.setText(cf.format(dSuppFrais));

		frais.setText(cf.format(dCategorieFrais + dAffiliationFrais + dEscompteFrais + dSuppFrais));		
	}
	
	private void recompute() {
		saveClientData();

		Display d = Display.NONE;
		if (escompte.getValue(escompte.getSelectedIndex()).equals("-1")) 
			d = Display.INLINE;
		((Element)cas_special_note.getElement().getParentNode()).getStyle().setDisplay(d);
		((Element)cas_special_pct.getElement().getParentNode()).getStyle().setDisplay(d);

		ServiceData sd = cd.getServices().get(currentServiceNumber);
		if (sd != null && !sd.getSaisons().equals("")) {
			Constants.Categorie c = cd.getCategorie(Constants.session(sd.getSaisons()).effective_year);
			categorie.setText(c.abbrev);
		}

		updateBlurb();
		updateFrais();
	}

	private void pushClientDataToServer() {
		// now write to the server!
		// http://stackoverflow.com/questions/3985309/making-post-requests-with-parameters-in-gwt
		// http://stackoverflow.com/questions/2699277/post-data-to-jsonp	
	}
	
	/**
	 * Make call to remote server.
	 */
	public native static void getJson(int requestId, String url,
	      ClientWidget handler) /*-{
	   var callback = "callback" + requestId;

	   var script = document.createElement("script");
	   script.setAttribute("src", url+callback);
	   script.setAttribute("type", "text/javascript");
	   window[callback] = function(jsonObj) {
	     handler.@ca.patricklam.judodb.client.ClientWidget::handleJsonResponse(Lcom/google/gwt/core/client/JavaScriptObject;)(jsonObj);
	     window[callback + "done"] = true;
	   }

	   setTimeout(function() {
	     if (!window[callback + "done"]) {
	       handler.@ca.patricklam.judodb.client.ClientWidget::handleJsonResponse(Lcom/google/gwt/core/client/JavaScriptObject;)(null);
	     }

	     document.body.removeChild(script);
	     delete window[callback];
	     delete window[callback + "done"];
	   }, 5000);

	   document.body.appendChild(script);
	  }-*/;

	/**
	 * Handle the response to the request for data from a remote server.
	 */
	public void handleJsonResponse(JavaScriptObject jso) {
		if (jso == null) {
			jdb.displayError("Couldn't retrieve JSON");
			return;
		}	

	    this.cd = jso.cast();
	    currentServiceNumber = cd.getMostRecentServiceNumber();
	    loadClientData();
	    jdb.clearStatus();
	}
}
