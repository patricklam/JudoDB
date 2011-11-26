package ca.patricklam.judodb.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

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
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.NamedFrame;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class ListWidget extends Composite {
	interface MyUiBinder extends UiBinder<Widget, ListWidget> {}
	public static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
	JudoDB jdb;
	
	private JsArray<ClientData> allClients;
	private HashMap<String, ClientData> cidToCD = new HashMap<String, ClientData>();
	
	private static final String PULL_ALL_CLIENTS_URL = JudoDB.BASE_URL + "pull_all_clients.php";
	private static final String PUSH_MULTI_CLIENTS_URL = JudoDB.BASE_URL + "push_multi_clients.php";
	private static final String CALLBACK_URL_SUFFIX_Q = "?callback=";
	private static final String CALLBACK_URL_SUFFIX_A = "&callback=";
	private static final String CONFIRM_PUSH_URL = JudoDB.BASE_URL + "confirm_push.php?guid=";
	
	@UiField(provided=true) FormPanel listForm = new FormPanel(new NamedFrame("_"));
	@UiField Anchor pdf;
	@UiField Anchor presences;
	@UiField Anchor xls;
	@UiField Anchor xls2;

	@UiField Hidden multi;
	@UiField Hidden title;
	@UiField Hidden subtitle;
	@UiField Hidden short_title;
	@UiField Hidden data;
	@UiField Hidden data_full;
	@UiField Hidden auxdata;
	
	@UiField HTMLPanel ft303_controls;
	@UiField TextBox evt;
	@UiField TextBox date;
	@UiField Anchor createFT;

	@UiField HTMLPanel edit_controls;
	@UiField TextBox edit_date;

	@UiField ListBox cours;
	@UiField Grid results;
	@UiField Label nb;

	@UiField Button save;
	@UiField Button quit;
	
	@UiField FormPanel listEditForm;
	@UiField Hidden guid_on_form;
	@UiField Hidden currentSession;
	@UiField Hidden dataToSave;
	private String guid;
	private int pushTries;
	
	@UiField ListBox session;

	private class Columns {
		final static int CID = 0;
		final static int VERIFICATION = 1;
		final static int NOM = 2;
		final static int PRENOM = 3;
		final static int SEXE = 4;
		final static int GRADE = 5;
		final static int DATE_GRADE = 6;
		final static int TEL = 7;
		final static int JUDOQC = 8;
		final static int DDN = 9;
		final static int CATEGORIE = 10;
		final static int COURS_DESC = 11;
		final static int COURS_NUM = 12;
		final static int SESSION = 13;
		final static int DIVISION = 14;
	}

	enum Mode {
		NORMAL, FT, EDIT, GERER_CLASSES
	};

	private Mode mode = Mode.NORMAL;
	private boolean isFiltering;
	
	public String[] heads = new String[] {
		"No", "V", "Nom", "Prenom", "Sexe", "Grade", "DateGrade", "Tel", "JudoQC", "DDN", "Cat", "Cours", "", "Saisons", "Division"
	};
	private boolean[] sortable = new boolean[] {
			false, false, true, true, false, true, false, false, false, true, true, true, false, false, false
	};

	private HashMap<CheckBox, Boolean> originalVerifValues = new HashMap<CheckBox, Boolean>();
	private HashMap<ListBox, Integer> originalCours = new HashMap<ListBox, Integer>();
	private HashMap<TextBox, String> originalJudoQC = new HashMap<TextBox, String>();
	private HashMap<TextBox, String> originalGrades = new HashMap<TextBox, String>();
	private HashMap<TextBox, String> originalGradeDates = new HashMap<TextBox, String>();
	
	public ListWidget(JudoDB jdb) {
		this.jdb = jdb;
		initWidget(uiBinder.createAndBindUi(this));
		jdb.pleaseWait();
		switchMode(Mode.NORMAL);
		
		cours.addItem("Tous", "-1");
		for (Constants.Cours c : Constants.COURS) {
			cours.addItem(c.name, c.seqno);
		}
		
		session.addItem("Tous", "-1");
		for (Constants.Session s : Constants.SESSIONS) {
			if (s != Constants.currentSession())
				session.insertItem(s.abbrev, Integer.toString(s.seqno), 1);
		}
		session.insertItem(Constants.currentSession().abbrev, Integer.toString(Constants.currentSessionNo()), 0);
		session.setSelectedIndex(0);
		
		cours.addChangeHandler(new ChangeHandler() { 
			public void onChange(ChangeEvent e) { showList(); } });
		session.addChangeHandler(new ChangeHandler() { 
			public void onChange(ChangeEvent e) { showList(); } });
		pdf.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent e) { collectDV(); clearFull(); submit("pdf"); } });
		presences.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent e) { collectDV(); clearFull(); submit("presences"); } });
		xls.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent e) { collectDV(); clearFull(); submit("xls"); } });
		xls2.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent e) { collectDV(); computeFull(); submit("xlsfull"); } });
		
		createFT.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent e) { if (makeFT()) submit("ft"); }});
		save.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent e) {
				save();
			}
		});
		quit.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent e) {
				reset();
				ListWidget.this.switchMode(Mode.NORMAL); }
		});
		
		listEditForm.setAction(PUSH_MULTI_CLIENTS_URL);
		
		getJson(jdb.jsonRequestId++, PULL_ALL_CLIENTS_URL + CALLBACK_URL_SUFFIX_Q, this);
		sorts.add(3); sorts.add(2);
	}

	private void save() {
		// iterate through originalVerifValues and see which ones to generate
		// create a list of tuples of the form: 163, "verification", "0"; 164, "verification", "1"
		// we'll push to the server, which will update the services that match the cid and the current session.

		guid = UUID.uuid();
		guid_on_form.setValue(guid);
		currentSession.setValue(Constants.currentSession().abbrev);
		
		StringBuffer dv = new StringBuffer();
		for (int i = 0; i < results.getRowCount(); i++) {
			CheckBox cb = (CheckBox)results.getWidget(i, Columns.VERIFICATION);
			if (cb != null && cb.getValue() != originalVerifValues.get(cb)) {
				String cid = results.getText(i, Columns.CID);
				String v = cb.getValue() ? "1" : "0";
				dv.append(cid + ",Sverification,"+v+";");
			}
			Widget lbw = results.getWidget(i, Columns.COURS_DESC);
			if (lbw != null && lbw instanceof ListBox) {
				ListBox lb = (ListBox) lbw;
				if (lb.getSelectedIndex() != originalCours.get(lb)) {
					String cid = results.getText(i, Columns.CID);
					dv.append(cid + ",Scours,"+lb.getSelectedIndex()+";");				
				}
			}
			Widget jqw = results.getWidget(i, Columns.JUDOQC);
			if (jqw != null && jqw instanceof TextBox) {
				TextBox jq = (TextBox) jqw;
				if (!jq.getValue().equals(originalJudoQC.get(jq))) {
					String cid = results.getText(i, Columns.CID);
					dv.append(cid + ",Caffiliation,"+jq.getValue()+";");				
				}
			}
		}
		dataToSave.setValue(dv.toString());
		listEditForm.submit();

		pushTries = 0;
		new Timer() { public void run() {
			getJsonForStageTwoPush(jdb.jsonRequestId++, CONFIRM_PUSH_URL + guid + CALLBACK_URL_SUFFIX_A, ListWidget.this);
		} }.schedule(500);
	}
	
	private void reset() {
		getJson(jdb.jsonRequestId++, PULL_ALL_CLIENTS_URL + CALLBACK_URL_SUFFIX_Q, this);	
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
				if (!Constants.COURS[c].entraineur.equals(""))
					subtitle.setValue("Entraineur: " + Constants.COURS[c].entraineur);
				else
					subtitle.setValue("");
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

			dv += toDVFullString(cd) + "*";
		}
		data_full.setValue(dv);
	}
	
	private String toDVFullString(ClientData cd) {
		String dv = "";
		
		dv += cd.getNom() + "|";
		dv += cd.getPrenom() + "|";
		dv += cd.getSexe() + "|";
		dv += cd.getJudoQC() + "|";
		dv += cd.getDDNString() + "|";
		dv += cd.getCategorie(requestedSession().effective_year).abbrev + "|";
		dv += cd.getCourriel() + "|";
		dv += cd.getAdresse() + "|";
		dv += cd.getVille() + "|";
		dv += cd.getCodePostal() + "|";
		dv += cd.getTel() + "|";
		dv += cd.getCarteAnjou() + "|";
		dv += cd.getTelContactUrgence() + "|";
		dv += cd.getMostRecentGrade().getGrade() + "|";
		dv += cd.getMostRecentGrade().getDateGrade() + "|";
		ServiceData sd = cd.getServiceFor(requestedSession()); 
		if (sd != null && !sd.getCours().equals(""))
			dv += Constants.COURS[Integer.parseInt(sd.getCours())].short_desc + "|";
		else
			dv += "|";
		return dv;
	}
	
	private void submit(String act) {
		addMetaData();
		listForm.setAction(JudoDB.BASE_URL+"listes"+act+".php");
		listForm.submit();
	}
	
	private boolean makeFT() {
		StringBuffer dv = new StringBuffer("");
		for (int i = 1; i < results.getRowCount(); i++) {
			CheckBox cb = (CheckBox)results.getWidget(i, Columns.VERIFICATION);
			if (cb != null && cb.getValue()) {
				ClientData cd = cidToCD.get(results.getText(i, Columns.CID));

				StringBuffer post = new StringBuffer();
				ListBox w = (ListBox)(results.getWidget(i, Columns.DIVISION));
				if (w != null)
					post.append(w.getValue(w.getSelectedIndex()));
				post.append("|");
				dv.append(toDVFullString(cd) + post + "*");			
			}
		}
		data.setValue("");
		data_full.setValue(dv.toString());
		auxdata.setValue(Constants.CLUB + "|" + Constants.CLUBNO);
		return !dv.equals("");
	}

	public Constants.Session requestedSession() {
		String requestedSessionNo = session.getValue(session.getSelectedIndex());
		if (requestedSessionNo.equals("-1")) return null;
		return Constants.SESSIONS[Integer.parseInt(requestedSessionNo)];
	}
		
	public boolean sessionFilter(ClientData cd) {
		Constants.Session rs = requestedSession();
		if (rs == null) return true;
		
		return cd.getServiceFor(rs) != null;
	}
	
	public boolean filter(ClientData cd) {
		// filter for season
		if (!sessionFilter(cd))
			return false;

		// filter for cours
		String selectedCours = cours.getValue(cours.getSelectedIndex());
		if (selectedCours.equals("-1"))
			return true;
		
		if (selectedCours.equals(cd.getServiceFor(requestedSession()).getCours()))
			return true;
		
		return false;
	}
	
	public void clearX() {
		for (int i = 0; i < results.getRowCount(); i++) {
			CheckBox cb = (CheckBox)results.getWidget(i, Columns.VERIFICATION);
			if (cb != null && cb.getValue()) {
				cb.setValue(false);
			}
		}
	}
	
	private ArrayList<Integer> sorts = new ArrayList<Integer>();
	
	@SuppressWarnings("deprecation")
	public void showList() {
		boolean all = "-1".equals(cours.getValue(cours.getSelectedIndex()));
		String requestedSessionNo = session.getValue(session.getSelectedIndex());
		final Constants.Session rs = requestedSession();
		int count = 0, curRow;
		final ArrayList<ClientData> filteredClients = new ArrayList<ClientData>();

		final class SortClickHandler implements ClickHandler {
			int col;
			SortClickHandler(int col) { this.col = col; }

			public void onClick(ClickEvent e) {
				if (sorts.get(sorts.size()-1).equals(col) ||
						sorts.get(sorts.size()-1).equals(-col)) {
					int q = sorts.get(sorts.size()-1);
					sorts.remove(sorts.size()-1);
					sorts.add(-q);
				} else {
					sorts.remove(new Integer(col));
					sorts.add(col);
				}
				showList();
			}
		}
		
		// two passes: 1) count; 2) populate the grid
		for (int i = 0; i < allClients.length(); i++) {
			ClientData cd = allClients.get(i);
			cidToCD.put(cd.getID(), cd);
			if (!filter(cd)) continue;
			
			filteredClients.add(cd);
			count++;
		}

		for (final Integer col : sorts) {
			final int colSign = (col < 0) ? -1 : 1;
			Collections.sort(filteredClients, new Comparator<ClientData>() {
				public int compare(ClientData x, ClientData y) {
					switch (col * colSign) {
					case 2:
						return colSign * x.getNom().compareToIgnoreCase(y.getNom());
					case 3:
						return colSign * x.getPrenom().compareToIgnoreCase(y.getPrenom());
					case 5:
						return colSign * x.getGrade().compareTo(y.getGrade());
					case 9:
						return colSign * x.getDDN().compareTo(y.getDDN());
					case 10:
						int xc = x.getCategorie(rs.effective_year).years_ago;
						int yc = y.getCategorie(rs.effective_year).years_ago;
						if (xc == 0) xc = Integer.MAX_VALUE;
						if (yc == 0) yc = Integer.MAX_VALUE;
						return colSign * (xc - yc);
					case 11: 
						ServiceData xsd = x.getServiceFor(rs);
						int xcours = xsd != null ? Integer.parseInt(xsd.getCours()) : -1;

						ServiceData ysd = y.getServiceFor(rs);
						int ycours = ysd != null ? Integer.parseInt(ysd.getCours()) : -1;
						return colSign * (xcours - ycours);
					}
					return 0;
				}
			});
		}
		
		results.resize(count+1, 15);
				
		if (mode==Mode.FT) 
			heads[Columns.VERIFICATION] = "FT"; 
		else 
			heads[Columns.VERIFICATION] = "V";
		
		boolean[] visibility = new boolean[] {
				true, mode==Mode.EDIT || mode==Mode.FT, true, true, mode==Mode.EDIT || mode==Mode.FT,
				true, true, true, true, true, true, mode==Mode.EDIT || all, false, 
				requestedSessionNo.equals("-1"), mode==Mode.FT
		};

		for (int i = 0; i < heads.length; i++) {
			if (visibility[i]) {
				if (sortable[i]) {
					Anchor a = new Anchor(heads[i]);
					a.addClickHandler(new SortClickHandler(i));
					results.setWidget(0, i, a);
				}
				else
					results.setText(0, i, heads[i]);
				results.getCellFormatter().setStyleName(0, i, "list-th");
				results.getCellFormatter().setVisible(0, i, true);
			} else { 
				results.setText(0, i, "");
				results.getCellFormatter().setVisible(0, i, false);
			}
		}
		
		curRow = 1;
		for (ClientData cd : filteredClients) {
			String grade = cd.getGrade();
			if (grade != null && grade.length() >= 3) grade = grade.substring(0, 3);
			
			ServiceData sd = cd.getServiceFor(rs);
			int cours = sd != null ? Integer.parseInt(sd.getCours()) : -1;
			
			Anchor nomAnchor = new Anchor(cd.getNom()), prenomAnchor = new Anchor(cd.getPrenom());
			ClickHandler c = jdb.new EditClientHandler(Integer.parseInt(cd.getID()));
			nomAnchor.addClickHandler(c);
			prenomAnchor.addClickHandler(c);

			results.setText(curRow, Columns.CID, cd.getID());
			results.setWidget(curRow, Columns.NOM, nomAnchor);
			results.setWidget(curRow, Columns.PRENOM, prenomAnchor);
			results.setText(curRow, Columns.SEXE, cd.getSexe());
			
			results.setText(curRow, Columns.GRADE, "");
			results.setText(curRow, Columns.DATE_GRADE, "");
			if (mode == Mode.EDIT) {
				TextBox g = new TextBox(); g.setValue(grade); g.setWidth("3em");
				results.setWidget(curRow, Columns.GRADE, g);
				originalGrades.put(g, grade);
				TextBox gd = new TextBox(); gd.setValue(cd.getDateGrade()); gd.setWidth("6em");
				results.setWidget(curRow, Columns.DATE_GRADE, gd);
				originalGradeDates.put(gd, cd.getDateGrade());
			} else if (grade != null && !grade.equals("")) {
				results.setText(curRow, Columns.GRADE, grade);
				results.setText(curRow, Columns.DATE_GRADE, cd.getDateGrade());
			}
			results.setText(curRow, Columns.TEL, cd.getTel());
			if (mode == Mode.EDIT) {
				TextBox jqc = new TextBox(); jqc.setValue(cd.getJudoQC()); jqc.setWidth("4em");
				results.setWidget(curRow, Columns.JUDOQC, jqc);
				originalJudoQC.put(jqc, cd.getJudoQC());
			} else {
				results.setText(curRow, Columns.JUDOQC, cd.getJudoQC());
			}
			results.setText(curRow, Columns.DDN, cd.getDDNString());
			results.setText(curRow, Columns.CATEGORIE, cd.getCategorie((rs == null ? Constants.currentSession() : rs).effective_year).abbrev);

			if (visibility[Columns.VERIFICATION]) {
				CheckBox cb = new CheckBox();
				results.setWidget(curRow, Columns.VERIFICATION, cb);
				if (mode==Mode.EDIT) { 
					cb.setValue(sd.getVerification());
					originalVerifValues.put(cb, sd.getVerification());
				}
			}
			
			if (visibility[Columns.DIVISION] && (Constants.currentSession().effective_year - (cd.getDDN().getYear() + 1900)) > Constants.VETERAN) {
				ListBox d = new ListBox();
				d.addItem("Senior", "S");
				d.addItem("Masters", "M");
				results.setWidget(curRow, Columns.DIVISION, d);
			} else {
				results.clearCell(curRow, Columns.DIVISION);
			}
			
			results.setText(curRow, Columns.COURS_DESC, "");
			results.setText(curRow, Columns.COURS_NUM, "");				

			if (mode == Mode.EDIT) {
				ListBox w = newCoursWidget(cours);
				results.setWidget(curRow, Columns.COURS_DESC, w);
				results.setText(curRow, Columns.COURS_NUM, Integer.toString(cours));				
				originalCours.put(w, cours);
			} else if (cours != -1 && cours < Constants.COURS.length) {
				results.setText(curRow, Columns.COURS_DESC, Constants.COURS[cours].short_desc);
				results.setText(curRow, Columns.COURS_NUM, Integer.toString(cours));
			}
			
			if (requestedSessionNo.equals("-1")) {
				results.setText(curRow, Columns.SESSION, cd.getAllActiveSaisons());
			} else {
				results.setText(curRow, Columns.SESSION, "");
			}
			
			for (int j = 0; j < visibility.length; j++)
				results.getCellFormatter().setVisible(curRow, j, visibility[j]);
			curRow++;
		}
		
		nb.setText("Nombre inscrit: "+count);
	}
	
	private ListBox newCoursWidget(int cours) {
		ListBox lb = new ListBox();
		for (Constants.Cours c : Constants.COURS) {
			lb.addItem(c.short_desc, c.seqno);
		}
		lb.setSelectedIndex(cours);
		return lb;
	}
	
	public void switchMode(Mode m) {
		this.mode = m;
		switch (m) {
		case NORMAL:
			jdb.normalListes.setVisible(false);
			jdb.clearXListes.setVisible(false);
			jdb.editerListes.setVisible(true);
			jdb.ftListes.setVisible(true);
			ft303_controls.setVisible(false);
			edit_controls.setVisible(false);
			session.setEnabled(true);
			save.setVisible(false);
			quit.setVisible(false);
			break;
		case EDIT:
			session.setSelectedIndex(0);
			session.setEnabled(false);
			originalVerifValues.clear();
			jdb.normalListes.setVisible(true);
			jdb.clearXListes.setVisible(true);
			jdb.editerListes.setVisible(false);
			jdb.ftListes.setVisible(false);
			ft303_controls.setVisible(false);
			edit_controls.setVisible(true);
			save.setVisible(true);
			quit.setVisible(true);
			break;
		case FT:
			session.setSelectedIndex(0);
			session.setEnabled(false);
			jdb.normalListes.setVisible(true);
			jdb.clearXListes.setVisible(true);
			jdb.editerListes.setVisible(false);
			jdb.ftListes.setVisible(false);
			ft303_controls.setVisible(true);
			edit_controls.setVisible(false);
			save.setVisible(false);
			quit.setVisible(false);
			break;
		default:
			break;
		}
		jdb.retourner.setVisible(true);
		if (cours.getItemCount() > 0)
			showList();
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
			jdb.displayError("Couldn't retrieve JSON (lists)");
			return;
		}

	    this.allClients = jso.cast();
	    showList();
	    jdb.clearStatus();
	}

	/**
	 * Make call to remote server to request client information.
	 */
	public native static void getJsonForStageTwoPush(int requestId, String url,
	      ListWidget handler) /*-{
	   var callback = "callback" + requestId;

	   var script = document.createElement("script");
	   script.setAttribute("src", url+callback);
	   script.setAttribute("type", "text/javascript");
	   window[callback] = function(jsonObj) {
	     handler.@ca.patricklam.judodb.client.ListWidget::handleJsonStageTwoPushResponse(Lcom/google/gwt/core/client/JavaScriptObject;)(jsonObj);
	     window[callback + "done"] = true;
	   }

	   setTimeout(function() {
	     if (!window[callback + "done"]) {
	       handler.@ca.patricklam.judodb.client.ListWidget::handleJsonStageTwoPushResponse(Lcom/google/gwt/core/client/JavaScriptObject;)(null);
	     }

	     document.body.removeChild(script);
	     delete window[callback];
	     delete window[callback + "done"];
	   }, 10000);

	   document.body.appendChild(script);
	  }-*/;
	
	/**
	 * Handle the response to the request for data from a remote server.
	 */
	public void handleJsonStageTwoPushResponse(JavaScriptObject jso) {
		if (jso == null) {
			if (pushTries == 3) {
				jdb.displayError("pas de réponse");
				new Timer() { public void run() { jdb.clearStatus(); } }.schedule(2000);
				return;
			} else {
				tryConfirmPushAgain();
			}
		}	

	    ConfirmResponseObject cro = jso.cast();
	    
	    if (cro.getResult() != null && cro.getResult().equals("NOTYET")) {
	    	tryConfirmPushAgain(); 
	    	return;
	    }

	    if (cro.getResult() == null || !cro.getResult().equals("OK")) {
	    	jdb.displayError("le serveur n'a pas accepté les données");
	    	jdb.ensureAuthentication();
			new Timer() { public void run() { jdb.clearStatus(); } }.schedule(2000);
	    }
	    else {
	    	jdb.setStatus("Sauvegardé.");
			new Timer() { public void run() { jdb.clearStatus(); } }.schedule(2000);
			getJson(jdb.jsonRequestId++, PULL_ALL_CLIENTS_URL + CALLBACK_URL_SUFFIX_Q, this);	
	    }
	}
	
	private void tryConfirmPushAgain() {
		pushTries++;
		new Timer() { public void run() {
			ListWidget.getJsonForStageTwoPush
				(jdb.jsonRequestId++, CONFIRM_PUSH_URL + guid + CALLBACK_URL_SUFFIX_A, ListWidget.this);
		}}.schedule(1000);	
	}
}
