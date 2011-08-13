package ca.patricklam.judodb.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
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

		categorie.getElement().setId(DOM.createUniqueId());
		categorieFrais.getElement().setId(DOM.createUniqueId());

		cours.getElement().setId(DOM.createUniqueId());
		
		for (Constants.Cours c : Constants.COURS) {
			cours.addItem(c.name, c.seqno);
		}
		
		date_inscription.setReadOnly(true);
		categorie.setReadOnly(true);
		categorieFrais.setReadOnly(true);
		
		saveClientButton.addClickHandler(new ClickHandler() { 
			public void onClick(ClickEvent e) {
				pushClientDataToServer();
				ClientWidget.this.jdb.returnToSearch(); }
		});
		discardClientButton.addClickHandler(new ClickHandler() { 
			public void onClick(ClickEvent e) {
				ClientWidget.this.jdb.returnToSearch(); }
		});

		getJson(jsonRequestId++, PULL_ONE_CLIENT_URL + cid + CALLBACK_URL_SUFFIX, this);
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
		
		ddn.addChangeHandler(recomputeHandler);
		grade.addChangeHandler(recomputeHandler);
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
	}

	private final ChangeHandler recomputeHandler = new ChangeHandler() {
		public void onChange(ChangeEvent e) { recompute(); }
	};
	
	private void recompute() {
		saveClientData();		
		categorie.setText(cd.getCategorieAbbrev());
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
	 * Handle the response to the request for stock data from a remote server.
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
