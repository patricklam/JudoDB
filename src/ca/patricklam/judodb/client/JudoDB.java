// -*-  indent-tabs-mode:nil; c-basic-offset:4; -*-
package ca.patricklam.judodb.client;

import java.util.ArrayList;
import java.util.Stack;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.datepicker.client.CalendarUtil;

import com.google.gwt.user.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.constants.AlertType;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class JudoDB implements EntryPoint {
    static final Logger logger = Logger.getLogger("");

    static class Mode {
        public static final String LIST_PARAM_FT303 = "ft303";
        public static final String LIST_PARAM_IMPOT = "impot";
        public static final String LIST_PARAM_AFFIL = "affil";
        enum ActualMode {
            MAIN("main"), LIST("list"), EDIT_CLIENT("edit"), CONFIG("config");
            final String label;
            ActualMode(String label) { this.label = label; }
            boolean matchesLabel(String s) {
                if (s.startsWith(label))
                    return true;
                return false;
            }
        };
        ActualMode am;
        String arg;

        public Mode(ActualMode am) { this.am = am; }
        public Mode(ActualMode am, String arg) {
            this.am = am; this.arg = arg;
        }

        public String toString() {
            if (am == ActualMode.EDIT_CLIENT) return am.label + arg;
            if (am == ActualMode.LIST) return am.label + arg;
            return am.label;
        }

        static Mode parse(String s) {
            for (ActualMode m : ActualMode.values())
                if (m.matchesLabel(s)) {
                    if (m == ActualMode.EDIT_CLIENT || m == ActualMode.LIST) {
                        String n = s.substring(m.label.length());
                        return new Mode(m, n);
                    } else {
                        return new Mode(m);
                    }
                }
            return new Mode(ActualMode.MAIN);
        }
    };

    public static final int MAX_RESULTS = 20;

    public static final String BACKEND_SUFFIX = "backend/";
    public static final String BASE_URL = GWT.getHostPageBaseURL() + BACKEND_SUFFIX;

    private static final String DO_BACKUP_URL = BASE_URL + "db_backup.php";
    private static final String PULL_CLIENT_LIST_URL = BASE_URL + "pull_client_list.php";
    public static final String PULL_CLUB_LIST_URL = BASE_URL + "pull_club_list.php";
    public static final String PULL_CLUB_COURS_URL = JudoDB.BASE_URL + "pull_club_cours.php";
    public static final String PULL_SESSIONS_URL = JudoDB.BASE_URL + "pull_sessions.php";
    public static final String PULL_CLUB_PRIX_URL = JudoDB.BASE_URL + "pull_club_prix.php";
    public static final String PULL_TARIF_URL = JudoDB.BASE_URL + "pull_tarif.php";
    public static final String PULL_ESCOMPTE_URL = JudoDB.BASE_URL + "pull_escompte.php";
    public static final String PULL_PRODUIT_URL = JudoDB.BASE_URL + "pull_produit.php";
    public static final String PUSH_MULTI_CLIENTS_URL = JudoDB.BASE_URL + "push_multi_clients.php";
    public static final String CONFIRM_PUSH_URL = JudoDB.BASE_URL + "confirm_push.php";
    int jsonRequestId = 0;

    /* main layout */
    final MainPanel mainPanel = new MainPanel();

    /* state */
    boolean isAdmin;
    List<ClientSummary> allClients;
    List<ClubSummary> allClubs;
    private String searchString;
    private int firstSearchResultToDisplay = 0;
    private Stack<Mode> modeStack = new Stack<Mode>();
    private Mode currentMode = null;

    /* these go in their respective ScrollPanels */
    private ClientWidget clientWidget;
    private ListWidget listWidget;
    private ConfigWidget cfWidget;

    // Create a handler for the searchButton and nameField
    class SearchHandler implements ClickHandler, KeyUpHandler {
        public void onClick(ClickEvent event) {
            refreshClientListAndFilter();
        }

        public void onKeyUp(KeyUpEvent event) {
            if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                refreshClientListAndFilter();
            }
        }

        private void refreshClientListAndFilter() {
            pleaseWait();
            retrieveClientList(true);
        }
    }

    class EditClientHandler implements ClickHandler {
        private ClubSummary club;
        private int cid;
        public EditClientHandler(ClubSummary club, int cid) { this.club = club; this.cid = cid; }

        public void onClick(ClickEvent event) {
            if (club != null)
                selectClub(club);
            switchMode(new Mode (Mode.ActualMode.EDIT_CLIENT, Integer.toString(cid)));
        }
    }

    // modes
    public void switchMode(Mode newMode) {
        Mode previousMode = currentMode;
        if (currentMode != null) {
            modeStack.push(currentMode);
        }
        currentMode = newMode;
        _switchMode(newMode, previousMode);
    }

    public void historySwitchMode(Mode newMode) {
        // remove the entire stack and start over
        modeStack.clear();

        Mode previousMode = currentMode;
        if (currentMode != null) {
            modeStack.push(currentMode);
        }
        currentMode = newMode;
        _switchMode(newMode, previousMode);
    }

    public void popMode() {
        if (!modeStack.isEmpty()) {
            Mode previousMode = currentMode;
            if (!modeStack.isEmpty()) {
                currentMode = modeStack.pop();
            } else {
                currentMode = new Mode(Mode.ActualMode.MAIN);
            }
            _switchMode(currentMode, previousMode);
        } else {
            _switchMode(new Mode(Mode.ActualMode.MAIN), null);
        }
    }

    private void _switchMode(Mode newMode, Mode previousMode) {
        if (newMode != null && previousMode != null &&
            newMode.am.equals(previousMode.am)) {
            History.replaceItem(newMode.toString(), false);
        } else {
            History.newItem(newMode.toString(), false);
        }

        switch (newMode.am) {
        case EDIT_CLIENT:
            _switchMode_editClient(newMode.arg);
            break;
        case LIST:
            _switchMode_viewLists(newMode.arg);
            break;
        case MAIN:
            _switchMode_main();
            break;
        case CONFIG:
            _switchMode_config();
            break;
        }
    }

    private void hideAllSubpanels() {
        mainPanel.editClient.setVisible(false);
        mainPanel.lists.setVisible(false);
        mainPanel.config.setVisible(false);
        mainPanel.mainPanel.setVisible(false);
    }

    private void _switchMode_editClient(String cid) {
        mainPanel.editClient.clear();
        this.clientWidget = new ClientWidget(Integer.parseInt(cid), this);
        mainPanel.editClient.add(this.clientWidget);

        hideAllSubpanels();
        mainPanel.editClient.setVisible(true);
    }

    public void _switchMode_viewLists(String arg) {
        if (this.listWidget == null) {
            this.listWidget = new ListWidget(this, arg);
            mainPanel.lists.add(this.listWidget);
        } else {
            this.listWidget.processArg(arg);
            this.listWidget.selectClub(selectedClub);
        }

        hideAllSubpanels();
        mainPanel.lists.setVisible(true);
   }

    public void _switchMode_config() {
        this.cfWidget.selectClub(selectedClub);

        hideAllSubpanels();
        mainPanel.config.setVisible(true);
    }

    public void _switchMode_main() {
        clearStatus();

        hideAllSubpanels();
        mainPanel.mainPanel.setVisible(true);

        mainPanel.dropDownUserClubs.setVisible(true);
        mainPanel.searchButton.setEnabled(true);
        mainPanel.searchTextBox.setFocus(true);
        mainPanel.searchResultsPanel.setVisible(false);
    }

    /** After changing any data, invalidate stored data. */
    public void invalidateListWidget() {
        if (this.listWidget != null) {
            this.listWidget.removeFromParent();
            this.listWidget = null;
        }

        if (mainPanel.searchResults != null) {
            mainPanel.searchResults.removeAllRows();
            firstSearchResultToDisplay = 0;
        }
    }

    public void doBackup() {
        String url = DO_BACKUP_URL;
        RequestCallback rc =
            createRequestCallback(new JudoDB.Function() {
                    public void eval(String s) {
                        setStatus("[backup] " + s);
                        new com.google.gwt.user.client.Timer() { public void run() {
                            clearStatus();
                        } }.schedule(5000);
                    }
                });
        retrieve(url, rc);
    }

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        // handle exceptions
        //if(false)
        GWT.setUncaughtExceptionHandler(new
                                        GWT.UncaughtExceptionHandler() {
                public void onUncaughtException(Throwable e) {
                    Throwable unwrapped = unwrap(e);
                    // do exception handling stuff
                    StringBuffer sb = new StringBuffer();
                    for (StackTraceElement element : unwrapped.getStackTrace()) {
                        sb.append(element + "\n");
                    }
                    com.google.gwt.user.client.Window.alert("got stack trace "+sb.toString());
                }

                public Throwable unwrap(Throwable e) {
                    if(e instanceof com.google.gwt.event.shared.UmbrellaException) {
                        com.google.gwt.event.shared.UmbrellaException ue =
                            (com.google.gwt.event.shared.UmbrellaException) e;
                        if(ue.getCauses().size() == 1) {
                            return unwrap(ue.getCauses().iterator().next());
                        }
                    }
                    return e;
                }
            });

	RootPanel.get().add(mainPanel);

        // main panel itself
	mainPanel.versionLabel.setText(Version.VERSION);

        mainPanel.nextResultsButton.setVisible(false);
        mainPanel.nextResultsButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent e) {
                firstSearchResultToDisplay += MAX_RESULTS;
                if (firstSearchResultToDisplay > allClients.size()) firstSearchResultToDisplay -= MAX_RESULTS;
                displaySearchResults(); } });
        mainPanel.prevResultsButton.setVisible(false);
        mainPanel.prevResultsButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent e) {
                firstSearchResultToDisplay -= MAX_RESULTS;
                if (firstSearchResultToDisplay < 0) firstSearchResultToDisplay = 0;
                displaySearchResults(); } });

        mainPanel.searchResultsPanel.setVisible(false);

        mainPanel.listeButton.addClickHandler(new ClickHandler() { public void onClick(ClickEvent e) {
            String clubString = selectedClub != null ? (";" + ListWidget.CLUB_LABEL + selectedClub.getNumeroClub()) : "";
            switchMode(new Mode(Mode.ActualMode.LIST, clubString)); }});
        mainPanel.affilButton.addClickHandler(new ClickHandler() { public void onClick(ClickEvent e) {
            String clubString = selectedClub != null ? (";" + ListWidget.CLUB_LABEL + selectedClub.getNumeroClub()) : "";
            switchMode(new Mode(Mode.ActualMode.LIST, Mode.LIST_PARAM_AFFIL + clubString));
        }});
        mainPanel.ftButton.addClickHandler(new ClickHandler() { public void onClick(ClickEvent e) {
            String clubString = selectedClub != null ? (";" + ListWidget.CLUB_LABEL + selectedClub.getNumeroClub()) : "";
            switchMode(new Mode(Mode.ActualMode.LIST, Mode.LIST_PARAM_FT303 + clubString));
        }});
        mainPanel.configButton.addClickHandler(new ClickHandler() { public void onClick(ClickEvent e) { switchMode(new Mode(Mode.ActualMode.CONFIG)); }});
        mainPanel.logoutButton.addClickHandler(new ClickHandler() { public void onClick(ClickEvent e) { Window.Location.assign("/logout.php"); }});
        mainPanel.backupButton.addClickHandler(new ClickHandler() { public void onClick(ClickEvent e) { doBackup(); }});

        // Focus the cursor on the name field when the app loads
        mainPanel.searchTextBox.setFocus(true);
        mainPanel.searchTextBox.selectAll();

        // Add a handler to send the name to the server
        SearchHandler shandler = new SearchHandler();
        mainPanel.searchButton.addClickHandler(shandler);
        mainPanel.searchTextBox.addKeyUpHandler(shandler);

        // Add a handler for "nouveau client"
        EditClientHandler ehandler = new EditClientHandler(null, -1);
        mainPanel.nouveauButton.addClickHandler(ehandler);

        // config widget
        this.cfWidget = new ConfigWidget(this, selectedClub);
        mainPanel.config.add(this.cfWidget);

        // history handlers
        History.addValueChangeHandler(new ValueChangeHandler<String>() {
            public void onValueChange(ValueChangeEvent<String> event) {
                String historyToken = event.getValue();
                Mode m = Mode.parse(historyToken);
                historySwitchMode(m);
            }
        });

        History.fireCurrentHistoryState();
        retrieveClientList(false);
    }

    /* --- client search UI functions --- */

    private void loadClientListResults(JsArray<ClientSummary> allClients, boolean display) {
        searchString = removeAccents(mainPanel.searchTextBox.getText());
        mainPanel.searchResults.removeAllRows();
        firstSearchResultToDisplay = 0;
        this.allClients = new ArrayList<ClientSummary>();
	for (int i = 0; i < allClients.length(); i++) {
	    this.allClients.add(allClients.get(i));
	}
	Collections.sort(this.allClients, new Comparator<ClientSummary>() {
		public final int compare(ClientSummary t, ClientSummary o) {
                    // somehow we got a NPE...
                    if ((t == null || t.getId() == null) &&
                        (o == null || o.getId() == null)) return 0;
                    if (t == null || t.getId() == null) return -1;
                    if (o == null || o.getId() == null) return 1;

                    if (!Constants.stringEquals(t.getNom(), o.getNom()))
                        return t.getNom().compareTo(o.getNom());
                    if (!Constants.stringEquals(t.getPrenom(), o.getPrenom()))
                        return t.getPrenom().compareTo(o.getPrenom());

                    return t.getId().compareTo(o.getId());
		} });
        if (display)
            displaySearchResults();
    }

    private void displaySearchResults() {
        int resultCount = 0, displayedCount = 0;

        mainPanel.nextResultsButton.setVisible(false);
        mainPanel.prevResultsButton.setVisible(false);

        if (firstSearchResultToDisplay != 0)
            mainPanel.prevResultsButton.setVisible(true);

        mainPanel.searchResults.removeAllRows();
        boolean bailOnNextIteration = false;
        for (ClientSummary cs : allClients) {
            if (bailOnNextIteration) {
                mainPanel.nextResultsButton.setVisible(true);
                break;
            }

            String s = "[" + cs.getId() + "] " + cs.getPrenom() + " " + cs.getNom();
	    ClubSummary club = null;

            if (selectedClub != null) {
		club = selectedClub;
                boolean found = false;
                for (int j = 0; j < cs.getClubs().length(); j++) {
                    String cn = cs.getClubs().get(j);
                    if (selectedClub.getId().equals(cn))
                        found = true;
                }
                if (!found) continue;
            } else {
		if (cs.getClubs() != null && cs.getClubs().length() > 0)
		    club = getClubSummaryByID(cs.getClubs().get(0));
	    }

            String ss = removeAccents(s);
            if (!ss.contains(searchString)) continue;

            if (cs.getSaisons() != null && !cs.getSaisons().isEmpty())
                s += " ("+cs.getSaisons()+")";

            if (resultCount >= firstSearchResultToDisplay) {
                Anchor h = new Anchor(s);
                try {
                    h.addClickHandler(new EditClientHandler(club, Integer.parseInt(cs.getId())));
                } catch (NumberFormatException e) {
                }
                mainPanel.searchResults.setWidget(displayedCount++, 0, h);
            }

            resultCount++;
            if (displayedCount >= MAX_RESULTS) {
                bailOnNextIteration = true;
            }
        }
        mainPanel.searchResultsPanel.setVisible(true);
    }

    /* --- club list UI functions --- */
    private ClubSummary selectedClub;
    public static final String TOUS = "TOUS";

    class ClubListHandler implements ClickHandler {
        final ClubSummary club;

        ClubListHandler(ClubSummary club) {
            this.club = club;
        }

        @Override
        public void onClick(ClickEvent e) {
            selectClub(club);
        }
    }

    interface ClubListHandlerFactory {
        public ClickHandler instantiate(ClubSummary s);
    }

    class MainClubListHandlerFactory implements ClubListHandlerFactory {
        public ClickHandler instantiate(ClubSummary s) {
            return new ClubListHandler(s);
        }
    }

    boolean pendingRetrieveClubList = false;
    void populateClubList(boolean tousOK, DropDownMenu dropDownUserClubs, ClubListHandlerFactory clhf) {
        // note: tousOK is false when called from ClientWidget

        if (allClubs == null) {
            if (pendingRetrieveClubList) return;
            retrieveClubList(tousOK);
            return;
        }

      dropDownUserClubs.clear();
      if (tousOK) {
          AnchorListItem tous = new AnchorListItem(TOUS);
          tous.addClickHandler(clhf.instantiate(null));
          dropDownUserClubs.add(tous);
      }

      int clubCount = 0;
      for(ClubSummary cs : allClubs) {
          AnchorListItem it = new AnchorListItem(cs.getClubText());
          it.setId("club-" + clubCount); clubCount++;
          it.addClickHandler(clhf.instantiate(cs));
          dropDownUserClubs.add(it);
      }

      // don't select TOUS by default if there is only 1 club
      if (allClubs.size() == 1) {
          selectedClub = allClubs.get(0);
      } else if (selectedClub != null) {
          // refresh selectedClub by id from allClubs (aliasing strikes again)
          for (ClubSummary cs : allClubs) {
              if (selectedClub.getId().equals(cs.getId()))
                  selectedClub = cs;
          }
      }
      selectClub(selectedClub);
    }

    /* --- club-list related utility functions --- */
    void selectClub(ClubSummary club) {
        mainPanel.nouveauButton.setEnabled(club != null);
        mainPanel.ftButton.setEnabled(club != null);
        if (club == null)
            mainPanel.dropDownUserClubsButton.setText(TOUS);
        else
            mainPanel.dropDownUserClubsButton.setText(club.getClubText());
        selectedClub = club;
    }

    boolean isClubSelected() {
	return selectedClub != null;
    }

    ClubSummary getSelectedClub() { return selectedClub; }

    String getSelectedClubID() {
        if (selectedClub == null) return null;
        return selectedClub.getId();
    }

    ClubSummary getClubSummaryByID(String cid) {
        if (allClubs == null) return null;
        for(ClubSummary cs : allClubs) {
          if (cs.getId().equals(cid)) return cs;
        }
        return null;
    }

    /* --- network functions --- */
    interface Function {
        public void eval(String s);
    }

    RequestCallback createRequestCallback(final Function f) {
        return new RequestCallback() {
                    public void onError(Request request, Throwable exception) {
                        JudoDB.this.displayError("pas de réponse; veuillez re-essayer");
                    }

                    public void onResponseReceived(Request request,
                                                   Response response) {
                        if (200 == response.getStatusCode()) {
                            f.eval(response.getText());
                        } else if (403 == response.getStatusCode()) {
                            // at any point we might blow away the whole app w/a 403.
                            Window.Location.replace("/forbidden.php");
                        } else {
                            JudoDB.this.displayError("Couldn't retrieve JSON (" +
                                                response.getStatusText() + ")");
                        }
                    }
        };
    }

    public void retrieve(String url, RequestCallback rc) {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET,
                                                    url);
        try {
            Request request = builder.sendRequest(null, rc);
        } catch (RequestException e) {
            displayError("Couldn't retrieve JSON");
        }
    }

    public void retrieveClientList(final boolean display) {
        String url = PULL_CLIENT_LIST_URL;
        RequestCallback rc =
            createRequestCallback(new JudoDB.Function() {
                    public void eval(String s) {
                        clearStatus();
                        loadClientListResults
                            (JsonUtils.<JsArray<ClientSummary>>safeEval(s), display);
                    }
                });
        retrieve(url, rc);
    }

    boolean gotClubList = false;
    public void retrieveClubList(final boolean tousOK) {
        String url = PULL_CLUB_LIST_URL;
        pendingRetrieveClubList = true;
        RequestCallback rc =
            createRequestCallback(new JudoDB.Function() {
                    public void eval(String s) {
                        gotClubList = true;
                        JsArray<ClubSummary> clubsArray = JsonUtils.<JsArray<ClubSummary>>safeEval(s);
                        List<ClubSummary> clubs = new ArrayList<ClubSummary>();
                        for (int i = 0; i < clubsArray.length(); i++)
                            clubs.add(clubsArray.get(i));
                        loadClubListResults(clubs);
                    }
                });
        retrieve(url, rc);
    }

    private void loadClubListResults(List<ClubSummary> clubs) {
        List<ClubSummary> newClubs = new ArrayList<ClubSummary>();
        for (ClubSummary c : clubs) {
            if (Integer.parseInt(c.getId()) > 0) {
                newClubs.add(c);
            } else {
                /* gross hack! */
                if (c.getId().equals("-1"))
                    isAdmin = c.getNom().equals("admin");
            }
        }
        this.allClubs = newClubs;
        refreshClubListResults();
    }

    void refreshClubListResults() {
        firstSearchResultToDisplay = 0;
        if (isAdmin) mainPanel.backupButton.setVisible(true);
        populateClubList(true, mainPanel.dropDownUserClubs, new MainClubListHandlerFactory());
        if (cfWidget != null)
            populateClubList(true, cfWidget.dropDownUserClubs,
                             cfWidget.new ConfigClubListHandlerFactory());
        if (listWidget != null)
            populateClubList(true, listWidget.dropDownUserClubs, 
                             listWidget.new ListClubListHandlerFactory());
        if (clientWidget != null)
            populateClubList(false, clientWidget.dropDownUserClubs, 
                             clientWidget.new ClientClubListHandlerFactory());
        pendingRetrieveClubList = false;
    }

    /* --- helper functions for status bar --- */

    void displayError(String error) {
        mainPanel.statusAlert.setType(AlertType.DANGER);
        mainPanel.statusText.setText("Erreur: " + error);
        mainPanel.statusAlert.setVisible(true);
    }
    void clearStatus() {
        mainPanel.statusText.setText("");
        mainPanel.statusAlert.setVisible(false);
    }
    void pleaseWait() {
        setStatus("Veuillez patienter...");
    }
    void setStatus(String s) {
        mainPanel.statusAlert.setType(AlertType.INFO);
        mainPanel.statusText.setText(s);
        mainPanel.statusAlert.setVisible(true);
    }

    /* --- miscellaneous utility functions --- */
    private native String removeAccents(String s) /*-{
        var r=s.toLowerCase();
        r = r.replace(new RegExp("\\s", 'g'),"");
        r = r.replace(new RegExp("[àáâãäå]", 'g'),"a");
        r = r.replace(new RegExp("æ", 'g'),"ae");
        r = r.replace(new RegExp("ç", 'g'),"c");
        r = r.replace(new RegExp("[èéêë]", 'g'),"e");
        r = r.replace(new RegExp("[ìíîï]", 'g'),"i");
        r = r.replace(new RegExp("ñ", 'g'),"n");
        r = r.replace(new RegExp("[òóôõö]", 'g'),"o");
        r = r.replace(new RegExp("œ", 'g'),"oe");
        r = r.replace(new RegExp("[ùúûü]", 'g'),"u");
        r = r.replace(new RegExp("[ýÿ]", 'g'),"y");
        r = r.replace(new RegExp("\\W", 'g'),"");
        return r;
    }-*/;

    static SessionSummary getLinkedSession(SessionSummary ss, List<SessionSummary> sessionSummaries) {
        String linkedSeqno = ss.getLinkedSeqno();
        for (SessionSummary s : sessionSummaries) {
            if (s.getSeqno().equals(linkedSeqno))
                return s;
        }
        return ss;
    }

    static SessionSummary getSessionForDate(Date inscriptionDate, List<SessionSummary> sessionSummaries) {
        for (SessionSummary s : sessionSummaries) {
            try {
                Date inscrBegin = Constants.DB_DATE_FORMAT.parse(s.getFirstSignupDate());
                Date inscrEnd = Constants.DB_DATE_FORMAT.parse(s.getLastSignupDate());

                int daysAfterBegin = CalendarUtil.getDaysBetween(inscrBegin, inscriptionDate);
                int daysBeforeEnd = CalendarUtil.getDaysBetween(inscriptionDate, inscrEnd);

                if (daysAfterBegin >= 0 && daysBeforeEnd >= 0) {
                    return s;
                }
            } catch (IllegalArgumentException e) { }
        }
        return null;
    }

    static String getSessionIds(Date d, int sessionCount, List<SessionSummary> sessionSummaries) {
        if (sessionSummaries == null) return "";

        SessionSummary m = JudoDB.getSessionForDate(d, sessionSummaries);
        if (m == null) return "";

        if (sessionCount == 1) return m.getAbbrev();
        if (sessionCount == 2) {
            SessionSummary next = getLinkedSession(m, sessionSummaries);
            if (m.isPrimary())
                return m.getAbbrev() + " " + next.getAbbrev();
            else
                return next.getAbbrev() + " " + m.getAbbrev();
        }
        return "";
    }

    static String sessionSeqnosFromAbbrevs(String in, List<SessionSummary> sessionSummaries) {
        StringBuilder res = new StringBuilder();
        boolean first = true;
        for (String i : in.split(" ")) {
            for (SessionSummary s : sessionSummaries) {
                if (s.getAbbrev().equals(i)) {
                    if (!first) {
                        res.append(" ");
                    } else {
                        first = false;
                    }
                    res.append(s.getSeqno());
                }
            }
        }
        return res.toString();
    }

    static String sessionAbbrevsFromSeqnos(String in, List<SessionSummary> sessionSummaries) {
        StringBuilder res = new StringBuilder();
        boolean first = true;
        for (String i : in.split(" ")) {
            for (SessionSummary s : sessionSummaries) {
                if (s.getSeqno().equals(i)) {
                    if (!first) {
                        res.append(" ");
                    } else {
                        first = false;
                    }
                    res.append(s.getAbbrev());
                }
            }
        }
        return res.toString();
    }
}
