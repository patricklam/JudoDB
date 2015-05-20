package ca.patricklam.judodb.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ResizeLayoutPanel;
import com.google.gwt.user.client.ui.TabLayoutPanel;
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

public class ConfigWidget extends Composite {
    interface MyUiBinder extends UiBinder<Widget, ConfigWidget> {}
    public static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiField FlowPanel configMain;

    private final JudoDB jdb;
    private static final String PULL_SESSIONS_URL = JudoDB.BASE_URL + "pull_sessions.php";

    CellTable sessions;
    private static final List<SessionSummary> sessionData = new ArrayList<SessionSummary>();

    // private static final String PULL_CONFIG_URL = JudoDB.BASE_URL + "pull_config.php";
    // private static final String PUSH_CONFIG_URL = JudoDB.BASE_URL + "push_config.php";

    // useful URLs: http://www.filsa.net/2010/01/23/more-on-tablayoutpanel/
    // http://www.filsa.net/2010/01/21/gwt-notes-tablayoutpanel/

    // club data:
    // nom du club
    // numero (Judo QC) du club

    // montants (should theoretically be stored with the session)
    // passeport judo QC [5]
    // frais non-resident [5]
    // penalty for prorata (percent of cost) [5]

    // misc:
    // age veteran [35]

    // for each session:
    // seqno
    // abbrev [A12]
    // effective_year [2013]
    // debut_cours, fin_cours [newDate("1/Sep/2012"), newDate("11/May/2013"))]
    // liste des prix:
    //  division, 1 session, 2 sessions, judoQC

    // for each division:
    // name [Junior]
    // abbrev [U20N]
    // years_ago [20]
    // noire [true]
    // aka [U20]

    // for each cours:
    //      new Cours("0", "Adultes (LM2015-2145, V2000-2145)", "LM2015 V2000", ""),
    // seqno [0]
    // name [Adultes (LM2015-2145, V2000-2145)]
    // abbrev [LM2015 V2000]
    // entraineur
    // which sessions does the cours apply to?

    // for each escompte:
    // seqno
    // name [2e membre]
    // percent [10]

    @UiField ListBox dropDownUserClubs;
    private ClubListHandler clHandler = new ClubListHandler();

    class ClubListHandler implements ChangeHandler {
      public void onChange(ChangeEvent e) {
        jdb.selectedClub = dropDownUserClubs.getSelectedIndex();
	retrieveSessions(jdb.selectedClub);
      }
    }

    public ConfigWidget(JudoDB jdb) {
        this.jdb = jdb;
        initWidget(uiBinder.createAndBindUi(this));
        jdb.pleaseWait();
        jdb.populateClubList(true, dropDownUserClubs);
	retrieveSessions(jdb.selectedClub);

        dropDownUserClubs.addChangeHandler(clHandler);

	initializeSessionTable();
	configMain.add(sessions);
    }

    /* --- session table --- */

    class SessionSummaryFieldUpdaterExample {
    }

    private static final ProvidesKey<SessionSummary> KEY_PROVIDER =
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

    private final ColumnFields NAME_COLUMN = new ColumnFields("name", "Nom", 10, Unit.EM),
	ABBREV_COLUMN = new ColumnFields("abbrev", "Abbr", 4, Unit.EM),
	YEAR_COLUMN = new ColumnFields("year", "Année", 4, Unit.EM),
	SEQNO_COLUMN = new ColumnFields("seqno", "no seq", 3, Unit.EM),
	LINKED_SEQNO_COLUMN = new ColumnFields("linkedSeqno", "seq alt", 3, Unit.EM),
	FIRST_CLASS_COLUMN = new ColumnFields("firstClassDate", "début cours" , 10, Unit.EM),
	FIRST_SIGNUP_COLUMN = new ColumnFields("firstSignupDate", "début inscription", 10, Unit.EM),
	LAST_CLASS_COLUMN = new ColumnFields("lastClassDate", "fin cours", 10, Unit.EM),
	LAST_SIGNUP_COLUMN = new ColumnFields("lastSignupDate", "fin inscription", 10, Unit.EM);

    private Column<SessionSummary, String> addSessionColumn(final CellTable t, final ColumnFields c, final boolean editable) {
	final Cell<String> cell = editable ? new EditTextCell() : new TextCell();
	Column<SessionSummary, String> newColumn = new Column<SessionSummary, String>(cell) {
	    public String getValue(SessionSummary object) {
		return object.get(c.key);
	    }
	};
	sessions.addColumn(newColumn, c.name);
	newColumn.setFieldUpdater(new FieldUpdater<SessionSummary, String>() {
		@Override
		public void update(int index, SessionSummary object, String value) {
		    // XXX push the new name also
		    object.set(c.key, value);
		    t.redraw();
		}
	    });
	sessions.setColumnWidth(newColumn, c.width, c.widthUnits);
	return newColumn;
    }

    void initializeSessionTable() {
	sessions = new CellTable<SessionSummary>(KEY_PROVIDER);
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
			addAddSessionSession();
		    }
		    // XXX push the new name also
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
    }
    /* --- end session table --- */

    private void loadSessions(JsArray<SessionSummary> sessionArray) {
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
    }

    /* --- network functions --- */
    private boolean gotSessions = false;
    public void retrieveSessions(int numero_club) {
        String url = PULL_SESSIONS_URL;
        url += "?club="+numero_club;
        RequestCallback rc =
            jdb.createRequestCallback(new JudoDB.Function() {
                    public void eval(String s) {
                        gotSessions = true;
                        loadSessions(JsonUtils.<JsArray<SessionSummary>>safeEval(s));
			jdb.clearStatus();
                    }
                });
        jdb.retrieve(url, rc);
    }


}
