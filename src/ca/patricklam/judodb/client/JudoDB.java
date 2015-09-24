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
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.ui.ListBox;

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

    public static final int MAX_RESULTS = 10;

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

    /* search stuff */
    private final VerticalPanel searchResultsPanel = new VerticalPanel();
    private final VerticalPanel clubListResultsPanel = new VerticalPanel();
    private final TextBox searchField = new TextBox();
    private final FlexTable searchResults = new FlexTable();
    private final Button searchButton = new Button("Recherche");
    private final Button newClientButton = new Button("Nouveau client");
    private final Button nextResultsButton = new Button("Résultats suivants");
    private final Button prevResultsButton = new Button("Résultats précedents");

    /* actions */
    private final Anchor voirListes = new Anchor("Voir listes des cours");
    private final Anchor editConfig = new Anchor("Configuration du club (cours, inscriptions)");
    private final Anchor logout = new Anchor("Fermer session", "logout.php");

    final Anchor filtrerListes = new Anchor("Filtrer");
    final Anchor editerListes = new Anchor("Éditer");
    final Anchor ftListes = new Anchor("FT-303");
    final Anchor impotListes = new Anchor("Reçus d'impot");
    final Anchor clearXListes = new Anchor("Effacer les X");
    final Anchor normalListes = new Anchor("Voir listes");
    final Anchor returnToMainFromListes = new Anchor("Retour page principale");
    final Anchor returnToMainFromConfig = new Anchor("Retour page principale");

    private ListBox dropDownUserClubs = new ListBox();
    LinkedHashMap<Integer, ClubSummary> idxToClub = new LinkedHashMap<Integer, ClubSummary>();

    ArrayList<Widget> allWidgets = new ArrayList<Widget>();

    /* state */
    List<ClientSummary> allClients;
    JsArray<ClubSummary> allClubs;
    private String searchString;
    private int firstSearchResultToDisplay = 0;
    private Stack<Mode> modeStack = new Stack<Mode>();
    private Mode currentMode = null;

    ClubSearchHandler csHandler = new ClubSearchHandler();

    /* edit client stuff */
    private ClientWidget c;

    /* view lists stuff */
    private ListWidget l;

    /* config management stuff */
    private ConfigWidget cf;

    int selectedClub = 0;

    class ClubSearchHandler implements ChangeHandler {
        @Override
        public void onChange(ChangeEvent e) {
            refreshSelectedClub();
        }
    }

    void generateClubList() {
        pleaseWait();
        retrieveClubList(true, dropDownUserClubs);
      }

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
        private int club, cid;
        public EditClientHandler(int club, int cid) { this.club = club; this.cid = cid; }

        public void onClick(ClickEvent event) {
            if (club != -1)
                refreshSelectedClub(club);
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
        for (Widget w : allWidgets)
            w.setVisible(false);

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

    private void hideRightBar(boolean hidden) {
	DockLayoutPanel d = mainLayoutPanel.dock;

	for (int i = 0; i < d.getWidgetCount(); i++) {
	    Widget w = d.getWidget(i);
	    if (d.getWidgetDirection(w) == DockLayoutPanel.Direction.EAST)
		d.setWidgetHidden(w, hidden);
	}
    }

    private void _switchMode_editClient (int cid) {
        mainLayoutPanel.editClient.clear();
        this.c = new ClientWidget(cid, this);
        mainLayoutPanel.editClient.add(this.c);
        mainLayoutPanel.editClient.setVisible(true);

        mainLayoutPanel.dock.remove(mainLayoutPanel.search);
        mainLayoutPanel.dock.remove(mainLayoutPanel.editClient);
        mainLayoutPanel.dock.remove(mainLayoutPanel.config);

        mainLayoutPanel.dock.add(mainLayoutPanel.editClient);
        hideRightBar(true);
    }

    public void _switchMode_viewLists() {
        if (this.l == null) {
            this.l = new ListWidget(this);
            mainLayoutPanel.lists.add(this.l);
        }
        mainLayoutPanel.editClient.clear();

        mainLayoutPanel.dock.remove(mainLayoutPanel.search);
        mainLayoutPanel.dock.remove(mainLayoutPanel.editClient);
        mainLayoutPanel.dock.remove(mainLayoutPanel.config);

        mainLayoutPanel.dock.add(mainLayoutPanel.lists);

        mainLayoutPanel.listActions.setVisible(true);
        mainLayoutPanel.lists.setVisible(true);
        mainLayoutPanel.configActions.setVisible(false);

        this.l.switchMode(ListWidget.Mode.NORMAL);
        hideRightBar(false);
    }

    public void _switchMode_config() {
        if (this.cf == null) {
            this.cf = new ConfigWidget(this);
            mainLayoutPanel.config.add(this.cf);
        }
        mainLayoutPanel.editClient.clear();

        mainLayoutPanel.dock.remove(mainLayoutPanel.search);
        mainLayoutPanel.dock.remove(mainLayoutPanel.editClient);
        mainLayoutPanel.dock.remove(mainLayoutPanel.config);
        mainLayoutPanel.dock.add(mainLayoutPanel.config);

        mainLayoutPanel.config.setVisible(true);
        mainLayoutPanel.configActions.setVisible(true);
        returnToMainFromConfig.setVisible(true);
        hideRightBar(false);
    }

    public void _switchMode_main() {
        clearStatus();

        mainLayoutPanel.dock.remove(mainLayoutPanel.search);
        mainLayoutPanel.dock.remove(mainLayoutPanel.editClient);
        mainLayoutPanel.dock.remove(mainLayoutPanel.config);

        mainLayoutPanel.dock.add(mainLayoutPanel.search);

        mainLayoutPanel.search.setVisible(true);

        mainLayoutPanel.mainActions.setVisible(true);
        mainLayoutPanel.listActions.setVisible(false);
        mainLayoutPanel.configActions.setVisible(false);
        mainLayoutPanel.versionLabel.setVisible(true);
        voirListes.setVisible(true);
        editConfig.setVisible(true);
        logout.setVisible(true);

        generateClubList();
        dropDownUserClubs.setVisible(true);

        searchButton.setEnabled(true);
        searchButton.setFocus(true);
        hideRightBar(false);
    }

    /** After changing any data, invalidate stored data. */
    public void invalidateListWidget() {
        if (this.l != null) {
            this.l.removeFromParent();
            this.l = null;
        }

        if (searchResults != null) {
            searchResults.removeAllRows();
            firstSearchResultToDisplay = 0;
        }
    }

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        // handle exceptions
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

	RootLayoutPanel.get().add(mainLayoutPanel);

	mainLayoutPanel.versionLabel.setText(Version.VERSION);

        // search buttons
        mainLayoutPanel.search.add(searchField);
        mainLayoutPanel.search.add(searchButton);
        mainLayoutPanel.search.add(newClientButton);

        // edit client buttons
        final Label resultsLabel = new Label("Résultats: ");

        final Panel searchNavPanel = new HorizontalPanel();
        searchNavPanel.add(nextResultsButton);
        nextResultsButton.setVisible(false);
        nextResultsButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent e) {
                firstSearchResultToDisplay += MAX_RESULTS;
                if (firstSearchResultToDisplay > allClients.size()) firstSearchResultToDisplay -= MAX_RESULTS;
                displaySearchResults(); } });
        searchNavPanel.add(prevResultsButton);
        prevResultsButton.setVisible(false);
        prevResultsButton.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent e) {
                firstSearchResultToDisplay -= MAX_RESULTS;
                if (firstSearchResultToDisplay < 0) firstSearchResultToDisplay = 0;
                displaySearchResults(); } });

        searchResultsPanel.add(resultsLabel);
        searchResultsPanel.add(searchResults);
        searchResultsPanel.add(searchNavPanel);
        searchResultsPanel.setVisible(false);
        mainLayoutPanel.search.add(searchResultsPanel);

        // right bar actions: main
        voirListes.addClickHandler(new ClickHandler() { public void onClick(ClickEvent e) { switchMode(new Mode(Mode.ActualMode.LIST)); }});
        mainLayoutPanel.mainActions.add(voirListes);
        mainLayoutPanel.mainActions.add(new Label(""));
        mainLayoutPanel.mainActions.add(new Label(""));
        editConfig.addClickHandler(new ClickHandler() { public void onClick(ClickEvent e) { switchMode(new Mode(Mode.ActualMode.CONFIG)); }});
        mainLayoutPanel.mainActions.add(editConfig);
        mainLayoutPanel.mainActions.add(new Label(""));
        mainLayoutPanel.mainActions.add(logout);

        mainLayoutPanel.search.add(dropDownUserClubs);
        dropDownUserClubs.setStyleName("clubBox");
        dropDownUserClubs.addChangeHandler(csHandler);

        // right bar actions: list
        filtrerListes.addClickHandler(new ClickHandler() { public void onClick(ClickEvent e) {
            if (JudoDB.this.l != null) JudoDB.this.l.toggleFiltering(); }});
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

        // temporarily disable; issue 50
        // mainLayout.listActions.add(impotListes);
        // mainLayout.listActions.add(new Label(""));
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

        // right bar actions: config
        mainLayoutPanel.configActions.add(new Label(""));
        returnToMainFromConfig.addClickHandler(new ClickHandler() { public void onClick(ClickEvent e) { switchMode(new Mode(Mode.ActualMode.MAIN)); }});
        mainLayoutPanel.configActions.add(returnToMainFromConfig);
        mainLayoutPanel.configActions.add(new Label(""));


        // Focus the cursor on the name field when the app loads
        searchField.setFocus(true);
        searchField.selectAll();

        // Add a handler to send the name to the server
        SearchHandler shandler = new SearchHandler();
        searchButton.addClickHandler(shandler);
        searchField.addKeyUpHandler(shandler);

        // Add a handler for "nouveau client"
        EditClientHandler ehandler = new EditClientHandler(-1, -1);
        newClientButton.addClickHandler(ehandler);


        // initialize sets of widgets (subpanels)
        allWidgets.add(mainLayoutPanel.search);
        allWidgets.add(mainLayoutPanel.editClient);
        allWidgets.add(mainLayoutPanel.lists);
        allWidgets.add(mainLayoutPanel.config);
        allWidgets.add(mainLayoutPanel.mainActions);
        allWidgets.add(mainLayoutPanel.listActions);
        allWidgets.add(mainLayoutPanel.versionLabel);

        // (anchors)
        allWidgets.add(voirListes);
        allWidgets.add(editConfig);
        allWidgets.add(logout);
        allWidgets.add(dropDownUserClubs);

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
        retrieveClientList(false);
    }

    /* --- client search UI functions --- */

    private void loadClientListResults(JsArray<ClientSummary> allClients, boolean display) {
        searchString = removeAccents(searchField.getText());
        searchResults.removeAllRows();
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
        refreshSelectedClub();

        if (firstSearchResultToDisplay != 0)
            prevResultsButton.setVisible(true);

        searchResults.removeAllRows();
        for (ClientSummary cs : allClients) {
            String s = "[" + cs.getId() + "] " + cs.getPrenom() + " " + cs.getNom();
	    int club = 0;

            if (selectedClub != 0) {
		club = selectedClub;
                boolean found = false;
                for (int j = 0; j < cs.getClubs().length(); j++) {
                    int cn = Integer.parseInt(cs.getClubs().get(j));
                    if (selectedClub == cn)
                        found = true;
                }
                if (!found) continue;
            } else {
		if (cs.getClubs().length() > 0)
		    club = Integer.parseInt(cs.getClubs().get(0));
	    }

            String ss = removeAccents(s);
            if (!ss.contains(searchString)) continue;

            if (cs.getSaisons() != null && !cs.getSaisons().isEmpty())
                s += " ("+cs.getSaisons()+")";

            if (resultCount >= firstSearchResultToDisplay) {
                Anchor h = new Anchor(s);
                h.addClickHandler(new EditClientHandler(club, Integer.parseInt(cs.getId())));
                searchResults.setWidget(displayedCount++, 0, h);
            }

            resultCount++;
            if (displayedCount >= MAX_RESULTS) {
                nextResultsButton.setVisible(true);
                break;
            }
        }
        searchResultsPanel.setVisible(true);
    }

    /* --- club list UI functions --- */
    boolean pendingRetrieveClubList = false;
    void populateClubList(boolean tousOK, ListBox dropDownUserClubs) {
        if (allClubs == null) {
            if (pendingRetrieveClubList) return;
            retrieveClubList(tousOK, dropDownUserClubs);
            return;
        }

      dropDownUserClubs.clear();
      dropDownUserClubs.addItem(tousOK ? "TOUS" : "---");
      dropDownUserClubs.setVisibleItemCount(1);
      idxToClub.clear();

      for(int i = 0; i < allClubs.length(); ++i) {
          ClubSummary cs = allClubs.get(i);
          dropDownUserClubs.addItem(getClubText(cs), cs.getNumeroClub());
          idxToClub.put(i+1, cs); // add one because "TOUS" occupies index 0
      }

      // don't select TOUS by default if there is only 1 club
      if (allClubs.length() == 1) selectedClub = 1;
      dropDownUserClubs.setSelectedIndex(selectedClub);
    }

    /* --- club-list related utility functions --- */

    void clearSelectedClub() {
        selectedClub = 0;
    }

    void refreshSelectedClub() {
        selectedClub = dropDownUserClubs.getSelectedIndex();
    }

    void refreshSelectedClub(int idx) {
        selectedClub = idx;
    }

    boolean isClubSelected() {
	return selectedClub != 0;
    }

    String getSelectedClubID() {
        if (allClubs == null) return null;
        if (0 != selectedClub) return idxToClub.get(selectedClub).getId();
        else return null;
    }

    static String getClubText(ClubSummary cs) {
      return "[" + cs.getNumeroClub() + "] " + cs.getNom();
    }

    ClubSummary getClubSummaryByID(String cid) {
        for(int i = 0; i < allClubs.length(); ++i) {
          ClubSummary cs = allClubs.get(i);
          if (cs.getId().equals(cid)) return cs;
        }
        return null;
    }

    int getClubListBoxIndexByID(String cid) {
        for (Map.Entry<Integer, ClubSummary> entry : idxToClub.entrySet()) {
            if (entry.getValue().getId().equalsIgnoreCase(cid))
                return entry.getKey();
        }
        return -1;
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

    public void retrieveClubList(final boolean tousOK, final ListBox dropDownUserClubs) {
        String url = PULL_CLUB_LIST_URL;
        pendingRetrieveClubList = true;
        RequestCallback rc =
            createRequestCallback(new JudoDB.Function() {
                    public void eval(String s) {
                        loadClubListResults
                            (tousOK, dropDownUserClubs,
                             JsonUtils.<JsArray<ClubSummary>>safeEval(s));
                    }
                });
        retrieve(url, rc);
    }

    private void loadClubListResults(boolean tousOK, ListBox dropDownUserClubs, JsArray<ClubSummary> clubs) {
        firstSearchResultToDisplay = 0;
        this.allClubs = clubs;
        populateClubList(tousOK, dropDownUserClubs);
        pendingRetrieveClubList = false;
    }

    /* --- helper functions for status bar --- */

    void displayError(String error) {
        mainLayoutPanel.statusLabel.setStyleName("status");
        mainLayoutPanel.statusLabel.addStyleName("status-error");
        mainLayoutPanel.statusLabel.setText("Erreur: " + error);
        mainLayoutPanel.statusLabel.setVisible(true);
    }
    void clearStatus() {
        mainLayoutPanel.statusLabel.setText("");
        mainLayoutPanel.statusLabel.setVisible(false);
    }
    void pleaseWait() {
        setStatus("Veuillez patienter...");
    }
    void setStatus(String s) {
        mainLayoutPanel.statusLabel.setStyleName("status");
        mainLayoutPanel.statusLabel.addStyleName("status-info");
        mainLayoutPanel.statusLabel.setText(s);
        mainLayoutPanel.statusLabel.setVisible(true);
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
