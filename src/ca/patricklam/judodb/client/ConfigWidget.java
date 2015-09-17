package ca.patricklam.judodb.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

public class ConfigWidget extends Composite {
    interface MyUiBinder extends UiBinder<Widget, ConfigWidget> {}
    public static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiField FlowPanel sessionTab;
    @UiField FlowPanel coursTab;
    @UiField FlowPanel prixTab;
    @UiField FlowPanel escompteTab;
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
    private final List<SessionSummary> sessionData = new ArrayList<SessionSummary>();

    CellTable cours;
    // rawCoursData is unconsolidated, coursData is merged by session
    private final List<CoursSummary> rawCoursData = new ArrayList<CoursSummary>();
    private final List<CoursSummary> coursData = new ArrayList<CoursSummary>();
    private final HashMap<String, List<String>> coursShortDescToDbIds =
        new HashMap<String, List<String>>();
    private final HashSet<CoursSummary> duplicateCours = new HashSet<CoursSummary>();

    CellTable prix;
    private final List<ClubPrix> rawPrixData = new ArrayList<ClubPrix>();
    private final List<ClubPrix> prixData = new ArrayList<ClubPrix>();
    private final HashSet<ClubPrix> duplicatePrix = new HashSet<ClubPrix>();

    CellTable escomptes;
    private final List<EscompteSummary> escompteData = new ArrayList<EscompteSummary>();

    // useful URLs: http://www.filsa.net/2010/01/23/more-on-tablayoutpanel/
    // http://www.filsa.net/2010/01/21/gwt-notes-tablayoutpanel/

    // not done yet: for each club,
    // montants--namely, (should theoretically be stored with the session)
    // frais passeport judo QC [5]
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
            retrieveEscomptes(jdb.getSelectedClubID());
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
	    retrieveEscomptes(jdb.getSelectedClubID());
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

	initializeEscompteTable();
	escompteTab.add(escomptes);
	populateEscomptes(escompteData);

        configEditForm.setAction(PUSH_MULTI_CLIENTS_URL);
    }

    /* accepts something like A14 H15 A15 H16
     * returns a list of sessions, ignoring linked_seqnos.
     */
    private List<SessionSummary> parseSessionIds(String sessionAbbrevs) {
        String[] sessionAbbrevArray = sessionAbbrevs.split(" ");
        List<SessionSummary> retval = new ArrayList<SessionSummary>();
        for (String s : sessionAbbrevArray) {
            SessionSummary ts = seqAbbrevToSession.get(s);
            if (ts != null)
                retval.add(ts);
        }
        return retval;
    }

    private HashMap<String, SessionSummary> seqnoToSession = new HashMap<String, SessionSummary>();
    // primary session only, not linked
    private HashMap<String, SessionSummary> seqAbbrevToSession = new HashMap<String, SessionSummary>();

    private void updateSessionToNameMapping() {
        seqnoToSession.clear();
        seqAbbrevToSession.clear();
        for (SessionSummary s : sessionData)
            seqnoToSession.put(s.getSeqno(), s);

        for (SessionSummary s : sessionData) {
            SessionSummary linkedSession = seqnoToSession.get(s.getLinkedSeqno());
            if (s.isPrimary())
                seqAbbrevToSession.put(s.getAbbrev(), s);
        }
    }

    /* --- session tab --- */
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
    /* --- end session tab --- */

    /* --- club tab --- */
    @UiField TextBox nom_club;
    @UiField TextBox nom_short;
    @UiField TextBox numero_club;
    @UiField TextBox ville;
    @UiField TextBox prefix_codepostale;
    @UiField TextBox indicatif_regional;
    @UiField TextBox escompte_resident;
    @UiField TextBox prime_prorata;
    @UiField CheckBox default_prorata;

    ValueChangeHandler newValueChangeHandler(final String key) {
	return new ValueChangeHandler<String>() {
	    @Override
	    public void onValueChange(ValueChangeEvent<String> event) {
		pushEdit("-1,c" + key + "," + event.getValue() + "," +
			 jdb.getSelectedClubID() + ";");
	    }
	};
    }

    ClickHandler newClickHandler(final String key) {
	return new ClickHandler() {
	    @Override
	    public void onClick(ClickEvent event) {
		pushEdit("-1,c" + key + "," +
			 ((((CheckBox)event.getSource()).getValue()) ? "1" : "0") + "," +
			 jdb.getSelectedClubID() + ";");
	    }
	};
    }

    private boolean clubHandlersInstalled = false;
    void initializeClubFields() {
        nom_club.setText(""); nom_club.setReadOnly(true);
        numero_club.setText(""); numero_club.setReadOnly(true);
        nom_short.setText(""); 
        ville.setText("");
        prefix_codepostale.setText("");
        indicatif_regional.setText("");
        escompte_resident.setText("");
        prime_prorata.setText("5"); prime_prorata.setReadOnly(true);
        default_prorata.setValue(false);

        if (!clubHandlersInstalled) {
            clubHandlersInstalled = true;
            nom_short.addValueChangeHandler(newValueChangeHandler("nom_short"));
            ville.addValueChangeHandler(newValueChangeHandler("ville"));
            prefix_codepostale.addValueChangeHandler(newValueChangeHandler("prefix_codepostale"));
            indicatif_regional.addValueChangeHandler(newValueChangeHandler("indicatif_regional"));
            escompte_resident.addValueChangeHandler(newValueChangeHandler("escompte_resident"));
            default_prorata.addClickHandler(newClickHandler("pro_rata"));
        }

        boolean setEverythingReadOnly = false;
        if (jdb.getSelectedClubID() == null) {
            nom_club.setText("n/d");
            setEverythingReadOnly = true;
        }

        nom_short.setReadOnly(setEverythingReadOnly);
        ville.setReadOnly(setEverythingReadOnly);
        prefix_codepostale.setReadOnly(setEverythingReadOnly);
        indicatif_regional.setReadOnly(setEverythingReadOnly);
        escompte_resident.setReadOnly(setEverythingReadOnly);
        default_prorata.setEnabled(!setEverythingReadOnly);
    }

    void populateCurrentClub() {
	String selectedClub = jdb.getSelectedClubID();
	initializeClubFields();
	if (selectedClub == null) {
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
	//prime_prorata.setText(cs.getPrimeProrata());
	default_prorata.setValue(cs.getDefaultProrata());
    }
    /* --- end club tab --- */

    /* --- cours tab --- */
    /* There are five possible front-end actions on the cours tab:
     * 0) delete a cours: just delete all of the corresponding cours-ids
     * 1) edit sessions field of new cours
     * 2) edit short_desc field of new cours
     * 3) edit sessions field of existing cours
     * 4) edit short_desc field of existing cours
     *
     * There are also three update types:
     * a) change short_desc of existing cours
     * b) create a new CoursSummary (new session, new short_desc)
     * c) remove a coursSummary (by session and id)
     *
     * action (1) involves (b) with blank short_desc; if any existing short_descs are blank, merge them into this
     * action (2) involves populating the sessions field with the current session and then just doing (b)
     * action (3) involves (b) on the new sessions and (c) on the deleted sessions
     * action (4) involves update (a)
     */

    /* test script:
     * - create a new cours by entering a description
     * - add another session to that cours
     * - edit the short_desc
     * - delete a session from that cours
     *
     * - edit the session of the "add new cours" cours
     * - change that session to a disjoint session (eg A15 H16 -> A14)
     * - edit the session of the "add new cours" cours again, should see a merge
     *
     * - edit a short_desc so that it is the same as some other existing short_desc
     */

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
        // this handles updating the short_desc column
        newColumn.setFieldUpdater(new FieldUpdater<CoursSummary, String>() {
                @Override
                public void update(int index, CoursSummary object, String value) {
                    List<SessionSummary> sessions = parseSessionIds(value);
                    StringBuffer sb = new StringBuffer();
                    refreshCours = true;

                    if (object.get(DESC_COLUMN.key).equals(ADD_COURS_VALUE)) {
                        // ... of a new cours, case (2)
                        assert (object.get(COURS_SESSION_COLUMN.key).equals(""));
                        String currentSessions = JudoDB.getSessionIds(new Date(), 2, sessionData);
                        object.set(COURS_SESSION_COLUMN.key, currentSessions);
                        sessions = parseSessionIds(currentSessions);
                        List<String> cs = new ArrayList();
                        StringBuffer edits = new StringBuffer();
                        for (SessionSummary ss : sessions) {
                            cs.add(ss.getAbbrev());
                            edits.append("-1,R," + ss.getSeqno() + "," +
                                     value + "," + jdb.getSelectedClubID() + ";");
                        }
                        pushEdit(edits.toString());
                        coursShortDescToDbIds.put(value, cs);
                        addAddCoursCours();
                    } else {
                        // ... of an existing cours, case (4)
                        assert coursShortDescToDbIds.containsKey(object.getShortDesc());

                        StringBuffer edits = new StringBuffer();
                        for (String coursId : coursShortDescToDbIds.get(object.getShortDesc())) {
                            edits.append("-1,r" + c.key + "," + coursId + "," +
                                     value + "," + jdb.getSelectedClubID() + ";");
                        }
                        removeDuplicateCours(edits);
                        pushEdit(edits.toString());
                    }
                    object.set(c.key, value);
                    cours.setRowData(coursData);
                    t.redraw();
                }
            });
        cours.setColumnWidth(newColumn, c.width, c.widthUnits);
        return newColumn;
    }

    void initializeCoursColumns() {
        while (cours.getColumnCount() > 0)
            cours.removeColumn(0);

        final Column<CoursSummary, String> sessionColumn =
            addCoursColumn(sessions, COURS_SESSION_COLUMN, true);
        // implement changes to the session column
        // todo: have a button for deleting all sessions?
        sessionColumn.setFieldUpdater(new FieldUpdater<CoursSummary, String>() {
                @Override
                public void update(int index, CoursSummary object, String value) {
                    List<SessionSummary> newSessions = parseSessionIds(value);
                    List<SessionSummary> oldSessions = parseSessionIds(object.get(COURS_SESSION_COLUMN.key));
                    if (oldSessions.equals(newSessions)) return;
                    refreshCours = true;

                    List<SessionSummary> addedSessions = new ArrayList<SessionSummary>(),
                        removedSessions = new ArrayList<SessionSummary>();
                    for (SessionSummary s : newSessions)
                        if (!oldSessions.contains(s)) addedSessions.add(s);
                    for (SessionSummary s : oldSessions)
                        if (!newSessions.contains(s)) removedSessions.add(s);

                    StringBuffer sb = new StringBuffer();
                    if (object.get(DESC_COLUMN.key).equals(ADD_COURS_VALUE)) {
                        object.set(DESC_COLUMN.key, "");
                        // if there is already a blank desc_column it automatically gets merged
                        assert (removedSessions.isEmpty());
                        StringBuffer edits = new StringBuffer();
                        for (SessionSummary ss : newSessions) {
                            edits.append("-1,R," + ss.getSeqno() + "," +
                                         object.getShortDesc() + "," + jdb.getSelectedClubID() + ";");
                        }
                        pushEdit(edits.toString());
                        addAddCoursCours();
                    } else {
                        // add added sessions
                        StringBuffer edits = new StringBuffer();
                        for (SessionSummary ss : addedSessions) {
                            edits.append("-1,R," + ss.getSeqno() + "," +
                                     object.getShortDesc() + "," + jdb.getSelectedClubID() + ";");
                        }

                        // remove deleted sessions
                        assert coursShortDescToDbIds.containsKey(object.getShortDesc());
                        for (SessionSummary ss : removedSessions) {
                            for (CoursSummary cs : rawCoursData) {
                                if (cs.getShortDesc().equals(object.getShortDesc()) &&
                                    cs.getSession().equals(ss.getSeqno())) {
                                    edits.append("-1,O," + cs.getId() + "," +
                                                 object.getShortDesc() + "," + jdb.getSelectedClubID() + ";");
                                }
                            }
                        }
                        removeDuplicateCours(edits);
                        pushEdit(edits.toString());
                    }
                }
            });
        addCoursColumn(sessions, DESC_COLUMN, true);
    }

    private void removeDuplicateCours(StringBuffer edits) {
        for (CoursSummary cs : duplicateCours) {
            edits.append("-1,O," + cs.getId() + "," +
                         cs.getShortDesc() + "," + jdb.getSelectedClubID() + ";");
        }
        duplicateCours.clear();
    }

    final private static String ADD_COURS_VALUE = "[ajouter cours]";
    void addAddCoursCours() {
        int maxId = -1;
        for (CoursSummary c : coursData) {
            if (Integer.parseInt(c.getId()) > maxId)
                maxId = Integer.parseInt(c.getId());
        }

        CoursSummary addNewCours =
            JsonUtils.<CoursSummary>safeEval
            ("{\"id\":\""+(maxId+1)+"\"}");
        addNewCours.setSession("");
        addNewCours.setClubId(jdb.getSelectedClubID());
        addNewCours.setShortDesc(ADD_COURS_VALUE);
        coursData.add(addNewCours);
    }

    private void populateCours(List<CoursSummary> coursArray) {
        initializeCoursColumns();

        // combine cours across sessions
        // l has keys shortdesc, values sessions
        // m has keys shortdesc, values ids
        HashMap<String, StringBuffer> l = new HashMap<String, StringBuffer>();
        HashMap<String, Set<String>> ll = new HashMap<String, Set<String>>();
        HashMap<String, List<String>> m = new HashMap<String, List<String>>();
        rawCoursData.clear(); rawCoursData.addAll(coursArray);
        duplicateCours.clear();

        for (CoursSummary cs : coursArray) {
            if (!l.containsKey(cs.getShortDesc())) {
                l.put(cs.getShortDesc(), new StringBuffer());
                ll.put(cs.getShortDesc(), new HashSet<String>());
                m.put(cs.getShortDesc(), new ArrayList<String>());
            }
            StringBuffer b = l.get(cs.getShortDesc());
            List<String> ids = m.get(cs.getShortDesc());
            ids.add(cs.getId());

            SessionSummary ss = seqnoToSession.get(cs.getSession());
            if (ll.get(cs.getShortDesc()).contains(ss.getAbbrev())) {
                duplicateCours.add(cs);
                continue;
            }

            ll.get(cs.getShortDesc()).add(ss.getAbbrev());
            if (b.length() > 0) b.append(" ");
            b.append(ss.getAbbrev());
            String ls = ss.getLinkedSeqno();
            if (!ls.equals("")) {
                b.append(" ");
                b.append(seqnoToSession.get(ls).getAbbrev());
                ll.get(cs.getShortDesc()).add(seqnoToSession.get(ls).getAbbrev());
            }
        }

        coursData.clear();
        int id = 0;
        for (String s : l.keySet()) {
            CoursSummary cs = (CoursSummary)JavaScriptObject.createObject().cast();
            cs.setId(String.valueOf(id)); cs.setShortDesc(s); cs.setSession(l.get(s).toString());
            coursShortDescToDbIds.put(cs.getShortDesc(), m.get(s));
            coursData.add(cs);
            id++;
        }

        if (jdb.isClubSelected())
            addAddCoursCours();
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
	DIV_COLUMN = new ColumnFields("division_abbrev", "Division", 4, Unit.EM),
	FRAIS_1_COLUMN = new ColumnFields("frais_1_session", "Frais 1 session", 4, Unit.EM),
	FRAIS_2_COLUMN = new ColumnFields("frais_2_session", "Frais 2 sessions", 4, Unit.EM),
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
	t.addColumn(newColumn, c.name);
	newColumn.setFieldUpdater(new FieldUpdater<ClubPrix, String>() {
		@Override
		public void update(int index, ClubPrix object, String value) {
                    refreshPrix = true;
                    if (object.get(DIV_COLUMN.key).equals(ADD_PRIX_VALUE)) {
                        assert (object.get(PRIX_SESSION_COLUMN.key).equals(""));
                        String currentSessions = JudoDB.getSessionIds(new Date(), 2, sessionData);
                        object.set(PRIX_SESSION_COLUMN.key, currentSessions);
                        List<SessionSummary> currentSessionsList = parseSessionIds(currentSessions);
                        StringBuffer edits = new StringBuffer();
                        for (SessionSummary ss : currentSessionsList) {
                            edits.append("-1,P," + ss.getSeqno() + "," +
                                         value + "," /* + frais_1 */ + "," /* + frais_2 */ + ","
                                         /* + frais_judo_qc */ + "," + jdb.getSelectedClubID() + ";");
                        }
                        pushEdit(edits.toString());
                        addAddPrixPrix();
                    } else {
                        StringBuffer edits = new StringBuffer();
                        for (String dbId : prixInternalIdToDbIds.get(object.getId())) {
                            edits.append("-1,p" + c.key + "," + dbId + "," + value + "," + jdb.getSelectedClubID() + ";");
                        }
                        pushEdit(edits.toString());
                    }
                    object.set(c.key, value);
                    t.redraw();
                }
	    });
	t.setColumnWidth(newColumn, c.width, c.widthUnits);
	return newColumn;
    }

    void initializePrixColumns() {
        while (prix.getColumnCount() > 0)
            prix.removeColumn(0);

        final Column<ClubPrix, String> sessionColumn =
            addPrixColumn(sessions, PRIX_SESSION_COLUMN, true);
        sessionColumn.setFieldUpdater(new FieldUpdater<ClubPrix, String>() {
                @Override
                public void update(int index, ClubPrix object, String value) {
                    List<SessionSummary> newSessions = parseSessionIds(value);
                    List<SessionSummary> oldSessions = parseSessionIds(object.get(PRIX_SESSION_COLUMN.key));
                    if (oldSessions.equals(newSessions)) return;
                    refreshPrix = true;

                    List<SessionSummary> addedSessions = new ArrayList<SessionSummary>(),
                        removedSessions = new ArrayList<SessionSummary>();
                    for (SessionSummary s : newSessions)
                        if (!oldSessions.contains(s)) addedSessions.add(s);
                    for (SessionSummary s : oldSessions)
                        if (!newSessions.contains(s)) removedSessions.add(s);

                    StringBuffer sb = new StringBuffer();
                    if (object.get(DIV_COLUMN.key).equals(ADD_PRIX_VALUE)) {
                        object.set(DIV_COLUMN.key, "");
                        assert (removedSessions.isEmpty());
			StringBuffer edits = new StringBuffer();
                        for (SessionSummary ss : newSessions) {
                            edits.append("-1,P," + ss.getSeqno() + "," +
                                         object.getDivisionAbbrev() + "," +
					 object.getFrais1Session() + "," +
					 object.getFrais2Session() + "," +
                                         object.getFraisJudoQC() + "," + jdb.getSelectedClubID() + ";");
                        }
			pushEdit(edits.toString());
                        addAddPrixPrix();
                    } else {
                        // add added prix
                        StringBuffer edits = new StringBuffer();
                        for (SessionSummary ss : addedSessions) {
                            edits.append("-1,P," + ss.getSeqno() + "," +
                                         object.getDivisionAbbrev() + "," +
					 object.getFrais1Session() + "," +
					 object.getFrais2Session() + "," +
                                         object.getFraisJudoQC() + "," + jdb.getSelectedClubID() + ";");
                        }

                        // remove deleted prix
                        for (SessionSummary ss : removedSessions) {
                            for (ClubPrix cs : rawPrixData) {
                                if (cs.getDivisionAbbrev().equals(object.getDivisionAbbrev()) &&
                                    cs.getSession().equals(ss.getSeqno())) {
                                    edits.append("-1,Q," + cs.getId() + "," +
                                                 cs.getDivisionAbbrev() + "," + jdb.getSelectedClubID() + ";");
                                }
                            }
                        }
                        removeDuplicatePrix(edits);
                        pushEdit(edits.toString());
                    }
                }
            });
        addPrixColumn(prix, DIV_COLUMN, true);
        addPrixColumn(prix, FRAIS_1_COLUMN, true);
        addPrixColumn(prix, FRAIS_2_COLUMN, true);
        addPrixColumn(prix, FRAIS_JUDO_QC_COLUMN, true);
    }

    private void removeDuplicatePrix(StringBuffer edits) {
        for (ClubPrix cs : duplicatePrix) {
            edits.append("-1,Q," + cs.getId() + "," +
                         cs.getDivisionAbbrev() + "," + jdb.getSelectedClubID() + ";");
        }
        duplicateCours.clear();
    }
    final private static String ADD_PRIX_VALUE = "[ajouter division]";
    void addAddPrixPrix() {
        int maxId = -1;
        for (ClubPrix c : prixData) {
            if (Integer.parseInt(c.getId()) > maxId)
                maxId = Integer.parseInt(c.getId());
        }

        ClubPrix addNewPrix =
            JsonUtils.<ClubPrix>safeEval
            ("{\"id\":\""+(maxId+1)+"\"}");
        addNewPrix.setDivisionAbbrev(ADD_PRIX_VALUE);
        prixData.add(addNewPrix);
    }

    private final HashMap<String, List<String>> prixInternalIdToDbIds = new HashMap<String, List<String>>();

    private void populatePrix(List<ClubPrix> prixArray) {
        initializePrixColumns();

        // l has keys=signatures, values=session abbrevs
        // m has keys=signatures, values=original ids
        HashMap<String, StringBuffer> l = new HashMap<String, StringBuffer>();
        HashMap<String, Set<String>> ll = new HashMap<String, Set<String>>();
        HashMap<String, List<String>> m = new HashMap<String, List<String>>();
        rawPrixData.clear(); rawPrixData.addAll(prixArray);
        duplicatePrix.clear();

        for (ClubPrix p : prixArray) {
            String ps = p.getSignature();
            if (!l.containsKey(ps)) {
                l.put(ps, new StringBuffer());
		ll.put(ps, new HashSet<String>());
                m.put(ps, new ArrayList<String>());
            }
            StringBuffer b = l.get(ps);
            if (b.length() > 0) b.append(" ");
            SessionSummary ss = seqnoToSession.get(p.getSession());
            if (ll.get(ps).contains(ss.getAbbrev())) {
                duplicatePrix.add(p);
                continue;
            }

            ll.get(ps).add(ss.getAbbrev());
            b.append(ss.getAbbrev());
            String ls = ss.getLinkedSeqno();
            if (!ls.equals("")) {
                b.append(" ");
                b.append(seqnoToSession.get(ls).getAbbrev());
                ll.get(ps).add(seqnoToSession.get(ls).getAbbrev());
            }

            List<String> ids = m.get(ps);
            ids.add(p.getId());
        }

        prixData.clear();
        prixInternalIdToDbIds.clear();
        int id = 0;
        for (String s : l.keySet()) {
            String[] pnArray = s.split("\\|");
            ClubPrix pn = (ClubPrix)JavaScriptObject.createObject().cast();

            prixInternalIdToDbIds.put(Integer.toString(id), m.get(s));
            pn.setId(String.valueOf(id));
            id++;

            // parse the components of ps and put them in pn
            pn.setSession(l.get(s).toString());
            pn.setDivisionAbbrev(pnArray.length > 0 ? pnArray[0] : "");
            pn.setFrais1Session(pnArray.length > 1 ? pnArray[1] : "");
            pn.setFrais2Session(pnArray.length > 2 ? pnArray[2] : "");
            pn.setFraisJudoQC(pnArray.length > 3 ? pnArray[3] : "");
            prixData.add(pn);
        }

        addAddPrixPrix();
        prix.setRowData(prixData);
        prix.redraw();
    }
    /* --- end prix tab --- */

    /* --- escompte tab --- */
    private static final ProvidesKey<EscompteSummary> ESCOMPTE_KEY_PROVIDER =
        new ProvidesKey<EscompteSummary>() {
        @Override
        public Object getKey(EscompteSummary item) {
            return item.getId();
        }
    };

    private final ColumnFields NOM_COLUMN = new ColumnFields("nom", "Nom", 2, Unit.EM),
        AMOUNT_PERCENT_COLUMN = new ColumnFields("amount_percent", "%", 1, Unit.EM),
        AMOUNT_ABSOLUTE_COLUMN = new ColumnFields("amount_absolute", "$", 1, Unit.EM);

    private List<ColumnFields> perEscompteColumns = Collections.unmodifiableList(Arrays.asList(NOM_COLUMN, AMOUNT_PERCENT_COLUMN, AMOUNT_ABSOLUTE_COLUMN));

    void initializeEscompteTable() {
        escomptes = new CellTable<EscompteSummary>(ESCOMPTE_KEY_PROVIDER);
        escomptes.setWidth("60em", true);

        initializeEscompteColumns();
    }

    private Column<EscompteSummary, String> addEscompteColumn(final CellTable t, final ColumnFields c, final boolean editable) {
        final Cell<String> cell = editable ? new EditTextCell() : new TextCell();
        Column<EscompteSummary, String> newColumn = new Column<EscompteSummary, String>(cell) {
            public String getValue(EscompteSummary object) {
                return object.get(c.key);
            }
        };
        escomptes.addColumn(newColumn, c.name);
        newColumn.setFieldUpdater(new FieldUpdater<EscompteSummary, String>() {
                @Override
                public void update(int index, EscompteSummary object, String value) {
                    refreshEscomptes = true;
                    if (object.get(NOM_COLUMN.key).equals(ADD_ESCOMPTE_VALUE)) {
                        // NB: do the set before the update because we read from object
                        String nom;
                        if (c.key.equals(AMOUNT_PERCENT_COLUMN.key) ||
                            c.key.equals(AMOUNT_ABSOLUTE_COLUMN.key)) {
                            nom = "Escompte "+object.getId();
                            object.set(c.key, value);
                        } else {
                            nom = value;
                            object.set(c.key, value);
                        }
                        pushEdit("-1,Z," + object.getId() + "," +
                                 nom + "," +
                                 object.get(AMOUNT_PERCENT_COLUMN.key) + "," +
                                 object.get(AMOUNT_ABSOLUTE_COLUMN.key) + "," +
                                 jdb.getSelectedClubID() + ";");
                        addAddEscompteEscompte();
                    } else {
                        StringBuffer edits = new StringBuffer();
                        if (c.key.equals(AMOUNT_PERCENT_COLUMN.key)) {
                            String k = AMOUNT_ABSOLUTE_COLUMN.key;
                            edits.append("-1,z" + k + "," + object.getId() + "," + "" + "," + jdb.getSelectedClubID() + ";");
                            object.set(k, "");
                        }
                        else if (c.key.equals(AMOUNT_ABSOLUTE_COLUMN.key)) {
                            String k = AMOUNT_PERCENT_COLUMN.key;
                            edits.append("-1,z" + k + "," + object.getId() + "," + "" + "," + jdb.getSelectedClubID() + ";");
                            object.set(k, "");
                        }
                        edits.append("-1,z" + c.key + "," + object.getId() + "," + value + "," + jdb.getSelectedClubID() + ";");
                        pushEdit(edits.toString());
                        object.set(c.key, value);
                    }
                    t.redraw();
                }
            });
        escomptes.setColumnWidth(newColumn, c.width, c.widthUnits);
        return newColumn;
    }

    void initializeEscompteColumns() {
        while (escomptes.getColumnCount() > 0)
            escomptes.removeColumn(0);

        addEscompteColumn(escomptes, NOM_COLUMN, true);
        addEscompteColumn(escomptes, AMOUNT_PERCENT_COLUMN, true);
        addEscompteColumn(escomptes, AMOUNT_ABSOLUTE_COLUMN, true);
    }

    final private static String ADD_ESCOMPTE_VALUE = "[ajouter escompte]";
    void addAddEscompteEscompte() {
        int maxId = 0;
        for (EscompteSummary c : escompteData) {
            if (Integer.parseInt(c.getId()) > maxId)
                maxId = Integer.parseInt(c.getId());
        }

        EscompteSummary addNewEscompte =
            JsonUtils.<EscompteSummary>safeEval
            ("{\"id\":\""+(maxId+1)+"\"}");
        addNewEscompte.setClubId(jdb.getSelectedClubID());
        addNewEscompte.setNom(ADD_ESCOMPTE_VALUE);
        addNewEscompte.setAmountPercent("");
        addNewEscompte.setAmountAbsolute("");
        escompteData.add(addNewEscompte);
    }

    private void populateEscomptes(List<EscompteSummary> escompteArray) {
        initializeEscompteColumns();

        escompteData.clear();
        for (EscompteSummary es : escompteArray) {
            escompteData.add(es);
        }

        if (jdb.isClubSelected())
            addAddEscompteEscompte();
        escomptes.setRowData(escompteData);
        escomptes.redraw();
    }
    /* --- end escompte tab --- */

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

    private boolean gotEscomptes = false;
    public void retrieveEscomptes(String club_id) {
	if (club_id.equals("")) return;

        String url = JudoDB.PULL_ESCOMPTE_URL;
        url += "?club_id="+club_id;
        RequestCallback rc =
            jdb.createRequestCallback(new JudoDB.Function() {
                    public void eval(String s) {
			gotEscomptes = true;
			List<EscompteSummary> les = new ArrayList<EscompteSummary>();
			JsArray<EscompteSummary> jes = JsonUtils.<JsArray<EscompteSummary>>safeEval(s);
			for (int i = 0; i < jes.length(); i++)
			    les.add(jes.get(i));
			populateEscomptes(les);
			jdb.clearStatus();
                    }
                });
        jdb.retrieve(url, rc);
    }

    private boolean refreshSessions = false;
    private boolean refreshCours = false;
    private boolean refreshPrix = false;
    private boolean refreshEscomptes = false;
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
			    if (refreshCours) {
				refreshCours = false;
				ClubSummary cs = jdb.getClubSummaryByID(jdb.getSelectedClubID());
				if (cs != null) {
				    retrieveCours(cs.getNumeroClub());
				}
			    }
			    if (refreshPrix) {
				refreshPrix = false;
				ClubSummary cs = jdb.getClubSummaryByID(jdb.getSelectedClubID());
				if (cs != null) {
				    retrieveClubPrix(cs.getNumeroClub());
				}
			    }
			    if (refreshEscomptes) {
				refreshEscomptes = false;
				retrieveEscomptes(jdb.getSelectedClubID());
			    }
                        }
                        new Timer() { public void run() { jdb.clearStatus(); } }.schedule(2000);
                    }
                });
        jdb.retrieve(url, rc);
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
}
