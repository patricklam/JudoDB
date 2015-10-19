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
    static class Mode {
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
        int arg;

        public Mode(ActualMode am) { this.am = am; }
        public Mode(ActualMode am, int arg) {
            this.am = am; this.arg = arg;
        }

        public String toString() {
            if (am == ActualMode.EDIT_CLIENT) return am.label + arg;
            return am.label;
        }

        static Mode parse(String s) {
            for (ActualMode m : ActualMode.values())
                if (m.matchesLabel(s)) {
                    if (m == ActualMode.EDIT_CLIENT) {
                        String n = s.substring(m.label.length());
                        return new Mode(ActualMode.EDIT_CLIENT, Integer.parseInt(n));
                    }
                    return new Mode(m);
                }
            return new Mode(ActualMode.MAIN);
        }
    };

    public static final int MAX_RESULTS = 20;

    public static final String BACKEND_SUFFIX = "backend/";
    public static final String BASE_URL = GWT.getHostPageBaseURL() + BACKEND_SUFFIX;

    private static final String PULL_CLIENT_LIST_URL = BASE_URL + "pull_client_list.php";
    public static final String PULL_CLUB_LIST_URL = BASE_URL + "pull_club_list.php";
    public static final String PULL_CLUB_COURS_URL = JudoDB.BASE_URL + "pull_club_cours.php";
    public static final String PULL_SESSIONS_URL = JudoDB.BASE_URL + "pull_sessions.php";
    public static final String PULL_CLUB_PRIX_URL = JudoDB.BASE_URL + "pull_club_prix.php";
    public static final String PULL_ESCOMPTE_URL = JudoDB.BASE_URL + "pull_escompte.php";
    public static final String PULL_PRODUIT_URL = JudoDB.BASE_URL + "pull_produit.php";
    int jsonRequestId = 0;

    /* main layout */
    private final MainLayoutPanel mainLayoutPanel = new MainLayoutPanel();

    /* actions */
    final Anchor filtrerListes = new Anchor("Filtrer");
    final Anchor editerListes = new Anchor("Éditer");
    final Anchor ftListes = new Anchor("FT-303");
    final Anchor impotListes = new Anchor("Reçus d'impot");
    final Anchor clearXListes = new Anchor("Effacer les X");
    final Anchor normalListes = new Anchor("Voir listes");
    final Anchor returnToMainFromListes = new Anchor("Retour page principale");

    /* state */
    List<ClientSummary> allClients;
    JsArray<ClubSummary> allClubs;
    private String searchString;
    private int firstSearchResultToDisplay = 0;
    private Stack<Mode> modeStack = new Stack<Mode>();
    private Mode currentMode = null;

    /* edit client stuff */
    private ClientWidget clientWidget;

    /* view lists stuff */
    private ListWidget listWidget;

    /* config management stuff */
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
            switchMode(new Mode (Mode.ActualMode.EDIT_CLIENT, cid));
        }
    }

    // modes
    public void switchMode(Mode newMode) {
        if (currentMode != null)
            modeStack.push(currentMode);
        currentMode = newMode;
        _switchMode(newMode);
    }

    public void popMode() {
        if (!modeStack.isEmpty()) {
            currentMode = modeStack.peek();
            _switchMode(currentMode);

            modeStack.pop();
        }
    }

    private void _switchMode(Mode newMode) {
        History.newItem(newMode.toString(), false);

        switch (newMode.am) {
        case EDIT_CLIENT:
            _switchMode_editClient(newMode.arg);
            break;
        case LIST:
            _switchMode_viewLists();
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
        mainLayoutPanel.editClient.setVisible(false);
        mainLayoutPanel.lists.setVisible(false);
        mainLayoutPanel.config.setVisible(false);
        mainLayoutPanel.mainPanel.setVisible(false);
    }

    private void _switchMode_editClient (int cid) {
        mainLayoutPanel.editClient.clear();
        this.clientWidget = new ClientWidget(cid, this);
        mainLayoutPanel.editClient.add(this.clientWidget);

        hideAllSubpanels();
        mainLayoutPanel.editClient.setVisible(true);
    }

    public void _switchMode_viewLists() {
        if (this.listWidget == null) {
            this.listWidget = new ListWidget(this);
            mainLayoutPanel.lists.add(this.listWidget);
        }
        this.listWidget.selectClub(selectedClub);

        hideAllSubpanels();
        mainLayoutPanel.lists.setVisible(true);

        this.listWidget.switchMode(ListWidget.Mode.NORMAL);
    }

    public void _switchMode_config() {
        this.cfWidget.selectClub(selectedClub);

        hideAllSubpanels();
        mainLayoutPanel.config.setVisible(true);
    }

    public void _switchMode_main() {
        clearStatus();

        hideAllSubpanels();
        mainLayoutPanel.mainPanel.setVisible(true);

        mainLayoutPanel.dropDownUserClubs.setVisible(true);
        mainLayoutPanel.searchButton.setEnabled(true);
        mainLayoutPanel.searchTextBox.setFocus(true);
        mainLayoutPanel.searchResultsPanel.setVisible(false);
    }

    /** After changing any data, invalidate stored data. */
    public void invalidateListWidget() {
        if (this.listWidget != null) {
            this.listWidget.removeFromParent();
            this.listWidget = null;
        }

        if (mainLayoutPanel.searchResults != null) {
            mainLayoutPanel.searchResults.removeAllRows();
            firstSearchResultToDisplay = 0;
        }
    }

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        // handle exceptions
        //        if(false)
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

	RootPanel.get().add(mainLayoutPanel);
	mainLayoutPanel.versionLabel.setText(Version.VERSION);

        mainLayoutPanel.nextResultsButton.setVisible(false);
        mainLayoutPanel.nextResultsButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent e) {
                firstSearchResultToDisplay += MAX_RESULTS;
                if (firstSearchResultToDisplay > allClients.size()) firstSearchResultToDisplay -= MAX_RESULTS;
                displaySearchResults(); } });
        mainLayoutPanel.prevResultsButton.setVisible(false);
        mainLayoutPanel.prevResultsButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent e) {
                firstSearchResultToDisplay -= MAX_RESULTS;
                if (firstSearchResultToDisplay < 0) firstSearchResultToDisplay = 0;
                displaySearchResults(); } });

        mainLayoutPanel.searchResultsPanel.setVisible(false);

        // config widget
        this.cfWidget = new ConfigWidget(this, selectedClub);
        mainLayoutPanel.config.add(this.cfWidget);

        mainLayoutPanel.listeButton.addClickHandler(new ClickHandler() { public void onClick(ClickEvent e) { switchMode(new Mode(Mode.ActualMode.LIST)); }});
        mainLayoutPanel.configButton.addClickHandler(new ClickHandler() { public void onClick(ClickEvent e) { switchMode(new Mode(Mode.ActualMode.CONFIG)); }});
        mainLayoutPanel.logoutButton.addClickHandler(new ClickHandler() { public void onClick(ClickEvent event) { Window.Location.assign("/logout.php"); }});

        /*
        // right bar actions: list
        filtrerListes.addClickHandler(new ClickHandler() { public void onClick(ClickEvent e) {
            if (JudoDB.this.listWidget != null) JudoDB.this.listWidget.toggleFiltering(); }});

        mainLayoutPanel.listActions.add(filtrerListes);
        mainLayoutPanel.listActions.add(new Label(""));
        editerListes.addClickHandler(new ClickHandler() { public void onClick(ClickEvent e) {
            if (JudoDB.this.l != null) {
              JudoDB.this.l.switchMode(ListWidget.Mode.EDIT);
            }
        }});
        mainLayoutPanel.listActions.add(editerListes);
        mainLayoutPanel.listActions.add(new Label(""));
        ftListes.addClickHandler(new ClickHandler() { public void onClick(ClickEvent e) {
          if (JudoDB.this.l != null) {
              JudoDB.this.l.switchMode(ListWidget.Mode.FT);
          }
        }});
        mainLayoutPanel.listActions.add(ftListes);
        mainLayoutPanel.listActions.add(new Label(""));
        impotListes.addClickHandler(new ClickHandler() { public void onClick(ClickEvent e) {
            if (JudoDB.this.l != null) {
                JudoDB.this.l.switchMode(ListWidget.Mode.IMPOT);
            }
        }});
        */

        // temporarily disable; issue 50
        // mainLayout.listActions.add(impotListes);
        // mainLayout.listActions.add(new Label(""));

        /*
        clearXListes.addClickHandler(new ClickHandler() { public void onClick(ClickEvent e) {
            if (JudoDB.this.l != null) JudoDB.this.l.clearX(); }});
        mainLayoutPanel.listActions.add(clearXListes);
        mainLayoutPanel.listActions.add(new Label(""));
        normalListes.addClickHandler(new ClickHandler() { public void onClick(ClickEvent e) {
           if (JudoDB.this.l != null) {
              JudoDB.this.l.switchMode(ListWidget.Mode.NORMAL);
            }
        }});
        mainLayoutPanel.listActions.add(normalListes);
        mainLayoutPanel.listActions.add(new Label(""));
        returnToMainFromListes.addClickHandler(new ClickHandler() { public void onClick(ClickEvent e) { switchMode(new Mode(Mode.ActualMode.MAIN)); }});
        mainLayoutPanel.listActions.add(returnToMainFromListes);
        mainLayoutPanel.listActions.add(new Label(""));
        */

        // Focus the cursor on the name field when the app loads
        mainLayoutPanel.searchTextBox.setFocus(true);
        mainLayoutPanel.searchTextBox.selectAll();

        // Add a handler to send the name to the server
        SearchHandler shandler = new SearchHandler();
        mainLayoutPanel.searchButton.addClickHandler(shandler);

        // Add a handler for "nouveau client"
        EditClientHandler ehandler = new EditClientHandler(null, -1);
        mainLayoutPanel.nouveauButton.addClickHandler(ehandler);

        // history handlers
        History.addValueChangeHandler(new ValueChangeHandler<String>() {
            public void onValueChange(ValueChangeEvent<String> event) {
                String historyToken = event.getValue();
                Mode m = Mode.parse(historyToken);
                switchMode(m);
            }
        });

        History.fireCurrentHistoryState();
        switchMode(new Mode(Mode.ActualMode.MAIN));
        generateClubList();
        retrieveClientList(false);
    }

    /* --- client search UI functions --- */

    private void loadClientListResults(JsArray<ClientSummary> allClients, boolean display) {
        searchString = removeAccents(mainLayoutPanel.searchTextBox.getText());
        mainLayoutPanel.searchResults.removeAllRows();
        firstSearchResultToDisplay = 0;
        this.allClients = new ArrayList<ClientSummary>();
	for (int i = 0; i < allClients.length(); i++) {
	    this.allClients.add(allClients.get(i));
	}
	Collections.sort(this.allClients, new Comparator<ClientSummary>() {
		public final int compare(ClientSummary t, ClientSummary o) {
		    if (!t.getNom().equals(o.getNom()))
			return t.getNom().compareTo(o.getNom());
		    if (!t.getPrenom().equals(o.getNom()))
			return t.getPrenom().compareTo(o.getPrenom());
		    return t.getId().compareTo(o.getId());
		} });
        if (display)
            displaySearchResults();
    }

    private void displaySearchResults() {
        int resultCount = 0, displayedCount = 0;

        mainLayoutPanel.nextResultsButton.setVisible(false);
        mainLayoutPanel.prevResultsButton.setVisible(false);

        if (firstSearchResultToDisplay != 0)
            mainLayoutPanel.prevResultsButton.setVisible(true);

        mainLayoutPanel.searchResults.removeAllRows();
        boolean bailOnNextIteration = false;
        for (ClientSummary cs : allClients) {
            if (bailOnNextIteration) {
                mainLayoutPanel.nextResultsButton.setVisible(true);
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
		if (cs.getClubs().length() > 0)
		    club = getClubSummaryByID(cs.getClubs().get(0));
	    }

            String ss = removeAccents(s);
            if (!ss.contains(searchString)) continue;

            if (cs.getSaisons() != null && !cs.getSaisons().isEmpty())
                s += " ("+cs.getSaisons()+")";

            if (resultCount >= firstSearchResultToDisplay) {
                Anchor h = new Anchor(s);
                h.addClickHandler(new EditClientHandler(club, Integer.parseInt(cs.getId())));
                mainLayoutPanel.searchResults.setWidget(displayedCount++, 0, h);
            }

            resultCount++;
            if (displayedCount >= MAX_RESULTS) {
                bailOnNextIteration = true;
            }
        }
        mainLayoutPanel.searchResultsPanel.setVisible(true);
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

      for(int i = 0; i < allClubs.length(); ++i) {
          ClubSummary cs = allClubs.get(i);
          AnchorListItem it = new AnchorListItem(cs.getClubText());
          it.addClickHandler(clhf.instantiate(cs));
          dropDownUserClubs.add(it);
      }

      // don't select TOUS by default if there is only 1 club
      if (allClubs.length() == 1) 
          selectedClub = allClubs.get(0);
      selectClub(selectedClub);
    }

    /* --- club-list related utility functions --- */
    void generateClubList() {
        pleaseWait();
        retrieveClubList(true);
      }

    void clearSelectedClub() {
        selectedClub = null;
    }

    void selectClub(ClubSummary club) {
        mainLayoutPanel.nouveauButton.setEnabled(club != null);
        if (club == null)
            mainLayoutPanel.dropDownUserClubsButton.setText(TOUS);
        else
            mainLayoutPanel.dropDownUserClubsButton.setText(club.getClubText());
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
        for(int i = 0; i < allClubs.length(); ++i) {
          ClubSummary cs = allClubs.get(i);
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
                            JudoDB.this.clearStatus();
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
                        loadClientListResults
                            (JsonUtils.<JsArray<ClientSummary>>safeEval(s), display);
                    }
                });
        retrieve(url, rc);
    }

    public void retrieveClubList(final boolean tousOK) {
        String url = PULL_CLUB_LIST_URL;
        pendingRetrieveClubList = true;
        RequestCallback rc =
            createRequestCallback(new JudoDB.Function() {
                    public void eval(String s) {
                        loadClubListResults
                            (JsonUtils.<JsArray<ClubSummary>>safeEval(s));
                    }
                });
        retrieve(url, rc);
    }

    private void loadClubListResults(JsArray<ClubSummary> clubs) {
        this.allClubs = clubs;
        refreshClubListResults();
    }

    void refreshClubListResults() {
        firstSearchResultToDisplay = 0;
        populateClubList(true, mainLayoutPanel.dropDownUserClubs, new MainClubListHandlerFactory());
        populateClubList(true, cfWidget.dropDownUserClubs, cfWidget.new ConfigClubListHandlerFactory());
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
        mainLayoutPanel.statusAlert.setType(AlertType.DANGER);
        mainLayoutPanel.statusText.setText("Erreur: " + error);
        mainLayoutPanel.statusAlert.setVisible(true);
    }
    void clearStatus() {
        mainLayoutPanel.statusText.setText("");
        mainLayoutPanel.statusAlert.setVisible(false);
    }
    void pleaseWait() {
        setStatus("Veuillez patienter...");
    }
    void setStatus(String s) {
        mainLayoutPanel.statusAlert.setType(AlertType.INFO);
        mainLayoutPanel.statusText.setText(s);
        mainLayoutPanel.statusAlert.setVisible(true);
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

    static String getSessionIds(Date d, int sessionCount, List<SessionSummary> sessionSummaries) {
        if (sessionSummaries == null) return "";

        SessionSummary m = null;
        for (SessionSummary s : sessionSummaries) {
            try {
                Date inscrBegin = Constants.DB_DATE_FORMAT.parse(s.getFirstSignupDate());
                Date inscrEnd = Constants.DB_DATE_FORMAT.parse(s.getLastSignupDate());
                if (d.after(inscrBegin) && d.before(inscrEnd)) {
                    m = s; continue;
                }
            } catch (IllegalArgumentException e) {}
        }
        if (m == null) return "";

        if (sessionCount == 1) return m.getAbbrev();
        if (sessionCount == 2) {
            String lsn = m.getLinkedSeqno();
            SessionSummary next = null;
            for (SessionSummary ss : sessionSummaries) {
                if (ss.getSeqno().equals(lsn))
                    next = ss;
            }
            if (next != null)
                return m.getAbbrev() + " " + next.getAbbrev();
            else
                return m.getAbbrev();
        }
        return "";
    }
}
