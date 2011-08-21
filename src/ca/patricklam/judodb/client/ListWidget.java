package ca.patricklam.judodb.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class ListWidget extends Composite {
	interface MyUiBinder extends UiBinder<Widget, ListWidget> {}
	public static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
	JudoDB jdb;
	
	private JsArray<ClientData> allClients;
	
	private static final String PULL_ALL_CLIENTS_URL = JudoDB.BASE_URL + "pull_all_clients.php";
	private static final String CALLBACK_URL_SUFFIX = "?callback=";
	private int jsonRequestId = 0;
	
	@UiField(provided=true) Grid results = new Grid();
	@UiField(provided=true) Label nb = new Label();

	//var heads =     ["", "Nom",   "Prenom", "Sexe", "Grade", "DateGrade", "Tel", "JudoQC", "DDN", "Cat", "V", "Cours", ""];
	  //var widthsForEditing = [-1, -1, -1, 1, 3, 8, -1, 8, -1, -1, -1, -1, -1];

	public ListWidget(JudoDB jdb) {
		this.jdb = jdb;
		initWidget(uiBinder.createAndBindUi(this));
		jdb.clearError();
		getJson(jsonRequestId++, PULL_ALL_CLIENTS_URL + CALLBACK_URL_SUFFIX, this);	
	}

	public boolean filter(ClientData cd) {
		ServiceData sd = cd.getMostRecentService();
		return sd != null && sd.getSaisons().equals("A10 H11");
	}
	
	public void showList() {
		int count = 0, curRow;
		ArrayList<ClientData> filteredClients = new ArrayList<ClientData>();
		
		// two passes: 1) count; 2) populate the grid
		for (int i = 0; i < allClients.length(); i++) {
			ClientData cd = allClients.get(i);
			if (!filter(cd)) continue;
			
			filteredClients.add(cd);
			count++;
		}
		Collections.sort(filteredClients, new Comparator<ClientData>() {
			public int compare(ClientData x, ClientData y) {
				int z = x.getNom().compareToIgnoreCase(y.getNom());
				if (z == 0)
					z = x.getPrenom().compareToIgnoreCase(y.getPrenom());
				return z; 
			}
		});
		
		results.resize(count, 13);
		curRow = 0;
		for (ClientData cd : filteredClients) {
			String grade = cd.getGrade();
			if (grade != null && grade.length() >= 3) grade = grade.substring(0, 3);
			
			results.setText(curRow, 1, cd.getNom());
			results.setText(curRow, 2, cd.getPrenom());
			results.setText(curRow, 3, cd.getSexe());
			results.setText(curRow, 4, grade);
			results.setText(curRow, 5, cd.getDateGrade());
			results.setText(curRow, 6, cd.getTel());
			results.setText(curRow, 7, cd.getJudoQC());
			results.setText(curRow, 8, cd.getDDNString());
			results.setText(curRow, 9, cd.getCategorie().abbrev);
			results.setText(curRow, 10, Constants.COURS[cd.getMostRecentService().getCours()].short_desc);
			results.setText(curRow, 11, cd.getMostRecentService().getVerification() ? "X" : "");
			curRow++;
		}

		nb.setText("Nombre inscrit: "+count);
	}
	
	/**
	 * Make call to remote server.
	 */
	public native static void getJson(int requestId, String url,
	      ListWidget handler) /*-{
	   var callback = "callback" + requestId;

	   var script = document.createElement("script");
	   script.setAttribute("src", url+callback);
	   script.setAttribute("type", "text/javascript");
	   window[callback] = function(jsonObj) {
	     handler.@ca.patricklam.judodb.client.ListWidget::handleJsonResponse(Lcom/google/gwt/core/client/JavaScriptObject;)(jsonObj);
	     window[callback + "done"] = true;
	   }

	   setTimeout(function() {
	     if (!window[callback + "done"]) {
	       handler.@ca.patricklam.judodb.client.ListWidget::handleJsonResponse(Lcom/google/gwt/core/client/JavaScriptObject;)(null);
	     }

	     document.body.removeChild(script);
	     delete window[callback];
	     delete window[callback + "done"];
	   }, 5000);

	   document.body.appendChild(script);
	  }-*/;

	/**
	 * Handle the response to the request for data from a remote server.
	 */
	public void handleJsonResponse(JavaScriptObject jso) {
		GWT.log("calling hjr, jso is " + (jso == null ? "null" : "non-null"));
		if (jso == null) {
			jdb.displayError("Couldn't retrieve JSON");
			return;
		}

	    this.allClients = asArrayOfClientData (jso);
	    showList();
	}

	private final native JsArray<ClientData> asArrayOfClientData(JavaScriptObject jso) /*-{
		return jso;
	}-*/;

}
