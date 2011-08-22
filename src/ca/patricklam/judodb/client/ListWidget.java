package ca.patricklam.judodb.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import ca.patricklam.judodb.client.Constants.Cours;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class ListWidget extends Composite {
	interface MyUiBinder extends UiBinder<Widget, ListWidget> {}
	public static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
	JudoDB jdb;
	
	private JsArray<ClientData> allClients;
	
	private static final String PULL_ALL_CLIENTS_URL = JudoDB.BASE_URL + "pull_all_clients.php";
	private static final String CALLBACK_URL_SUFFIX = "?callback=";
	
	@UiField(provided=true) FormPanel listForm = new FormPanel();
	@UiField(provided=true) Anchor pdf = new Anchor();
	@UiField(provided=true) Anchor presences = new Anchor();
	@UiField(provided=true) Anchor xls = new Anchor();
	@UiField(provided=true) Anchor xls2 = new Anchor();

	@UiField(provided=true) Hidden multi = new Hidden();
	@UiField(provided=true) Hidden title = new Hidden();
	@UiField(provided=true) Hidden subtitle = new Hidden();
	@UiField(provided=true) Hidden short_title = new Hidden();
	@UiField(provided=true) Hidden data = new Hidden();
	@UiField(provided=true) Hidden data_full = new Hidden();
	@UiField(provided=true) Hidden auxdata = new Hidden();
	
	@UiField(provided=true) ListBox cours = new ListBox();
	@UiField(provided=true) Grid results = new Grid();
	@UiField(provided=true) Label nb = new Label();

	enum Mode {
		NORMAL, FT, EDIT, GERER_CLASSES
	};

	private Mode mode = Mode.NORMAL;
	private boolean isFiltering;
	
	public String[] heads = new String[] {
		"", "Nom", "Prenom", "Sexe", "Grade", "DateGrade", "Tel", "JudoQC", "DDN", "Cat", "V", "Cours", ""
	};

	//var widthsForEditing = [-1, -1, -1, 1, 3, 8, -1, 8, -1, -1, -1, -1, -1];

	public ListWidget(JudoDB jdb) {
		this.jdb = jdb;
		initWidget(uiBinder.createAndBindUi(this));
		
		cours.addItem("Tous", "-1");
		for (Constants.Cours c : Constants.COURS) {
			cours.addItem(c.name, c.seqno);
		}
		cours.addChangeHandler(new ChangeHandler() { 
			public void onChange(ChangeEvent e) { showList(); } });
		pdf.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent e) { clearFull(); submit("pdf"); } });
		presences.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent e) { clearFull(); submit("presences"); } });
		xls.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent e) { clearFull(); submit("xls"); } });
		xls2.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent e) { computeFull(); submit("xlsfull"); } });
		
		jdb.clearError();
		getJson(jdb.jsonRequestId++, PULL_ALL_CLIENTS_URL + CALLBACK_URL_SUFFIX, this);	
	}

	private void addMetaData() {
		int c = Integer.parseInt(cours.getValue(cours.getSelectedIndex()));
		if (c > 0 || isFiltering) {
			multi.setValue("0");
			if (isFiltering) {
				if (c != -1) {
					title.setValue(Constants.COURS[c].name);
					short_title.setValue(Constants.COURS[c].short_desc);
				}
				else {
					title.setValue("");
					short_title.setValue("");
				}
			
				String st = "";
				// add filters to subtitle
				subtitle.setValue(st);
			} else {
				title.setValue(Constants.COURS[c].name);
				subtitle.setValue("Entraineur: " + Constants.COURS[c].entraineur);
				short_title.setValue(Constants.COURS[c].short_desc);
			}
		} else {
			// all classes
			String tt = "", subt = "", st = "";
			multi.setValue("1");

			for (Cours cc : Constants.COURS) {
				tt += cc.name + "|";
				subt += cc.entraineur + "|";
				st += cc.short_desc + "|";
			}
			title.setValue(tt);
			subtitle.setValue(subt);
			short_title.setValue(st);
		}
	}
	
	private void collectDV() {
		String dv = "";
		for (int i = 1; i < results.getRowCount(); i++) {
			for (int j = 1; j < results.getColumnCount(); j++)
				dv += results.getText(i, j) + "|";
			dv += "*";
		}
		data.setValue(dv);
	}
	
	private void clearFull() {
		data_full.setValue("");
	}
	
	private void computeFull() {
		String dv = "";
		for (int i = 0; i < allClients.length(); i++) {
			ClientData cd = allClients.get(i);
			if (!sessionFilter(cd)) continue;
			
			dv += cd.getNom() + "|";
			dv += cd.getPrenom() + "|";
			dv += cd.getJudoQC() + "|";
			dv += cd.getDDNString() + "|";
			dv += cd.getCategorieAbbrev() + "|";
			dv += cd.getCourriel() + "|";
			dv += cd.getAdresse() + "|";
			dv += cd.getVille() + "|";
			dv += cd.getCodePostal() + "|";
			dv += cd.getTel() + "|";
			dv += cd.getCarteAnjou() + "|";
			dv += cd.getNomContactUrgence() + "|";
			dv += cd.getTelContactUrgence() + "|";
			dv += cd.getMostRecentGrade().getGrade() + "|";
			dv += cd.getMostRecentGrade().getDateGrade() + "|";
			// TODO get the service for the session we're querying
			dv += Constants.COURS[Integer.parseInt(cd.getMostRecentService().getCours())].short_desc + "|";
			dv += "*";
		}
		data_full.setValue(dv);
	}
	
	private void submit(String act) {
		addMetaData();
		listForm.setAction(JudoDB.BASE_URL+"listes"+act+".php");
		listForm.submit();
	}

	// TODO actually implement this.
	public boolean sessionFilter(ClientData cd) {
		ServiceData sd = cd.getMostRecentService();
		if (sd == null) return false;
		return sd.getSaisons().contains("H11");
	}
	
	public boolean filter(ClientData cd) {
		ServiceData sd = cd.getMostRecentService();
		
		// must be signed up for something to pass filter
		if (sd == null) return false;
		
		// filter for season; TODO: fix to not necessarily require most recent season!
		if (!sessionFilter(cd))
			return false;
		
		// filter for cours
		String selectedCours = cours.getValue(cours.getSelectedIndex());
		if (!(selectedCours.equals("-1") || sd.getCours().equals(selectedCours)))
			return false;
		
		return true;
	}
	
	public void showList() {
		boolean all = "-1".equals(cours.getValue(cours.getSelectedIndex()));
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
		
		results.resize(count+1, 13);
		
		boolean[] visibility = new boolean[] {
				mode==Mode.FT, true, true, mode==Mode.EDIT || mode==Mode.FT,
				true, true, true, true, true, true, mode==Mode.EDIT, all, false
		};
		
		for (int i = 0; i < heads.length; i++) {
			if (visibility[i]) {
				results.setText(0, i, heads[i]);
				results.getCellFormatter().setStyleName(0, i, "list-th");
			} else { 
				results.setText(0, i, "");
				results.getCellFormatter().setVisible(0, i, false);
			}
		}
		
		curRow = 1;
		for (ClientData cd : filteredClients) {
			String grade = cd.getGrade();
			if (grade != null && grade.length() >= 3) grade = grade.substring(0, 3);
			int cours = Integer.parseInt(cd.getMostRecentService().getCours());
			
			results.setText(curRow, 1, cd.getNom());
			results.setText(curRow, 2, cd.getPrenom());
			results.setText(curRow, 3, cd.getSexe());
			
			if (grade != null && !grade.equals("")) {
				results.setText(curRow, 4, grade);
				results.setText(curRow, 5, cd.getDateGrade());
			} else {
				results.setText(curRow, 4, "");
				results.setText(curRow, 5, "");
			}
			results.setText(curRow, 6, cd.getTel());
			results.setText(curRow, 7, cd.getJudoQC());
			results.setText(curRow, 8, cd.getDDNString());
			results.setText(curRow, 9, cd.getCategorie().abbrev);

			if (visibility[10]) {
			// actually a checkbox:
			//results.setText(curRow, 10, cd.getMostRecentService().getVerification() ? "X" : "");
			}
			
			results.setText(curRow, 11, Constants.COURS[cours].short_desc);
			results.setText(curRow, 12, Integer.toString(cours));
			
			for (int j = 0; j < visibility.length; j++)
				results.getCellFormatter().setVisible(curRow, j, visibility[j]);
			curRow++;
		}
		collectDV();
		
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
