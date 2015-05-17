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
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.http.client.RequestCallback;

public class ConfigWidget extends Composite {
    interface MyUiBinder extends UiBinder<Widget, ConfigWidget> {}
    public static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    private final JudoDB jdb;
    private static final String PULL_SESSIONS_URL = JudoDB.BASE_URL + "pull_sessions.php";

    @UiField Grid sessions;

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
        dropDownUserClubs.addChangeHandler(clHandler);
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

        sessions.resize(sessionArray.length(), 9);
	// com.google.gwt.user.client.Window.alert(Integer.toString(sessionArray.length()));
        for (int i = 0; i < sessionArray.length(); i++) {
            SessionSummary s = sessionArray.get(i);

            sessions.setText(i, SessionColumns.SEQNO, s.getSeqno());
            sessions.setText(i, SessionColumns.NAME, s.getName());
            sessions.setText(i, SessionColumns.ABBREV, s.getAbbrev());
            sessions.setText(i, SessionColumns.YEAR, s.getYear());
            sessions.setText(i, SessionColumns.LINKED_SEQNO, s.getLinkedSeqno());
            sessions.setText(i, SessionColumns.FIRST_CLASS_DATE, s.getFirstClassDate());
            sessions.setText(i, SessionColumns.FIRST_SIGNUP_DATE, s.getFirstSignupDate());
            sessions.setText(i, SessionColumns.LAST_CLASS_DATE, s.getLastClassDate());
            sessions.setText(i, SessionColumns.LAST_SIGNUP_DATE, s.getLastSignupDate());

            //cours.addItem(c.getShortDesc(), c.getId());
            //backingCours.add(c);
        }
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
