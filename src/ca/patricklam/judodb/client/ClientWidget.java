package ca.patricklam.judodb.client;

import java.text.ParseException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.text.client.DoubleParser;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.Widget;

public class ClientWidget extends Composite {
	interface MyUiBinder extends UiBinder<Widget, ClientWidget> {}
	public static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

	private final JudoDB jdb;
	
	@UiField(provided=true) TextBox nom = new TextBox();
	@UiField(provided=true) TextBox prenom = new TextBox();
	@UiField(provided=true) TextBox ddn = new TextBox();
	@UiField(provided=true) TextBox sexe = new TextBox();

	@UiField(provided=true) TextBox adresse = new TextBox();
	@UiField(provided=true) TextBox ville = new TextBox();
	@UiField(provided=true) TextBox codePostal = new TextBox();
	@UiField(provided=true) TextBox tel = new TextBox();
	@UiField(provided=true) TextBox courriel = new TextBox();
	
	@UiField(provided=true) TextBox affiliation = new TextBox();
	@UiField(provided=true) TextBox grade = new TextBox();
	@UiField(provided=true) TextBox date_grade = new TextBox();
	@UiField(provided=true) TextBox carte_anjou = new TextBox();
	@UiField(provided=true) TextBox nom_recu_impot = new TextBox();

	@UiField(provided=true) TextBox nom_contact_urgence = new TextBox();
	@UiField(provided=true) TextBox tel_contact_urgence = new TextBox();

	@UiField(provided=true) TextBox date_inscription = new TextBox();
	@UiField(provided=true) TextBox saisons = new TextBox();
	@UiField(provided=true) CheckBox verification = new CheckBox();

	@UiField(provided=true) TextBox categorie = new TextBox();
	@UiField(provided=true) TextBox categorieFrais = new TextBox();

	@UiField(provided=true) ListBox cours = new ListBox();
	@UiField(provided=true) ListBox sessions = new ListBox();

	@UiField(provided=true) ListBox escompte = new ListBox();
	@UiField(provided=true) TextBox cas_special_note = new TextBox();
	@UiField(provided=true) TextBox cas_special_pct = new TextBox();
	@UiField(provided=true) TextBox escompteFrais = new TextBox();

	@UiField(provided=true) CheckBox sans_affiliation = new CheckBox();
	@UiField(provided=true) TextBox affiliationFrais = new TextBox();

	@UiField(provided=true) TextBox judogi = new TextBox();
	@UiField(provided=true) CheckBox passeport = new CheckBox();
	@UiField(provided=true) CheckBox non_anjou = new CheckBox();
	@UiField(provided=true) TextBox suppFrais = new TextBox();

	@UiField(provided=true) TextBox frais = new TextBox();
	
	@UiField(provided=true) Button saveClientButton = new Button();
	@UiField(provided=true) Button discardClientButton = new Button();
	
	private static final String PULL_ONE_CLIENT_URL = JudoDB.BASE_URL + "pull_one_client.php?id=";
	private static final String CALLBACK_URL_SUFFIX = "&callback=";
	private int jsonRequestId = 0;

	private ClientData cd;
	
	public ClientWidget(JudoDB jdb, int cid) {
		this.jdb = jdb;
		initWidget(uiBinder.createAndBindUi(this));
		
		nom.getElement().setId(DOM.createUniqueId());
		prenom.getElement().setId(DOM.createUniqueId());
		ddn.getElement().setId(DOM.createUniqueId());
		sexe.getElement().setId(DOM.createUniqueId());

		adresse.getElement().setId(DOM.createUniqueId());
		ville.getElement().setId(DOM.createUniqueId());
		codePostal.getElement().setId(DOM.createUniqueId());
		tel.getElement().setId(DOM.createUniqueId());
		courriel.getElement().setId(DOM.createUniqueId());

		affiliation.getElement().setId(DOM.createUniqueId());
		grade.getElement().setId(DOM.createUniqueId());
		date_grade.getElement().setId(DOM.createUniqueId());
		carte_anjou.getElement().setId(DOM.createUniqueId());
		nom_recu_impot.getElement().setId(DOM.createUniqueId());

		nom_contact_urgence.getElement().setId(DOM.createUniqueId());
		tel_contact_urgence.getElement().setId(DOM.createUniqueId());

		date_inscription.getElement().setId(DOM.createUniqueId());
		saisons.getElement().setId(DOM.createUniqueId());
		verification.getElement().setId(DOM.createUniqueId());
		
		categorie.getElement().setId(DOM.createUniqueId());
		categorieFrais.getElement().setId(DOM.createUniqueId());

		cours.getElement().setId(DOM.createUniqueId());
		sessions.getElement().setId(DOM.createUniqueId());
		
		escompte.getElement().setId(DOM.createUniqueId());
		cas_special_note.getElement().setId(DOM.createUniqueId());
		cas_special_pct.getElement().setId(DOM.createUniqueId());
		escompteFrais.getElement().setId(DOM.createUniqueId());

		sans_affiliation.getElement().setId(DOM.createUniqueId());
		affiliationFrais.getElement().setId(DOM.createUniqueId());

		judogi.getElement().setId(DOM.createUniqueId());
		passeport.getElement().setId(DOM.createUniqueId());
		non_anjou.getElement().setId(DOM.createUniqueId());
		suppFrais.getElement().setId(DOM.createUniqueId());
		
		frais.getElement().setId(DOM.createUniqueId());
		
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
		affiliationFrais.setReadOnly(true); affiliationFrais.setAlignment(ValueBoxBase.TextAlignment.RIGHT);
		suppFrais.setReadOnly(true); suppFrais.setAlignment(ValueBoxBase.TextAlignment.RIGHT);
		frais.setReadOnly(true); frais.setAlignment(ValueBoxBase.TextAlignment.RIGHT);
		((Element)cas_special_note.getElement().getParentNode()).getStyle().setDisplay(Display.NONE);
		((Element)cas_special_pct.getElement().getParentNode()).getStyle().setDisplay(Display.NONE);

		sessions.setItemSelected(1, true);
		
		saveClientButton.addClickHandler(new ClickHandler() { 
			public void onClick(ClickEvent e) {
				pushClientDataToServer();
				ClientWidget.this.jdb.returnToSearch(); }
		});
		discardClientButton.addClickHandler(new ClickHandler() { 
			public void onClick(ClickEvent e) {
				ClientWidget.this.jdb.clearError();
				ClientWidget.this.jdb.returnToSearch(); }
		});
		
		jdb.clearError();
		if (cid != -1)
			getJson(jsonRequestId++, PULL_ONE_CLIENT_URL + cid + CALLBACK_URL_SUFFIX, this);
		// TODO else create new data objects!
	}
	
	/** Takes data from ClientData into the form. */
	private void loadClientData () {
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

		nom_contact_urgence.setText(cd.getNomContactUrgence());
		tel_contact_urgence.setText(cd.getTelContactUrgence());
		
		date_inscription.setText(cd.getMostRecentService().getDateInscription());
		// categories: set in recompute().
		saisons.setText(cd.getMostRecentService().getSaisons());
		verification.setValue(cd.getMostRecentService().getVerification());
		cours.setItemSelected(cd.getMostRecentService().getCours(), true);
		sessions.setItemSelected(cd.getMostRecentService().getSessions()-1, true);
		categorieFrais.setText(cd.getMostRecentService().getCategorieFrais());
		
		sans_affiliation.setValue(cd.getMostRecentService().getSansAffiliation());
		affiliationFrais.setText(cd.getMostRecentService().getAffiliationFrais());

		escompte.setSelectedIndex(cd.getMostRecentService().getEscompte());
		cas_special_note.setText(cd.getMostRecentService().getCasSpecialNote());
		// XXX todo cas_special_pct from escompte_special
		
		judogi.setText(cd.getMostRecentService().getJudogi());
		passeport.setValue(cd.getMostRecentService().getPasseport());
		non_anjou.setValue(cd.getMostRecentService().getNonAnjou());
		suppFrais.setText(cd.getMostRecentService().getSuppFrais());	
		
		frais.setText(cd.getMostRecentService().getFrais());
		
		ddn.addChangeHandler(recomputeHandler);
		grade.addChangeHandler(recomputeHandler);
		escompte.addChangeHandler(recomputeHandler);
		// TODO add all handlers
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

		cd.setNomContactUrgence(nom_contact_urgence.getText());
		cd.setTelContactUrgence(tel_contact_urgence.getText());
		// TODO: remaining fields
	}

	private final ChangeHandler recomputeHandler = new ChangeHandler() {
		public void onChange(ChangeEvent e) { recompute(); }
	};
	
	// evil hack: getCurrencyFormat doesn't seem to want to parse stuff.
	private String stripDollars(String s) {
		return s.replaceAll("$", "");
	}

	private void updateFrais() {
		NumberFormat cf = NumberFormat.getCurrencyFormat("CAD");
		Parser<Double> nf = DoubleParser.instance();

		boolean twoSessions = false;
		if (sessions.getValue(sessions.getSelectedIndex()).equals("2")) {
			twoSessions = true;
		}

		String s = Constants.CURRENT_SESSION;
		if (twoSessions)
			s += " " + Constants.NEXT_SESSION;
		saisons.setText(s);

		Constants.Categorie c = cd.getCategorie();
		double dCategorieFrais = 0.0;
		if (twoSessions)
			dCategorieFrais = 
				Constants.getFrais2Session(Constants.CURRENT_SESSION_SEQNO, c);
		else
			dCategorieFrais =
				Constants.getFrais1Session(Constants.CURRENT_SESSION_SEQNO, c);
		double e = Constants.getEscompte(escompte.getValue(escompte.getSelectedIndex())); 
		if (e == -1)
			try {
				e = nf.parse(cas_special_pct.getText());
			} catch (ParseException ex) {}

		double dEscompteFrais = -(dCategorieFrais * e)/100;
		
		double dAffiliationFrais = 0.0;
		if (!sans_affiliation.getValue())
			dAffiliationFrais = Constants.getFraisJudoQC(Constants.CURRENT_SESSION_SEQNO, c);
		
		double dSuppFrais = 0.0;
		try {
			dSuppFrais += nf.parse(stripDollars(judogi.getText()));
		} catch (ParseException ex) {}
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

	    this.cd = asClientData (jso);
	    loadClientData();
	}

	private final native ClientData asClientData(JavaScriptObject jso) /*-{
	    return jso;
	  }-*/;
}
