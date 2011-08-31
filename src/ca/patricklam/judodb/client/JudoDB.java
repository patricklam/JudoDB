package ca.patricklam.judodb.client;

import java.util.Stack;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.http.client.URL;
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

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class JudoDB implements EntryPoint {
	static class Mode {
		enum ActualMode {
			LOGIN, MAIN, LIST, EDIT_CLIENT;
		};
		ActualMode am;
		int arg;
		
		public Mode(ActualMode am) { this.am = am; }
		public Mode(ActualMode am, int arg) {
			this.am = am; this.arg = arg;
		}
	};
	
	public static final int MAX_RESULTS = 10;
	
	// testing db:
	//public static final String BASE_URL = "http://noether-wireless/~plam/anjoudb-backend/";

	public static final String BASE_URL = "http://www.judo-anjou.qc.ca/anjoudb-backend/";
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
	private final Anchor retourner = new Anchor("Retourner");
	private final Anchor logout = new Anchor("Fermer session");
	
	/* state */
	private JsArray<ClientSummary> allClients;
	private String searchString;
	private int firstSearchResultToDisplay = 0;
	private Stack<Mode> modes = new Stack<Mode>();
	
	/* edit client stuff */
	private ClientWidget c;

	/* view lists stuff */
	private ListWidget l;

	/* login stuff */
	private LoginWidget login;
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
			pushMode(new Mode (Mode.ActualMode.EDIT_CLIENT, cid));
		}		
	}

	// modes
	public Mode getCurrentMode() {
		return modes.peek();
	}
	
	public void switchMode(Mode newMode) {
		modes.clear();
		pushMode(newMode);
	}
	
	public void pushMode(Mode newMode) {
		modes.push(newMode);
		actuallySwitchMode(newMode);
	}
	
	public void popMode() {
		if (!modes.isEmpty()) {
			modes.pop();
			if (!modes.isEmpty())
			actuallySwitchMode(modes.peek());
		}
	}
	
	private void actuallySwitchMode(Mode newMode) {
		switch (newMode.am) {
		case EDIT_CLIENT:
			switchMode_editClient(newMode.arg);
			break;
		case LIST:
			switchMode_viewLists();
			break;
		case MAIN:
			switchMode_main();
			break;
		case LOGIN:
			switchMode_login();
			break;
		}
	}
	
	private void switchMode_editClient (int cid) {
		RootPanel.get("search").setVisible(false);
		RootPanel.get("actions").setVisible(false);
		RootPanel.get("lists").setVisible(false);
		RootPanel.get("editClient").clear();

		this.c = new ClientWidget(this, cid);
		RootPanel.get("editClient").add(this.c);
		RootPanel.get("editClient").setVisible(true);
	}
	
	public void switchMode_viewLists() {
		RootPanel.get("search").setVisible(false);
		RootPanel.get("actions").setVisible(true);
		RootPanel.get("editClient").clear();
		if (this.l == null) {
			this.l = new ListWidget(this);
			RootPanel.get("lists").add(this.l);
		}
		RootPanel.get("lists").setVisible(true);

		retourner.setVisible(true);
		voirListes.setVisible(false);
		logout.setVisible(false);
	}
	
	public void switchMode_main() {
		clearStatus();
		if (login != null) {
			RootPanel.get("login").remove(login);
			login = null;
		}
		
		RootPanel.get("editClient").setVisible(false);
		RootPanel.get("lists").setVisible(false);
		RootPanel.get("login").setVisible(false);
		RootPanel.get("actions").setVisible(true);
		RootPanel.get("search").setVisible(true);

		retourner.setVisible(false);
		voirListes.setVisible(true);
		logout.setVisible(true);
		
		searchButton.setEnabled(true);
		searchButton.setFocus(true);
	}


	/** Show the authentication dialog box. */
	private void switchMode_login() {
		this.login = new LoginWidget(this);
		RootPanel.get("login").add(login);
		RootPanel.get("login").setVisible(true);
		login.focus();
	}

	public void invalidateListWidget() {
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
		
		// right bar actions
		Panel actions = RootPanel.get("actions");
		voirListes.addClickHandler(new ClickHandler() { public void onClick(ClickEvent e) { switchMode(new Mode(Mode.ActualMode.LIST)); }});
		actions.add(voirListes);
		actions.add(new Label(""));
		retourner.setVisible(false);
		retourner.addClickHandler(new ClickHandler() { public void onClick(ClickEvent e) { switchMode(new Mode(Mode.ActualMode.MAIN)); }});
		actions.add(retourner);
		actions.add(new Label(""));
		logout.addClickHandler(new ClickHandler() { 
			public void onClick(ClickEvent e) { getJsonForAuth(jsonRequestId++, LOGOUT_URL + "?callback=", JudoDB.this);
			}});
		actions.add(logout);

		// hide the login box
		RootPanel.get("login").setVisible(false);

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

		switchMode(new Mode(Mode.ActualMode.MAIN));
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
			String s = cs.getPrenom() + " " + cs.getNom();

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
	   }, 1000);

	   document.body.appendChild(script);
	  }-*/;

	// authentication stuff
	/** Start the authentication process: if authenticated, do nothing. 
	 * Else display the authbox. */
	public void ensureAuthentication() {
		clearStatus();
	    getJsonForAuth(jsonRequestId++, AUTHENTICATE_URL + "?callback=", JudoDB.this);
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

	    Authenticated a = jso.cast();
	    if (a.getAuthenticated().equals(AUTH_OK)) {
	    	switchMode(new Mode(Mode.ActualMode.MAIN));
	    	setStatus("merci, identifié avec succes.");
	    	new Timer() { public void run() { clearStatus(); } }.schedule(1000);
	    	return;
	    } else if (a.getAuthenticated().equals(AUTH_BAD) || 
	    		a.getAuthenticated().equals(AUTH_EXPIRED)) {
	    	pushMode(new Mode(Mode.ActualMode.LOGIN));
	    } else {
	    	displayError("Bad response from auth script.");
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
