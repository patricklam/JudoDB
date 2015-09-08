package ca.patricklam.judodb.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.ResizeLayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.Style.Unit;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

public class ConfigWidget extends Composite {
    interface MyUiBinder extends UiBinder<Widget, ConfigWidget> {}
    public static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiField FlowPanel sessionTab;
    @UiField FlowPanel coursTab;
    @UiField FlowPanel prixTab;
    @UiField FormPanel configEditForm;
    @UiField Hidden current_session;
    @UiField Hidden dataToSave;
    @UiField Hidden guid_on_form;
    private String guid;
    private int pushTries;

    private final JudoDB jdb;
    private static final String PUSH_MULTI_CLIENTS_URL = JudoDB.BASE_URL + "push_multi_clients.php";
    private static final String CONFIRM_PUSH_URL = JudoDB.BASE_URL + "confirm_push.php";

    CellTable sessions;
    private static final List<SessionSummary> sessionData = new ArrayList<SessionSummary>();

    CellTable cours;
    private static final List<CoursSummary> coursData = new ArrayList<CoursSummary>();

    CellTable prix;
    private static final List<ClubPrix> prixData = new ArrayList<ClubPrix>();

    // private static final String PULL_CONFIG_URL = JudoDB.BASE_URL + "pull_config.php";
    // private static final String PUSH_CONFIG_URL = JudoDB.BASE_URL + "push_config.php";

    // useful URLs: http://www.filsa.net/2010/01/23/more-on-tablayoutpanel/
    // http://www.filsa.net/2010/01/21/gwt-notes-tablayoutpanel/

    // not done yet: for each club,
    // montants--namely, (should theoretically be stored with the session)
    // passeport judo QC [5]
    // frais non-resident [5]
    // penalty for prorata (constant factor, in addition to percent of cost) [5]

    // misc:
    // age veteran [35]

    // not needed for now:
    // for each division:
    // name [Junior]
    // abbrev [U20N]
    // years_ago [20]
    // noire [true]
    // aka [U20]

    // for each escompte:
    // seqno
    // name [2e membre]
    // percent [10]

    @UiField ListBox dropDownUserClubs;
    private ClubListHandler clHandler = new ClubListHandler();

    class ClubListHandler implements ChangeHandler {
      public void onChange(ChangeEvent e) {
        jdb.selectedClub = dropDownUserClubs.getSelectedIndex();
        retrieveSessions(Integer.toString(jdb.selectedClub));
        ClubSummary cs = jdb.getClubSummaryByID(jdb.getSelectedClubID());
        if (cs != null) {
            retrieveCours(cs.getNumeroClub());
            retrieveClubPrix(cs.getNumeroClub());
        }
        populateCurrentClub();
      }
    }

    public ConfigWidget(JudoDB jdb) {
        this.jdb = jdb;
        initWidget(uiBinder.createAndBindUi(this));
        jdb.pleaseWait();
        jdb.populateClubList(true, dropDownUserClubs);
        retrieveSessions(Integer.toString(jdb.selectedClub));
        ClubSummary cs = jdb.getClubSummaryByID(jdb.getSelectedClubID());
        if (cs != null) {
            retrieveCours(cs.getNumeroClub());
            retrieveClubPrix(cs.getNumeroClub());
        }

        dropDownUserClubs.addChangeHandler(clHandler);

	initializeSessionTable();
	populateCurrentClub();
	sessionTab.add(sessions);

	initializeCoursTable();
	coursTab.add(cours);
	populateCours(coursData);

	initializePrixTable();
	prixTab.add(prix);
	populatePrix(prixData);

        configEditForm.setAction(PUSH_MULTI_CLIENTS_URL);
    }

    private static final ProvidesKey<SessionSummary> SESSION_KEY_PROVIDER =
	new ProvidesKey<SessionSummary>() {
        @Override
        public Object getKey(SessionSummary item) {
	    return item.getSeqno();
        }
    };

    private class ColumnFields {
	public ColumnFields(String key, String name, int width, Unit widthUnits) {
	    this.key = key;
	    this.name = name;
	    this.width = width;
	    this.widthUnits = widthUnits;
	}

	public String key;
	public String name;
	public int width;
	public Unit widthUnits;
    }

    /* --- session table --- */
    private static final String DELETE_SESSION_KEY = "DELETE";

    private final ColumnFields NAME_COLUMN = new ColumnFields("name", "Nom", 10, Unit.EM),
	ABBREV_COLUMN = new ColumnFields("abbrev", "Abbr", 4, Unit.EM),
	YEAR_COLUMN = new ColumnFields("year", "Année", 5, Unit.EM),
	SEQNO_COLUMN = new ColumnFields("seqno", "no seq", 3, Unit.EM),
	LINKED_SEQNO_COLUMN = new ColumnFields("linked_seqno", "seq alt", 3, Unit.EM),
	FIRST_CLASS_COLUMN = new ColumnFields("first_class_date", "début cours" , 10, Unit.EM),
	FIRST_SIGNUP_COLUMN = new ColumnFields("first_signup_date", "début inscription", 10, Unit.EM),
	LAST_CLASS_COLUMN = new ColumnFields("last_class_date", "fin cours", 10, Unit.EM),
	LAST_SIGNUP_COLUMN = new ColumnFields("last_signup_date", "fin inscription", 10, Unit.EM),
	DELETE_SESSION_COLUMN = new ColumnFields(DELETE_SESSION_KEY, "", 1, Unit.EM);

    private List<ColumnFields> perClubColumns = Collections.unmodifiableList(Arrays.asList(FIRST_CLASS_COLUMN, FIRST_SIGNUP_COLUMN, LAST_CLASS_COLUMN, LAST_SIGNUP_COLUMN, DELETE_SESSION_COLUMN));

    private static final String BALLOT_X = "\u2717";

    private Column<SessionSummary, String> addSessionColumn(final CellTable t, final ColumnFields c, final boolean editable) {
	final Cell<String> cell = editable ? new EditTextCell() : new TextCell();
	Column<SessionSummary, String> newColumn = new Column<SessionSummary, String>(cell) {
	    public String getValue(SessionSummary object) {
		if (c.key.equals(DELETE_SESSION_KEY)) {
		    return BALLOT_X;
		}
		return object.get(c.key);
	    }
	};
	sessions.addColumn(newColumn, c.name);
	newColumn.setFieldUpdater(new FieldUpdater<SessionSummary, String>() {
		@Override
		public void update(int index, SessionSummary object, String value) {
		    if (c.key == null) return;
		    object.set(c.key, value);
		    if (perClubColumns.contains(c)) {
			if (object.getId().equals("-1")) {
			    refreshSessions = true;
			    pushEdit("-1,F" + c.key + "," + value + "," +
				     Integer.toString(jdb.selectedClub) + "," + object.getSeqno() + ";");
			} else {
			    pushEdit("-1,f" + c.key + "," + value + "," +
				     object.getClub() + "," + object.getId() + ";");
			}
		    } else {
			pushEdit("-1,e" + c.key + "," + value + "," + object.getSeqno() + ";");
		    }
		    updateSessionToNameMapping();
		    t.redraw();
		}
	    });
	sessions.setColumnWidth(newColumn, c.width, c.widthUnits);
	return newColumn;
    }

    void initializeSessionTable() {
	sessions = new CellTable<SessionSummary>(SESSION_KEY_PROVIDER);
	sessions.setWidth("60em", true);

	initializeSessionColumns();
    }

    private void pushEdit(String dv) {
        guid = UUID.uuid();
        guid_on_form.setValue(guid);
	current_session.setValue("A00");
        dataToSave.setValue(dv.toString());
        configEditForm.submit();

        pushTries = 0;
        new Timer() { public void run() {
            pushChanges(guid);
        } }.schedule(500);

    }

    final private static String ADD_SESSION_VALUE = "[ajouter session]";
    void addAddSessionSession() {
	int maxSeqno = -1;
	for (SessionSummary s : sessionData) {
	    if (Integer.parseInt(s.getSeqno()) > maxSeqno)
		maxSeqno = Integer.parseInt(s.getSeqno());
	}

	SessionSummary addNewSession =
	    JsonUtils.<SessionSummary>safeEval
	    ("{\"seqno\":\""+(maxSeqno+1)+"\",\"name\":\""+ADD_SESSION_VALUE+"\"}");
	addNewSession.setAbbrev("");
	addNewSession.setYear("");
	addNewSession.setLinkedSeqno("");
	sessionData.add(addNewSession);
    }

    void initializeSessionColumns() {
	while (sessions.getColumnCount() > 0)
	    sessions.removeColumn(0);

	// name is special: handle inserting a new session
	final Column<SessionSummary, String> nameColumn =
	    addSessionColumn(sessions, NAME_COLUMN, !jdb.isClubSelected());
	nameColumn.setFieldUpdater(new FieldUpdater<SessionSummary, String>() {
		@Override
		public void update(int index, SessionSummary object, String value) {
		    if (object.get(NAME_COLUMN.key).equals(ADD_SESSION_VALUE)) {
			pushEdit("-1,E" + NAME_COLUMN.key + "," + value + "," + object.getSeqno() + ";");
			addAddSessionSession();
		    } else {
			pushEdit("-1,e" + NAME_COLUMN.key + "," + value + "," + object.getSeqno() + ";");
		    }
		    object.set(NAME_COLUMN.key, value);

		    sessions.setRowData(sessionData);
		    sessions.redraw();
		}
	    });

	addSessionColumn(sessions, ABBREV_COLUMN, !jdb.isClubSelected());
	addSessionColumn(sessions, YEAR_COLUMN, !jdb.isClubSelected());
	addSessionColumn(sessions, SEQNO_COLUMN, false);
	addSessionColumn(sessions, LINKED_SEQNO_COLUMN, !jdb.isClubSelected());
	if (jdb.isClubSelected()) {
	    addSessionColumn(sessions, FIRST_CLASS_COLUMN, true);
	    addSessionColumn(sessions, FIRST_SIGNUP_COLUMN, true);
	    addSessionColumn(sessions, LAST_CLASS_COLUMN, true);
	    addSessionColumn(sessions, LAST_SIGNUP_COLUMN, true);
	}
	addSessionColumn(sessions, DELETE_SESSION_COLUMN, false);
    }

    private void populateSessions(JsArray<SessionSummary> sessionArray) {
	// reset the editable status of the cells
	initializeSessionColumns();

	sessionData.clear();
        for (int i = 0; i < sessionArray.length(); i++) {
	    sessionData.add(sessionArray.get(i));
	}

	if (!jdb.isClubSelected()) {
	    addAddSessionSession();
	}

	sessions.setRowData(sessionData);
	sessions.redraw();
	updateSessionToNameMapping();
    }

    private HashMap<String, SessionSummary> sessionNameToSession = new HashMap<String, SessionSummary>();

    void updateSessionToNameMapping() {
	sessionNameToSession.clear();
	for (SessionSummary s : sessionData) {
	    sessionNameToSession.put(s.getSeqno(), s);
	}
    }
    /* --- end session table --- */

    /* --- club tab --- */
    @UiField TextBox nom_club;
    @UiField TextBox nom_short;
    @UiField TextBox numero_club;
    @UiField TextBox ville;
    @UiField TextBox prefix_codepostale;
    @UiField TextBox indicatif_regional;
    @UiField TextBox escompte_resident;
    @UiField CheckBox default_prorata;

    void clearClubFields() {
	nom_club.setText(""); nom_club.setReadOnly(true);
	nom_short.setText(""); nom_short.setReadOnly(true);
	numero_club.setText(""); numero_club.setReadOnly(true);
	ville.setText(""); ville.setReadOnly(true);
	prefix_codepostale.setText(""); prefix_codepostale.setReadOnly(true);
	indicatif_regional.setText(""); indicatif_regional.setReadOnly(true);
	escompte_resident.setText(""); escompte_resident.setReadOnly(true);
	default_prorata.setValue(false); default_prorata.setEnabled(false);
    }

    void populateCurrentClub() {
	String selectedClub = jdb.getSelectedClubID();
	clearClubFields();
	if (selectedClub == null) {
	    nom_club.setText("n/d");
	    return;
	}

	ClubSummary cs = jdb.getClubSummaryByID(jdb.getSelectedClubID());
	nom_club.setText(cs.getNom());
	nom_short.setText(cs.getNomShort());
	numero_club.setText(cs.getNumeroClub());
	ville.setText(cs.getVille());
	prefix_codepostale.setText(cs.getPrefixCodepostale());
	indicatif_regional.setText(cs.getIndicatifRegional());
	escompte_resident.setText(cs.getEscompteResident());
	default_prorata.setValue(cs.getDefaultProrata());
    }
    /* --- end club tab --- */

    /* --- cours tab --- */
    private static final ProvidesKey<CoursSummary> COURS_KEY_PROVIDER =
	new ProvidesKey<CoursSummary>() {
        @Override
        public Object getKey(CoursSummary item) {
	    return item.getId();
        }
    };

    private final ColumnFields COURS_SESSION_COLUMN = new ColumnFields("session", "Session", 2, Unit.EM),
	DESC_COLUMN = new ColumnFields("short_desc", "Description", 4, Unit.EM);

    private List<ColumnFields> perCoursColumns = Collections.unmodifiableList(Arrays.asList(COURS_SESSION_COLUMN, DESC_COLUMN));

    void initializeCoursTable() {
	cours = new CellTable<CoursSummary>(COURS_KEY_PROVIDER);
	cours.setWidth("60em", true);

	initializeCoursColumns();
    }

    private Column<CoursSummary, String> addCoursColumn(final CellTable t, final ColumnFields c, final boolean editable) {
	final Cell<String> cell = editable ? new EditTextCell() : new TextCell();
	Column<CoursSummary, String> newColumn = new Column<CoursSummary, String>(cell) {
	    public String getValue(CoursSummary object) {
		return object.get(c.key);
	    }
	};
	cours.addColumn(newColumn, c.name);
	newColumn.setFieldUpdater(new FieldUpdater<CoursSummary, String>() {
		@Override
		public void update(int index, CoursSummary object, String value) {
		    object.set(c.key, value);
		    // push stuff
		    t.redraw();
		}
	    });
	cours.setColumnWidth(newColumn, c.width, c.widthUnits);
	return newColumn;
    }

    void initializeCoursColumns() {
	while (cours.getColumnCount() > 0)
	    cours.removeColumn(0);

	addCoursColumn(sessions, COURS_SESSION_COLUMN, true);
	addCoursColumn(sessions, DESC_COLUMN, true);
    }

    private void populateCours(List<CoursSummary> coursArray) {
	initializeCoursColumns();

	// combine cours across sessions
	// l has key shortdesc, values sessions
	HashMap<String, StringBuffer> l = new HashMap<String, StringBuffer>();

        for (CoursSummary cs : coursArray) {
	    if (!l.containsKey(cs.getShortDesc())) {
		l.put(cs.getShortDesc(), new StringBuffer());
	    }
	    StringBuffer b = l.get(cs.getShortDesc());
	    b.append(" ");
	    SessionSummary ss = sessionNameToSession.get(cs.getSession());
	    b.append(ss.getAbbrev());
	    String ls = ss.getLinkedSeqno();
	    if (!ls.equals("")) {
		b.append(" ");
		b.append(sessionNameToSession.get(ls).getAbbrev());
	    }
	}

	coursData.clear();
	int id = 0;
	for (String s : l.keySet()) {
	    CoursSummary cs = (CoursSummary)JavaScriptObject.createObject().cast();
	    cs.setId(String.valueOf(id)); cs.setShortDesc(s); cs.setSession(l.get(s).toString());
	    coursData.add(cs);
	    id++;
	}

	cours.setRowData(coursData);
	cours.redraw();
    }
    /* --- end cours tab --- */

    /* --- prix tab --- */
    private static final ProvidesKey<ClubPrix> CLUB_PRIX_KEY_PROVIDER =
	new ProvidesKey<ClubPrix>() {
        @Override
        public Object getKey(ClubPrix item) {
	    return item.getId();
        }
    };

    private final ColumnFields PRIX_SESSION_COLUMN = new ColumnFields("session", "Session", 2, Unit.EM),
	DIV_COLUMN = new ColumnFields("div", "Division", 4, Unit.EM),
	FRAIS_1_COLUMN = new ColumnFields("frais_1", "Frais 1 session", 4, Unit.EM),
	FRAIS_2_COLUMN = new ColumnFields("frais_2", "Frais 2 sessions", 4, Unit.EM),
	FRAIS_JUDO_QC_COLUMN = new ColumnFields("frais_judo_qc", "Frais Judo QC", 4, Unit.EM);

    private List<ColumnFields> perPrixColumns = Collections.unmodifiableList(Arrays.asList(PRIX_SESSION_COLUMN, DIV_COLUMN, FRAIS_1_COLUMN, FRAIS_2_COLUMN, FRAIS_JUDO_QC_COLUMN));

    void initializePrixTable() {
	prix = new CellTable<ClubPrix>(CLUB_PRIX_KEY_PROVIDER);
	prix.setWidth("60em", true);

	initializePrixColumns();
    }

    private Column<ClubPrix, String> addPrixColumn(final CellTable t, final ColumnFields c, final boolean editable) {
	final Cell<String> cell = editable ? new EditTextCell() : new TextCell();
	Column<ClubPrix, String> newColumn = new Column<ClubPrix, String>(cell) {
	    public String getValue(ClubPrix object) {
		return object.get(c.key);
	    }
	};
	prix.addColumn(newColumn, c.name);
	newColumn.setFieldUpdater(new FieldUpdater<ClubPrix, String>() {
		@Override
		public void update(int index, ClubPrix object, String value) {
		    object.set(c.key, value);
		    // push stuff
		    t.redraw();
		}
	    });
	cours.setColumnWidth(newColumn, c.width, c.widthUnits);
	return newColumn;
    }

    void initializePrixColumns() {
	while (prix.getColumnCount() > 0)
	    prix.removeColumn(0);

	addPrixColumn(sessions, PRIX_SESSION_COLUMN, true);
	addPrixColumn(sessions, DIV_COLUMN, true);
	addPrixColumn(sessions, FRAIS_1_COLUMN, true);
	addPrixColumn(sessions, FRAIS_2_COLUMN, true);
	addPrixColumn(sessions, FRAIS_JUDO_QC_COLUMN, true);
    }

    private void populatePrix(List<ClubPrix> prixArray) {
        initializePrixColumns();

        // l has key signature, values are prix
        HashMap<String, StringBuffer> l = new HashMap<String, StringBuffer>();

        for (ClubPrix p : prixArray) {
            String ps = p.getSignature();
            if (!l.containsKey(ps)) {
                l.put(ps, new StringBuffer());
            }
            StringBuffer b = l.get(ps);
            b.append(" ");
            SessionSummary ss = sessionNameToSession.get(p.getSession());
            b.append(ss.getAbbrev());
            String ls = ss.getLinkedSeqno();
            if (!ls.equals("")) {
                b.append(" ");
                b.append(sessionNameToSession.get(ls).getAbbrev());
            }
        }

        prixData.clear();
        int id = 0;
        for (String s : l.keySet()) {
            String[] pnArray = s.split("\\|");
            ClubPrix pn = (ClubPrix)JavaScriptObject.createObject().cast();

            pn.setId(String.valueOf(id));
            id++;

            // parse the components of ps and put them in pn
            pn.setSession(l.get(s).toString());
            pn.setDivisionAbbrev(pnArray[0]);
            pn.setFrais1Session(pnArray[1]);
            pn.setFrais2Session(pnArray[2]);
            pn.setFraisJudoQC(pnArray[3]);
            prixData.add(pn);
        }

	prix.setRowData(prixData);
	prix.redraw();
    }
    /* --- end cours tab --- */

    /* --- network functions --- */
    private boolean gotSessions = false;
    public void retrieveSessions(String numero_club) {
        String url = JudoDB.PULL_SESSIONS_URL;
        url += "?club="+numero_club;
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

    public void retrieveClubPrix(final String numero_club) {
        if (!gotSessions) {
            new Timer() {
                public void run() { retrieveClubPrix(numero_club); }
            }.schedule(100);
            return;
        }

        prixData.clear();

        ClubSummary cs = jdb.getClubSummaryByID(jdb.getSelectedClubID());
        String url = JudoDB.PULL_CLUB_PRIX_URL +
            "?numero_club=" + numero_club;

        RequestCallback rc =
            jdb.createRequestCallback(new JudoDB.Function() {
                    public void eval(String s) {
                        List<ClubPrix> lcp = new ArrayList<ClubPrix>();
                        JsArray<ClubPrix> cp = JsonUtils.<JsArray<ClubPrix>>safeEval(s);
                        for (int i = 0; i < cp.length(); i++)
                            lcp.add(cp.get(i));
                        populatePrix(lcp);
                    }
                });
        jdb.retrieve(url, rc);
    }

    private boolean gotCours = false;
    public void retrieveCours(String numero_club) {
	if (numero_club.equals("")) return;

        String url = JudoDB.PULL_CLUB_COURS_URL;
        url += "?numero_club="+numero_club;
        RequestCallback rc =
            jdb.createRequestCallback(new JudoDB.Function() {
                    public void eval(String s) {
			gotCours = true;
			List<CoursSummary> lcs = new ArrayList<CoursSummary>();
			JsArray<CoursSummary> jcs = JsonUtils.<JsArray<CoursSummary>>safeEval(s);
			for (int i = 0; i < jcs.length(); i++)
			    lcs.add(jcs.get(i));
			populateCours(lcs);
			jdb.clearStatus();
                    }
                });
        jdb.retrieve(url, rc);
    }

    private boolean refreshSessions = false;
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
			    if (refreshSessions) {
				refreshSessions = false;
				retrieveSessions(Integer.toString(jdb.selectedClub));
			    }
                        }
                        new Timer() { public void run() { jdb.clearStatus(); } }.schedule(2000);
                    }
                });
        jdb.retrieve(url, rc);
    }
}
