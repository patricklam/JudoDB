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
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.http.client.RequestCallback;
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

    class SessionSummaryFieldUpdaterExample {
    }

    private static final ProvidesKey<SessionSummary> KEY_PROVIDER =
	new ProvidesKey<SessionSummary>() {
        @Override
        public Object getKey(SessionSummary item) {
	    return item.getSeqno();
        }
    };

    interface ColumnFields<T> {
	public String getValue(T object);
	public void setValue(T object, String newValue);
	public String getName();
	public int getWidth();
	public Unit getWidthUnits();
    }

    private class NameColumn implements ColumnFields<SessionSummary> {
	@Override public String getValue(SessionSummary object)
	    { return object.getName(); }
	@Override public void setValue(SessionSummary object, String newValue)
	    { object.setName(newValue); }
	@Override public String getName()
	    { return "Nom"; }
	@Override public int getWidth()
	    { return 15; }
	@Override public Unit getWidthUnits()
	    { return Unit.EM; }
    }
    private class AbbrevColumn implements ColumnFields<SessionSummary> {
	@Override public String getValue(SessionSummary object)
	    { return object.getAbbrev(); }
	@Override public void setValue(SessionSummary object, String newValue)
	    { object.setAbbrev(newValue); }
	@Override public String getName()
	    { return "Abbr"; }
	@Override public int getWidth()
	    { return 4; }
	@Override public Unit getWidthUnits()
	    { return Unit.EM; }
    }
    private class YearColumn implements ColumnFields<SessionSummary> {
	@Override public String getValue(SessionSummary object)
	    { return object.getYear(); }
	@Override public void setValue(SessionSummary object, String newValue)
	    { object.setYear(newValue); }
	@Override public String getName()
	    { return "Année"; }
	@Override public int getWidth()
	    { return 4; }
	@Override public Unit getWidthUnits()
	    { return Unit.EM; }
    }
    private class SeqnoColumn implements ColumnFields<SessionSummary> {
	@Override public String getValue(SessionSummary object)
	    { return object.getSeqno(); }
	@Override public void setValue(SessionSummary object, String newValue)
	    { }
	@Override public String getName()
	    { return "no seq"; }
	@Override public int getWidth()
	    { return 3; }
	@Override public Unit getWidthUnits()
	    { return Unit.EM; }
    }
    private class LinkedSeqnoColumn implements ColumnFields<SessionSummary> {
	@Override public String getValue(SessionSummary object)
	    { return object.getLinkedSeqno(); }
	@Override public void setValue(SessionSummary object, String newValue)
	    { object.setLinkedSeqno(newValue); }
	@Override public String getName()
	    { return "seq alt"; }
	@Override public int getWidth()
	    { return 3; }
	@Override public Unit getWidthUnits()
	    { return Unit.EM; }
    }
    private class FirstClassDateColumn implements ColumnFields<SessionSummary> {
	@Override public String getValue(SessionSummary object)
	    { return object.getFirstClassDate(); }
	@Override public void setValue(SessionSummary object, String newValue)
	    { object.setFirstClassDate(newValue); }
	@Override public String getName()
	    { return "debut cours"; }
	@Override public int getWidth()
	    { return 6; }
	@Override public Unit getWidthUnits()
	    { return Unit.EM; }
    }
    private class FirstSignupDateColumn implements ColumnFields<SessionSummary> {
	@Override public String getValue(SessionSummary object)
	    { return object.getFirstSignupDate(); }
	@Override public void setValue(SessionSummary object, String newValue)
	    { object.setFirstSignupDate(newValue); }
	@Override public String getName()
	    { return "debut inscription"; }
	@Override public int getWidth()
	    { return 6; }
	@Override public Unit getWidthUnits()
	    { return Unit.EM; }
    }
    private class LastClassDateColumn implements ColumnFields<SessionSummary> {
	@Override public String getValue(SessionSummary object)
	    { return object.getLastClassDate(); }
	@Override public void setValue(SessionSummary object, String newValue)
	    { object.setLastClassDate(newValue); }
	@Override public String getName()
	    { return "fin cours"; }
	@Override public int getWidth()
	    { return 6; }
	@Override public Unit getWidthUnits()
	    { return Unit.EM; }
    }
    private class LastSignupDateColumn implements ColumnFields<SessionSummary> {
	@Override public String getValue(SessionSummary object)
	    { return object.getLastSignupDate(); }
	@Override public void setValue(SessionSummary object, String newValue)
	    { object.setLastSignupDate(newValue); }
	@Override public String getName()
	    { return "fin inscription"; }
	@Override public int getWidth()
	    { return 6; }
	@Override public Unit getWidthUnits()
	    { return Unit.EM; }
    }

    private void addSessionColumn(final CellTable t, final ColumnFields<SessionSummary> c) {
	final EditTextCell cell = new EditTextCell();
	Column<SessionSummary, String> newColumn = new Column<SessionSummary, String>(cell) {
	    public String getValue(SessionSummary object) {
		return c.getValue(object);
	    }
	};
	sessions.addColumn(newColumn, c.getName());
	newColumn.setFieldUpdater(new FieldUpdater<SessionSummary, String>() {
		@Override
		public void update(int index, SessionSummary object, String value) {
		    // XXX push the new name also
		    c.setValue(object, value);
		    t.redraw();
		}
	    });
	sessions.setColumnWidth(newColumn, c.getWidth(), c.getWidthUnits());
    }

    public ConfigWidget(JudoDB jdb) {
        this.jdb = jdb;
        initWidget(uiBinder.createAndBindUi(this));
        jdb.pleaseWait();
        jdb.populateClubList(true, dropDownUserClubs);
        dropDownUserClubs.addChangeHandler(clHandler);

	sessions = new CellTable<SessionSummary>(KEY_PROVIDER);
	sessions.setWidth("60em", true);

	addSessionColumn(sessions, new NameColumn());
	addSessionColumn(sessions, new AbbrevColumn());
	addSessionColumn(sessions, new YearColumn());
	addSessionColumn(sessions, new SeqnoColumn());
	addSessionColumn(sessions, new LinkedSeqnoColumn());
	addSessionColumn(sessions, new FirstClassDateColumn());
	addSessionColumn(sessions, new FirstSignupDateColumn());
	addSessionColumn(sessions, new LastClassDateColumn());
	addSessionColumn(sessions, new LastSignupDateColumn());

	sessions.setRowData(sessionData);

	configMain.add(sessions);
    }

    private class SessionColumns {
        final static int SEQNO = 0;
        final static int NAME = 1;
        final static int ABBREV = 2;
        final static int YEAR = 3;
        final static int LINKED_SEQNO = 4;
        final static int FIRST_CLASS_DATE = 5;
        final static int FIRST_SIGNUP_DATE = 6;
        final static int LAST_CLASS_DATE = 7;
        final static int LAST_SIGNUP_DATE = 8;
    }

    private void loadSessions(JsArray<SessionSummary> sessionArray) {
	sessionData.clear();
        for (int i = 0; i < sessionArray.length(); i++) {
	    sessionData.add(sessionArray.get(i));
	}
	sessions.setRowData(sessionData);
	sessions.redraw();
    }

    /* --- network functions --- */
    private boolean gotSessions = false;
    public void retrieveSessions(int numero_club) {
	if (numero_club == 0) return;
        // backingCours.clear();
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
