// -*-  indent-tabs-mode:nil; c-basic-offset:4; -*-
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
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.ResizeLayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.AbstractEditableCell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.Style.Unit;

import org.gwtbootstrap3.client.ui.TabListItem;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.FormGroup;
import org.gwtbootstrap3.client.ui.Container;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.DropDownMenu;

import org.gwtbootstrap3.extras.toggleswitch.client.ui.ToggleSwitch;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.TreeSet;

public class ConfigWidget extends Composite {
    interface MyUiBinder extends UiBinder<Widget, ConfigWidget> {}
    public static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiField FlowPanel sessionTab;
    @UiField FlowPanel coursTab;
    @UiField FlowPanel prixTab;
    @UiField FlowPanel escompteTab;
    @UiField FlowPanel produitTab;
    @UiField Button retour;

    @UiField FormPanel configEditForm;
    @UiField FormGroup ajustable;
    @UiField Hidden current_session;
    @UiField Hidden dataToSave;
    @UiField Hidden guid_on_form;

    private String guid;
    private int pushTries;

    private final JudoDB jdb;

    CellTable<SessionSummary> sessions;
    // contains the [add session] link also
    private final List<SessionSummary> sessionData = new ArrayList<>();
    private final List<SessionSummary> rawSessionData = new ArrayList<>();

    CellTable<CoursSummary> cours;
    // rawCoursData is unconsolidated, coursData is merged by session
    private final List<CoursSummary> rawCoursData = new ArrayList<>();
    private final List<CoursSummary> coursData = new ArrayList<>();
    private final HashMap<String, List<String>> coursShortDescToDbIds = new HashMap<>();
    private final HashSet<CoursSummary> duplicateCours = new HashSet<>();

    private final List<Prix> rawPrixData = new ArrayList<>();
    private final List<CoursPrix> prixRows = new ArrayList<>();
    private SessionSummary currentPrixSession;
    private String currentPrixSeqnoString;
    private boolean isPairPrixSession;

    CellTable<EscompteSummary> escomptes;
    private final List<EscompteSummary> escompteData = new ArrayList<>();

    CellTable<ProduitSummary> produits;
    private final List<ProduitSummary> produitData = new ArrayList<>();

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

    @UiField ButtonGroup dropDownUserClubsButtonGroup;
    @UiField Button dropDownUserClubsButton;
    @UiField DropDownMenu dropDownUserClubs;

    void selectClub(ClubSummary club) {
        jdb.selectClub(club);

        if (club == null)
            dropDownUserClubsButton.setText(JudoDB.TOUS);
        else
            dropDownUserClubsButton.setText(club.getClubText());

        if (club != null) {
            retrieveSessions(club.getId());
            retrieveCours(club.getId());
            retrievePrix(club.getId());
            retrieveEscomptes(club.getId());
            retrieveProduits(club.getId());
        } else {
            retrieveSessions("0");
            retrievePrix("0");
            clearCours();
            clearPrix();
            clearEscomptes();
            clearProduits();
        }
        populateCurrentClub();
    }

    class ConfigClubListHandlerFactory implements JudoDB.ClubListHandlerFactory {
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

    public ConfigWidget(JudoDB jdb, ClubSummary selectedClub) {
        this.jdb = jdb;
        initWidget(uiBinder.createAndBindUi(this));

        retour.addClickHandler(new ClickHandler() { public void onClick(ClickEvent e) { ConfigWidget.this.jdb.popMode(); }});

        jdb.pleaseWait();
        jdb.populateClubList(true, dropDownUserClubs, new ConfigClubListHandlerFactory());

        initializeSessionTable(); sessionTab.add(sessions);
        initializeCoursTable(); coursTab.add(cours);
        initializePrixTable(); prixTab.add(prix);
        initializeEscompteTable(); escompteTab.add(escomptes);
        initializeProduitTable(); produitTab.add(produits);

        configEditForm.setAction(JudoDB.PUSH_MULTI_CLIENTS_URL);
        jdb.clearStatus();

        selectClub(selectedClub);
    }

    /* accepts something like A14 H15 A15 H16
     * returns a list of sessions, ignoring linked_seqnos.
     */
    private List<SessionSummary> parseSessionIds(String sessionAbbrevs) {
        String[] sessionAbbrevArray = sessionAbbrevs.split(" ");
        List<SessionSummary> retval = new ArrayList<>();
        for (String s : sessionAbbrevArray) {
            SessionSummary ts = seqAbbrevToSession.get(s);
            if (ts != null)
                retval.add(ts);
        }
        return retval;
    }

    private HashMap<String, SessionSummary> seqnoToSession = new HashMap<>();
    // primary session only, not linked
    private HashMap<String, SessionSummary> seqAbbrevToSession = new HashMap<>();

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

    /* --- club tab --- */
    @UiField TextBox nom_club;
    @UiField TextBox nom_short;
    @UiField TextBox numero_club;
    @UiField TextBox ville;
    @UiField TextBox prefix_codepostale;
    @UiField TextBox indicatif_regional;
    @UiField TextBox escompte_resident;
    @UiField TextBox supplement_prorata;
    @UiField ToggleSwitch default_prorata;
    @UiField ToggleSwitch afficher_paypal;

    ValueChangeHandler<String> newValueChangeHandler(final String key) {
	return new ValueChangeHandler<String>() {
	    @Override
	    public void onValueChange(ValueChangeEvent<String> event) {
                refreshClub = true;
                pushEdit("-1,c" + key + "," + event.getValue() + "," +
                         jdb.getSelectedClubID() + ";");
	    }
	};
    }

    ValueChangeHandler<Boolean> newValueChangeHandlerBoolean(final String key) {
	return new ValueChangeHandler<Boolean>() {
	    @Override
	    public void onValueChange(ValueChangeEvent<Boolean> event) {
                refreshClub = true;
                refreshPrix = true;
                pushEdit("-1,c" + key + "," + (event.getValue() ? "1" : "0") + "," +
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
        supplement_prorata.setText("");
        default_prorata.setValue(false);

        if (!clubHandlersInstalled) {
            clubHandlersInstalled = true;
            nom_short.addValueChangeHandler(newValueChangeHandler("nom_short"));
            ville.addValueChangeHandler(newValueChangeHandler("ville"));
            prefix_codepostale.addValueChangeHandler(newValueChangeHandler("prefix_codepostale"));
            indicatif_regional.addValueChangeHandler(newValueChangeHandler("indicatif_regional"));
            escompte_resident.addValueChangeHandler(newValueChangeHandler("escompte_resident"));
            supplement_prorata.addValueChangeHandler(newValueChangeHandler("supplement_prorata"));
            default_prorata.addValueChangeHandler(newValueChangeHandlerBoolean("pro_rata"));
            afficher_paypal.addValueChangeHandler(newValueChangeHandlerBoolean("afficher_paypal"));
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
        supplement_prorata.setEnabled(!setEverythingReadOnly);
        default_prorata.setEnabled(!setEverythingReadOnly);
        afficher_paypal.setEnabled(!setEverythingReadOnly);
    }

    void populateCurrentClub() {
        String selectedClub = jdb.getSelectedClubID();
        initializeClubFields();
        if (selectedClub == null) {
            ajustable.setVisible(false);
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
        supplement_prorata.setText(cs.getSupplementProrata());
        default_prorata.setValue(cs.getEnableProrata());
        afficher_paypal.setValue(cs.getAfficherPaypal());

        // on prix tab, but set by club:
        ajustable.setVisible(true);
        ajustableCours.setValue(cs.getAjustableCours());
        ajustableDivision.setValue(cs.getAjustableDivision());
    }
    /* --- end club tab --- */

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

    private static final String BALLOT_X = "x";

    private Column<SessionSummary, String> addSessionColumn(final CellTable<SessionSummary> t, final ColumnFields c, final boolean editable) {
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
                    String oldValue = object.get(c.key);
                    object.set(c.key, value);
                    if (perClubColumns.contains(c)) {
                        if (object.getId().equals("-1")) {
                            refreshSessions = true;
                            pushEdit("-1,F" + c.key + "," + value + "," +
                                     jdb.getSelectedClub().getNumeroClub() + "," + object.getSeqno() + ";");
                        } else {
                            if (c.key.equals(FIRST_SIGNUP_COLUMN.key) ||
                                c.key.equals(LAST_SIGNUP_COLUMN.key)) {
                                boolean error = false;
                                try {
                                    SessionSummary conflicting = JudoDB.getSessionForDate(new Date(value), sessionData);
                                    if (conflicting != object && conflicting != null) {
                                        error = true;
                                        jdb.displayError("conflit de dates entre "+object.getAbbrev()+ " et "+conflicting.getAbbrev());
                                        new Timer() { public void run() { jdb.clearStatus(); } }.schedule(5000);
                                    }

                                } catch (IllegalArgumentException e) {
                                    error = true;
                                }
                                if (error) {
                                    object.set(c.key, oldValue);
                                    ((EditTextCell)cell).clearViewData
                                        (SESSION_KEY_PROVIDER.getKey(object));
                                    t.redraw();
                                    return;
                                }
                            }
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
	sessions = new CellTable<>(SESSION_KEY_PROVIDER);
	sessions.setWidth("60em", true);

	initializeSessionColumns();
    }

    final private static String ADD_SESSION_VALUE = "[ajouter session]";
    void addAddSessionSession() {
	int maxSeqno = -1;
	for (SessionSummary s : rawSessionData) {
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
	if (jdb.isClubSelected()) {
	    addSessionColumn(sessions, FIRST_CLASS_COLUMN, true);
	    addSessionColumn(sessions, FIRST_SIGNUP_COLUMN, true);
	    addSessionColumn(sessions, LAST_CLASS_COLUMN, true);
	    addSessionColumn(sessions, LAST_SIGNUP_COLUMN, true);
	} else {
            if (jdb.isAdmin) {
                addSessionColumn(sessions, SEQNO_COLUMN, false);
                addSessionColumn(sessions, LINKED_SEQNO_COLUMN, !jdb.isClubSelected());
            }
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
        rawSessionData.clear(); rawSessionData.addAll(sessionData);

        if (!jdb.isClubSelected()) {
            addAddSessionSession();
        }

        sessions.setRowData(sessionData);
        sessions.redraw();
        updateSessionToNameMapping();
        populateSessionsForPrix();
    }
    /* --- end session tab --- */

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
	cours = new CellTable<>(COURS_KEY_PROVIDER);
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
                        List<String> cs = new ArrayList<>();
                        StringBuffer edits = new StringBuffer();
                        for (SessionSummary ss : sessions) {
                            cs.add(ss.getAbbrev());
                            edits.append("-1,R," + ss.getSeqno() + "," +
                                         value + "," +
                                         jdb.getSelectedClubID() + ";");
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

                    List<SessionSummary> addedSessions = new ArrayList<>(),
                        removedSessions = new ArrayList<>();
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
                                         object.getShortDesc() + "," +
                                         jdb.getSelectedClubID() + ";");
                        }
                        pushEdit(edits.toString());
                        addAddCoursCours();
                    } else {
                        // add added sessions
                        StringBuffer edits = new StringBuffer();
                        for (SessionSummary ss : addedSessions) {
                            edits.append("-1,R," + ss.getSeqno() + "," +
                                         object.getShortDesc() + "," +
                                         jdb.getSelectedClubID() + ";");
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

    // requires sessions to be populated first
    private void populateCours(List<CoursSummary> coursArray) {
        // combine cours across sessions
        // l has keys shortdesc, values sessions
        // m has keys shortdesc, values ids
        HashMap<String, StringBuffer> l = new HashMap<>();
        HashMap<String, Set<String>> ll = new HashMap<>();
        HashMap<String, List<String>> m = new HashMap<>();
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
            if (ss == null) continue;
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
            cs.setId(String.valueOf(id)); cs.setShortDesc(s);
            cs.setSession(l.get(s).toString());
            coursShortDescToDbIds.put(cs.getShortDesc(), m.get(s));
            coursData.add(cs);
            id++;
        }

        addAddCoursCours();
        cours.setRowData(coursData);
        cours.redraw();
    }

    private void clearCours() {
        coursData.clear();
        cours.setRowData(coursData);
        cours.redraw();
    }
    /* --- end cours tab --- */

    /* --- prix tab --- */
    @UiField ButtonGroup prixSessionButtonGroup;
    @UiField Button prixSessionsButton;
    @UiField DropDownMenu prixSessions;
    @UiField ToggleSwitch ajustableCours;
    @UiField ToggleSwitch ajustableDivision;
    @UiField CellTable<CoursPrix> prix;

    // session: A15/H16/A15 H16 [H16 => copier A15]
    // next two visible when there is a club:
    // prix uniforme par cours O/N
    // prix uniforme par age O/N
    // when no club, take entries for Affiliation JQ

    // if all no:
    //            U8 U10 U12 U14 U16 U16N U18 U18N U20 U20N S SN
    // cours 1...
    // cours 2...

    // if not ajustable par cours:
    //            U8 U10 U12 U14 U16 U16N U18 U18N U20 U20N S SN
    // prix:     ...

    // if not ajustable par age:
    // cours 1...
    // cours 2 ...

    // prix cours is a function of: club, session, division, cours

    class CoursPrix {
        CoursSummary c;
        List<Prix> prix;
    }

    private static final Object AFFILIATION_PRIX_KEY = new Object();

    private static final ProvidesKey<CoursPrix> PRIX_KEY_PROVIDER =
        new ProvidesKey<CoursPrix>() {
        @Override
        public Object getKey(CoursPrix item) {
            if (item.c == null) return AFFILIATION_PRIX_KEY;
            return item.c.getId();
        }
    };

    void initializePrixTable() {
        ajustableCours.addValueChangeHandler(newValueChangeHandlerBoolean("ajustable_cours"));
        ajustableDivision.addValueChangeHandler(newValueChangeHandlerBoolean("ajustable_division"));
        prix = new CellTable<>(PRIX_KEY_PROVIDER);
    }

    private boolean isUnidivision() {
        return jdb.getSelectedClub() != null && !ajustableDivision.getValue();
    }

    void initializePrixUnidivisionColumn() {
        Column<CoursPrix, String> col = new Column<CoursPrix, String>(new EditTextCell()) {
            public String getValue(CoursPrix object) {
                for (Prix p : object.prix) {
                    if (p.getDivisionAbbrev().equals(CostCalculator.ALL_DIVISIONS))
                        return p.getFrais();
                }
                return "";
            }
        };
        col.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        prix.addColumn(col, "TOUS");
        prix.setColumnWidth(col, 3, Unit.EM);
        col.setFieldUpdater(new FieldUpdater<CoursPrix, String>() {
                @Override public void update(int index, CoursPrix object, String value) {
                    if (object == null || object.prix.size() < 1) return;

                    Prix p = null;
                    for (Prix pp : object.prix) {
                        if (pp.getDivisionAbbrev().equals(CostCalculator.ALL_DIVISIONS)) {
                            p = pp; break;
                        }
                    }
                    // should not happen; we should always have a match (even for new Prix)
                    if (p == null) return;
                    p.setFrais(value);

                    StringBuilder edits = new StringBuilder();
                    String coursId;
                    if (object.c == null) {
                        coursId = "-1";
                    } else {
                        coursId = object.c.getId();
                    }

                    if (p.getId().equals("0")) {
                        // new prix, not previously in db
                        edits.append("-1,P," +
                                     value + "," + p.getClubId() + "," + p.getSessionSeqno() + "," +
                                     CostCalculator.ALL_DIVISIONS + "," + coursId + ";");
                    } else {
                        edits.append("-1,p," + p.getId() + "," +
                                     value + "," + p.getClubId() + "," + p.getSessionSeqno() + "," +
                                     CostCalculator.ALL_DIVISIONS + "," + coursId + ";");
                    }
                    pushEdit(edits.toString());
                    refreshPrix = true;
                }
            });
    }

    void initializePrixDivisionColumns() {
        for (final Constants.Division d : Constants.DIVISIONS) {
            Column<CoursPrix, String> col = new Column<CoursPrix, String>(new EditTextCell()) {
                public String getValue(CoursPrix object) {
                    if (object == null) return "";

                    for (Prix p : object.prix) {
                        if (p.getDivisionAbbrev().equals(d.abbrev))
                            return p.getFrais();
                    }
                    return "";
                }
            };
            col.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
            prix.addColumn(col, d.abbrev);
            prix.setColumnWidth(col, 2, Unit.EM);
            col.setFieldUpdater(new FieldUpdater<CoursPrix, String>() {
                    @Override public void update(int index, CoursPrix object, String value) {
                        Prix p = null;
                        for (Prix pp : object.prix) {
                            if (pp.getDivisionAbbrev().equals(d.abbrev)) {
                                p = pp; break;
                            }
                        }
                        // should not happen; we should always have a match (even for new Prix)
                        if (p == null) return;
                        p.setFrais(value);

                        StringBuilder edits = new StringBuilder();
                        String coursId;
                        if (object.c == null) {
                            // indicate frais d'affiliation with coursId -1 (and clubId -1 also)
                            // note: on server side, only admin can write clubId -1
                            coursId = "-1";
                        } else {
                            coursId = object.c.getId();
                        }

                        if (p.getId().equals("0")) {
                            // new prix, not previously in db
                            edits.append("-1,P," +
                                         value + "," + p.getClubId() + "," + p.getSessionSeqno() + "," +
                                         p.getDivisionAbbrev() + "," + coursId + ";");
                        } else {
                            edits.append("-1,p," + p.getId() + "," +
                                         value + "," + p.getClubId() + "," + p.getSessionSeqno() + "," +
                                         p.getDivisionAbbrev() + "," + coursId + ";");
                        }
                        pushEdit(edits.toString());
                        refreshPrix = true;
                    }
                });
        }
    }

    void initializePrixColumns() {
        while (prix.getColumnCount() > 0)
            prix.removeColumn(0);

        Column<CoursPrix, String> coursColumn = new Column<CoursPrix, String>(new TextCell()) {
            public String getValue(CoursPrix object) {
                if (object == null) return "";
                if (jdb.getSelectedClub() != null && !ajustableCours.getValue()) return "TOUS";
                if (object.c == null || object.c.equals("")) return "Affiliation";
                return object.c.getShortDesc();
            }
        };
        prix.addColumn(coursColumn, "Cours");
        prix.setColumnWidth(coursColumn, 10, Unit.EM);

        if (isUnidivision()) {
            initializePrixUnidivisionColumn();
        } else {
            initializePrixDivisionColumns();
        }
    }

    private void populatePrix(List<Prix> lcp) {
        initializePrixColumns();
        rawPrixData.clear(); rawPrixData.addAll(lcp);
        populatePrixRows();
    }

    private void addPrixRow(CoursSummary cs) {
        String coursId = cs == null ? CostCalculator.ALL_COURS : cs.getId();
        List<Prix> ps =
            CostCalculator.getPrixForClubSessionCours
            (rawPrixData, jdb.getSelectedClubID(),
             currentPrixSeqnoString, coursId, isUnidivision());
        CoursPrix cp = new CoursPrix();
        cp.c = cs; cp.prix = ps;
        prixRows.add(cp);
    }

    private void populatePrixRows() {
        if (currentPrixSession == null) return;

        prixRows.clear();
        if (jdb.getSelectedClub() == null || !ajustableCours.getValue()) {
            addPrixRow(null);
        } else {
            for (CoursSummary cs : rawCoursData) {
                if (!coursApplicableToCurrentSession(cs))
                    continue;
                addPrixRow(cs);
            }
        }
        refreshPrix();
    }

    private void refreshPrix() {
        prix.setRowData(prixRows);
        prix.redraw();
    }

    private void clearPrix() {
        prixRows.clear();
        refreshPrix();
    }

    class SessionAnchorListItem extends AnchorListItem implements Comparable<SessionAnchorListItem> {
        int effective_seqno;
        boolean isPair;
        public SessionAnchorListItem(String label, int effective_seqno, boolean isPair) {
            super(label);
            this.effective_seqno = effective_seqno;
            this.isPair = isPair;
        }

        @Override public int compareTo(SessionAnchorListItem other) {
            return other.effective_seqno * 2 + (other.isPair ? 1 : 0) -
                (effective_seqno * 2 + (isPair ? 1 : 0));
        }
    }

    class SessionItemHandler implements ClickHandler {
        final SessionSummary session;
        final boolean isPair;

        SessionItemHandler(SessionSummary session, boolean isPair) {
            this.session = session;
            this.isPair = isPair;
        }

        @Override public void onClick(ClickEvent e) {
            selectSession(session, isPair);
        }
    }

    private AnchorListItem addSali(SessionSummary s, Set<SessionAnchorListItem> sss, int pos) {
        SessionAnchorListItem sali =
            new SessionAnchorListItem(s.getAbbrev(), pos, false);
        sali.addClickHandler(new SessionItemHandler(s, false));

        // only display paired sessions when in affiliation mode
        if (jdb.getSelectedClub() != null)
            sss.add(sali);

        if (s.isPrimary()) {
            StringBuilder a = new StringBuilder(s.getAbbrev());
            a.append(" ");
            a.append(JudoDB.getLinkedSession(s, rawSessionData).getAbbrev());

            SessionAnchorListItem sali2 =
                new SessionAnchorListItem(a.toString(), pos, true);
            sali2.addClickHandler(new SessionItemHandler(s, true));
            sss.add(sali2);
            return sali2;
        }
        return sali;
    }

    void populateSessionsForPrix() {
        prixSessions.clear();
        SessionSummary currentSession = null;

        Date today = new Date();
        TreeSet<SessionAnchorListItem> sss = new TreeSet<>();
        SessionSummary latest = null;

        for (SessionSummary s : rawSessionData) {
            if (latest == null || (s.isPrimary() && Integer.parseInt(s.getSeqno()) > Integer.parseInt(latest.getSeqno())))
                latest = s;

            try {
                Date inscrBegin = Constants.DB_DATE_FORMAT.parse(s.getFirstSignupDate());
                Date inscrEnd = Constants.DB_DATE_FORMAT.parse(s.getLastSignupDate());
                if (today.after(inscrBegin) && today.before(inscrEnd)) {
                    currentSession = s; continue;
                }
            } catch (IllegalArgumentException e) {}

            addSali(s, sss, Integer.parseInt(s.getSeqno()));
        }

        AnchorListItem cs = null;
        if (currentSession != null) {
            cs = addSali(currentSession, sss, Integer.parseInt(currentSession.getSeqno()));
            prixSessions.add(cs);
        }

        for (AnchorListItem s : sss) {
            if (s != cs) {
                prixSessions.add(s);
            }
        }

        if (currentSession != null)
            selectSession(currentSession, true);
        else {
            // isPair certainly true when in affiliation mode (club == TOUS)
            // otherwise we might not have a pairee...
            selectSession(latest, jdb.getSelectedClub() == null);
        }
    }

    private void selectSession(SessionSummary session, boolean isPair) {
        SessionSummary primary;
        if (session.isPrimary())
            primary = session;
        else
            primary = JudoDB.getLinkedSession(session, rawSessionData);

        StringBuilder a = new StringBuilder(primary.getAbbrev());
        if (isPair) {
            a.append(" ");
            a.append(JudoDB.getLinkedSession(primary, rawSessionData).getAbbrev());
        }
        prixSessionsButton.setText(a.toString());
        currentPrixSeqnoString = JudoDB.sessionSeqnosFromAbbrevs(a.toString(), rawSessionData);
        currentPrixSession = session;
        this.isPairPrixSession = isPair;
        populatePrixRows();
    }

    private boolean coursApplicableToCurrentSession(CoursSummary cs) {
        if (cs.getSession().equals(currentPrixSession.getSeqno()) ||
            cs.getSession().equals(currentPrixSession.getLinkedSeqno()))
            return true;
        return false;
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
        escomptes = new CellTable<>(ESCOMPTE_KEY_PROVIDER);
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
                        String otherValue = "";
                        if (value.equals("-1")) otherValue = "-1";
                        if (c.key.equals(AMOUNT_PERCENT_COLUMN.key)) {
                            String k = AMOUNT_ABSOLUTE_COLUMN.key;
                            edits.append("-1,z" + k + "," +
                                         object.getId() + "," + otherValue + "," +
                                         jdb.getSelectedClubID() + ";");
                            object.set(k, otherValue);
                        }
                        else if (c.key.equals(AMOUNT_ABSOLUTE_COLUMN.key)) {
                            String k = AMOUNT_PERCENT_COLUMN.key;
                            edits.append("-1,z" + k + "," +
                                         object.getId() + "," + otherValue + "," +
                                         jdb.getSelectedClubID() + ";");
                            object.set(k, otherValue);
                        }
                        edits.append("-1,z" + c.key + "," + object.getId() + "," +
                                     value + "," + jdb.getSelectedClubID() + ";");
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

    private void clearEscomptes() {
        escompteData.clear();
        escomptes.setRowData(escompteData);
        escomptes.redraw();
    }
    /* --- end escompte tab --- */

    /* --- produits detail tab --- */
    private static final ProvidesKey<ProduitSummary> PRODUIT_KEY_PROVIDER =
        new ProvidesKey<ProduitSummary>() {
        @Override
        public Object getKey(ProduitSummary item) {
            return item.getId();
        }
    };

    private final ColumnFields NOM_PRODUIT_COLUMN = new ColumnFields("nom", "Nom", 2, Unit.EM),
        MONTANT_COLUMN = new ColumnFields("montant", "Montant", 1, Unit.EM);

	private List<ColumnFields> perProduitColumns = Collections.unmodifiableList(Arrays.asList(NOM_PRODUIT_COLUMN, MONTANT_COLUMN));

    void initializeProduitTable() {
        produits = new CellTable<>(PRODUIT_KEY_PROVIDER);
        produits.setWidth("60em", true);

        initializeProduitColumns();
    }

    private Column<ProduitSummary, String> addProduitColumn(final CellTable t, final ColumnFields c, final boolean editable) {
        final Cell<String> cell = editable ? new EditTextCell() : new TextCell();
        Column<ProduitSummary, String> newColumn = new Column<ProduitSummary, String>(cell) {
            public String getValue(ProduitSummary object) {
                return object.get(c.key);
            }
        };
        produits.addColumn(newColumn, c.name);
        newColumn.setFieldUpdater(new FieldUpdater<ProduitSummary, String>() {
                @Override
                public void update(int index, ProduitSummary object, String value) {
                    refreshProduits = true;
                    if (object.get(NOM_PRODUIT_COLUMN.key).equals(ADD_PRODUIT_VALUE)) {
                        // NB: do the set before the update because we read from object
                        String nom;
                        if (c.key.equals(MONTANT_COLUMN.key)) {
                            nom = "Produit "+object.getId();
                            object.set(c.key, value);
                        } else {
                            nom = value;
                            object.set(c.key, value);
                        }
                        pushEdit("-1,J," + object.getId() + "," +
                                 nom + "," +
                                 object.get(MONTANT_COLUMN.key) + "," +
                                 jdb.getSelectedClubID() + ";");
                        addAddProduitProduit();
                    } else {
                        StringBuffer edits = new StringBuffer();
                        if (c.key.equals(MONTANT_COLUMN.key)) {
                            String k = MONTANT_COLUMN.key;
                            edits.append("-1,j" + k + "," + object.getId() + "," + "" + "," + jdb.getSelectedClubID() + ";");
                            object.set(k, "");
                        }
                        edits.append("-1,j" + c.key + "," + object.getId() + "," + value + "," + jdb.getSelectedClubID() + ";");
                        pushEdit(edits.toString());
                        object.set(c.key, value);
                    }
                    t.redraw();
                }
            });
        produits.setColumnWidth(newColumn, c.width, c.widthUnits);
        return newColumn;
    }

    void initializeProduitColumns() {
        while (produits.getColumnCount() > 0)
            produits.removeColumn(0);

        addProduitColumn(produits, NOM_PRODUIT_COLUMN, true);
        addProduitColumn(produits, MONTANT_COLUMN, true);
    }

    final private static String ADD_PRODUIT_VALUE = "[ajouter produit]";
    void addAddProduitProduit() {
        int maxId = 0;
        for (ProduitSummary c : produitData) {
            if (Integer.parseInt(c.getId()) > maxId)
                maxId = Integer.parseInt(c.getId());
        }

        ProduitSummary addNewProduit =
            JsonUtils.<ProduitSummary>safeEval
            ("{\"id\":\""+(maxId+1)+"\"}");
        addNewProduit.setClubId(jdb.getSelectedClubID());
        addNewProduit.setNom(ADD_PRODUIT_VALUE);
        addNewProduit.setMontant("");
        produitData.add(addNewProduit);
    }

    private void populateProduits(List<ProduitSummary> produitArray) {
        initializeProduitColumns();

        produitData.clear();
        for (ProduitSummary es : produitArray) {
            produitData.add(es);
        }

        if (jdb.isClubSelected())
            addAddProduitProduit();
        produits.setRowData(produitData);
        produits.redraw();
    }

    private void clearProduits() {
        produitData.clear();
        produits.setRowData(produitData);
        produits.redraw();
    }
    /* --- end produits detail tab --- */

    /* --- network functions --- */
    private boolean gotSessions = false;
    public void retrieveSessions(String club_id) {
        String url = JudoDB.PULL_SESSIONS_URL;
        url += "?club_id=" + club_id;
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

    public void retrievePrix(final String club_id) {
        if (!gotSessions) {
            new Timer() {
                public void run() { retrievePrix(club_id); }
            }.schedule(100);
            return;
        }

        prixRows.clear();
        rawPrixData.clear();

        ClubSummary cs = jdb.getClubSummaryByID(jdb.getSelectedClubID());
        String url = JudoDB.PULL_CLUB_PRIX_URL +
            "?club_id=" + club_id;

        RequestCallback rc =
            jdb.createRequestCallback(new JudoDB.Function() {
                    public void eval(String s) {
                        List<Prix> lp = new ArrayList<Prix>();
                        JsArray<Prix> cp = JsonUtils.<JsArray<Prix>>safeEval(s);
                        for (int i = 0; i < cp.length(); i++)
                            lp.add(cp.get(i));
                        populatePrix(lp);
                    }
                });
        jdb.retrieve(url, rc);
    }

    private boolean gotCours = false;
    // requires sessions
    public void retrieveCours(final String club_id) {
        rawCoursData.clear(); coursData.clear();
        if (club_id.equals("")) return;

        if (!gotSessions) {
            new Timer() {
                public void run() { retrieveCours(club_id); }
            }.schedule(100);
            return;
        }

        String url = JudoDB.PULL_CLUB_COURS_URL;
        url += "?club_id="+club_id;
        RequestCallback rc =
            jdb.createRequestCallback(new JudoDB.Function() {
                    public void eval(String s) {
                        gotCours = true;
                        List<CoursSummary> lcs = new ArrayList<>();
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
			List<EscompteSummary> les = new ArrayList<>();
			JsArray<EscompteSummary> jes = JsonUtils.<JsArray<EscompteSummary>>safeEval(s);
			for (int i = 0; i < jes.length(); i++)
			    les.add(jes.get(i));
			populateEscomptes(les);
			jdb.clearStatus();
                    }
                });
        jdb.retrieve(url, rc);
    }

    private boolean gotProduits = false;
    public void retrieveProduits(String club_id) {
	if (club_id.equals("")) return;

        String url = JudoDB.PULL_PRODUIT_URL;
        url += "?club_id="+club_id;
        RequestCallback rc =
            jdb.createRequestCallback(new JudoDB.Function() {
                    public void eval(String s) {
			gotProduits = true;
			List<ProduitSummary> les = new ArrayList<>();
			JsArray<ProduitSummary> jes = JsonUtils.<JsArray<ProduitSummary>>safeEval(s);
			for (int i = 0; i < jes.length(); i++)
			    les.add(jes.get(i));
			populateProduits(les);
			jdb.clearStatus();
                    }
                });
        jdb.retrieve(url, rc);
    }

    private boolean refreshClub = false;
    private boolean refreshSessions = false;
    private boolean refreshCours = false;
    private boolean refreshPrix = false;
    private boolean refreshEscomptes = false;
    private boolean refreshProduits = false;
    public void pushChanges(final String guid) {
        String url = JudoDB.CONFIRM_PUSH_URL + "?guid=" + guid;
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
                            if (refreshClub) {
                                refreshClub = false;
                                jdb.retrieveClubList(true);
                            }
			    if (refreshSessions) {
				refreshSessions = false;
				retrieveSessions(jdb.getSelectedClub().getNumeroClub());
			    }
			    if (refreshCours) {
				refreshCours = false;
				ClubSummary cs = jdb.getClubSummaryByID(jdb.getSelectedClubID());
				if (cs != null) {
				    retrieveCours(cs.getId());
				}
			    }
			    if (refreshPrix) {
                                refreshPrix = false;
                                ClubSummary cs = jdb.getClubSummaryByID(jdb.getSelectedClubID());
                                if (cs != null) {
                                    retrievePrix(cs.getId());
                                } else {
                                    retrievePrix("0");
                                }
			    }
			    if (refreshEscomptes) {
				refreshEscomptes = false;
				retrieveEscomptes(jdb.getSelectedClubID());
			    }
			    if (refreshProduits) {
				refreshProduits = false;
				retrieveProduits(jdb.getSelectedClubID());
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
