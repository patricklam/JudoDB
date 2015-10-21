// -*-  indent-tabs-mode:nil; c-basic-offset:4; -*-
package ca.patricklam.judodb.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import ca.patricklam.judodb.client.Constants.Division;
import ca.patricklam.judodb.client.Constants.Grade;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.NamedFrame;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.http.client.URL;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.DropDownMenu;

import org.gwtbootstrap3.client.shared.event.HideEvent;
import org.gwtbootstrap3.client.shared.event.HideHandler;
import org.gwtbootstrap3.client.shared.event.ShowEvent;
import org.gwtbootstrap3.client.shared.event.ShowHandler;

public class ListWidget extends Composite {
    interface MyUiBinder extends UiBinder<Widget, ListWidget> {}
    public static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
    JudoDB jdb;

    private JsArray<ClientData> allClients;
    private HashMap<String, ClientData> cidToCD = new HashMap<String, ClientData>();
    /** A list of cours as retrieved from the server.
     * Must stay in synch with the ListBox field cours. */
    private List<CoursSummary> backingCours = new ArrayList<CoursSummary>();
    private List<EscompteSummary> escompteSummaries = new ArrayList<EscompteSummary>();
    private List<ProduitSummary> produitSummaries = new ArrayList<ProduitSummary>();
    private Set<String> clubsPresent = new HashSet<String>();

    private static final String PULL_ALL_CLIENTS_URL = JudoDB.BASE_URL + "pull_all_clients.php";
    private static final String PUSH_MULTI_CLIENTS_URL = JudoDB.BASE_URL + "push_multi_clients.php";
    private static final String CONFIRM_PUSH_URL = JudoDB.BASE_URL + "confirm_push.php";

    @UiField(provided=true) FormPanel listForm = new FormPanel(new NamedFrame("_"));

    @UiField Hidden multi;
    @UiField Hidden title;
    @UiField Hidden short_title;
    @UiField Hidden data;
    @UiField Hidden data_full;
    @UiField Hidden auxdata;

    @UiField Button sortirButton;
    @UiField DropDownMenu sortir;
    @UiField AnchorListItem sortir_pdf;
    @UiField AnchorListItem sortir_presences;
    @UiField AnchorListItem sortir_xls;
    @UiField AnchorListItem sortir_xls_complet;
    @UiField AnchorListItem sortir_ft303;
    @UiField AnchorListItem sortir_impot;

    @UiField Button return_to_main;

    @UiField Hidden club_id;

    @UiField Collapse ft303_controls;
    @UiField TextBox evt;
    @UiField TextBox date;

    @UiField HTMLPanel impot_controls;
    /*@UiField Anchor recalc;*/
    @UiField CheckBox prorata;

    @UiField HTMLPanel edit_controls;
    @UiField TextBox edit_date;

    @UiField Collapse filter_controls;
    @UiField ListBox division;
    @UiField ListBox grade_lower;
    @UiField ListBox grade_upper;

    @UiField ListBox cours;

    @UiField Grid results;

    @UiField Label nb;

    @UiField FormPanel listEditForm;
    @UiField Hidden guid_on_form;
    @UiField Hidden currentSessionField;
    @UiField Hidden dataToSave;
    private String guid;
    private int pushTries;

    @UiField ListBox sessionListBox;
    @UiField ButtonGroup dropDownUserClubsButtonGroup;
    @UiField Button dropDownUserClubsButton;
    @UiField DropDownMenu dropDownUserClubs;

    void selectClub(ClubSummary club) {
        jdb.selectClub(club);
        if (club == null)
            dropDownUserClubsButton.setText(jdb.TOUS);
        else
            dropDownUserClubsButton.setText(club.getClubText());

        if (jdb.isClubSelected()) {
            retrieveSessions(jdb.getSelectedClub());
            actuallyHandleClubChange();
        }
    }

    private void actuallyHandleClubChange() {
        if (!jdb.isClubSelected()) return;

        if (!gotSessions) {
            new Timer() {
                public void run() { actuallyHandleClubChange(); }
            }.schedule(100);
            return;
        }

        coursHandler.generateCoursList();
        showList();
    }

    class ListClubListHandlerFactory implements JudoDB.ClubListHandlerFactory {
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

    private class Columns {
        final static int CID = 0;
        final static int VERIFICATION = 1;
        final static int NOM = 2;
        final static int PRENOM = 3;
        final static int SEXE = 4;
        final static int GRADE = 5;
        final static int DATE_GRADE = 6;
        final static int TEL = 7;
        final static int JUDOQC = 8;
        final static int DDN = 9;
        final static int DIVISION = 10;
        final static int COURS_DESC = 11;
        final static int COURS_NUM = 12;
        final static int SESSION = 13;
        final static int DIVISION_SM = 14;
    }

    enum Mode {
        NORMAL, FT, EDIT, IMPOT
    };

    class CoursHandler implements ChangeHandler {
      public void onChange(ChangeEvent event) {
        showList();
      }

      void generateCoursList() {
        if (!jdb.isClubSelected() || !gotSessions) {
            new Timer() {
                public void run() { generateCoursList(); }
            }.schedule(100);
            return;
        }

        jdb.clearStatus();

        String session_seqno = requestedSessionNo();
        if (session_seqno == null)
            session_seqno = currentSession.getSeqno();

        String numero_club = null;
        if (jdb.isClubSelected())
            numero_club = jdb.getSelectedClub().getNumeroClub();

        jdb.pleaseWait();
        retrieveCours(session_seqno, numero_club);
      }
    }

    final HashMap<Mode, Widget[]> listModeVisibility = new HashMap<Mode, Widget[]>();

    private Mode mode = Mode.NORMAL;
    private boolean isFiltering;

    public String[] heads = new String[] {
        "No", "V", "Nom", "Prenom", "Sexe", "Grade", "DateGrade", "Tel", "JudoQC", "DDN", "Cat", "Cours", "", "Saisons", "Division"
    };
    private boolean[] sortable = new boolean[] {
            false, false, true, true, false, true, false, false, false, true, true, true, false, false, false
    };

    private HashMap<CheckBox, Boolean> originalVerifValues = new HashMap<CheckBox, Boolean>();
    private HashMap<ListBox, Integer> originalCours = new HashMap<ListBox, Integer>();
    private HashMap<TextBox, String> originalJudoQC = new HashMap<TextBox, String>();
    private HashMap<TextBox, String> originalGrades = new HashMap<TextBox, String>();
    private HashMap<TextBox, String> originalGradeDates = new HashMap<TextBox, String>();

    private CoursHandler coursHandler = new CoursHandler();

    public ListWidget(JudoDB jdb) {
        this.jdb = jdb;
        initWidget(uiBinder.createAndBindUi(this));
        listForm.addStyleName("hidden-print");
        jdb.populateClubList(true, dropDownUserClubs, new ListClubListHandlerFactory());
        selectClub(jdb.getSelectedClub());

        jdb.pleaseWait();
        switchMode(Mode.NORMAL);

        sessionListBox.addItem("Tous", "-1");
        sessionListBox.setSelectedIndex(0);

        division.addItem("Tous", "-1");
        for (Division c : Constants.DIVISIONS)
            if (!c.noire)
                division.insertItem(c.abbrev, c.abbrev, 1);

        for (int i = 0; i < Constants.GRADES.length; i++) {
            Grade g = Constants.GRADES[i];
            grade_lower.insertItem(g.name, String.valueOf(g.order), i);
        }
        grade_lower.insertItem("---", "", 0);
        grade_lower.setSelectedIndex(0);
        for (int i = 0; i < Constants.GRADES.length; i++) {
            Grade g = Constants.GRADES[i];
            grade_upper.insertItem(g.name, String.valueOf(g.order), i);
        }
        grade_upper.insertItem("---", "", 0);
        grade_upper.setSelectedIndex(0);

        edit_date.setValue(Constants.STD_DATE_FORMAT.format(new Date()));

        cours.addChangeHandler(coursHandler);

        sessionListBox.addChangeHandler(new ChangeHandler() {
            public void onChange(ChangeEvent e) { coursHandler.generateCoursList(); showList(); } });
        sortir_pdf.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent e) { collectDV(); clearFull(); submit("pdf"); } });
        sortir_presences.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent e) { collectDV(); clearFull(); submit("presences"); } });
        sortir_xls.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent e) { collectDV(); clearFull(); submit("xls"); } });
        sortir_xls_complet.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent e) { collectDV(); computeFull(); submit("xlsfull"); } });
        sortir_ft303.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent e) { if (makeFT()) submit("ft"); }});
        sortir_impot.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent e) { collectDV(); computeImpotMailMerge(); submit("impot"); } });

        filter_controls.addShowHandler(new ShowHandler() {
                @Override public void onShow(ShowEvent e) { isFiltering = true; showList(); } } );
        filter_controls.addHideHandler(new HideHandler() {
                @Override public void onHide(HideEvent e) { isFiltering = false; showList(); } } );

        division.addChangeHandler(new ChangeHandler() {
                @Override public void onChange(ChangeEvent e) { showList(); } });
        grade_lower.addChangeHandler(new ChangeHandler() {
                @Override public void onChange(ChangeEvent e) { showList(); } });
        grade_upper.addChangeHandler(new ChangeHandler() {
                @Override public void onChange(ChangeEvent e) { showList(); } });

/*      recalc.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent e) { recalc(); } }); */

        return_to_main.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent e) {
                ListWidget.this.jdb.clearStatus();
                ListWidget.this.jdb.popMode();
            }
        });

        listEditForm.setAction(PUSH_MULTI_CLIENTS_URL);

        retrieveSessions(jdb.getSelectedClub());
        coursHandler.generateCoursList();

        retrieveAllClients();
        sorts.add(3); sorts.add(2);
    }

    private void save() {
        // iterate through originalVerifValues and see which ones to generate
        // create a list of tuples of the form: 163, "verification", "0"; 164, "verification", "1"
        // we'll push to the server, which will update the services that match the cid and the current session.

        guid = UUID.uuid();
        guid_on_form.setValue(guid);
        currentSessionField.setValue(currentSession.getAbbrev());

        StringBuffer dv = new StringBuffer();
        for (int i = 0; i < results.getRowCount(); i++) {
            CheckBox cb = (CheckBox)results.getWidget(i, Columns.VERIFICATION);
            if (cb != null && cb.getValue() != originalVerifValues.get(cb)) {
                String cid = results.getText(i, Columns.CID);
                String v = cb.getValue() ? "1" : "0";
                dv.append(cid + ",Sverification,"+v+";");
            }
            Widget lbw = results.getWidget(i, Columns.COURS_DESC);
            if (lbw != null && lbw instanceof ListBox) {
                ListBox lb = (ListBox) lbw;
                if (lb.getSelectedIndex() != originalCours.get(lb)) {
                    String cid = results.getText(i, Columns.CID);
                    dv.append(cid + ",Scours,"+backingCours.get(lb.getSelectedIndex()).getId()+";");
                }
            }
            Widget jqw = results.getWidget(i, Columns.JUDOQC);
            if (jqw != null && jqw instanceof TextBox) {
                TextBox jq = (TextBox) jqw;
                if (!jq.getValue().equals(originalJudoQC.get(jq))) {
                    String cid = results.getText(i, Columns.CID);
                    dv.append(cid + ",Caffiliation,"+jq.getValue()+";");
                }
            }
            Widget gw = results.getWidget(i, Columns.GRADE);

            // if the grade changed (but not the date)---delete old grade
            // if the grade didn't change but the date changed---delete old grade/date, but use old grade (untruncated) as new grade
            // if the grade and date changed---don't delete anything
            if (gw != null && gw instanceof TextBox) {
                TextBox g = (TextBox) gw,
                        gd = (TextBox)results.getWidget(i, Columns.DATE_GRADE);
                boolean changed = false, deleteOld = false;
                String origGrade = originalGrades.get(g), ogs;
                String newGrade = g.getValue(), ngs;

                if (origGrade == null) origGrade = "";
                ogs = new String(origGrade); ngs = new String(newGrade);
                if (ogs.length() >= 3) ogs = ogs.substring(0, 3);
                if (ngs.length() >= 3) ngs = ngs.substring(0, 3);

                String ogd = originalGradeDates.get(gd);
                String ngd = Constants.stdToDbDate(gd.getValue());

                if (!ogs.equals(ngs) && ngd.equals(ogd))
                    { changed = true; deleteOld = true; }
                if (ogs.equals(ngs) && !ngd.equals(ogd))
                    { changed = true; ngs = origGrade; deleteOld = true; }
                if (!ogs.equals(ngs) && !ngd.equals(ogd))
                    { changed = true; }
                // otherwise, everything is equal, no changes.

                String cid = results.getText(i, Columns.CID);

                if (deleteOld)
                    dv.append(cid + ",!,"+origGrade+"|"+ogd+";");
                if (changed)
                    dv.append(cid + ",G,"+ngs+"|"+ngd+";");
            }
        }
        dataToSave.setValue(dv.toString());
        listEditForm.submit();

        pushTries = 0;
        new Timer() { public void run() {
            pushChanges(guid);
        } }.schedule(500);
    }

    private void reset() {
        retrieveAllClients();
    }

    private void recalc() {
        for (int i = 0; i < results.getRowCount(); i++) {
            ClientData cd = cidToCD.get(results.getText(i, Columns.CID));
            ServiceData sd = cd.getServiceFor(currentSession);
	    if (sd == null) continue;
            ClubSummary cs = jdb.getClubSummaryByID(sd.getClubID());
            ProduitSummary ps = CostCalculator.getApplicableProduit(sd, produitSummaries);;

            // XXX getPrix on ListWidget as well
            CostCalculator.recompute(currentSession, cd, sd, cs, backingCours, ps, true, null, escompteSummaries);
        }
    }

    private void addMetaData() {
        int c = cours.getSelectedIndex();
        title.setValue("");
        if (c > 0 || isFiltering) {
            multi.setValue("0");
            if (isFiltering) {
                title.setValue("");
                if (c != -1) {
                    short_title.setValue(backingCours.get(c-1).getShortDesc());
                }
                else {
                    short_title.setValue("");
                }

                String st = "";
                // TODO: add filters to title
                title.setValue(st);
            } else {
                short_title.setValue(backingCours.get(c-1).getShortDesc());
            }
        } else {
            // all classes
            String tt = "", shortt = "";
            multi.setValue("1");

            // TODO renumber the cours so as to eliminate blank spots
            int maxId = -1;
            for (CoursSummary cc : backingCours) {
                if (Integer.parseInt(cc.getId()) > maxId)
                    maxId = Integer.parseInt(cc.getId());
            }

            if (maxId > 0) {
                String[] shortDescs = new String[maxId];
                for (CoursSummary cc : backingCours) {
                    shortDescs[Integer.parseInt(cc.getId())] = cc.getShortDesc();
                }

                for (int i = 0; i < maxId; i++)
                    shortt += shortDescs[i] + "|";
            }
            title.setValue(tt);
            short_title.setValue(shortt);
        }
    }

    private void collectDV() {
        String dv = "|";
        for (int i = 1; i < results.getRowCount(); i++) {
            for (int j = 0; j < results.getColumnCount(); j++) {
                if (j == Columns.VERIFICATION) continue;
                dv += results.getText(i, j) + "|";
            }
            dv += "*|";
        }
        data.setValue(dv);
    }

    private void clearFull() {
        data_full.setValue("");
    }

    private void computeFull() {
        String dv = "";
        for (int i = 0; i < allClients.length(); i++) {
            ClientData cd = allClients.get(i);
            if (!sessionFilter(cd)) continue;

            dv += toDVFullString(cd) + "*";
        }
        data_full.setValue(dv);
    }

    private String toDVFullString(ClientData cd) {
        String dv = "";

        dv += cd.getID() + "|";
        dv += cd.getNom() + "|";
        dv += cd.getPrenom() + "|";
        dv += cd.getSexe() + "|";
        dv += cd.getJudoQC() + "|";
        dv += cd.getDDNString() + "|";
        dv += cd.getDivision(requestedSession().getYear()).abbrev + "|";
        dv += cd.getCourriel() + "|";
        dv += cd.getAdresse() + "|";
        dv += cd.getVille() + "|";
        dv += cd.getCodePostal() + "|";
        dv += cd.getTel() + "|";
        dv += cd.getCarteResident() + "|";
        dv += cd.getTelContactUrgence() + "|";
        dv += cd.getMostRecentGrade().getGrade() + "|";
        dv += cd.getMostRecentGrade().getDateGrade() + "|";
        ServiceData sd = cd.getServiceFor(currentSession);
        if (sd != null && !sd.getCours().equals("")) {
            ClubSummary cs = jdb.getClubSummaryByID(sd.getClubID());
            ProduitSummary ps = CostCalculator.getApplicableProduit(sd, produitSummaries);;
            // XXX getPrix on ListWidget as well
            CostCalculator.recompute(currentSession, cd, sd, cs, backingCours, ps, prorata.getValue(), null, escompteSummaries);
            // this is potentially slow; use a hash map instead.
            for (CoursSummary cc : backingCours) {
                if (cc.getId().equals(sd.getCours()))
                    dv += cc.getShortDesc();
            }
        }
        dv += "|";
        if (sd != null) {
            dv += sd.getFrais();
        }
        dv += "|";
        return dv;
    }

   private void computeImpotMailMerge() {
        String dv = "";
        ArrayList<ClientData> filteredClients = new ArrayList<ClientData>();

        for (int i = 0; i < allClients.length(); i++) {
            ClientData cd = allClients.get(i);
            if (!sessionFilter(cd)) continue;

            Division d = cd.getDivision(requestedSession().getYear());
            if (d.abbrev.equals("S") || d.aka.equals("S")) continue;
            filteredClients.add(cd);
        }

        Collections.sort(filteredClients, new Comparator<ClientData>() {
            public int compare(ClientData x, ClientData y) {
                if (!x.getNom().equals(y.getNom()))
                    return x.getNom().compareTo(y.getNom());
                return x.getPrenom().compareTo(y.getPrenom());
            }
        });

        for (ClientData cd : filteredClients) {
            dv += toDVImpot(cd) + "*";
        }

        data_full.setValue(dv);
    }

   private String toDVImpot(ClientData cd) {
       String dv = "";

       dv += cd.getID() + "|";
       dv += cd.getPrenom() + " " + cd.getNom() + "|";
       dv += cd.getDDNString() + "|";
       ServiceData sd = cd.getServiceFor(currentSession);
       ClubSummary cs = jdb.getClubSummaryByID(sd.getClubID());
       ProduitSummary ps = CostCalculator.getApplicableProduit(sd, produitSummaries);;
       // XXX clubPrix on ListWidget
       CostCalculator.recompute(currentSession, cd, sd, cs, backingCours, ps, prorata.getValue(), null, escompteSummaries);
       if (sd != null) {
           dv += Constants.currencyFormat.format(Double.parseDouble(sd.getFrais()));
       }
       dv += "|";
       return dv;
   }


    private void submit(String act) {
        addMetaData();
        listForm.setAction(JudoDB.BASE_URL+"listes"+act+".php");
        listForm.submit();
    }

    private boolean makeFT() {
        StringBuffer dv = new StringBuffer("");
        for (int i = 1; i < results.getRowCount(); i++) {
            CheckBox cb = (CheckBox)results.getWidget(i, Columns.VERIFICATION);
            if (cb != null && cb.getValue()) {
                ClientData cd = cidToCD.get(results.getText(i, Columns.CID));

                StringBuffer post = new StringBuffer();
                ListBox w = (ListBox)(results.getWidget(i, Columns.DIVISION_SM));
                if (w != null)
                    post.append(w.getValue(w.getSelectedIndex()));
                post.append("|");
                dv.append(toDVFullString(cd) + post + "*");
            }
        }
        data.setValue("");
        data_full.setValue(dv.toString());
        if (jdb.getSelectedClubID() == null) {
            jdb.setStatus("Veuillez selectionner un club.");
            new Timer() { public void run() { jdb.clearStatus(); } }.schedule(2000);
            return false;
        }
        ClubSummary cs = jdb.getClubSummaryByID(jdb.getSelectedClubID());
        auxdata.setValue(cs.getNomShort() + "|" + cs.getNumeroClub());
        return !dv.equals("");
    }

    public String requestedSessionNo() {
        if (sessionListBox == null || sessionListBox.getSelectedIndex() == -1) return null;
        return sessionListBox.getValue(sessionListBox.getSelectedIndex());
    }

    public SessionSummary requestedSession() {
        String requestedSessionNo = requestedSessionNo();
        if (requestedSessionNo == null) return null;
	for (SessionSummary s : sessions) {
	    if (s.getSeqno().equals(requestedSessionNo))
		return s;
	}
	return null;
    }

    private boolean clubServiceFilter(ServiceData sd) {
        if (jdb.getSelectedClubID() == null) return true;
	if (sd == null) return false;
        return jdb.getSelectedClubID().equals(sd.getClubID());
    }

    /* unlike the other filters, this one can't be disabled */
    private boolean clubFilter(ClientData cd) {
        SessionSummary rs = requestedSession();
        if (rs == null) {
            for (int i = 0; i < cd.getServices().length(); i++) {
                if (clubServiceFilter(cd.getServices().get(i)))
                    return true;
            }
            return false;
        }
        ServiceData sd = cd.getServiceFor(rs);
        return clubServiceFilter(sd);
    }

    private boolean sessionFilter(ClientData cd) {
        SessionSummary rs = requestedSession();
        if (rs == null) return true;

        return cd.getServiceFor(rs) != null;
    }

    private boolean coursFilter(ClientData cd) {
        String selectedCours = cours.getValue(cours.getSelectedIndex());
        if (selectedCours.equals("-1"))
            return true;

        if (selectedCours.equals(cd.getServiceFor(requestedSession()).getCours()))
            return true;

        return false;
    }

    private boolean divisionFilter(ClientData cd) {
        String selectedDivision = division.getValue(division.getSelectedIndex());
        if (selectedDivision.equals("-1"))
            return true;

        Division c = cd.getDivision(requestedSession().getYear());
        if (selectedDivision.equals(c.abbrev) || selectedDivision.equals(c.aka))
            return true;
        return false;
    }

    private boolean gradeFilter(ClientData cd) {
        String lc = grade_lower.getValue(grade_lower.getSelectedIndex());
        String uc = grade_upper.getValue(grade_upper.getSelectedIndex());
        boolean emptyLC = lc.equals(""), emptyUC = uc.equals("");

        Grade g = Constants.stringToGrade(cd.getGrade());
        if (g != null) {
            int lower_constraint = emptyLC ? 0 : Integer.parseInt(lc);
            int upper_constraint = emptyUC ? 0 : Integer.parseInt(uc);
            return (emptyLC || lower_constraint <= g.order) &&
                     (emptyUC || g.order <= upper_constraint);
        } else {
            return emptyLC && emptyUC;
        }
    }

    public boolean filter(ClientData cd) {
        if (!sessionFilter(cd))
            return false;

        if (!coursFilter(cd))
            return false;

        if (isFiltering) {
            if (!divisionFilter(cd))
                return false;
            if (!gradeFilter(cd))
                return false;
        }

        return true;
    }

    public void clearX() {
        for (int i = 0; i < results.getRowCount(); i++) {
            CheckBox cb = (CheckBox)results.getWidget(i, Columns.VERIFICATION);
            if (cb != null && cb.getValue()) {
                cb.setValue(false);
            }
        }
    }

    private ArrayList<Integer> sorts = new ArrayList<Integer>();

    @SuppressWarnings("deprecation")
    public void showList() {
        if (cours.getItemCount() == 0) return;
	if (!gotSessions) return;

        boolean all = "-1".equals(cours.getValue(cours.getSelectedIndex()));
        String requestedSessionNo = requestedSessionNo();
        final SessionSummary rs = requestedSession();
        int count = 0, curRow;
        final ArrayList<ClientData> filteredClients = new ArrayList<ClientData>();

        final class SortClickHandler implements ClickHandler {
            int col;
            SortClickHandler(int col) { this.col = col; }

            public void onClick(ClickEvent e) {
                if (sorts.get(sorts.size()-1).equals(col) ||
                        sorts.get(sorts.size()-1).equals(-col)) {
                    int q = sorts.get(sorts.size()-1);
                    sorts.remove(sorts.size()-1);
                    sorts.add(-q);
                } else {
                    sorts.remove(new Integer(col));
                    sorts.add(col);
                }
                showList();
            }
        }

        // two passes: 1) count; 2) populate the grid
        if (allClients == null) return;

        for (int i = 0; i < allClients.length(); i++) {
            ClientData cd = allClients.get(i);
            cidToCD.put(cd.getID(), cd);
            if (!clubFilter(cd)) continue;
            if (!filter(cd)) continue;

            filteredClients.add(cd);
            count++;
        }

        for (final Integer col : sorts) {
            final int colSign = (col < 0) ? -1 : 1;
            Collections.sort(filteredClients, new Comparator<ClientData>() {
                public int compare(ClientData x, ClientData y) {
                    switch (col * colSign) {
                    case 2:
                        return colSign * x.getNom().compareToIgnoreCase(y.getNom());
                    case 3:
                        return colSign * x.getPrenom().compareToIgnoreCase(y.getPrenom());
                    case 5:
                        return colSign * x.getGrade().compareTo(y.getGrade());
                    case 9:
                        return colSign * x.getDDN().compareTo(y.getDDN());
                    case 10:
                        int xc = x.getDivision(rs.getYear()).years_ago;
                        int yc = y.getDivision(rs.getYear()).years_ago;
                        if (xc == 0) xc = Integer.MAX_VALUE;
                        if (yc == 0) yc = Integer.MAX_VALUE;
                        return colSign * (xc - yc);
                    case 11:
                        ServiceData xsd = x.getServiceFor(rs);
                        int xcours = xsd != null ? Integer.parseInt(xsd.getCours()) : -1;

                        ServiceData ysd = y.getServiceFor(rs);
                        int ycours = ysd != null ? Integer.parseInt(ysd.getCours()) : -1;
                        return colSign * (xcours - ycours);
                    }
                    return 0;
                }
            });
        }

        results.resize(count+1, 15);

        if (mode==Mode.FT)
            heads[Columns.VERIFICATION] = "FT";
        else
            heads[Columns.VERIFICATION] = "V";

        boolean[] visibility = new boolean[] {
                true, mode==Mode.EDIT || mode==Mode.FT, true, true, mode==Mode.EDIT || mode==Mode.FT,
                true, true, true, true, true, true, mode==Mode.EDIT || all, false,
                requestedSessionNo == null, mode==Mode.FT
        };

        for (int i = 0; i < heads.length; i++) {
            if (visibility[i]) {
                if (sortable[i]) {
                    Anchor a = new Anchor(heads[i]);
                    a.addClickHandler(new SortClickHandler(i));
                    results.setWidget(0, i, a);
                }
                else
                    results.setText(0, i, heads[i]);
                results.getCellFormatter().setStyleName(0, i, "list-th");
                results.getCellFormatter().setVisible(0, i, true);
            } else {
                results.setText(0, i, "");
                results.getCellFormatter().setVisible(0, i, false);
            }
        }

        curRow = 1;
        clubsPresent.clear();
        for (ClientData cd : filteredClients) {
            String grade = cd.getGrade();
            if (grade != null && grade.length() >= 3) grade = grade.substring(0, 3);

            ServiceData sd = cd.getServiceFor(rs);
            int cours;
            if (sd == null) {
                cours = -1;
            } else {
                cours = Integer.parseInt(sd.getCours());
                clubsPresent.add(sd.getClubID());
            }

            Anchor nomAnchor = new Anchor(cd.getNom()), prenomAnchor = new Anchor(cd.getPrenom());
            ClickHandler c = jdb.new EditClientHandler(jdb.getSelectedClub(), Integer.parseInt(cd.getID()));
            nomAnchor.addClickHandler(c);
            prenomAnchor.addClickHandler(c);

            results.setText(curRow, Columns.CID, cd.getID());
            results.setWidget(curRow, Columns.NOM, nomAnchor);
            results.setWidget(curRow, Columns.PRENOM, prenomAnchor);
            results.setText(curRow, Columns.SEXE, cd.getSexe());

            results.setText(curRow, Columns.GRADE, "");
            results.setText(curRow, Columns.DATE_GRADE, "");
            if (mode == Mode.EDIT) {
                TextBox g = new TextBox();
                final TextBox gd = new TextBox();
                g.setValue(grade); g.setWidth("3em");
                g.addChangeHandler(new ChangeHandler() {
                    public void onChange(ChangeEvent e) {
                        String gdv = gd.getValue();
                        if (gdv.equals("") || gdv.equals(Constants.STD_DUMMY_DATE))
                            gd.setValue(edit_date.getValue());
                    }
                });
                results.setWidget(curRow, Columns.GRADE, g);
                originalGrades.put(g, cd.getGrade());
                gd.setValue(Constants.dbToStdDate(cd.getDateGrade())); gd.setWidth("6em");
                results.setWidget(curRow, Columns.DATE_GRADE, gd);
                originalGradeDates.put(gd, cd.getDateGrade());
            } else if (grade != null && !grade.equals("")) {
                results.setText(curRow, Columns.GRADE, grade);
                results.setText(curRow, Columns.DATE_GRADE, Constants.dbToStdDate(cd.getDateGrade()));
            }
            results.setText(curRow, Columns.TEL, cd.getTel());
            if (mode == Mode.EDIT) {
                TextBox jqc = new TextBox(); jqc.setValue(cd.getJudoQC()); jqc.setWidth("4em");
                results.setWidget(curRow, Columns.JUDOQC, jqc);
                originalJudoQC.put(jqc, cd.getJudoQC());
            } else {
                results.setText(curRow, Columns.JUDOQC, cd.getJudoQC());
            }
            Date ddns = cd.getDDN();
            results.setText(curRow, Columns.DDN, ddns == null ? Constants.STD_DUMMY_DATE : Constants.STD_DATE_FORMAT.format(ddns));
	    String div = "";
	    if (rs != null || currentSession != null)
		div = cd.getDivision((rs == null ? currentSession : rs).getYear()).abbrev;
	    results.setText(curRow, Columns.DIVISION, div);

            if (visibility[Columns.VERIFICATION]) {
                CheckBox cb = new CheckBox();
                cb.addStyleName("hidden-print");
                results.setWidget(curRow, Columns.VERIFICATION, cb);
                if (mode==Mode.EDIT) {
                    cb.setValue(sd.getVerification());
                    originalVerifValues.put(cb, sd.getVerification());
                }
            }

            if (visibility[Columns.DIVISION_SM] && (Integer.parseInt(currentSession.getYear()) - (cd.getDDN().getYear() + 1900)) > Constants.VETERAN) {
                ListBox d = new ListBox();
                d.addItem("Senior", "S");
                d.addItem("Masters", "M");
                results.setWidget(curRow, Columns.DIVISION_SM, d);
            } else {
                results.clearCell(curRow, Columns.DIVISION_SM);
            }

            results.setText(curRow, Columns.COURS_DESC, "");
            results.setText(curRow, Columns.COURS_NUM, "");

            if (mode == Mode.EDIT) {
                ListBox w = newCoursWidget(sd.getClubID(), cours);
                results.setWidget(curRow, Columns.COURS_DESC, w);
                results.setText(curRow, Columns.COURS_NUM, Integer.toString(cours));
                originalCours.put(w, cours);
            } else if (cours != -1) {
                results.setText(curRow, Columns.COURS_DESC, getShortDescForCoursId(cours));
                results.setText(curRow, Columns.COURS_NUM, Integer.toString(cours));
            }

            if (requestedSessionNo == null) {
                results.setText(curRow, Columns.SESSION, cd.getAllActiveSaisons());
            } else {
                results.setText(curRow, Columns.SESSION, "");
            }

            for (int j = 0; j < visibility.length; j++)
                results.getCellFormatter().setVisible(curRow, j, visibility[j]);

            if (curRow % 2 == 1)
                results.getRowFormatter().setStyleName(curRow, "darkBG");

            curRow++;
        }

        nb.setText("Nombre inscrit: "+count);
    }

    private ListBox newCoursWidget(String clubId, int coursId) {
        ListBox lb = new ListBox();
        int matching_index = -1;
        int i = 0;
        String ciString = Integer.toString(coursId);

        for (CoursSummary cs : backingCours) {
            if (clubId.equals(cs.getClubId())) {
                lb.addItem(cs.getShortDesc(), cs.getId());
                if (cs.getId().equals(ciString))
                    matching_index = i;
                i++;
            }
        }
        if (matching_index != -1)
            lb.setSelectedIndex(matching_index);
        return lb;
    }

    public void switchMode(Mode m) {
        this.mode = m;

        originalVerifValues.clear();

        if (cours.getItemCount() > 0)
            showList();
    }

    private void loadCours(JsArray<CoursSummary> coursArray) {
        backingCours.clear();
        cours.setVisibleItemCount(1);
        cours.clear();
        cours.addItem("Tous", "-1");
        for (int i = 0; i < coursArray.length(); i++) {
            CoursSummary c = coursArray.get(i);
            cours.addItem(c.getShortDesc(), c.getId());
            backingCours.add(c);
        }
    }

    private void loadEscomptes(JsArray<EscompteSummary> escompteArray) {
        escompteSummaries.clear();
        escompteSummaries.add(Constants.EMPTY_ESCOMPTE);
        for (int i = 0; i < escompteArray.length(); i++) {
            EscompteSummary e = escompteArray.get(i);
            if (e.getClubId().equals("0") || e.getClubId().equals(jdb.getSelectedClubID())) {
                escompteSummaries.add(e);
            }
        }
    }

    private void loadProduits(JsArray<ProduitSummary> produitArray) {
        produitSummaries.clear();
        produitSummaries.add(Constants.EMPTY_PRODUIT);
        for (int i = 0; i < produitArray.length(); i++) {
            ProduitSummary e = produitArray.get(i);
            if (e.getClubId().equals(jdb.getSelectedClubID())) {
                produitSummaries.add(e);
            }
        }
    }

    private String getShortDescForCoursId(int coursId) {
        String cidString = Integer.toString(coursId);
        for (CoursSummary c : backingCours) {
            if (c.getId().equals(cidString))
                return c.getShortDesc();
        }
        return "inconnu";
    }

    /* --- sessions --- */
    List<SessionSummary> sessions = new ArrayList<SessionSummary>();
    SessionSummary currentSession;

    void populateSessions(JsArray<SessionSummary> ss) {
	sessions.clear();
	sessionListBox.clear();
	currentSession = null;
	Date today = new Date();
	TreeSet<SessionSummary> sss = new TreeSet<SessionSummary>();
	for (int i = 0; i < ss.length(); i++) {
	    SessionSummary s = ss.get(i);
	    sessions.add(s);

	    try {
		Date inscrBegin = Constants.DB_DATE_FORMAT.parse(s.getFirstSignupDate());
		Date inscrEnd = Constants.DB_DATE_FORMAT.parse(s.getLastSignupDate());
		if (today.after(inscrBegin) && today.before(inscrEnd)) {
		    currentSession = s; continue;
		}
	    } catch (IllegalArgumentException e) {}
	    sss.add(s);
	}

	sessionListBox.insertItem("Tous", "-1", 0);
	for (SessionSummary s : sss) {
	    sessionListBox.insertItem(s.getAbbrev(), s.getSeqno(), 1);
        }
	if (currentSession != null)
	    sessionListBox.insertItem(currentSession.getAbbrev(), currentSession.getSeqno(), 0);
	sessionListBox.setSelectedIndex(0);
    }
    /* --- end sessions --- */

    /* --- network functions --- */
    private boolean gotSessions = false;
    public void retrieveSessions(ClubSummary cs) {
        if (cs == null) return;

        gotSessions = false;
        String url = JudoDB.PULL_SESSIONS_URL;
        url += "?club_id="+cs.getId();
        RequestCallback rc =
            jdb.createRequestCallback(new JudoDB.Function() {
                    public void eval(String s) {
                        gotSessions = true;
                        populateSessions(JsonUtils.<JsArray<SessionSummary>>safeEval(s));
			jdb.clearStatus();
                    }
                });
        jdb.retrieve(url, rc);
    }

    private boolean gotCours = false;
    public void retrieveCours(final String session_seqno, final String numero_club) {
        if (!jdb.isClubSelected() || !gotSessions) {
            new Timer() {
                public void run() { retrieveCours(session_seqno, numero_club); }
            }.schedule(100);
            return;
        }

        backingCours.clear();
        String url = JudoDB.PULL_CLUB_COURS_URL;
        url += "?session_seqno="+session_seqno;
        if (numero_club != null) url += "&numero_club="+numero_club;
        RequestCallback rc =
            jdb.createRequestCallback(new JudoDB.Function() {
                    public void eval(String s) {
                        gotCours = true;
                        loadCours(JsonUtils.<JsArray<CoursSummary>>safeEval(s));
                        ListWidget.this.showList();
                    }
                });
        jdb.retrieve(url, rc);
    }

    private boolean gotEscomptes = false;
    public void retrieveEscomptes() {
        if (jdb.getSelectedClubID() == null || !gotSessions) {
            new Timer() {
                public void run() { retrieveEscomptes(); }
            }.schedule(100);
            return;
        }

        escompteSummaries.clear();
        String url = JudoDB.PULL_ESCOMPTE_URL +
            "?club_id=" + jdb.getSelectedClubID();
        RequestCallback rc =
            jdb.createRequestCallback(new JudoDB.Function() {
                    public void eval(String s) {
                        loadEscomptes
                            (JsonUtils.<JsArray<EscompteSummary>>safeEval(s));
                    }
                });
        jdb.retrieve(url, rc);
    }

    private boolean gotProduits = false;
    public void retrieveProduits() {
        if (jdb.getSelectedClubID() == null || !gotSessions) {
            new Timer() {
                public void run() { retrieveProduits(); }
            }.schedule(100);
            return;
        }

        produitSummaries.clear();
        String url = JudoDB.PULL_PRODUIT_URL +
            "?club_id=" + jdb.getSelectedClubID();
        RequestCallback rc =
            jdb.createRequestCallback(new JudoDB.Function() {
                    public void eval(String s) {
                        loadProduits
                            (JsonUtils.<JsArray<ProduitSummary>>safeEval(s));
                    }
                });
        jdb.retrieve(url, rc);
    }

    public void retrieveAllClients() {
        String url = PULL_ALL_CLIENTS_URL;
        RequestCallback rc =
            jdb.createRequestCallback(new JudoDB.Function() {
                    public void eval(String s) {
                        ListWidget.this.allClients =
                            (JsonUtils.<JsArray<ClientData>>safeEval(s));
                        new Timer() {
                            public void run() {
                                if (gotCours) {
                                    ListWidget.this.showList();
                                    cancel();
                                } else {
                                    scheduleRepeating(100);
                                }
                            } } .scheduleRepeating(10);
                    }
                });
        jdb.retrieve(url, rc);
    }

    public void pushChanges(final String guid) {
        String url = CONFIRM_PUSH_URL + "?guid=" + guid;
        RequestCallback rc =
            jdb.createRequestCallback(new JudoDB.Function() {
                    public void eval(String s) {
                        ConfirmResponseObject cro =
                            JsonUtils.<ConfirmResponseObject>safeEval(s);
                        String rs = cro.getResult();
                        if (rs.equals("NOT_YET")) {
                            if (pushTries >= 3) {
                                jdb.displayError("le serveur n'a pas accepté les données");
                                return;
                            }

                            new Timer() { public void run() {
                                pushChanges(guid);
                            } }.schedule(2000);
                            pushTries++;
                        } else {
                            jdb.setStatus("Sauvegardé.");
                        }
                        new Timer() { public void run() { jdb.clearStatus(); } }.schedule(2000);
                    }
                });
        jdb.retrieve(url, rc);
    }
}
