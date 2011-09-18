package ca.patricklam.judodb.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.FormElement;
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
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.Widget;

public class ClientWidget extends Composite {
	interface MyUiBinder extends UiBinder<Widget, ClientWidget> {}
	public static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

	private final JudoDB jdb;

	@UiField DivElement cid;
	@UiField HTMLPanel clientMain;

	@UiField TextBox nom;
	@UiField TextBox prenom;
	@UiField TextBox ddn;
	@UiField TextBox sexe;

	@UiField Anchor copysib;
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
	
	@UiField Hidden grades_encoded;
	@UiField Hidden grade_dates_encoded;
	
	@UiField Hidden date_inscription_encoded;
	@UiField Hidden saisons_encoded;
	@UiField Hidden verification_encoded;

	@UiField Hidden categorieFrais_encoded;

	@UiField Hidden cours_encoded;
	@UiField Hidden sessions_encoded;

	@UiField Hidden escompte_encoded;
	@UiField Hidden cas_special_note_encoded;
	@UiField Hidden cas_special_pct_encoded;
	@UiField Hidden escompteFrais_encoded;

	@UiField Hidden sans_affiliation_encoded;
	@UiField Hidden affiliationFrais_encoded;

	@UiField Hidden judogi_encoded;
	@UiField Hidden passeport_encoded;
	@UiField Hidden non_anjou_encoded;
	@UiField Hidden suppFrais_encoded;

	@UiField Hidden solde_encoded;
	@UiField Hidden frais_encoded;

	@UiField Hidden guid_on_form;
	@UiField Hidden sid;
	@UiField Hidden deleted;
	
	@UiField Button saveClientButton;
	@UiField Button saveAndReturnClientButton;
	@UiField Button deleteClientButton;
	@UiField Button discardClientButton;

	@UiField HTMLPanel blurb;
	@UiField HTMLPanel gradeHistory;
	@UiField Grid gradeTable;
	@UiField Anchor saveGrades;
	@UiField Anchor annulerGrades;

	private final FormElement clientform;
	
	private static final String PULL_ONE_CLIENT_URL = JudoDB.BASE_URL + "pull_one_client.php?id=";
	private static final String CALLBACK_URL_SUFFIX = "&callback=";
	private static final String PUSH_ONE_CLIENT_URL = JudoDB.BASE_URL + "push_one_client.php";
	private static final String CONFIRM_PUSH_URL = JudoDB.BASE_URL + "confirm_push.php?guid=";
	private int pushTries;
	
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
	private String guid;
	private int currentServiceNumber;
	public int getCurrentServiceNumber() { return currentServiceNumber; }
	
	private String sibid;
	private List<ChangeHandler> onPopulated = new ArrayList<ChangeHandler>();
		
	public ClientWidget(int cid, JudoDB jdb) {
		this.jdb = jdb;
		initWidget(uiBinder.createAndBindUi(this));
		clientform = FormElement.as(clientMain.getElementById("clientform"));
		clientform.setAction(PUSH_ONE_CLIENT_URL);
		jdb.pleaseWait();
		deleted.setValue("");
		
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

		adresse.addChangeHandler(updateCopySibHandler);
		ville.addChangeHandler(updateCopySibHandler);
		codePostal.addChangeHandler(updateCopySibHandler);
		tel.addChangeHandler(updateCopySibHandler);
		tel_contact_urgence.addChangeHandler(updateCopySibHandler);
		courriel.addChangeHandler(updateCopySibHandler);
		
		ddn.addChangeHandler(recomputeHandler);
		grade.addChangeHandler(directGradeChangeHandler);
		date_grade.addChangeHandler(directGradeDateChangeHandler);
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
		deleteClientButton.addClickHandler(new ClickHandler() { 
			public void onClick(ClickEvent e) {
				pushDeleteToServer();
				ClientWidget.this.jdb.popMode(); 
			}
		});
		
		copysib.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent e) {
				copysib();
			}
		});

		if (cid != -1)
			getJsonForPull(jdb.jsonRequestId++, PULL_ONE_CLIENT_URL + cid + CALLBACK_URL_SUFFIX, this);
		else {
			this.cd = JavaScriptObject.createObject().cast();
			this.cd.setID(null); this.cd.setNom(""); 
			this.cd.setVille("Anjou (QC)"); this.cd.setCodePostal("H1K ");
			this.cd.setTel("514-");
			
			JsArray<ServiceData> sa = JavaScriptObject.createArray().cast();
			ServiceData sd = ServiceData.newServiceData();
			sd.inscrireAujourdhui();
			sa.set(0, sd);
			this.cd.setServices(sa);
			// new client: first service, 
			//   which exists because we called inscrireAujourdhui
			currentServiceNumber = 0; 
			
			JsArray<GradeData> ga = JavaScriptObject.createArray().cast();
			GradeData gd = JavaScriptObject.createObject().cast();
			gd.setGrade("Blanche"); gd.setDateGrade(DateTimeFormat.getFormat("yyyy-MM-dd").format(new Date()));
			ga.set(0, gd);
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
		
		int by = ddn.getYear(), bm = ddn.getMonth(), bd = ddn.getDate(), 
			ny = today.getYear(), nm = today.getMonth(), nd = ddn.getDate();
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
		boolean hasToday = false, isToday = false;
		boolean hasThisSession = cd.getServiceFor(Constants.currentSession()) != null; 
		String todayString = DateTimeFormat.getFormat("yyyy-MM-dd").format(new Date()); 
		
		for (int i = 0; i < cd.getServices().length(); i++) {
			ServiceData ssd = cd.getServices().get(i);
			if (todayString.equals(ssd.getDateInscription())) {
				hasToday = true;
				isToday = (i == currentServiceNumber);
			}
			date_inscription.addItem(ssd.getDateInscription(), Integer.toString(i));
		}
		date_inscription.setSelectedIndex(currentServiceNumber);
		inscrire.setVisible(!hasToday && !hasThisSession);
		modifier.setVisible(!hasToday && hasThisSession);
		desinscrire.setVisible(cd.getServices().length() > 0);
		
		// categories is set in recompute().
		saisons.setText(sd.getSaisons());
		verification.setValue(sd.getVerification());
		int cnum = Integer.parseInt(sd.getCours());
		if (cnum >= 0 && cnum < cours.getItemCount())
			cours.setItemSelected(cnum, true);
		sessions.setItemSelected(sd.getSessionCount()-1, true);
		sessions.setEnabled(isToday);
		categorieFrais.setText(sd.getCategorieFrais());
		
		sans_affiliation.setValue(sd.getSansAffiliation());
		sans_affiliation.setEnabled(isToday);
		affiliationFrais.setText(sd.getAffiliationFrais());

		escompte.setSelectedIndex(sd.getEscompteType());
		escompte.setEnabled(isToday);
		cas_special_note.setText(sd.getCasSpecialNote());
		cas_special_note.setReadOnly(!isToday);
		cas_special_pct.setValue(sd.getCasSpecialPct());
		cas_special_pct.setReadOnly(!isToday);
		escompteFrais.setText(sd.getEscompteFrais());

		judogi.setText(sd.getJudogi());
		judogi.setReadOnly(!isToday);
		passeport.setValue(sd.getPasseport());
		passeport.setEnabled(isToday);
		non_anjou.setValue(sd.getNonAnjou());
		non_anjou.setEnabled(isToday);
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
		cd.setCarteAnjou(carte_anjou.getText());
		cd.setNomRecuImpot(nom_recu_impot.getText());

		cd.setTelContactUrgence(tel_contact_urgence.getText());
		
		if (currentServiceNumber == -1) return;
		
		ServiceData sd = cd.getServices().get(currentServiceNumber);
		sd.setDateInscription(removeCommas(date_inscription.getItemText(currentServiceNumber)));
		sd.setSaisons(removeCommas(saisons.getText()));
		sd.setVerification(verification.getValue());
		sd.setCours(Integer.toString(cours.getSelectedIndex()));
		sd.setSessionCount(sessions.getSelectedIndex()+1);
		sd.setCategorieFrais(stripDollars(categorieFrais.getText()));
		
		sd.setSansAffiliation(sans_affiliation.getValue());
		sd.setAffiliationFrais(stripDollars(affiliationFrais.getText()));
		
		sd.setEscompteType(escompte.getSelectedIndex());
		sd.setCasSpecialNote(removeCommas(cas_special_note.getText()));
		sd.setCasSpecialPct(stripDollars(cas_special_pct.getText()));
		sd.setEscompteFrais(stripDollars(escompteFrais.getText()));
		
		sd.setJudogi(stripDollars(judogi.getText()));
		sd.setPasseport(passeport.getValue());
		sd.setNonAnjou(non_anjou.getValue());
		sd.setSuppFrais(stripDollars(suppFrais.getText()));
		
		sd.setFrais(stripDollars(frais.getText()));
		sd.setSolde(solde.getValue());
	}
	
	/** Load grades data from ClientData into the gradeTable & grade/date_grade. */
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
		
		Arrays.sort(grades, new GradeData.GradeComparator());
		
		for (int i = 0; i < grades.length; i++) {
			setGradesTableRow(i+1, grades[i].getGrade(), grades[i].getDateGrade());
		}
		setGradesTableRow(grades.length+1, "", "");

		grade.setText(cd.getGrade());
		date_grade.setText(cd.getDateGrade());
	}

	private void setGradesTableRow(int row, String grade, String dateGrade) {
		TextBox g = new TextBox(); 
		TextBox gd = new TextBox(); 

		g.setValue(grade); g.setVisibleLength(5);
		gd.setValue(dateGrade); gd.setVisibleLength(10);
		gradeTable.setWidget(row, 0, g);
		gradeTable.setWidget(row, 1, gd);
		((TextBox)gradeTable.getWidget(row, 0)).addChangeHandler(ensureGradeSpace);
		((TextBox)gradeTable.getWidget(row, 1)).addChangeHandler(ensureGradeSpace);		
	}

	private TextBox getGradeTableTextBox(int row, int col) {
		return (TextBox) gradeTable.getWidget(row, col);
	}
	
	/** If there are any empty rows in the middle, move them up.
	 * If there is no empty row at the end, create one.
	 */
	private ChangeHandler ensureGradeSpace = new ChangeHandler() {
		public void onChange(ChangeEvent e) {
			int rc = gradeTable.getRowCount();
			for (int i = 1; i < rc-2; i++) {
				if (getGradeTableTextBox(i, 0).getText().equals("") && 
						getGradeTableTextBox(i, 1).getText().equals("")) {
					for (int j = i; j < rc-1; j++) {
						getGradeTableTextBox(j, 0).setText(getGradeTableTextBox(j+1, 0).getText());
						getGradeTableTextBox(j, 1).setText(getGradeTableTextBox(j+1, 1).getText());
					}
					getGradeTableTextBox(rc-1, 0).setText("");
					getGradeTableTextBox(rc-1, 1).setText("");
				}
			}
			
			if (!getGradeTableTextBox(rc-1, 0).getText().equals("") && 
					!getGradeTableTextBox(rc-1, 1).getText().equals("")) {
				gradeTable.resize(rc+1, 2);
				setGradesTableRow(rc, "", "");
			}			
		}
	};
	
	/** Save data from the grades table into the ClientData. */
	private void saveGradesData() {
		JsArray<GradeData> newGradesJS = JavaScriptObject.createArray().cast();
		ArrayList<GradeData> newGradesList = new ArrayList<GradeData>();
		
		for (int i = 1; i < gradeTable.getRowCount(); i++) {
			String g = getGradeTableTextBox(i, 0).getText(), 
				gdate = getGradeTableTextBox(i, 1).getText();
			if (!g.equals("")) {
				GradeData gd = GradeData.createObject().cast();
				gd.setGrade(g.replaceAll(",", ";")); 
				gd.setDateGrade(gdate.replaceAll(",", ";"));
				newGradesList.add(gd);
			}
		}
		Collections.sort(newGradesList, new GradeData.GradeComparator());
		
		int gi = 0;
		for (GradeData gd : newGradesList) {
			newGradesJS.set(gi++, gd);
		}
		cd.setGrades(newGradesJS);
	}
	
	private String encodeGrades() {
		StringBuffer sb = new StringBuffer();

		JsArray<GradeData> grades = cd.getGrades();
		for (int i = 0; i < grades.length(); i++) {
			GradeData gd = grades.get(i);
			if (i > 0) sb.append(",");
			sb.append(gd.getGrade());
		}
		return sb.toString();
	}

	private String encodeGradeDates() {
		StringBuffer sb = new StringBuffer();

		JsArray<GradeData> grades = cd.getGrades();
		for (int i = 0; i < grades.length(); i++) {
			GradeData gd = grades.get(i);
			if (i > 0) sb.append(",");
			sb.append(gd.getDateGrade());
		}
		return sb.toString();
	}

	private final ChangeHandler directGradeChangeHandler = new ChangeHandler() {
		public void onChange(ChangeEvent e) { 
			// either no previous grade or no previous date-grade;
			// erase the old grade
			if (date_grade.getText().equals("0000-00-00") || cd.getGrade().equals("")) {
				date_grade.setText(DateTimeFormat.getFormat("yyyy-MM-dd").format(new Date()));
				setGradesTableRow(1, grade.getText(), date_grade.getText());
				saveGradesData();
			} else {
				// old grade set, and has date;  keep the old grade-date in the array
				// and update the array.
				if (!cd.getGrade().equals("")) {
					ensureGradeSpace.onChange(null);
					date_grade.setText(DateTimeFormat.getFormat("yyyy-MM-dd").format(new Date()));
					setGradesTableRow(0, 
							grade.getText(), date_grade.getText());
					saveGradesData();
				}
			}
		}
	};

	private final ChangeHandler directGradeDateChangeHandler = new ChangeHandler() {
		public void onChange(ChangeEvent e) { 
			// if you change the grade-date, and grade is not empty, then
			// update the array.
		}
	};

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
	private final ChangeHandler updateCopySibHandler = new ChangeHandler() {
		public void onChange(ChangeEvent e) { updateCopySib(); }
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
	
	private int emptyGradeDates() {
		int empty = 0;
		for (int i = 1; i < gradeTable.getRowCount(); i++) {
			String gv = getGradeTableTextBox(i, 0).getText();
			String gdv = getGradeTableTextBox(i, 1).getText();
			if (gdv.equals("0000-00-00") || (!gv.equals("") && gv.equals("")))
				empty++;
		}
		return empty;
	}
	
	private final ClickHandler saveGradesHandler = new ClickHandler() {
		public void onClick(ClickEvent e) {
			if (emptyGradeDates() > 1) {
				jdb.setStatus("Seulement une grade sans date est permise.");
				new Timer() { public void run() { jdb.clearStatus(); } }.schedule(2000);
				return;
			}
			saveGradesData();
			loadGradesData();
			gradeHistory.setVisible(false);			
		}
	};

	private final ClickHandler annulerGradesHandler = new ClickHandler() {
		public void onClick(ClickEvent e) {
			loadGradesData();
			gradeHistory.setVisible(false);
		}
	};
	
	/** We use , as a separator, so get rid of it in the input. */
	private String removeCommas(String s) {
		return s.replaceAll(",", ";");
	}
	
	// argh NumberFormat doesn't work for me at all!
	// stupidly, it says that you have to use ',' as the decimal separator, but people use both '.' and ','.
	private String stripDollars(String s) {
		StringBuffer ss = new StringBuffer("");
		for (int i = 0; i < s.length(); i++) {
			if (s.charAt(i) == '$' || s.charAt(i) == ' ' || s.charAt(i) == ')' || s.charAt(i) == '\u00a0') 
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
		return d1.getYear() == d2.getYear() && d1.getMonth() == d2.getMonth() && d1.getDate() == d2.getDate();
	}
	
	private void updateFrais() {
		ServiceData sd = cd.getServices().get(currentServiceNumber);
		Date dateInscription = DateTimeFormat.getFormat("yyyy-MM-dd").parse(sd.getDateInscription());
		escompteFrais.setReadOnly(true);
		
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
	
	private void updateCopySib() {
		copysib.setVisible(false);
		// check 1) address fields are empty and 2) there exists a sibling
		StringBuffer addrFields = new StringBuffer();
		addrFields.append(adresse.getText());
		addrFields.append(ville.getText());
		addrFields.append(codePostal.getText());
		addrFields.append(tel.getText());
		addrFields.append(tel_contact_urgence.getText());
		addrFields.append(courriel.getText());
		if (!addrFields.toString().equals(""))
			return;
		
		// oh well, tough luck!
		if (jdb.allClients == null) return;
		
		for (int i = 0; i < jdb.allClients.length(); i++) {
			ClientSummary cs = jdb.allClients.get(i);
			if (cs.getId().equals(cd.getID()))
				continue;
			
			String csn = cs.getNom().toLowerCase();
			String n = nom.getText().toLowerCase();
			if (n.equals(csn)) {
				sibid = cs.getId();
				copysib.setVisible(true);
			}
		}
	}
	
	private void copysib() {
		final ClientWidget cp = new ClientWidget(Integer.parseInt(sibid), jdb);
		cp.onPopulated.add (new ChangeHandler () {
			public void onChange(ChangeEvent e) {
				cp.actuallyCopy(ClientWidget.this);				
			}
		});
	}
	
	private void actuallyCopy(ClientWidget d) {
		d.adresse.setText(adresse.getText());
		d.ville.setText(ville.getText());
		d.codePostal.setText(codePostal.getText());
		d.tel.setText(tel.getText());
		d.tel_contact_urgence.setText(tel_contact_urgence.getText());
		d.courriel.setText(courriel.getText());
		d.updateCopySib();
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
		updateCopySib();
	}

	private void encodeServices() {
		StringBuffer di = new StringBuffer(), sais = new StringBuffer(), 
			v = new StringBuffer(), cf = new StringBuffer(), c = new StringBuffer(), 
			sess = new StringBuffer(), e = new StringBuffer(), csn = new StringBuffer(), 
			csp = new StringBuffer(), ef = new StringBuffer(), sa = new StringBuffer(), 
			af = new StringBuffer(), j = new StringBuffer(), p = new StringBuffer(), 
			n = new StringBuffer(), sf = new StringBuffer(), s = new StringBuffer(), 
			f = new StringBuffer();
		
		JsArray<ServiceData> services = cd.getServices();
		for (int i = 0; i < services.length(); i++) {
			ServiceData sd = services.get(i);
			di.append(sd.getDateInscription()+",");
			sais.append(sd.getSaisons()+",");
			v.append(sd.getVerification() ? "1," : "0,");
			cf.append(sd.getCategorieFrais()+",");
			c.append(sd.getCours()+",");
			sess.append(Integer.toString(sd.getSessionCount())+",");
			e.append(Integer.toString(sd.getEscompteType())+",");
			csn.append(sd.getCasSpecialNote()+",");
			csp.append(sd.getCasSpecialPct()+",");
			ef.append(sd.getEscompteFrais()+",");
			sa.append(sd.getSansAffiliation() ? "1," : "0,");
			af.append(sd.getAffiliationFrais()+",");
			j.append(sd.getJudogi()+",");
			p.append(sd.getPasseport()+",");
			n.append(sd.getNonAnjou()+",");
			sf.append(sd.getSuppFrais()+",");
			s.append(sd.getSolde() ? "1,":"0,");
			f.append(sd.getFrais()+",");
		}
		
		date_inscription_encoded.setValue(di.toString());
		saisons_encoded.setValue(sais.toString());
		verification_encoded.setValue(v.toString());
		categorieFrais_encoded.setValue(cf.toString());
		cours_encoded.setValue(c.toString());
		sessions_encoded.setValue(sess.toString());
		escompte_encoded.setValue(e.toString());
		cas_special_note_encoded.setValue(csn.toString());
		cas_special_pct_encoded.setValue(csp.toString());
		escompteFrais_encoded.setValue(ef.toString());
		sans_affiliation_encoded.setValue(sa.toString());
		affiliationFrais_encoded.setValue(af.toString());
		judogi_encoded.setValue(j.toString());
		passeport_encoded.setValue(p.toString());
		non_anjou_encoded.setValue(n.toString());
		suppFrais_encoded.setValue(sf.toString());
		solde_encoded.setValue(s.toString());
		frais_encoded.setValue(f.toString());
	}
	
	private void pushClientDataToServer() {
		guid = UUID.uuid();
		sid.setValue(cd.getID());
		guid_on_form.setValue(guid);
		
		grades_encoded.setValue(encodeGrades());
		grade_dates_encoded.setValue(encodeGradeDates());
		saveClientData(); loadClientData();
		encodeServices();

		// http://stackoverflow.com/questions/2699277/post-data-to-jsonp	
		clientform.submit();

		pushTries = 0;
		new Timer() { public void run() {
			getJsonForStageTwoPush(jdb.jsonRequestId++, CONFIRM_PUSH_URL + guid + CALLBACK_URL_SUFFIX, ClientWidget.this);
		} }.schedule(500);
	}
	
	private void pushDeleteToServer() {
		// no need to delete if there's no ID yet.
		if (cd.getID().equals("")) return;

		guid = UUID.uuid();
		sid.setValue(cd.getID());
		
		guid_on_form.setValue(guid);
		deleted.setValue("true");
		clientform.submit();

		pushTries = 0;
		new Timer() { public void run() {
			getJsonForStageTwoPush(jdb.jsonRequestId++, CONFIRM_PUSH_URL + guid + CALLBACK_URL_SUFFIX, ClientWidget.this);
		} }.schedule(500);
	}
	
	/**
	 * Make call to remote server to request client information.
	 */
	public native static void getJsonForPull(int requestId, String url,
	      ClientWidget handler) /*-{
	   var callback = "callback" + requestId;

	   var script = document.createElement("script");
	   script.setAttribute("src", url+callback);
	   script.setAttribute("type", "text/javascript");
	   window[callback] = function(jsonObj) {
	     handler.@ca.patricklam.judodb.client.ClientWidget::handleJsonPullResponse(Lcom/google/gwt/core/client/JavaScriptObject;)(jsonObj);
	     window[callback + "done"] = true;
	   }

	   setTimeout(function() {
	     if (!window[callback + "done"]) {
	       handler.@ca.patricklam.judodb.client.ClientWidget::handleJsonPullResponse(Lcom/google/gwt/core/client/JavaScriptObject;)(null);
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
	public void handleJsonPullResponse(JavaScriptObject jso) {
		if (jso == null) {
			jdb.displayError("Couldn't retrieve JSON");
			return;
		}	

	    this.cd = jso.cast();
	    currentServiceNumber = cd.getMostRecentServiceNumber();
	    loadClientData();
	    jdb.clearStatus();
	    
	    for (ChangeHandler ch : onPopulated) {
	    	ch.onChange(null);
	    }
	}
	
	/**
	 * Make call to remote server to request client information.
	 */
	public native static void getJsonForStageTwoPush(int requestId, String url,
	      ClientWidget handler) /*-{
	   var callback = "callback" + requestId;

	   var script = document.createElement("script");
	   script.setAttribute("src", url+callback);
	   script.setAttribute("type", "text/javascript");
	   window[callback] = function(jsonObj) {
	     handler.@ca.patricklam.judodb.client.ClientWidget::handleJsonStageTwoPushResponse(Lcom/google/gwt/core/client/JavaScriptObject;)(jsonObj);
	     window[callback + "done"] = true;
	   }

	   setTimeout(function() {
	     if (!window[callback + "done"]) {
	       handler.@ca.patricklam.judodb.client.ClientWidget::handleJsonStageTwoPushResponse(Lcom/google/gwt/core/client/JavaScriptObject;)(null);
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
	public void handleJsonStageTwoPushResponse(JavaScriptObject jso) {
		if (jso == null) {
			if (pushTries == 3) {
				jdb.displayError("pas de réponse");
				new Timer() { public void run() { jdb.clearStatus(); } }.schedule(2000);
				return;
			} else {
				tryConfirmPushAgain();
			}
		}	

	    ConfirmResponseObject cro = jso.cast();
	    
	    if (cro.getResult().equals("NOTYET")) {
	    	tryConfirmPushAgain(); 
	    	return;
	    }

	    if (!cro.getResult().equals("OK")) {
	    	jdb.displayError("le serveur n'a pas accepté les données");
			new Timer() { public void run() { jdb.clearStatus(); } }.schedule(2000);
	    }
	    else {
	    	jdb.setStatus("Sauvegardé.");
	    	jdb.invalidateListWidget();
			new Timer() { public void run() { jdb.clearStatus(); } }.schedule(2000);
		    if (cd.getID() == null || cd.getID().equals("")) {
		    	cd.setID(Integer.toString(cro.getSid()));
		    	loadClientData();
		    }		    
	    }
	}
	
	private void tryConfirmPushAgain() {
		pushTries++;
		new Timer() { public void run() {
			ClientWidget.getJsonForStageTwoPush
				(jdb.jsonRequestId++, CONFIRM_PUSH_URL + guid + CALLBACK_URL_SUFFIX, ClientWidget.this);
		}}.schedule(1000);	
	}
}
