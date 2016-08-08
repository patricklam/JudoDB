// -*-  indent-tabs-mode:nil; c-basic-offset:4; -*-
package ca.patricklam.judodb.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.ValueBoxBase;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.Column;
import org.gwtbootstrap3.client.ui.Form;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.DropDownMenu;

import org.gwtbootstrap3.extras.toggleswitch.client.ui.ToggleSwitch;
import org.gwtbootstrap3.extras.select.client.ui.MultipleSelect;
import org.gwtbootstrap3.extras.select.client.ui.Option;

public class ClientWidget extends Composite {
    interface MyUiBinder extends UiBinder<Widget, ClientWidget> {}
    public static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    private final JudoDB jdb;

    @UiField DivElement cid;
    @UiField HTMLPanel clientMain;

    @UiField TextBox nom;
    @UiField TextBox prenom;
    @UiField TextBox ddn_display;
    @UiField Hidden ddn;
    @UiField TextBox sexe;

    @UiField Button copysib;
    @UiField TextBox adresse;
    @UiField TextBox ville;
    @UiField TextBox codePostal;
    @UiField TextBox tel;
    @UiField TextBox tel_contact_urgence;
    @UiField TextBox courriel;

    @UiField TextBox affiliation;
    @UiField ListBox grade;
    @UiField Anchor showgrades;
    @UiField TextBox date_grade;
    @UiField TextBox carte_resident;
    @UiField TextBox nom_recu_impot;
    @UiField TextBox notes;

    @UiField ListBox date_inscription;
    @UiField InlineLabel semaines;
    @UiField Anchor inscrire;
    @UiField Anchor modifier;
    @UiField Anchor desinscrire;
    @UiField ListBox sessions;
    @UiField HTMLPanel prorata_group;
    @UiField ToggleSwitch prorata;
    @UiField ToggleSwitch affiliation_envoye;

    @UiField TextBox categorie;
    @UiField TextBox categorieFrais;

    @UiField ListBox cours;

    @UiField ListBox escompte;
    @UiField TextBox cas_special_note;
    @UiField TextBox cas_special_pct;
    @UiField TextBox escompteFrais;

    @UiField TextBox date_affiliation_envoye;
    @UiField ToggleSwitch carte_judoca_recu;
    @UiField ListBox affiliation_speciale;
    @UiField TextBox affiliationFrais;

    @UiField MultipleSelect produit;
    @UiField Column rabais_resident_label;
    @UiField ToggleSwitch resident;
    @UiField Column frais_paypal_label;
    @UiField ToggleSwitch paypal;
    @UiField TextBox suppFrais;

    @UiField ToggleSwitch solde;
    @UiField TextBox frais;

    @UiField Hidden grades_encoded;
    @UiField Hidden grade_dates_encoded;

    @UiField Hidden date_inscription_encoded;
    @UiField Hidden saisons_encoded;
    @UiField Hidden prorata_encoded;
    @UiField Hidden affiliation_envoye_encoded;

    @UiField Hidden categorieFrais_encoded;

    @UiField Hidden cours_encoded;

    @UiField Hidden escompte_encoded;
    @UiField Hidden cas_special_note_encoded;
    @UiField Hidden cas_special_pct_encoded;
    @UiField Hidden escompteFrais_encoded;

    @UiField Hidden date_affiliation_envoye_encoded;
    @UiField Hidden carte_judoca_recu_encoded;
    @UiField Hidden sans_affiliation_encoded;
    @UiField Hidden affiliation_initiation_encoded;
    @UiField Hidden affiliation_ecole_encoded;
    @UiField Hidden affiliation_parascolaire_encoded;
    @UiField Hidden affiliationFrais_encoded;

    @UiField Hidden judogi_encoded;
    @UiField Hidden resident_encoded;
    @UiField Hidden paypal_encoded;
    @UiField Hidden suppFrais_encoded;

    @UiField Hidden solde_encoded;
    @UiField Hidden frais_encoded;
    @UiField Hidden club_id_encoded;

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

    @UiField ButtonGroup dropDownUserClubsButtonGroup;
    @UiField Button dropDownUserClubsButton;
    @UiField DropDownMenu dropDownUserClubs;

    @UiField Form clientform;

    private static final int AF_NONE = 0;
    private static final int AF_INITIATION = 1;
    private static final int AF_SCOLAIRE = 2;
    private static final int AF_PARASCOLAIRE = 3;
    private static final int AF_DEJA_AFFILIE = 4;

    private static final String PULL_ONE_CLIENT_URL = JudoDB.BASE_URL + "pull_one_client.php";
    private static final String PUSH_ONE_CLIENT_URL = JudoDB.BASE_URL + "push_one_client.php";
    private int pushTries;

    public interface BlurbTemplate extends SafeHtmlTemplates {
        @Template
          ("<p>Je {0} certifie que les informations inscrites sur ce formulaire sont véridiques. "+
           "En adhèrant au {3}, j'accepte tous les risques d'accident liés à la pratique du "+
           "judo qui pourraient survenir dans les locaux ou lors d'activités extérieurs organisées par le Club. "+
           "J'accepte de respecter les règlements du Club en tout temps y compris lors des déplacements.</p>"+
           "<h4>Politique de remboursement</h4>"+
           "<p>Aucun remboursement ne sera accordé sans présentation d'un certificat médical du participant. "+
           "Seuls les frais de cours correspondant à la période restante sont remboursables. "+
           "En cas d'annulation par le participant, un crédit pourra être émis par le Club, "+
           "pour les frais de cours correspondant à la période restante.</p>"+
           "<p>Signature {1}: <span style='float:right'>Date: _________________</span></p>"+
           "<p>Signature résponsable du club:  <span style='float:right'>Date: {2}</span></p>")
           SafeHtml blurb(String nom, String membreOuParent, String today, String clubName);
    }
    private static final BlurbTemplate BLURB = GWT.create(BlurbTemplate.class);

    private List<Prix> prix = new ArrayList<>();
    /** A list of cours as retrieved from the server.
     * Must stay in synch with the ListBox field cours. */
    private List<CoursSummary> backingCours = new ArrayList<>();
    private List<EscompteSummary> escompteSummaries = new ArrayList<>();
    private List<ProduitSummary> produitSummaries = new ArrayList<>();
    private ClientData cd;
    private String guid;
    private int currentServiceNumber;
    public int getCurrentServiceNumber() { return currentServiceNumber; }

    private String sibid;
    private List<ChangeHandler> onPopulated = new ArrayList<ChangeHandler>();

    void selectClub(ClubSummary club) {
        jdb.selectClub(club);

        assert club != null;
        // assert's not enough, add some defensive programming too.
        if (club == null) return;
        dropDownUserClubsButton.setText(club.getClubText());

        hideEscompteResidentIfUnneeded(club);
        hidePaypalIfDisabled(club);
        retrieveSessions(club);
        retrievePrix(club.getId());
        retrieveCours(club.getId());
        retrieveEscomptes(club.getId());
        retrieveProduits(club.getId());
        if (cd != null)
            updateFrais();
    }

    class ClientClubListHandlerFactory implements JudoDB.ClubListHandlerFactory {
        public ClickHandler instantiate(ClubSummary s) {
            return new ClubListHandler(s);
        }
    }

    class ClubListHandler implements ClickHandler {
        final ClubSummary club;

        ClubListHandler(ClubSummary club) { this.club = club; }

        @Override public void onClick(ClickEvent e) {
            selectClub(club);
        }
    }

    private void populateGradeListBox(ListBox lb) {
        lb.clear();
        lb.addItem(Constants.EMPTY_GRADE.name);
        for (Constants.Grade g : Constants.GRADES) {
            lb.addItem(g.name);
        }
    }

    public ClientWidget(int cid, final JudoDB jdb) {
        this.jdb = jdb;
        initWidget(uiBinder.createAndBindUi(this));
        com.google.gwt.core.client.Scheduler.get().scheduleDeferred
            (new com.google.gwt.user.client.Command() {
                    public void execute() {
                        jdb.mainPanel.editClient.ensureVisible(nom);
                    }
                });

        clientform.setAction(PUSH_ONE_CLIENT_URL);
        deleted.setValue("");

        populateGradeListBox(grade);

        gradeHistory.setVisible(false);
        prorata.setValue(false);
        categorie.setReadOnly(true);
        categorieFrais.setReadOnly(true); categorieFrais.setAlignment(ValueBoxBase.TextAlignment.RIGHT);
        cas_special_pct.setAlignment(ValueBoxBase.TextAlignment.RIGHT);
        escompteFrais.setReadOnly(true); escompteFrais.setAlignment(ValueBoxBase.TextAlignment.RIGHT);
        affiliationFrais.setReadOnly(true); affiliationFrais.setAlignment(ValueBoxBase.TextAlignment.RIGHT);
        suppFrais.setReadOnly(true); suppFrais.setAlignment(ValueBoxBase.TextAlignment.RIGHT);
        frais.setReadOnly(true); frais.setAlignment(ValueBoxBase.TextAlignment.RIGHT);
        ((Element)cas_special_note.getElement().getParentNode()).getStyle().setDisplay(Display.NONE);
        ((Element)cas_special_pct.getElement().getParentNode()).getStyle().setDisplay(Display.NONE);

        if (cid == -1 && jdb.getSelectedClubID() == null) {
            jdb.setStatus("Veuillez selectionner un club pour le client.");
            new Timer() { public void run() {
                ClientWidget.this.jdb.popMode();
            } }.schedule(1000);
            return;
        }

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

        ddn_display.addChangeHandler(recomputeHandler);
        grade.addChangeHandler(directGradeChangeHandler);
        date_grade.addChangeHandler(directGradeDateChangeHandler);
        grade.addChangeHandler(recomputeHandler);
        date_inscription.addChangeHandler(changeSaisonHandler);
        prorata.addValueChangeHandler(recomputeValueHandler);
        sessions.addChangeHandler(recomputeHandler);
        cours.addChangeHandler(recomputeHandler);
        escompte.addChangeHandler(changeEscompteHandler);
        affiliation_envoye.addValueChangeHandler(recomputeValueHandler);
        cas_special_pct.addChangeHandler(clearEscompteAmtAndRecomputeHandler);
        escompteFrais.addChangeHandler(clearEscomptePctAndRecomputeHandler);
        affiliation_speciale.addChangeHandler(recomputeHandler);
        produit.addValueChangeHandler(recomputeValueHandlerLS);
        resident.addValueChangeHandler(recomputeValueHandler);
        paypal.addValueChangeHandler(recomputeValueHandler);

        saveAndReturnClientButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent e) {
                pushClientDataToServer(true);
            }
        });
        saveClientButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent e) {
                pushClientDataToServer(false);
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
            }
        });

        copysib.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent e) {
                copysib();
            }
        });

        if (cid != -1) {
            retrieveClient(cid);
        } else {
            ClubSummary currentClub = jdb.getSelectedClub();
            selectClub(currentClub);

            this.cd = JavaScriptObject.createObject().cast();
            this.cd.setID(null); this.cd.setNom("");

            addNewService();
            cd.setDefaultsPerClub(currentClub);
            currentServiceNumber = this.cd.getMostRecentServiceNumber();

            JsArray<GradeData> ga = JavaScriptObject.createArray().cast();
            GradeData gd = JavaScriptObject.createObject().cast();
            gd.setGrade("Blanche"); gd.setDateGrade(Constants.DB_DATE_FORMAT.format(new Date()));
            ga.set(0, gd);
            this.cd.setGrades(ga);
            loadClientData();
        }
    }

    @Override
    protected void onLoad() {
        jdb.refreshClubListResults();
    }

    private void addNewService() {
        ServiceData sd = ServiceData.newServiceData();
        mustInscrire = true;
        sd.setClubID(jdb.getSelectedClubID());

        JsArray<ServiceData> sa = cd.getServices();
        if (sa == null) {
            sa = JavaScriptObject.createArray().cast();
            cd.setServices(sa);
        }
        sa.push(sd);
    }

    private void loadEscomptes(JsArray<EscompteSummary> escompteArray) {
        HashMap<String, Integer> escompteIdxToSeqno = new HashMap<String, Integer>();
        escompte.clear(); escompteSummaries.clear();
        escompte.addItem(Constants.EMPTY_ESCOMPTE.getNom(), Constants.EMPTY_ESCOMPTE.getId());
        escompteSummaries.add(Constants.EMPTY_ESCOMPTE);
        int idx = 1;
        for (int i = 0; i < escompteArray.length(); i++) {
            EscompteSummary e = escompteArray.get(i);
            if (e.getClubId().equals("0") || e.getClubId().equals(jdb.getSelectedClubID())) {
                escompteSummaries.add(e);
                escompte.addItem(e.getNom(), e.getId());
                escompteIdxToSeqno.put(e.getId(), idx);
                idx++;
            }
        }
        // XXX should move out the setSelectedIndex to when we definitely have services;
        // XXX is currently a race condition.
        if (cd == null || cd.getServices() == null || cd.getServices().length() == 0) return;
        ServiceData sd = cd.getServices().get(cd.getMostRecentServiceNumber());
        String escompteIndex = sd.getEscompteId();
        if (escompteIdxToSeqno.get(escompteIndex) != null)
            escompte.setSelectedIndex(escompteIdxToSeqno.get(escompteIndex));
        else
            escompte.setSelectedIndex(0);
    }

    private void loadProduits(JsArray<ProduitSummary> produitArray) {
        produit.clear(); produitSummaries.clear();
	if (produitArray == null) {
            jdb.displayError("aucun produit recu");
            new Timer() { public void run() { jdb.clearStatus(); } }.schedule(5000);
        }

        for (int i = 0; i < produitArray.length(); i++) {
            ProduitSummary e = produitArray.get(i);
            if (e.getClubId().equals("0") || e.getClubId().equals(jdb.getSelectedClubID())) {
                produitSummaries.add(e);
                Option p = new Option();
                p.setText(e.getNom()); p.setId(e.getId());
                produit.add(p);
            }
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
            nn = "__________________________, parent ou tuteur du membre indiqué plus haut,";
            mm = "parent ou tuteur";
        }

        ServiceData sd = cd.getServices().get(currentServiceNumber);
        SafeHtml blurbContents = BLURB.blurb(nn, mm, Constants.STD_DATE_FORMAT.format(today), jdb.getClubSummaryByID(sd.getClubID()).getNom());

        blurb.clear();
        blurb.add(new HTMLPanel(blurbContents));
    }

    private void disableAllSessionEditingInfo() {
        sessions.clear();
        sessions.addItem("");

        affiliation_envoye.setValue(false);
        prorata.setValue(false);
        prorata.setEnabled(false);
        categorieFrais.setText("");

        date_affiliation_envoye.setText("");
        date_affiliation_envoye.setEnabled(false);
        carte_judoca_recu.setValue(false);
        carte_judoca_recu.setEnabled(false);
        affiliation_speciale.setSelectedIndex(0);
        affiliation_speciale.setEnabled(false);
        affiliationFrais.setText("");

        escompte.setEnabled(false);

        cas_special_note.setText("");
        cas_special_note.setReadOnly(true);
        cas_special_pct.setValue("");
        cas_special_pct.setReadOnly(true);
        escompteFrais.setText("");

        produit.setEnabled(false);
        resident.setValue(false);
        resident.setEnabled(false);
        paypal.setValue(false);
        paypal.setEnabled(false);
        suppFrais.setText("");

        frais.setText("");
        solde.setValue(false);
    }

    /* depends on jdb.retrieveClubList having succeeded */
    /** Takes data from ClientData into the form. */
    private void loadClientData () {
        // cannot just test for getSelectedClubID() == null,
        // since it sets the selected club ID based on the client's data!
        if (jdb.allClubs == null || currentSession == null || !gotEscomptes || !gotProduits || !gotPrix) {
            new Timer() {
                public void run() { loadClientData(); }
            }.schedule(100);
            return;
        }

        cid.setInnerText(cd.getID());
        nom.setText(cd.getNom());
        prenom.setText(cd.getPrenom());
        Date ddns = cd.getDDN();
        if (ddns != null)
            ddn_display.setText(Constants.STD_DATE_FORMAT.format(ddns));
        ddn.setValue(ddns == null ? Constants.DB_DUMMY_DATE : Constants.DB_DATE_FORMAT.format(ddns));
        sexe.setText(cd.getSexe());
        adresse.setText(cd.getAdresse());
        ville.setText(cd.getVille());
        codePostal.setText(cd.getCodePostal());
        tel.setText(cd.getTel());
        courriel.setText(cd.getCourriel());

        affiliation.setText(cd.getJudoQC());
        carte_resident.setText(cd.getCarteResident());
        nom_recu_impot.setText(cd.getNomRecuImpot());
        notes.setText(cd.getNotes());

        tel_contact_urgence.setText(cd.getTelContactUrgence());

        if (currentServiceNumber == -1) {
            addNewService();
        }

        loadGradesData();

        date_inscription.clear();
        boolean hasToday = false, isToday = false;
        boolean hasThisSession = cd.getServiceFor(currentSession) != null;
        String todayString = Constants.DB_DATE_FORMAT.format(new Date());

        for (int i = 0; i < cd.getServices().length(); i++) {
            ServiceData ssd = cd.getServices().get(i);
            if (todayString.equals(ssd.getDateInscription())) {
                hasToday = true;
                isToday = (i == currentServiceNumber);
            }
            date_inscription.addItem(Constants.dbToStdDate(ssd.getDateInscription()), Integer.toString(i));
        }
        date_inscription.setSelectedIndex(currentServiceNumber);
        inscrire.setVisible(!hasToday && !hasThisSession);
        modifier.setVisible(!hasToday && hasThisSession);
        desinscrire.setVisible(hasThisSession);

        ServiceData sd = cd.getServices().get(currentServiceNumber);
        if (sd == null) {
            disableAllSessionEditingInfo();
            return;
        }

        ClubSummary club = jdb.getClubSummaryByID(sd.getClubID());
        if (club != null) {
            jdb.selectClub(club);
        } else {
            jdb.setStatus("Le client n'a pas de club enregistré.");
            return;
        }

        updateSessionLB();
        sessions.setEnabled(isToday);
        affiliation_envoye.setValue(sd.getAffiliationEnvoye());
        int matching_index = 0;
        for (CoursSummary cs : backingCours) {
            if (cs.getId().equals(sd.getCours())) {
                cours.setSelectedIndex(matching_index);
                break;
            }
            matching_index++;
        }
        prorata.setEnabled(isToday);
        categorieFrais.setText(sd.getCategorieFrais());

        date_affiliation_envoye.setValue(Constants.dbToStdDate(sd.getDateAffiliationEnvoye()));
        date_affiliation_envoye.setEnabled(isToday && sd.getAffiliationEnvoye());
        carte_judoca_recu.setValue(sd.getCarteJudocaRecu());
        carte_judoca_recu.setEnabled(isToday);

        if (sd.getSansAffiliation())
            affiliation_speciale.setSelectedIndex(AF_DEJA_AFFILIE);
        else if (sd.getAffiliationInitiation())
            affiliation_speciale.setSelectedIndex(AF_INITIATION);
        else if (sd.getAffiliationEcole())
            affiliation_speciale.setSelectedIndex(AF_SCOLAIRE);
        else if (sd.getAffiliationParascolaire())
            affiliation_speciale.setSelectedIndex(AF_PARASCOLAIRE);
        else
            affiliation_speciale.setSelectedIndex(AF_NONE);
        affiliation_speciale.setEnabled(isToday);

        affiliationFrais.setText(sd.getAffiliationFrais());

        escompte.setEnabled(isToday);

        cas_special_note.setText(sd.getCasSpecialNote());
        cas_special_note.setReadOnly(!isToday);
        regularizeEscompte();
        cas_special_pct.setReadOnly(!isToday);
        escompteFrais.setText(sd.getEscompteFrais());

        List<String> produits = Arrays.asList(sd.getJudogi().split(";"));
        produit.deselectAll();
        for (Option o : produit.getItems()) {
            if (produits.contains(o.getId()))
                o.setSelected(true);
        }
        produit.setEnabled(isToday);
        resident.setValue(sd.getResident());
        resident.setEnabled(isToday);
        paypal.setValue(sd.getPaypal());
        paypal.setEnabled(isToday);
        suppFrais.setText(sd.getSuppFrais());

        frais.setText(sd.getFrais());
        solde.setValue(sd.getSolde());

        updateDynamicFields();
    }

    /** Puts data from the form back onto ClientData. */
    private void saveClientData() {
        cd.setNom(nom.getText());
        cd.setPrenom(prenom.getText());
        cd.setDDNString(Constants.stdToDbDate(ddn_display.getText()));
        cd.setSexe(sexe.getText());

        cd.setAdresse(adresse.getText());
        cd.setVille(ville.getText());
        cd.setCodePostal(codePostal.getText());
        cd.setTel(tel.getText());
        cd.setCourriel(courriel.getText());

        cd.setJudoQC(affiliation.getText());
        cd.setCarteResident(carte_resident.getText());
        cd.setNomRecuImpot(nom_recu_impot.getText());
        cd.setNotes(notes.getText());

        cd.setTelContactUrgence(tel_contact_urgence.getText());

        if (currentServiceNumber == -1) return;
        if (cd.getServices() == null) return;

        ServiceData sd = cd.getServices().get(currentServiceNumber);
        if (sd == null) return;

        if (currentServiceNumber < date_inscription.getItemCount())
            sd.setDateInscription(removeCommas(Constants.stdToDbDate(date_inscription.getItemText(currentServiceNumber))));
        sd.setSessions(sessions.getSelectedItemText());
        sd.setClubID(jdb.getSelectedClubID());
        sd.setAffiliationEnvoye(affiliation_envoye.getValue());
        if (cours.getSelectedIndex() != -1)
            sd.setCours(cours.getValue(cours.getSelectedIndex()));
        sd.setCategorieFrais(stripDollars(categorieFrais.getText()));

        sd.setDateAffiliationEnvoye(Constants.stdToDbDate(date_affiliation_envoye.getText()));
        sd.setCarteJudocaRecu(carte_judoca_recu.getValue());

        sd.setSansAffiliation(false);
        sd.setAffiliationInitiation(false);
        sd.setAffiliationEcole(false);
        sd.setAffiliationParascolaire(false);

        switch (affiliation_speciale.getSelectedIndex()) {
        case AF_DEJA_AFFILIE:
            sd.setSansAffiliation(true); break;
        case AF_INITIATION:
            sd.setAffiliationInitiation(true); break;
        case AF_SCOLAIRE:
            sd.setAffiliationEcole(true); break;
        case AF_PARASCOLAIRE:
            sd.setAffiliationParascolaire(true); break;
        case AF_NONE:
            break;
        }

        sd.setAffiliationFrais(stripDollars(affiliationFrais.getText()));

        if (escompte.getSelectedIndex() != -1) {
            sd.setEscompteId(escompte.getValue(escompte.getSelectedIndex()));
	}
        sd.setCasSpecialNote(removeCommas(cas_special_note.getText()));
        sd.setEscompteFrais(stripDollars(escompteFrais.getText()));

        StringBuffer produitStr = new StringBuffer();
        if (!produit.getSelectedItems().isEmpty()) {
            for (Option p : produit.getSelectedItems()) {
                if (produitStr.length() > 0)
                    produitStr.append(";");
                produitStr.append(p.getId());
            }
        }
        sd.setJudogi(produitStr.toString());
        sd.setResident(resident.getValue());
        sd.setPaypal(paypal.getValue());
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

        Arrays.sort(grades, new GradeData.GradeDateComparator());

        for (int i = 0; i < grades.length; i++) {
            setGradesTableRow(i+1, grades[i].getGrade(), Constants.dbToStdDate(grades[i].getDateGrade()));
        }
        setGradesTableRow(grades.length+1, "", "");

        for (int i = 0; i < grade.getItemCount(); i++) {
            Constants.Grade g = Constants.stringToGrade(cd.getGrade());
            if (g != null && g.name.equals(grade.getItemText(i))) {
                grade.setSelectedIndex(i); break;
            }
        }
        date_grade.setText(Constants.dbToStdDate(cd.getDateGrade()));
    }

    private void setGradesTableRow(int row, String grade, String dateGrade) {
        ListBox g = new ListBox();
        populateGradeListBox(g);
        TextBox gd = new TextBox();
        gd.getElement().setPropertyString("placeholder", "jj/mm/aa");

        gd.setValue(dateGrade);
        gradeTable.setWidget(row, 0, g);
        gradeTable.setWidget(row, 1, gd);
        for (int i = 0; i < g.getItemCount(); i++) {
            Constants.Grade gg = Constants.stringToGrade(grade);
            if (gg != null && gg.name.equals(g.getItemText(i))) {
                g.setSelectedIndex(i); break;
            }
        }

        ((ListBox)gradeTable.getWidget(row, 0)).addChangeHandler(ensureGradeSpace);
        ((TextBox)gradeTable.getWidget(row, 1)).addChangeHandler(ensureGradeSpace);
    }

    private ListBox getGradeTableListBox(int row, int col) {
        return (ListBox) gradeTable.getWidget(row, col);
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
                if (getGradeTableListBox(i, 0).getSelectedIndex() == 0 &&
                    getGradeTableTextBox(i, 1).getText().equals("")) {
                    for (int j = i; j < rc-1; j++) {
                        getGradeTableListBox(j, 0).setSelectedIndex
                            (getGradeTableListBox(j+1, 0).getSelectedIndex());
                        getGradeTableTextBox(j, 1).setText(getGradeTableTextBox(j+1, 1).getText());
                    }
                    getGradeTableListBox(rc-1, 0).setSelectedIndex(0);
                    getGradeTableTextBox(rc-1, 1).setText("");
                }
            }

            if (getGradeTableListBox(rc-1, 0).getSelectedIndex() != 0 &&
                !getGradeTableTextBox(rc-1, 1).getText().equals("")) {
                gradeTable.resize(rc+1, 2);
                setGradesTableRow(rc, Constants.EMPTY_GRADE.name, "");
            }
        }
    };

    /** Save data from the grades table into the ClientData. */
    private void saveGradesData() {
        JsArray<GradeData> newGradesJS = JavaScriptObject.createArray().cast();
        ArrayList<GradeData> newGradesList = new ArrayList<GradeData>();

        for (int i = 1; i < gradeTable.getRowCount(); i++) {
            int gi = getGradeTableListBox(i, 0).getSelectedIndex();
            String g = getGradeTableListBox(i, 0).getSelectedItemText(),
                gdate = getGradeTableTextBox(i, 1).getText();
            if (gi != 0) {
                GradeData gd = GradeData.createObject().cast();
                gd.setGrade(g);
                gd.setDateGrade(Constants.stdToDbDate(gdate).replaceAll(",", ";"));
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
            String todayDate = Constants.STD_DATE_FORMAT.format(new Date());
            // either no previous grade or no previous date-grade;
            // erase the old grade
            if (date_grade.getText().equals(Constants.STD_DUMMY_DATE) || cd.getGrade().equals("")) {
                date_grade.setText(todayDate);
                setGradesTableRow(1, grade.getSelectedItemText(), date_grade.getText());
            } else if (date_grade.getText().equals(todayDate)) {
                if (!grade.getSelectedItemText().equals(cd.getGrade())) {
                    String todayDateDB = Constants.DB_DATE_FORMAT.format(new Date());
                    for (int i = 0; i < cd.getGrades().length(); i++) {
                        String dg = cd.getGrades().get(i).getDateGrade();
                        if (dg.equals(todayDateDB)) {
                            getGradeTableListBox(i+1, 0).setSelectedIndex(grade.getSelectedIndex());
                        }
                    }
                }
            } else {
                // old grade set, and has date;  keep the old grade-date in the array
                // and update the array.
                ensureGradeSpace.onChange(null);
                date_grade.setText(Constants.STD_DATE_FORMAT.format(new Date()));
                setGradesTableRow(gradeTable.getRowCount()-1,
                        grade.getSelectedItemText(), date_grade.getText());
            }
            saveGradesData();
        }
    };

    private final ChangeHandler directGradeDateChangeHandler = new ChangeHandler() {
        public void onChange(ChangeEvent e) {
            // if you change the grade-date, and grade is not empty, then
            // update the array.
            if (grade.getSelectedIndex() != 0) {
                int lastIndex = 0;
                GradeData m = cd.getGrades().get(0);
                for (int i = 0; i < cd.getGrades().length(); i++) {
                    String dg = cd.getGrades().get(i).getDateGrade();
                    if (dg != null && dg.compareTo(m.getDateGrade()) > 0) {
                        m = cd.getGrades().get(i);
                        lastIndex = i;
                    }
                }
                setGradesTableRow(lastIndex+1, grade.getSelectedItemText(), date_grade.getText());
                saveGradesData();
            }
        }
    };

    private final ChangeHandler changeEscompteHandler = new ChangeHandler() {
        public void onChange(ChangeEvent e) {
            EscompteSummary es = null;
            for (EscompteSummary ee : escompteSummaries) {
                if (ee.getId().equals(escompte.getValue(escompte.getSelectedIndex())))
                    es = ee;
            }
            if (es != null && es.getAmountPercent().equals("-1") &&
                cas_special_pct.getValue().equals("-1"))
                cas_special_pct.setValue("0");
            updateDynamicFields();
        }
    };
    private final ChangeHandler recomputeHandler = new ChangeHandler() {
        public void onChange(ChangeEvent e) { updateDynamicFields(); }
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
        @Override public void onValueChange(ValueChangeEvent<Boolean> e) { updateDynamicFields(); }
    };
    private final ValueChangeHandler<List<String>> recomputeValueHandlerLS = new ValueChangeHandler<List<String>>() {
        @Override public void onValueChange(ValueChangeEvent<List<String>> e) { updateDynamicFields(); }
    };

    private final ChangeHandler clearEscomptePctAndRecomputeHandler = new ChangeHandler() {
        public void onChange(ChangeEvent e) {
            cas_special_pct.setValue("-1");
            regularizeEscompte();
            updateDynamicFields();
        }
    };

    private final ChangeHandler clearEscompteAmtAndRecomputeHandler = new ChangeHandler() {
        public void onChange(ChangeEvent e) {
            escompteFrais.setValue("-1");
            regularizeEscompte();
            updateDynamicFields();
        }
    };

    /** Create a new inscription for the current session. */
    private final ClickHandler inscrireClickHandler = new ClickHandler() {
        public void onClick(ClickEvent e) {
            saveClientData();

            ServiceData sd = cd.getServiceFor(currentSession);

            if (sd == null) {
                sd = ServiceData.newServiceData();
		sd.setClubID(jdb.getSelectedClubID());
                cd.getServices().push(sd);
            }  // actually, sd != null should not occur; that case should be modify.
            sd.inscrireAujourdhui(sessionSummaries);
            currentServiceNumber = cd.getMostRecentServiceNumber();
            loadClientData();
            updateDynamicFields();
        }
    };

    /** Resets the date d'inscription for the current session to today's date. */
    private final ClickHandler modifierClickHandler = new ClickHandler() {
        public void onClick(ClickEvent e) {
            ServiceData sd = cd.getServiceFor(currentSession);

            // shouldn't happen, actually.
            if (sd == null)
                return;

            saveClientData();
            sd.inscrireAujourdhui(sessionSummaries);
            currentServiceNumber = cd.getMostRecentServiceNumber();
            loadClientData();
            updateDynamicFields();
        }
    };

    /* behaviour of desinscrire:
     * 1) can only desinscrire from current session;
     * removes previous inscription and re-creates services JsArray with it. */
    private final ClickHandler desinscrireClickHandler = new ClickHandler() {
        public void onClick(ClickEvent e) {
            ServiceData sd = cd.getServiceFor(currentSession);
            if (sd == null)
                return;
            saveClientData();

            JsArray<ServiceData> newServices = JavaScriptObject.createArray().cast();
            for (int i = 0, j = 0; i < cd.getServices().length(); i++) {
                if (i != currentServiceNumber)
                    newServices.set(j++, cd.getServices().get(i));
            }
            cd.setServices(newServices);
            currentServiceNumber = cd.getMostRecentServiceNumber();

            loadClientData();
            updateDynamicFields();
        }
    };

    private int emptyGradeDates() {
        int empty = 0;
        for (int i = 1; i < gradeTable.getRowCount(); i++) {
            int gvi = getGradeTableListBox(i, 0).getSelectedIndex();
            String gdv = getGradeTableTextBox(i, 1).getText();
            if ((gdv.equals(Constants.STD_DUMMY_DATE) || gdv.equals("")) && gvi != 0)
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

    /** Enables user to edit both % escompte and escompte amount by putting them in synch.
     * Works directly at the View level, not the Model level. */
    private void regularizeEscompte() {
        ServiceData sd = cd.getServices().get(currentServiceNumber);
        ClubSummary cs = jdb.getClubSummaryByID(sd.getClubID());
        double dCategorieFrais = CostCalculator.proratedFraisCours(cd, sd, cs, currentSession, sessionSummaries, backingCours, prix);

        if (CostCalculator.isCasSpecial(sd,
					CostCalculator.getApplicableEscompte(sd, escompteSummaries))) {
            NumberFormat nf = NumberFormat.getDecimalFormat();
            if (cas_special_pct.getValue().equals("-1")) {
                float actualEscompteFrais = parseFloat(stripDollars(escompteFrais.getValue()));
                if (actualEscompteFrais > 0) {
                    actualEscompteFrais *= -1;
                    escompteFrais.setValue(nf.format(actualEscompteFrais));
                }
                cas_special_pct.setValue(nf.format(-100 * actualEscompteFrais / dCategorieFrais));
            } else if (escompteFrais.getValue().equals("-1")) {
                String cpct = stripDollars(cas_special_pct.getValue());
                float fCpct = parseFloat(cpct);
                if (fCpct < 0) {
                    fCpct *= -1;
                    cas_special_pct.setValue(nf.format(fCpct));
                }
                escompteFrais.setValue(nf.format(-fCpct * dCategorieFrais / 100.0));
            }
        }
    }

    /** View-level method to pull sessions from ServiceData and select the correct index in the ListBox. */
    private void updateSessionLB() {
        if (cd.getServices() == null) return;
        ServiceData sd = cd.getServices().get(currentServiceNumber);
        Set<String> sdSessions = new HashSet<>(); sdSessions.addAll(Arrays.asList(sd.getSessions().split(" ")));

        Date inscrDate;
        try {
            inscrDate = Constants.DB_DATE_FORMAT.parse(sd.getDateInscription());
        } catch (IllegalArgumentException e) {
            inscrDate = new Date();
        }

        sessions.clear();
        SessionSummary ts = JudoDB.getSessionForDate(inscrDate, sessionSummaries);
        if (ts.isPrimary()) {
            sessions.addItem(JudoDB.getSessionIds(inscrDate, 2, sessionSummaries));
            sessions.addItem(JudoDB.getSessionIds(inscrDate, 1, sessionSummaries));
        } else {
            sessions.addItem(JudoDB.getSessionIds(inscrDate, 1, sessionSummaries));
            sessions.addItem(JudoDB.getSessionIds(inscrDate, 2, sessionSummaries));
        }

        boolean found = false;
        for (int i = 0; i < sessions.getItemCount(); i++) {
            Set<String> sss = new HashSet<>(); sss.addAll(Arrays.asList(sessions.getItemText(i).split(" ")));
            if (sss.equals(sdSessions)) {
                sessions.setSelectedIndex(i);
                found = true;
            }
        }

        if (!sdSessions.equals("") && !found) {
            jdb.displayError("aucun session en cours pour date d'inscription " + sd.getDateInscription());
            new Timer() { public void run() { jdb.clearStatus(); } }.schedule(5000);
        }
    }

    /** View-level method to pull information from ServiceData and put it onto the form in currency format. */
    private void updateFrais() {
        NumberFormat cf = NumberFormat.getCurrencyFormat("CAD");
        ServiceData sd = cd.getServices().get(currentServiceNumber);
        Date dateInscription;
        try {
            dateInscription = Constants.DB_DATE_FORMAT.parse(sd.getDateInscription());
        } catch (IllegalArgumentException e) {
            dateInscription = new Date();
        }
        int sessionCount = sd.getSessionCount();

        semaines.setText(CostCalculator.getWeeksSummary(sd, currentSession, dateInscription, sessionSummaries));
        escompteFrais.setReadOnly
	    (!CostCalculator.isCasSpecial(sd,
					  CostCalculator.getApplicableEscompte(sd, escompteSummaries)));

        try {
            categorieFrais.setText(cf.format(Double.parseDouble(sd.getCategorieFrais())));
            affiliationFrais.setText(cf.format(Double.parseDouble(sd.getAffiliationFrais())));
            escompteFrais.setValue(cf.format(Double.parseDouble(sd.getEscompteFrais())));
            suppFrais.setText(cf.format(Double.parseDouble(sd.getSuppFrais())));
            frais.setText(cf.format(Double.parseDouble(sd.getFrais())));
        } catch (NumberFormatException e) {}
    }

    private void updateCopySib() {
        copysib.setVisible(false);
        // check 1) address fields are empty and 2) there exists a sibling
        // Note that the following check relies on the data being saved
        // to the ClientData, which is true after you enter the birthday.
        ServiceData sd = cd.getServices().get(currentServiceNumber);
        ClubSummary clb = jdb.getClubSummaryByID(sd.getClubID());
        if (!cd.isDefault(clb)) return;

        // XXX should do an allClients fetch upon load...
        if (jdb.allClients == null) return;

        for (ClientSummary cs : jdb.allClients) {
	    // XXX don't copy data across clubs
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

    private void hideEscompteResidentIfUnneeded(ClubSummary cs) {
        if (cs == null) return;
        String escompteResident = cs.getEscompteResident();
        if (escompteResident == null || escompteResident.equals("") || escompteResident.equals("0")) {
            rabais_resident_label.setVisible(false);
            resident.setVisible(false);
        } else {
            resident.setVisible(true);
            rabais_resident_label.setVisible(true);
        }
    }

    private void hidePaypalIfDisabled(ClubSummary cs) {
        if (cs == null) return;
        boolean showPaypal = cs.getAfficherPaypal();
        if (!showPaypal) {
            frais_paypal_label.setVisible(false);
            paypal.setVisible(false);
        } else {
            paypal.setVisible(true);
            frais_paypal_label.setVisible(true);
        }
    }

    private void disableProrataPerConfig(ClubSummary cs) {
        if (cs == null) return;
        boolean showProrata = cs.getEnableProrata();
        if (!showProrata) {
            prorata_group.setVisible(false);
            prorata.setValue(false);
        } else {
            prorata_group.setVisible(true);
        }
    }

    private void updateDynamicFields() {
        saveClientData();
        ServiceData sd = cd.getServices().get(currentServiceNumber);
        if (sd == null) return;

        ClubSummary cs = jdb.getClubSummaryByID(sd.getClubID());
        hideEscompteResidentIfUnneeded(cs);
        hidePaypalIfDisabled(cs);
        disableProrataPerConfig(cs);

        Collection<ProduitSummary> ps = CostCalculator.getApplicableProduits(sd, produitSummaries);;

        CostCalculator.recompute(currentSession, cd, sd, cs, sessionSummaries, backingCours, ps, prorata.getValue(), prix, escompteSummaries);

        /* view stuff here */
        Display d = Display.NONE;
        int escompteIdx = escompte.getSelectedIndex();
        if (CostCalculator.isCasSpecial(sd, CostCalculator.getApplicableEscompte(sd, escompteSummaries)))
            d = Display.INLINE;
        ((Element)cas_special_note.getElement().getParentNode()).getStyle().setDisplay(d);
        ((Element)cas_special_pct.getElement().getParentNode()).getStyle().setDisplay(d);
        regularizeEscompte();
        if (sd != null) {
            String todayString = Constants.DB_DATE_FORMAT.format(new Date());
            boolean isToday = todayString.equals(sd.getDateInscription());
            date_affiliation_envoye.setEnabled(isToday && sd.getAffiliationEnvoye());
        }

        if (sd != null && !sd.getSessions().equals("")) {
            String a = sd.getSessions().split(" ")[0];
            SessionSummary that_session = null;
            for (SessionSummary s : sessionSummaries) {
                if (a.equals(s.getAbbrev()))
                    that_session = s;
            }
            if (that_session != null) {
                Constants.Division c = cd.getDivision(that_session.getYear());
                categorie.setText(c.abbrev);
            }
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
            dae = new StringBuffer(), cjr = new StringBuffer(), ai = new StringBuffer(),
            ae = new StringBuffer(), aps = new StringBuffer(),
            af = new StringBuffer(), j = new StringBuffer(), p = new StringBuffer(),
            n = new StringBuffer(), pp = new StringBuffer(), sf = new StringBuffer(), s = new StringBuffer(),
            f = new StringBuffer(), clubid = new StringBuffer();

        JsArray<ServiceData> services = cd.getServices();
        for (int i = 0; i < services.length(); i++) {
            ServiceData sd = services.get(i);
            di.append(sd.getDateInscription()+",");
            sais.append(sd.getSessions()+",");
            v.append(sd.getAffiliationEnvoye() ? "1," : "0,");
            cf.append(sd.getCategorieFrais()+",");
            c.append(sd.getCours()+",");
            sess.append("0,"); /* deprecated session count */
            e.append(sd.getEscompteId()+",");
            csn.append(sd.getCasSpecialNote()+",");
            ef.append(sd.getEscompteFrais()+",");
            dae.append(sd.getDateAffiliationEnvoye()+",");
            cjr.append(sd.getCarteJudocaRecu() ? "1," : "0,");
            sa.append(sd.getSansAffiliation() ? "1," : "0,");
            ai.append(sd.getAffiliationInitiation() ? "1," : "0,");
            ae.append(sd.getAffiliationEcole() ? "1," : "0,");
            aps.append(sd.getAffiliationParascolaire() ? "1," : "0,");
            af.append(sd.getAffiliationFrais()+",");
            j.append(sd.getJudogi()+",");
            // disabled passeport
            //p.append(sd.getPasseport()+",");
            p.append("0,");
            n.append(sd.getResident()+",");
            pp.append(sd.getPaypal()+",");
            sf.append(sd.getSuppFrais()+",");
            s.append(sd.getSolde() ? "1,":"0,");
            f.append(sd.getFrais()+",");
            clubid.append(sd.getClubID()+",");
        }

        date_inscription_encoded.setValue(di.toString());
        saisons_encoded.setValue(sais.toString());
        affiliation_envoye_encoded.setValue(v.toString());
        categorieFrais_encoded.setValue(cf.toString());
        cours_encoded.setValue(c.toString());
        escompte_encoded.setValue(e.toString());
        cas_special_note_encoded.setValue(csn.toString());
        cas_special_pct_encoded.setValue(csp.toString());
        escompteFrais_encoded.setValue(ef.toString());
        date_affiliation_envoye_encoded.setValue(dae.toString());
        carte_judoca_recu_encoded.setValue(cjr.toString());
        sans_affiliation_encoded.setValue(sa.toString());
        affiliation_initiation_encoded.setValue(ai.toString());
        affiliation_ecole_encoded.setValue(ae.toString());
        affiliation_parascolaire_encoded.setValue(aps.toString());
        affiliationFrais_encoded.setValue(af.toString());
        judogi_encoded.setValue(j.toString());
        resident_encoded.setValue(n.toString());
        paypal_encoded.setValue(pp.toString());
        suppFrais_encoded.setValue(sf.toString());
        solde_encoded.setValue(s.toString());
        frais_encoded.setValue(f.toString());
        club_id_encoded.setValue(clubid.toString());
    }

    private void pushClientDataToServer(final boolean leaveAfterPush) {
        saveClientData();

        if (cd.getNom().equals("") || cd.getPrenom().equals("")) {
            jdb.displayError("pas de nom ou prenom");
            return;
        }

        guid = UUID.uuid();
        sid.setValue(cd.getID());
        guid_on_form.setValue(guid);

        grades_encoded.setValue(encodeGrades());
        grade_dates_encoded.setValue(encodeGradeDates());
        loadClientData();
        encodeServices();

        // http://stackoverflow.com/questions/2699277/post-data-to-jsonp
        clientform.submit();

        pushTries = 0;
        new Timer() { public void run() {
            pushOneClient(guid, leaveAfterPush);
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
            pushOneClient(guid, true);
        } }.schedule(500);
    }

    private void loadCours(JsArray<CoursSummary> coursArray) {
        backingCours.clear();
        cours.setVisibleItemCount(1);
        cours.clear();

        for (int i = 0; i < coursArray.length(); i++) {
            CoursSummary c = coursArray.get(i);
            cours.addItem(c.getShortDesc(), c.getId());
            backingCours.add(c);
        }

        if (backingCours.size() == 0) {
            jdb.displayError("aucun cours defini pour " + jdb.getSelectedClub().getNom() + " pour la session "+currentSession.getName());
            new Timer() { public void run() { jdb.clearStatus(); } }.schedule(5000);
        }
    }

    /* --- sessions --- */
    List<SessionSummary> sessionSummaries = new ArrayList<SessionSummary>();
    SessionSummary currentSession;
    boolean mustInscrire = false;

    void populateSessions(JsArray<SessionSummary> ss) {
        SessionSummary linkedSession = null;

	sessionSummaries.clear();
	for (int i = 0; i < ss.length(); i++) {
	    sessionSummaries.add(ss.get(i));
	}

        currentSession = JudoDB.getSessionForDate(new Date(), sessionSummaries);
        if (currentSession == null) {
            if (jdb.getSelectedClub() != null) {
                jdb.displayError("aucun session en cours pour " + jdb.getSelectedClub().getNom());
            }
            new Timer() { public void run() { jdb.clearStatus(); } }.schedule(5000);
        } else {
            if (mustInscrire) {
                ServiceData sd = cd.getServices().get(currentServiceNumber);
                sd.inscrireAujourdhui(sessionSummaries);
                mustInscrire = false;
            }
            updateSessionLB();
        }

        gotSessions = true;
    }

    /* --- end sessions --- */

    /* --- network functions --- */
    public void retrieveClient(final int cid) {
        String url = PULL_ONE_CLIENT_URL + "?id=" + cid;
        RequestCallback rc =
            jdb.createRequestCallback(new JudoDB.Function() {
                    public void eval(String s) {
                        if (s.equals("")) {
                            ClientWidget.this.jdb.displayError("ce client n'existe pas");
                            new Timer() { public void run() {
                                ClientWidget.this.jdb.popMode();
                            } }.schedule(2000);
                            return;
                        }

                        ClientWidget.this.cd = JsonUtils.<ClientData>safeEval(s);
                        currentServiceNumber = cd.getMostRecentServiceNumber();
                        selectClub(jdb.getClubSummaryByID(cd.getServices().get(currentServiceNumber).getClubID()));
                        loadClientData();

                        for (ChangeHandler ch : onPopulated) {
                            ch.onChange(null);
                        }
                    }
                });
        jdb.retrieve(url, rc);
    }

    private boolean gotSessions = false;
    public void retrieveSessions(ClubSummary cs) {
        String url = JudoDB.PULL_SESSIONS_URL;
        url += "?club_id=" + ((cs == null) ? "0" : cs.getId());
        RequestCallback rc =
            jdb.createRequestCallback(new JudoDB.Function() {
                    public void eval(String s) {
                        populateSessions(JsonUtils.<JsArray<SessionSummary>>safeEval(s));
                    }
                });
        jdb.retrieve(url, rc);
    }

    /* depends on jdb.retrieveClubList() and retrieveSessions() having succeeded */
    private boolean gotPrix = false;
    public void retrievePrix(final String club_id) {
        if (!gotSessions) {
            new Timer() {
                public void run() { retrievePrix(club_id); }
            }.schedule(100);
            return;
        }

        String url = JudoDB.PULL_CLUB_PRIX_URL +
            "?club_id=" + club_id;

        RequestCallback rc =
            jdb.createRequestCallback(new JudoDB.Function() {
                    public void eval(String s) {
                        gotPrix = true;
                        JsArray<Prix> cp = JsonUtils.<JsArray<Prix>>safeEval(s);
                        prix.clear();
                        for (int i = 0; i < cp.length(); i++)
                            prix.add(cp.get(i));
                    }
                });
        jdb.retrieve(url, rc);
    }

    /* depends on jdb.retrieveClubList() and retrieveSessions() having succeeded with an actual club */
    public void retrieveCours(final String club_id) {
        if (currentSession == null) {
            new Timer() {
                public void run() { retrieveCours(club_id); }
            }.schedule(100);
            return;
        }

        backingCours.clear();
        String url = JudoDB.PULL_CLUB_COURS_URL +
            "?club_id=" + club_id +
            "&session_seqno=" + currentSession.getSeqno();
        RequestCallback rc =
            jdb.createRequestCallback(new JudoDB.Function() {
                    public void eval(String s) {
                        loadCours
                            (JsonUtils.<JsArray<CoursSummary>>safeEval(s));
                    }
                });
        jdb.retrieve(url, rc);
    }

    private boolean gotEscomptes = false;
    public void retrieveEscomptes(String club_id) {
        escompte.clear();
        escompteSummaries.clear();
        String url = JudoDB.PULL_ESCOMPTE_URL +
            "?club_id=" + club_id;
        RequestCallback rc =
            jdb.createRequestCallback(new JudoDB.Function() {
                    public void eval(String s) {
			gotEscomptes = true;
                        loadEscomptes
                            (JsonUtils.<JsArray<EscompteSummary>>safeEval(s));
                    }
                });
        jdb.retrieve(url, rc);
    }

    /* depends on jdb.retrieveClubList() having succeeded */
    /* technically does not require sessions, but adding it removes a race condition */
    private boolean gotProduits = false;
    public void retrieveProduits(final String club_id) {
        if (!gotSessions) {
            new Timer() {
                public void run() { retrieveProduits(club_id); }
            }.schedule(100);
            return;
        }

        produit.clear();
        produitSummaries.clear();
        String url = JudoDB.PULL_PRODUIT_URL +
            "?club_id=" + club_id;
        RequestCallback rc =
            jdb.createRequestCallback(new JudoDB.Function() {
                    public void eval(String s) {
			gotProduits = true;
                        loadProduits
                            (JsonUtils.<JsArray<ProduitSummary>>safeEval(s));
                    }
                });
        jdb.retrieve(url, rc);
    }

    public void pushOneClient(final String guid, final boolean leaveAfterPush) {
        String url = JudoDB.CONFIRM_PUSH_URL + "?guid=" + guid;
        RequestCallback rc =
            jdb.createRequestCallback(new JudoDB.Function() {
                    public void eval(String s) {
                        ConfirmResponseObject cro =
                            JsonUtils.<ConfirmResponseObject>safeEval(s);
                        String rs = cro.getResult();
                        if (rs.equals("NOT_YET")) {
                            if (pushTries >= 3) {
                                jdb.displayError("[to] le serveur n'a pas accepté les données");
                                return;
                            }

                            new Timer() { public void run() {
                                pushOneClient(guid, leaveAfterPush);
                            } }.schedule(1000);
                            pushTries++;
                        } else {
                            if (cro.getExecuted() == 0) {
                                jdb.displayError("[ze] le server à refusé les données");
                                return;
                            }
                            jdb.setStatus("Sauvegardé.");
                            jdb.invalidateListWidget();
                            new Timer() { public void run() {
                                jdb.clearStatus();
                                if (leaveAfterPush)
                                    ClientWidget.this.jdb.popMode();
                            } }.schedule(500);
                            if (cd.getID() == null || cd.getID().equals("")) {
                                cd.setID(Integer.toString(cro.getSid()));
                                loadClientData();
                            }
                        }
                        new Timer() { public void run() { jdb.clearStatus(); } }.schedule(2000);
                    }
                });
        jdb.retrieve(url, rc);
    }
}
