package ca.patricklam.judodb.client;

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
	@UiField TextBox date_grade;
	@UiField TextBox carte_anjou;
	@UiField TextBox nom_recu_impot;

	@UiField TextBox tel_contact_urgence;

	@UiField TextBox date_inscription;
	@UiField Anchor today;
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

	@UiField TextBox frais;
	
	@UiField Button saveClientButton;
	@UiField Button saveAndReturnClientButton;
	@UiField Button discardClientButton;

	@UiField HTMLPanel blurb;
	
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
		
		date_inscription.setReadOnly(true);
		categorie.setReadOnly(true);
		saisons.setReadOnly(true);
		categorieFrais.setReadOnly(true); categorieFrais.setAlignment(ValueBoxBase.TextAlignment.RIGHT);
		escompteFrais.setReadOnly(true); escompteFrais.setAlignment(ValueBoxBase.TextAlignment.RIGHT);
		judogi.setAlignment(ValueBoxBase.TextAlignment.RIGHT);
		affiliationFrais.setReadOnly(true); affiliationFrais.setAlignment(ValueBoxBase.TextAlignment.RIGHT);
		suppFrais.setReadOnly(true); suppFrais.setAlignment(ValueBoxBase.TextAlignment.RIGHT);
		frais.setReadOnly(true); frais.setAlignment(ValueBoxBase.TextAlignment.RIGHT);
		((Element)cas_special_note.getElement().getParentNode()).getStyle().setDisplay(Display.NONE);
		((Element)cas_special_pct.getElement().getParentNode()).getStyle().setDisplay(Display.NONE);

		sessions.setItemSelected(1, true);
		
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
			JsArray<ServiceData> sa = asServiceArray(JavaScriptObject.createArray());
			ServiceData sd = ServiceData.newServiceData();
			sd.inscrireAujourdhui();
			sa.set(0, sd);
			JsArray<GradeData> ga = asGradeArray(JavaScriptObject.createArray());
			this.cd.setServices(sa);
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
		
		ServiceData mrs = cd.getMostRecentService();
		if (mrs == null) mrs = ServiceData.newServiceData();
		date_inscription.setText(mrs.getDateInscription());
		// categories is set in recompute().
		saisons.setText(mrs.getSaisons());
		verification.setValue(mrs.getVerification());
		cours.setItemSelected(Integer.parseInt(mrs.getCours()), true);
		sessions.setItemSelected(mrs.getSessions()-1, true);
		categorieFrais.setText(mrs.getCategorieFrais());
		
		sans_affiliation.setValue(mrs.getSansAffiliation());
		affiliationFrais.setText(mrs.getAffiliationFrais());

		escompte.setSelectedIndex(mrs.getEscompteType());
		cas_special_note.setText(mrs.getCasSpecialNote());
		cas_special_pct.setText("-1"); // leave cas_special_pct for the calculation phase
		if (escompte.getValue(escompte.getSelectedIndex()).equals("-1"))
			escompteFrais.setText(mrs.getEscompteSpecial());
		
		judogi.setText(mrs.getJudogi());
		passeport.setValue(mrs.getPasseport());
		non_anjou.setValue(mrs.getNonAnjou());
		suppFrais.setText(mrs.getSuppFrais());	
		
		frais.setText(mrs.getFrais());

		today.addClickHandler(aujourdhuiClickHandler);
		ddn.addChangeHandler(aujourdhuiHandler);
		sessions.addChangeHandler(aujourdhuiHandler);
		escompte.addChangeHandler(aujourdhuiHandler);
		cas_special_pct.addChangeHandler(aujourdhuiHandler);
		escompteFrais.addChangeHandler(aujourdhuiHandler);
		sans_affiliation.addValueChangeHandler(aujourdhuiValueHandler);
		judogi.addChangeHandler(aujourdhuiHandler);
		passeport.addValueChangeHandler(aujourdhuiValueHandler);
		non_anjou.addValueChangeHandler(aujourdhuiValueHandler);
		
		ddn.addChangeHandler(recomputeHandler);
		grade.addChangeHandler(recomputeHandler);
		date_inscription.addChangeHandler(recomputeHandler);
		sessions.addChangeHandler(recomputeHandler);
		escompte.addChangeHandler(recomputeHandler);
		cas_special_pct.addChangeHandler(recomputeHandler);
		escompteFrais.addChangeHandler(recomputeCasSpecialAmtHandler);
		sans_affiliation.addValueChangeHandler(recomputeValueHandler);
		judogi.addChangeHandler(recomputeHandler);
		passeport.addValueChangeHandler(recomputeValueHandler);
		non_anjou.addValueChangeHandler(recomputeValueHandler);

		// TODO add handlers for setting date d'inscription
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
		
		// TODO: preserve previous season's inscriptions...
		ServiceData sd = cd.getMostRecentService();
		sd.setDateInscription(date_inscription.getText());
		sd.setSaisons(saisons.getText());
		sd.setVerification(verification.getValue());
		sd.setCours(Integer.toString(cours.getSelectedIndex()));
		sd.setSessions(sessions.getSelectedIndex()+1);
		sd.setCategorieFrais(stripDollars(categorieFrais.getText()));
		
		sd.setSansAffiliation(sans_affiliation.getValue());
		sd.setAffiliationFrais(stripDollars(affiliationFrais.getText()));
		
		sd.setEscompteType(escompte.getSelectedIndex());
		sd.setCasSpecialNote(cas_special_note.getText());
		
		sd.setJudogi(judogi.getText());
		sd.setPasseport(passeport.getValue());
		sd.setNonAnjou(non_anjou.getValue());
		sd.setSuppFrais(suppFrais.getText());
		
		sd.setFrais(frais.getText());
	}
	
	private final ChangeHandler recomputeHandler = new ChangeHandler() {
		public void onChange(ChangeEvent e) { recompute(); }
	};
	private final ValueChangeHandler<Boolean> recomputeValueHandler = new ValueChangeHandler<Boolean>() {
		public void onValueChange(ValueChangeEvent<Boolean> e) { recompute(); }
	};

	private final ChangeHandler recomputeCasSpecialAmtHandler = new ChangeHandler() {
		public void onChange(ChangeEvent e) { 
			// TODO recompute pct of escompte
			recompute(); 
		}
	};

	private void aujourdhui() { saveClientData(); cd.getMostRecentService().inscrireAujourdhui(); loadClientData(); recompute(); }
	private final ChangeHandler aujourdhuiHandler = new ChangeHandler() {
		public void onChange(ChangeEvent e) { aujourdhui(); }
	};
	private final ClickHandler aujourdhuiClickHandler = new ClickHandler() {
		public void onClick(ClickEvent e) { aujourdhui(); }
	};
	private final ValueChangeHandler<Boolean> aujourdhuiValueHandler = new ValueChangeHandler<Boolean>() {
		public void onValueChange(ValueChangeEvent<Boolean> e) { aujourdhui(); }
	};
	
	// argh NumberFormat doesn't work for me at all!
	// stupidly, it says that you have to use ',' as the decimal separator, but people use both '.' and ','.
	private String stripDollars(String s) {
		StringBuffer ss = new StringBuffer("");
		for (int i = 0; i < s.length(); i++)
			if (s.charAt(i) != '$' && s.charAt(i) != ' ')
				ss.append(s.charAt(i));
		return ss.toString();
	}

	private static native float parseFloat(String s) /*-{ return parseFloat(s); }-*/;
	
	private void updateFrais() {
		NumberFormat cf = NumberFormat.getCurrencyFormat("CAD");

		int sessionCount = 1;
		if (sessions.getValue(sessions.getSelectedIndex()).equals("2")) {
			sessionCount = 2;
		}

		saisons.setText(Constants.getCurrentSessionIds(sessionCount));

		Constants.Categorie c = cd.getCategorie();
		
		double dCategorieFrais = Constants.getFraisCours
			(Constants.currentSessionNo(), c, sessionCount);
		
		double dEscompteFrais = 0.0;
		escompteFrais.setReadOnly(true); 
		if (escompte.getValue(escompte.getSelectedIndex()).equals("-1")) {
			dEscompteFrais = parseFloat(escompteFrais.getText());
			GWT.log(Double.toString(dEscompteFrais));
			if (cas_special_pct.getText().equals("-1")) {
				String ef = escompteFrais.getText();
				cas_special_pct.setText(
						NumberFormat.getDecimalFormat().format(100 * parseFloat(ef) / dCategorieFrais));
			}
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
		escompteFrais.setText(cf.format(dEscompteFrais));
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

		// TODO: set the categorie corresponding to the date d'inscription
		Constants.Categorie c = cd.getCategorie();
		categorie.setText(c.abbrev);

		updateBlurb();
		
		// TODO: if (date d'inscription == today)
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
	    loadClientData();
	    jdb.clearStatus();
	}

	private final native JsArray<GradeData> asGradeArray(JavaScriptObject jso) /*-{
    	return jso;
  	}-*/;
	private final native JsArray<ServiceData> asServiceArray(JavaScriptObject jso) /*-{
    	return jso;
  	}-*/;
}
