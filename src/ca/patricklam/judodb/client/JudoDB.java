package ca.patricklam.judodb.client;

import java.util.ArrayList;
import java.util.Stack;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

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
	
	// testing db:
	//public static final String BASE_URL = "http://noether-wireless/~plam/anjoudb-backend/";
	public static final String BASE_URL = "http://www.judo-anjou.qc.ca/mascouche-backend/";
	
	private static final String PULL_CLIENT_LIST_URL = BASE_URL + "pull_client_list.php";
	private static final String AUTHENTICATE_URL = BASE_URL + "authenticate.php"; // used for testing authentication
	private static final String LOGOUT_URL = BASE_URL + "logout.php"; 
	int jsonRequestId = 0;
	
	private final Label statusLabel = new Label();

	/* search stuff */
	private final VerticalPanel searchResultsPanel = new VerticalPanel();
	private final TextBox searchField = new TextBox();
	private final FlexTable searchResults = new FlexTable();
	private final Button searchButton = new Button("Recherche");
	private final Button newClientButton = new Button("Nouveau client");
	private final Button nextResultsButton = new Button("Résultats suivants");
	private final Button prevResultsButton = new Button("Résultats précedents"); 

	/* actions */
	private final Anchor voirListes = new Anchor("Voir listes des cours");
	private final Anchor editConfig = new Anchor("Éditer configuration");
	private final Anchor logout = new Anchor("Fermer session");

	final Anchor filtrerListes = new Anchor("Filtrer");
	final Anchor editerListes = new Anchor("Éditer");
	final Anchor ftListes = new Anchor("FT-303");
	final Anchor impotListes = new Anchor("Reçus d'impot");
	final Anchor clearXListes = new Anchor("Effacer les X");
	final Anchor normalListes = new Anchor("Voir listes");
	final Anchor returnToMainFromListes = new Anchor("Retourner");

	ArrayList<Widget> allWidgets = new ArrayList<Widget>();
	
	/* state */
	JsArray<ClientSummary> allClients;
	private String searchString;
	private int firstSearchResultToDisplay = 0;
	private Stack<Mode> modeStack = new Stack<Mode>();
	private Mode currentMode = null;
	
	boolean isAuthenticated = false;
	private boolean authenticationPending = false;
	
	/* edit client stuff */
	private ClientWidget c;

	/* view lists stuff */
	private ListWidget l;

	/* config management stuff */
	private ConfigWidget cf;
	
	/* login stuff */
	private LoginWidget login = new LoginWidget(this);
	private String AUTH_OK = "OK", AUTH_EXPIRED = "EXPIRED", AUTH_BAD = "BAD";
	static class Authenticated extends JavaScriptObject {
		protected Authenticated() {}

		public final native String getAuthenticated() /*-{ return this.authenticated; }-*/;
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
			statusLabel.setText("");
			//searchButton.setEnabled(false);
			
			String url = PULL_CLIENT_LIST_URL;
		    url = URL.encode(url) + "?callback=";

		    pleaseWait();
		    getJsonForSearch(jsonRequestId++, url, JudoDB.this);
		}
	}
	
	class EditClientHandler implements ClickHandler {
		private int cid;
		public EditClientHandler(int cid) { this.cid = cid; }
		
		public void onClick(ClickEvent event) {
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
	
	private void _switchMode_editClient (int cid) {
		RootPanel.get("editClient").clear();
		this.c = new ClientWidget(cid, this);
		RootPanel.get("editClient").add(this.c);
		RootPanel.get("editClient").setVisible(true);
	}
	
	public void _switchMode_viewLists() {
		if (this.l == null) {
			this.l = new ListWidget(this);
			RootPanel.get("lists").add(this.l);
		}
		RootPanel.get("editClient").clear();

		RootPanel.get("listActions").setVisible(true);
		RootPanel.get("lists").setVisible(true);

		this.l.switchMode(ListWidget.Mode.NORMAL);
	}

	public void _switchMode_config() {
		if (this.cf == null) {
			this.cf = new ConfigWidget(this);
			RootPanel.get("config").add(this.cf);
		}
		RootPanel.get("editClient").clear();

		RootPanel.get("listActions").setVisible(true);
		RootPanel.get("config").setVisible(true);
		returnToMainFromListes.setVisible(true);
	}

	public void _switchMode_main() {
		clearStatus();
		
		RootPanel.get("mainActions").setVisible(true);
		RootPanel.get("search").setVisible(true);
		voirListes.setVisible(true);
		editConfig.setVisible(true);
		logout.setVisible(true);
		
		searchButton.setEnabled(true);
		searchButton.setFocus(true);
	}

	/** After changing any data, invalidate the old list. */
	public void invalidateListWidget() {		
		this.l.removeFromParent();
		this.l = null;
	}
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {		
		// Add content to the RootPanel
		RootPanel.get("statusContainer").add(statusLabel);

		// search buttons
		RootPanel.get("search").add(searchField);
		RootPanel.get("search").add(searchButton);
		RootPanel.get("search").add(newClientButton);
		
		// edit client buttons		
		final Label resultsLabel = new Label("Résultats: ");
		
		final Panel searchNavPanel = new HorizontalPanel();
		searchNavPanel.add(nextResultsButton);
		nextResultsButton.setVisible(false);
		nextResultsButton.addClickHandler(new ClickHandler() { 
			public void onClick(ClickEvent e) { 
				firstSearchResultToDisplay += MAX_RESULTS; 
				if (firstSearchResultToDisplay + MAX_RESULTS > allClients.length()) firstSearchResultToDisplay -= MAX_RESULTS;
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
		RootPanel.get("search").add(searchResultsPanel);
		
		// right bar actions: main
		Panel mainActions = RootPanel.get("mainActions");
		voirListes.addClickHandler(new ClickHandler() { public void onClick(ClickEvent e) { switchMode(new Mode(Mode.ActualMode.LIST)); }});
		mainActions.add(voirListes);
		mainActions.add(new Label(""));
		editConfig.addClickHandler(new ClickHandler() { public void onClick(ClickEvent e) { switchMode(new Mode(Mode.ActualMode.CONFIG)); }});
		mainActions.add(editConfig);
		mainActions.add(new Label(""));
		
		logout.addClickHandler(new ClickHandler() { 
			public void onClick(ClickEvent e) { 
				getJsonForAuth(jsonRequestId++, LOGOUT_URL + "?callback=", JudoDB.this); 
				isAuthenticated = false; 
				authenticationPending = false;
			}});
		mainActions.add(logout);

		// right bar actions: list
		Panel listActions = RootPanel.get("listActions");
		filtrerListes.addClickHandler(new ClickHandler() { public void onClick(ClickEvent e) { 
			if (JudoDB.this.l != null) JudoDB.this.l.toggleFiltering(); }});
		listActions.add(filtrerListes);
		listActions.add(new Label(""));
		editerListes.addClickHandler(new ClickHandler() { public void onClick(ClickEvent e) { 
			if (JudoDB.this.l != null) JudoDB.this.l.switchMode(ListWidget.Mode.EDIT); }});
		listActions.add(editerListes);
		listActions.add(new Label(""));
		ftListes.addClickHandler(new ClickHandler() { public void onClick(ClickEvent e) { 
			if (JudoDB.this.l != null) JudoDB.this.l.switchMode(ListWidget.Mode.FT); }});
		listActions.add(ftListes);
        listActions.add(new Label(""));
        impotListes.addClickHandler(new ClickHandler() { public void onClick(ClickEvent e) { 
            if (JudoDB.this.l != null) JudoDB.this.l.switchMode(ListWidget.Mode.IMPOT); }});
        listActions.add(impotListes);
		listActions.add(new Label(""));
		clearXListes.addClickHandler(new ClickHandler() { public void onClick(ClickEvent e) { 
			if (JudoDB.this.l != null) JudoDB.this.l.clearX(); }});
		listActions.add(clearXListes);
		listActions.add(new Label(""));
		normalListes.addClickHandler(new ClickHandler() { public void onClick(ClickEvent e) { 
			if (JudoDB.this.l != null) JudoDB.this.l.switchMode(ListWidget.Mode.NORMAL); }});
		listActions.add(normalListes);
		listActions.add(new Label(""));
		returnToMainFromListes.addClickHandler(new ClickHandler() { public void onClick(ClickEvent e) { switchMode(new Mode(Mode.ActualMode.MAIN)); }});
		listActions.add(returnToMainFromListes);
		listActions.add(new Label(""));

		// enable login box's glassing
		login.setGlassEnabled(true);
		
		// Focus the cursor on the name field when the app loads
		searchField.setFocus(true);
		searchField.selectAll();

		// Add a handler to send the name to the server
		SearchHandler shandler = new SearchHandler();
		searchButton.addClickHandler(shandler);
		searchField.addKeyUpHandler(shandler);
		
		// Add a handler for "nouveau client"
		EditClientHandler ehandler = new EditClientHandler(-1);
		newClientButton.addClickHandler(ehandler);

		// initialize sets of widgets (subpanels)
		allWidgets.add(RootPanel.get("editClient"));
		allWidgets.add(RootPanel.get("lists"));
		allWidgets.add(RootPanel.get("config"));
		allWidgets.add(RootPanel.get("mainActions"));
		allWidgets.add(RootPanel.get("listActions"));
		allWidgets.add(RootPanel.get("search"));

		// (anchors)
		allWidgets.add(voirListes);
		allWidgets.add(editConfig);
		allWidgets.add(logout);
		
		// history handlers
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			public void onValueChange(ValueChangeEvent<String> event) {
				String historyToken = event.getValue();
				Mode m = Mode.parse(historyToken);
				switchMode(m);
			}
		});
		
		History.fireCurrentHistoryState();
		modeStack.push(new Mode(Mode.ActualMode.MAIN));
		ensureAuthentication();
	}

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
	
	private void loadSearchResults(JsArray<ClientSummary> allClients) {
		searchString = removeAccents(searchField.getText());
		searchResults.removeAllRows();
		firstSearchResultToDisplay = 0;
		this.allClients = allClients;
		displaySearchResults();
	}

	private void displaySearchResults() {
		int resultCount = 0, displayedCount = 0;
		
		if (firstSearchResultToDisplay != 0)
			prevResultsButton.setVisible(true);
			
		for (int i = 0; i < allClients.length(); i++) {
			ClientSummary cs = allClients.get(i);
			String s = "[" + cs.getId() + "] " + cs.getPrenom() + " " + cs.getNom();

			String ss = removeAccents(s);
			if (!ss.contains(searchString)) continue;
			
			if (cs.getSaisons() != null && !cs.getSaisons().isEmpty())
				s += " ("+cs.getSaisons()+")";
			
			if (resultCount >= firstSearchResultToDisplay) {
				Anchor h = new Anchor(s);
				h.addClickHandler(new EditClientHandler(Integer.parseInt(cs.getId())));
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
	
	/**
	 * Handle the response to the request for search data.
	 */
	public void handleJsonSearchResponse(JavaScriptObject jso) {
	    if (jso == null) {
	      displayError("pas de réponse; veuillez re-essayer");
	      return;
	    }
	    clearStatus();

	    loadSearchResults(asArrayOfClientSummary (jso));
	  }
	  
	/**
	 * Make call to remote server to get search results.
	 */
	public native static void getJsonForSearch(int requestId, String url,
	      JudoDB handler) /*-{
	   var callback = "callback" + requestId;

	   var script = document.createElement("script");
	   script.setAttribute("src", url+callback);
	   script.setAttribute("type", "text/javascript");
	   window[callback] = function(jsonObj) {
	     handler.@ca.patricklam.judodb.client.JudoDB::handleJsonSearchResponse(Lcom/google/gwt/core/client/JavaScriptObject;)(jsonObj);
	     window[callback + "done"] = true;
	   }

	   setTimeout(function() {
	     if (!window[callback + "done"]) {
	       handler.@ca.patricklam.judodb.client.JudoDB::handleJsonSearchResponse(Lcom/google/gwt/core/client/JavaScriptObject;)(null);
	     }

	     document.body.removeChild(script);
	     delete window[callback];
	     delete window[callback + "done"];
	   }, 10000);

	   document.body.appendChild(script);
	  }-*/;

	// authentication stuff
	/** Start the authentication process: if authenticated, do nothing. 
	 * Else display the authbox. */
	public void ensureAuthentication() {
		clearStatus();
		
		if (isAuthenticated) return;
		
		if (!authenticationPending) {
			authenticationPending = true;
		    getJsonForAuth(jsonRequestId++, AUTHENTICATE_URL + "?callback=", JudoDB.this);
		}
	}
	
	public native static void getJsonForAuth(int requestId, String url,
		      JudoDB handler) /*-{
		   var callback = "callback" + requestId;

		   var script = document.createElement("script");
		   script.setAttribute("src", url+callback);
		   script.setAttribute("type", "text/javascript");
		   window[callback] = function(jsonObj) {
		     window[callback + "done"] = true;
		     handler.@ca.patricklam.judodb.client.JudoDB::handleJsonAuthResponse(Lcom/google/gwt/core/client/JavaScriptObject;)(jsonObj);
		   }

		   setTimeout(function() {
		     if (!window[callback + "done"]) {
		       handler.@ca.patricklam.judodb.client.JudoDB::handleJsonAuthResponse(Lcom/google/gwt/core/client/JavaScriptObject;)(null);
		     }

		     document.body.removeChild(script);
		     delete window[callback];
		     delete window[callback + "done"];
		   }, 1000);

		   document.body.appendChild(script);
		  }-*/;

	public void handleJsonAuthResponse(JavaScriptObject jso) {
	    if (jso == null) {
	    	ensureAuthentication();
	    	return;
	    }

	    authenticationPending = false;
	    Authenticated a = jso.cast();
	    if (a.getAuthenticated().equals(AUTH_OK)) {
	    	setStatus("merci, identifié avec succes.");
	    	login.hide();
	    	isAuthenticated = true;
	    	_switchMode(currentMode);
	    	new Timer() { public void run() { clearStatus(); } }.schedule(1000);
	    	return;
	    } else if (a.getAuthenticated().equals(AUTH_EXPIRED)) {
	    	login.renewChallenge(); login.center();
	    } else if (a.getAuthenticated().equals(AUTH_BAD)) { 
	    	displayError("mot de passe invalide; veuillez re-essayer");
	    	new Timer() { public void run() { clearStatus(); } }.schedule(1000);
	    	login.renewChallenge(); login.center();
	    } else {
	    	displayError("Bad response from auth script.");
	    	new Timer() { public void run() { clearStatus(); } }.schedule(1000);
	    	login.renewChallenge(); login.center();
	    	return;
	    }
	}
		
	/**
	 * Convert the string 'json' into a JavaScript object.
	 */
	private final native JsArray<ClientSummary> asArrayOfClientSummary(JavaScriptObject jso) /*-{
	    return jso;
	}-*/;
	
	void displayError(String error) {
		statusLabel.addStyleName("status-error");
		statusLabel.removeStyleName("status-info");
		statusLabel.setText("Erreur: " + error);
	    statusLabel.setVisible(true);
	}	
	void clearStatus() {
		statusLabel.setText("");
	    statusLabel.setVisible(false);
	}
	void pleaseWait() {
		setStatus("Veuillez patienter...");
	}
	void setStatus(String s) {
		statusLabel.removeStyleName("status-error");
		statusLabel.addStyleName("status-info");
		statusLabel.setText(s);
		statusLabel.setVisible(true);
	}
}
