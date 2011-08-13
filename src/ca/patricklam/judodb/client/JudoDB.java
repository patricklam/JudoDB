package ca.patricklam.judodb.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class JudoDB implements EntryPoint {
	public static final int MAX_RESULTS = 10;
	
	public static final String BASE_URL = "http://127.0.0.1/~plam/anjoudb/";
	private static final String PULL_CLIENT_LIST_URL = BASE_URL + "pull_client_list.php";
	private static final String REQUEST_CHALLENGE_URL = BASE_URL + "request_challenge.php";
	//private static final String AUTHENTICATE_URL = BASE_URL + "authenticate.php";
	private int jsonRequestId = 0;
	
	private final Label statusLabel = new Label();

	/* search stuff */
	private final VerticalPanel searchResultsPanel = new VerticalPanel();
	private final TextBox searchField = new TextBox();
	private final FlexTable searchResults = new FlexTable();
	private final Button searchButton = new Button("Recherche");
	private final Button newClientButton = new Button("Nouveau client");
	private final Button nextResultsButton = new Button("Résultats suivants");
	private final Button prevResultsButton = new Button("Résultats précedents"); 
	private JsArray<ClientSummary> allClients;
	private String searchString;
	private int firstSearchResultToDisplay = 0;
	
	/* edit client stuff */
	private ClientWidget c;
	
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

		    getJson(jsonRequestId++, url, JudoDB.this);
		}
	}
	
	class EditClientHandler implements ClickHandler {
		private int cid;
		public EditClientHandler(int cid) { this.cid = cid; }
		
		public void onClick(ClickEvent event) {
			editClient(cid);
		}		
	}

	public void editClient (int cid) {
		RootPanel.get("search").setVisible(false);		
		RootPanel.get("editClient").clear();

		this.c = new ClientWidget(this, cid);
		RootPanel.get("editClient").add(c);
		RootPanel.get("editClient").setVisible(true);
	}
	
	public void returnToSearch() {
		RootPanel.get("editClient").setVisible(false);
		RootPanel.get("search").setVisible(true);
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
				displaySearchResults(); } });
		searchNavPanel.add(prevResultsButton);
		prevResultsButton.setVisible(false);
		prevResultsButton.addClickHandler(new ClickHandler() { 
			public void onClick(ClickEvent e) { 
				firstSearchResultToDisplay -= MAX_RESULTS; 
				displaySearchResults(); } });
		
		searchResultsPanel.add(resultsLabel);
		searchResultsPanel.add(searchResults);
		searchResultsPanel.add(searchNavPanel);
		searchResultsPanel.setVisible(false);
		RootPanel.get("search").add(searchResultsPanel);
		
		// Focus the cursor on the name field when the app loads
		searchField.setFocus(true);
		searchField.selectAll();

		// Create the login dialog box
		final DialogBox dialogBox = new DialogBox();
		dialogBox.setText("Identification");
		dialogBox.setAnimationEnabled(true);
		final TextBox idField = new TextBox();
		final PasswordTextBox pwField = new PasswordTextBox();
		final Button entrerButton = new Button("Entrer");
		VerticalPanel dialogVPanel = new VerticalPanel();
		dialogVPanel.addStyleName("dialogVPanel");
		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		dialogVPanel.add(idField);
		dialogVPanel.add(pwField);
		dialogVPanel.add(entrerButton);
		dialogBox.setWidget(dialogVPanel);

		// Add a handler to close the login DialogBox
		entrerButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				dialogBox.hide();
				searchButton.setEnabled(true);
				searchButton.setFocus(true);

				// need to additionally parametrize this getJson 
				// call with the two different JSONs we're getting.
			    getJson(jsonRequestId++, REQUEST_CHALLENGE_URL, JudoDB.this);

			    //String url = AUTHENTICATE_URL;
			    //url += "username=" + idField.getText();
			    //url += "&password=" + pwField.getText();			    				
			}
		});

		// Add a handler to send the name to the server
		SearchHandler shandler = new SearchHandler();
		searchButton.addClickHandler(shandler);
		searchField.addKeyUpHandler(shandler);
		
		// Add a handler for "nouveau client"
		EditClientHandler ehandler = new EditClientHandler(-1);
		newClientButton.addClickHandler(ehandler);
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
	 * Handle the response to the request for stock data from a remote server.
	 */
	public void handleJsonResponse(JavaScriptObject jso) {
	    if (jso == null) {
	      displayError("Couldn't retrieve JSON");
	      return;
	    }

	    loadSearchResults(asArrayOfClientSummary (jso));
	  }
	  
	/**
	 * Make call to remote server.
	 */
	public native static void getJson(int requestId, String url,
	      JudoDB handler) /*-{
	   var callback = "callback" + requestId;

	   var script = document.createElement("script");
	   script.setAttribute("src", url+callback);
	   script.setAttribute("type", "text/javascript");
	   window[callback] = function(jsonObj) {
	     handler.@ca.patricklam.judodb.client.JudoDB::handleJsonResponse(Lcom/google/gwt/core/client/JavaScriptObject;)(jsonObj);
	     window[callback + "done"] = true;
	   }

	   setTimeout(function() {
	     if (!window[callback + "done"]) {
	       handler.@ca.patricklam.judodb.client.JudoDB::handleJsonResponse(Lcom/google/gwt/core/client/JavaScriptObject;)(null);
	     }

	     document.body.removeChild(script);
	     delete window[callback];
	     delete window[callback + "done"];
	   }, 5000);

	   document.body.appendChild(script);
	  }-*/;
	
	/**
	 * Convert the string 'json' into a JavaScript object.
	 */
	private final native JsArray<ClientSummary> asArrayOfClientSummary(JavaScriptObject jso) /*-{
	    return jso;
	  }-*/;
	
	void displayError(String error) {
		statusLabel.setStylePrimaryName("error");
		statusLabel.setText("Erreur: " + error);
	    statusLabel.setVisible(true);
	}	
}
