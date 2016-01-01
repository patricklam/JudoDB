// -*-  indent-tabs-mode:nil; c-basic-offset:4; -*-
package ca.patricklam.judodb.client;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import ca.patricklam.judodb.client.Constants.Division;
import ca.patricklam.judodb.client.Constants.Grade;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.History;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Timer;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Hidden;
import com.google.gwt.user.client.ui.NamedFrame;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.http.client.URL;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.http.client.RequestCallback;

import org.gwtbootstrap3.client.ui.gwt.CellTable;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.Collapse;
import org.gwtbootstrap3.client.ui.Label;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.ListBox;
import org.gwtbootstrap3.client.ui.DropDownMenu;

import org.gwtbootstrap3.client.shared.event.HideEvent;
import org.gwtbootstrap3.client.shared.event.HideHandler;
import org.gwtbootstrap3.client.shared.event.ShowEvent;
import org.gwtbootstrap3.client.shared.event.ShowHandler;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.gwtbootstrap3.client.ui.constants.Toggle;

import com.google.web.bindery.event.shared.HandlerRegistration;

public class ListWidget extends Composite {
    interface MyUiBinder extends UiBinder<Widget, ListWidget> {}
    public static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
    JudoDB jdb;

    private JsArray<ClientData> allClients;
    private Map<String, ClientData> cidToCD = new HashMap<>();
    private List<ClientData> filteredClients = new ArrayList<>();

    private List<SessionSummary> sessionSummaries = new ArrayList<>();
    private List<CoursSummary> coursSummaries = new ArrayList<>();
    private List<Prix> prix = new ArrayList<>();
    private Map<String, List<CoursSummary>> coursSummariesByShortDesc = new HashMap<>();
    private List<CoursSummary> uniqueCoursSummariesForSession = new ArrayList<>();
    private List<EscompteSummary> escompteSummaries = new ArrayList<>();
    private List<ProduitSummary> produitSummaries = new ArrayList<>();

    private SessionSummary currentSession;
    private CoursSummary currentCours;

    private boolean isFiltering;
    private boolean isFT;

    private boolean aeFilter_indifferent, aeFilter_value;
    private boolean pcFilter_indifferent, pcFilter_value;

    private static final String PULL_ALL_CLIENTS_URL = JudoDB.BASE_URL + "pull_all_clients.php";
    static final String CLUB_LABEL = "club=";

    @UiField(provided=true) FormPanel listForm = new FormPanel(new NamedFrame("_"));

    @UiField Hidden multi;
    @UiField Hidden title;
    @UiField Hidden short_title;
    @UiField Hidden data;
    @UiField Hidden data_full;
    @UiField Hidden auxdata;
    @UiField Hidden ft_evt;
    @UiField Hidden ft_date;

    @UiField Button sortirButton;
    @UiField DropDownMenu sortir;
    @UiField AnchorListItem sortir_pdf;
    @UiField AnchorListItem sortir_presences;
    @UiField AnchorListItem sortir_xls;
    @UiField AnchorListItem sortir_xls_complet;
    @UiField AnchorListItem sortir_impot;

    @UiField Button return_to_main;

    @UiField Hidden club_id;

    @UiField Collapse ft303_controls;
    @UiField TextBox evt;
    @UiField TextBox date;

    @UiField HTMLPanel impot_controls;
    @UiField CheckBox prorata;

    @UiField Button filter_button;
    @UiField Collapse filter_controls;
    @UiField ListBox division;
    @UiField ListBox grade_lower;
    @UiField ListBox grade_upper;
    @UiField Button aeButton;
    @UiField AnchorListItem aff_indifferent;
    @UiField AnchorListItem aff_oui;
    @UiField AnchorListItem aff_non;
    @UiField Button pcButton;
    @UiField AnchorListItem pc_indifferent;
    @UiField AnchorListItem pc_oui;
    @UiField AnchorListItem pc_non;

    @UiField CellTable<ClientData> results;

    @UiField Label nb;

    @UiField FormPanel listEditForm;
    @UiField Hidden guid_on_form;
    @UiField Hidden currentSessionField;
    @UiField Hidden dataToSave;

    @UiField ButtonGroup dropDownSessionButtonGroup;
    @UiField Button dropDownSessionButton;
    @UiField DropDownMenu dropDownSessions;

    @UiField ButtonGroup dropDownUserClubsButtonGroup;
    @UiField Button dropDownUserClubsButton;
    @UiField DropDownMenu dropDownUserClubs;

    @UiField ButtonGroup dropDownCoursButtonGroup;
    @UiField Button dropDownCoursButton;
    @UiField DropDownMenu dropDownCours;

    ListDataProvider<ClientData> resultsDataProvider;
    SelectionModel<ClientData> resultsSelectionModel;
    ListHandler<ClientData> resultsListHandler;

    boolean checkColumnVisible = false;
    VerificationCheckboxHeader checkHeader;
    Column<ClientData, Boolean> checkColumn;
    Column<ClientData, String> ddnColumn;
    Column<ClientData, String> gradeColumn;
    boolean dateGradeColumnVisible = true;
    Column<ClientData, String> dateGradeColumn;
    Column<ClientData, Boolean> affEnvoyeColumn;
    boolean payeColumnVisible = true;
    Column<ClientData, Boolean> payeColumn;
    boolean divisionColumnVisible = false;
    Column<ClientData, String> divisionColumn;
    Column<ClientData, String> sessionsColumn;
    Column<ClientData, String> coursColumn;
    boolean coursColumnVisible = true;

    boolean divisionSMColumnVisible = false;
    Map<ClientData, String> cdToSM = new HashMap<>();
    private ButtonGroupCell<Grade> gradeButtonCell;
    private ButtonGroupCell<CoursSummary> coursButtonCell;

    String mostRecentGradeDate;

    private String guid;
    private int pushTries;

    private class Columns {
        final static int SELECTION = 0;
        final static int NOM = 1;
        final static int PRENOM = 2;
        final static int SEXE = 3;
        final static int GRADE = 4;
        final static int DATE_GRADE = 5;
        final static int TEL = 6;
        final static int JUDOQC = 7;
        final static int AFF_ENVOYE = 8;
        final static int PAYE = 9;
        final static int DDN = 10;
        final static int DIVISION = 11;
        final static int COURS_DESC = 12;
        final static int SESSION = 13;
        final static int DIVISION_SM = 14;
    }

    private static final SafeHtml ARROWS = SafeHtmlUtils.fromSafeConstant("<i class=\"fa fa-fw fa-sort\"></i> ");
    private String[] heads = new String[] {
        "V", "Nom", "Prénom", "Sexe", "Grade", "DateGrade", "Tel", "JudoCA", "Affilié", "Payé", "DDN", "Div", "Cours", "Saisons", "Div (FT303)"
    };
    Column currentSortColumn;

    private static final String SORTIR_LABEL = "afficher...";
    private static final String SORTIR_FT303_LABEL = "afficher FT-303";
    private HandlerRegistration ft303_handler_registration;
    private final ClickHandler ft303_handler = new ClickHandler() {
            public void onClick(ClickEvent e) {
                if (makeFT()) submit("ft");
            }};

    private static final String INDIFFERENT_LABEL = "---";
    private static final String OUI_LABEL = "oui";
    private static final String NON_LABEL = "non";

    void selectClub(ClubSummary club) {
        jdb.selectClub(club);
        if (club == null) {
            if (isFT) {
                jdb.displayError("Veuillez selectionner un club pour les FT-303.");
                new Timer() { public void run() {
                    ListWidget.this.jdb.popMode();
                } }.schedule(2000);
                return;
            }

            dropDownUserClubsButton.setText(jdb.TOUS);
        } else {
            dropDownUserClubsButton.setText(club.getClubText());
        }
        coursButtonCell.setShowButton(club != null && currentSession != null);

        retrieveSessions(jdb.getSelectedClub());
        if (club != null)
            retrievePrix(club.getId());
        actuallyHandleClubChange();
    }

    private void selectSession(SessionSummary session) {
        if (session == null) {
            dropDownSessionButton.setText(jdb.TOUS);

            if (divisionColumnVisible) {
                results.removeColumn(divisionColumn);

                int coursIndex = results.getColumnIndex(coursColumn);
                if (coursIndex == -1) coursIndex = results.getColumnIndex(ddnColumn);
                results.insertColumn(coursIndex+1, sessionsColumn, heads[Columns.SESSION]);
                divisionColumnVisible = false;
            }
            if (checkColumnVisible) {
                results.removeColumn(checkColumn);
                checkColumnVisible = false;
            }
        } else {
            dropDownSessionButton.setText(session.getAbbrev());

            if (!divisionColumnVisible) {
                int coursIndex = results.getColumnIndex(coursColumn);
                if (coursIndex == -1) coursIndex = results.getColumnIndex(ddnColumn);
                results.insertColumn(coursIndex+1, divisionColumn, heads[Columns.DIVISION]);
                results.removeColumn(sessionsColumn);
                divisionColumnVisible = true;
            }
            if (!checkColumnVisible && isFT) {
                results.insertColumn(0, checkColumn, checkHeader);
                checkColumnVisible = true;
            }
        }
        coursButtonCell.setShowButton(jdb.getSelectedClub() != null && session != null);

        currentSession = session;
        updateUniqueCoursSummariesForSession();
        showList();
    }

    private void selectCours(CoursSummary cours) {
        if (coursColumnVisible) {
            results.removeColumn(coursColumn);
        }

        if (cours == null) {
            dropDownCoursButton.setText(jdb.TOUS);
            coursColumnVisible = true;
        } else {
            dropDownCoursButton.setText(cours.getShortDesc());
            coursColumnVisible = false;
        }

        if (coursColumnVisible) {
            int ddnIndex = results.getColumnIndex(ddnColumn);
            results.insertColumn(ddnIndex+1, coursColumn, heads[Columns.COURS_DESC]);
        }

        currentCours = cours;
        showList();
    }

    private void actuallyHandleClubChange() {
        if (!gotSessions) {
            new Timer() {
                public void run() { actuallyHandleClubChange(); }
            }.schedule(100);
            return;
        }

        retrieveCours();
        showList();
    }

    class ListClubListHandlerFactory implements JudoDB.ClubListHandlerFactory {
        @Override public ClickHandler instantiate(ClubSummary s) {
            return new ClubItemHandler(s);
        }
    }

    class ClubItemHandler implements ClickHandler {
        final ClubSummary club;

        ClubItemHandler(ClubSummary club) { this.club = club; }

        @Override public void onClick(ClickEvent e) {
            String ft = isFT ? JudoDB.Mode.LIST_PARAM_FT303 : "";
            String clubString = club != null ? (";" + CLUB_LABEL + club.getNumeroClub()) : "";
            jdb.switchMode(new JudoDB.Mode(JudoDB.Mode.ActualMode.LIST,
                                           ft + clubString));
        }
    }

    class SessionItemHandler implements ClickHandler {
        final SessionSummary session;

        SessionItemHandler(SessionSummary session) { this.session = session; }

        @Override public void onClick(ClickEvent e) {
            selectSession(session);
        }
    }

    class SessionAnchorListItem extends AnchorListItem implements Comparable<SessionAnchorListItem> {
        int effective_seqno;
        public SessionAnchorListItem(String label, int effective_seqno) {
            super(label);
            this.effective_seqno = effective_seqno;
        }

        @Override public int compareTo(SessionAnchorListItem other) {
            return other.effective_seqno - effective_seqno;
        }
    }

    class CoursItemHandler implements ClickHandler {
        final CoursSummary cours;

        CoursItemHandler(CoursSummary cours) {
            this.cours = cours;
        }

        @Override public void onClick(ClickEvent e) {
            selectCours(cours);
        }
    }

    private String getShortDescForCoursId(int coursId) {
        String cidString = Integer.toString(coursId);
        for (CoursSummary c : coursSummaries) {
            if (c.getId().equals(cidString))
                return c.getShortDesc();
        }
        return "inconnu";
    }

    class VerificationCheckboxHeader extends Header<Boolean> {
        public VerificationCheckboxHeader() {
            super(new CheckboxCell());
        }

        @Override
        public Boolean getValue() {
            boolean allItemsSelected = true, allItemsDeselected = true;
            if (currentSession == null) return false;

            for (ClientData cd : filteredClients) {
                if (cd.getServiceFor(currentSession).getVerification())
                    allItemsDeselected = false;
                else
                    allItemsSelected = false;
                if (!allItemsDeselected && !allItemsSelected)
                    break;
            }

            if (!allItemsDeselected && !allItemsSelected) {
                // xxx too bad indeterminate doesn't work
                return true;
            }
            return allItemsSelected;
        }

        @Override
        public void onBrowserEvent(Context context, Element elem, NativeEvent event) {
            InputElement input = elem.getFirstChild().cast();
            Boolean value = input.isChecked();
            String valueString = value ? "1" : "0";

            StringBuffer edits = new StringBuffer();

            for (ClientData cd : filteredClients) {
                edits.append(cd.getID() + ",Sverification," + valueString + ";");
                cd.getServiceFor(currentSession).setVerification(value);
            }
            pushEdit(edits.toString());
            }
    }

    private void enableFTMode() {
        isFT = true;
        showList();
        ft303_controls.show();
        sortirButton.setText(SORTIR_FT303_LABEL);
        sortirButton.setToggleCaret(false);
        sortirButton.setDataToggle(Toggle.BUTTON);
        ft303_handler_registration = sortirButton.addClickHandler(ft303_handler);
        results.setSelectionModel(resultsSelectionModel,
                                  DefaultSelectionEventManager.<ClientData>
                                  createCheckboxManager());

        if (!checkColumnVisible) {
            results.insertColumn(0, checkColumn, checkHeader);
            checkColumnVisible = true;
        }
        if (dateGradeColumnVisible) {
            results.removeColumn(dateGradeColumn);
            dateGradeColumnVisible = false;
        }
        if (payeColumnVisible) {
            results.removeColumn(payeColumn);
            payeColumnVisible = false;
        }
        divisionSMColumnVisible = true;
        jdb.populateClubList(!isFT, dropDownUserClubs, new ListClubListHandlerFactory());
    }

    private void disableFTMode() {
        isFT = false;
        showList();
        ft303_controls.hide();
        sortirButton.setText(SORTIR_LABEL);
        sortirButton.setToggleCaret(true);
        sortirButton.setDataToggle(Toggle.DROPDOWN);
        sortirButton.setActive(false);
        if (ft303_handler_registration != null)
            ft303_handler_registration.removeHandler();
        results.setSelectionModel(null);
        if (checkColumnVisible) {
            results.removeColumn(checkColumn);
            checkColumnVisible = false;
        }
        if (!dateGradeColumnVisible) {
            int gradeIndex = results.getColumnIndex(gradeColumn);
            results.insertColumn(gradeIndex + 1, dateGradeColumn, heads[Columns.DATE_GRADE]);
            dateGradeColumnVisible = true;
        }
        if (!payeColumnVisible) {
            int affEnvoyeIndex = results.getColumnIndex(affEnvoyeColumn);
            results.insertColumn(affEnvoyeIndex + 1, payeColumn, heads[Columns.PAYE]);
            payeColumnVisible = true;
        }
        divisionSMColumnVisible = false;
        jdb.populateClubList(!isFT, dropDownUserClubs, new ListClubListHandlerFactory());
    }

    void processArg(String arg) {
        String[] args = arg.split(";");
        String submode = args[0];

        disableFTMode();
        if (JudoDB.Mode.LIST_PARAM_FT303.equals(submode)) {
            enableFTMode();
        }

        boolean haveClub = false;
        for (int i = 1; i < args.length; i++) {
            if (args[i].startsWith(CLUB_LABEL)) {
                haveClub = true;
                String numero = args[i].substring(CLUB_LABEL.length());

                for (ClubSummary cs : jdb.allClubs) {
                    if (cs.getNumeroClub().equals(numero)) {
                        selectClub(cs);
                    }
                }
            }
        }
        if (!haveClub && !isFT) selectClub(null);
    }

    private void enableImpotMode() {
        // XXX TODO
    }

    public ListWidget(JudoDB jdb, String arg) {
        this.jdb = jdb;
        initWidget(uiBinder.createAndBindUi(this));

        aeFilter_indifferent = true;
        pcFilter_indifferent = true;
        initializeResultsTable();
        processArg(arg);

        listForm.addStyleName("hidden-print");
        jdb.populateClubList(!isFT, dropDownUserClubs, new ListClubListHandlerFactory());
        selectClub(jdb.getSelectedClub());

        jdb.pleaseWait();

        division.addItem("Tous", "-1");
        for (Division c : Constants.DIVISIONS)
            if (!c.noire)
                division.insertItem(c.abbrev, c.abbrev, 1);

        for (int i = 0; i < Constants.GRADES.length; i++) {
            Grade g = Constants.GRADES[i];
            grade_lower.insertItem(g.name, String.valueOf(g.order), i);
            grade_upper.insertItem(g.name, String.valueOf(g.order), i);
        }
        grade_lower.insertItem("---", "", 0); grade_lower.setSelectedIndex(0);
        grade_upper.insertItem("---", "", 0); grade_upper.setSelectedIndex(0);

        sortirButton.setText(SORTIR_LABEL);
        sortir_pdf.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent e) { collectDV(); clearFull(); submit("pdf"); } });
        sortir_presences.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent e) { collectDV(); clearFull(); submit("presences"); } });
        sortir_xls.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent e) { collectDV(); clearFull(); submit("xls"); } });
        sortir_xls_complet.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent e) { collectDV(); computeFull(); submit("xlsfull"); } });

        sortir_impot.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent e) { collectDV(); computeImpotMailMerge(); submit("impot"); } });

        filter_controls.addShowHandler(new ShowHandler() {
                @Override public void onShow(ShowEvent e) {
                    isFiltering = true; showList();
                    filter_button.setIcon(IconType.MINUS);
                } } );
        filter_controls.addHideHandler(new HideHandler() {
                @Override public void onHide(HideEvent e) {
                    isFiltering = false; showList();
                    filter_button.setIcon(IconType.PLUS);
                } } );

        division.addChangeHandler(new ChangeHandler() {
                @Override public void onChange(ChangeEvent e) { showList(); } });
        grade_lower.addChangeHandler(new ChangeHandler() {
                @Override public void onChange(ChangeEvent e) { showList(); } });
        grade_upper.addChangeHandler(new ChangeHandler() {
                @Override public void onChange(ChangeEvent e) { showList(); } });

        aeButton.setText(INDIFFERENT_LABEL);
        aff_indifferent.addClickHandler(new ClickHandler() {
                @Override public void onClick(ClickEvent e) { aeButton.setText(INDIFFERENT_LABEL); aeFilter_indifferent = true; showList(); } });
        aff_oui.addClickHandler(new ClickHandler() {
                @Override public void onClick(ClickEvent e) { aeButton.setText(OUI_LABEL); aeFilter_indifferent = false; aeFilter_value = true; showList(); } });
        aff_non.addClickHandler(new ClickHandler() {
                @Override public void onClick(ClickEvent e) { aeButton.setText(NON_LABEL); aeFilter_indifferent = false; aeFilter_value = false; showList(); } });

        pcButton.setText(INDIFFERENT_LABEL);
        pc_indifferent.addClickHandler(new ClickHandler() {
                @Override public void onClick(ClickEvent e) { pcButton.setText(INDIFFERENT_LABEL); pcFilter_indifferent = true; showList(); } });
        pc_oui.addClickHandler(new ClickHandler() {
                @Override public void onClick(ClickEvent e) { pcButton.setText(OUI_LABEL); pcFilter_indifferent = false; aeFilter_value = true; showList(); } });
        pc_non.addClickHandler(new ClickHandler() {
                @Override public void onClick(ClickEvent e) { pcButton.setText(NON_LABEL); pcFilter_indifferent = false; aeFilter_value = false; showList(); } });

        sortir_impot.setVisible(false);

        return_to_main.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent e) {
                History.back();
            }
        });

        mostRecentGradeDate = Constants.STD_DATE_FORMAT.format(new Date());
        listEditForm.setAction(JudoDB.PUSH_MULTI_CLIENTS_URL);

        retrieveAllClients();
    }

    abstract class AnchorColumn extends Column<ClientData, SafeHtml> {
        public AnchorColumn(Cell<SafeHtml> c) {
            super(c);
        }

        @Override public SafeHtml getValue(ClientData cd) {
            Anchor anchor = new Anchor();
            anchor.setHref("#edit"+cd.getID());
            anchor.setText(getText(cd));

            SafeHtmlBuilder sb = new SafeHtmlBuilder();
            sb.appendHtmlConstant(anchor.toString());
            return sb.toSafeHtml();
        }

        abstract String getText(ClientData cd);
    }

    private void initializeResultsTable() {
        boolean f = true;
        results.setAutoHeaderRefreshDisabled(false);
        results.setAutoFooterRefreshDisabled(true);

        resultsDataProvider = new ListDataProvider<>();
        resultsDataProvider.addDataDisplay(results);

        resultsListHandler =
            new ListHandler<ClientData>(resultsDataProvider.getList());
        results.addColumnSortHandler(resultsListHandler);

        resultsSelectionModel =
            new MultiSelectionModel<ClientData>(new ProvidesKey<ClientData>() {
                    @Override
                    public Object getKey(ClientData cd) {
                        return cd.getID();
                    }});

        checkHeader = new VerificationCheckboxHeader();
        checkColumn =
            new Column<ClientData, Boolean>(new CheckboxCell(true, false)) {
            @Override
            public Boolean getValue(ClientData cd) {
                return resultsSelectionModel.isSelected(cd);
            }
        };
        checkColumn.setFieldUpdater(new FieldUpdater<ClientData, Boolean>() {
                @Override public void update(int index, ClientData cd, Boolean value) {
                    StringBuffer edits = new StringBuffer();
                    String valueString = value ? "1" : "0";
                    edits.append(cd.getID() + ",Sverification," + valueString + ";");
                    cd.getServiceFor(currentSession).setVerification(value);
                    pushEdit(edits.toString());
                }
            });

        final Column<ClientData, SafeHtml> nomColumn =
            new AnchorColumn(new SafeHtmlCell())
            { @Override String getText(ClientData cd) { return cd.getNom(); } };
        nomColumn.setSortable(true);
        resultsListHandler.setComparator(nomColumn, new Comparator<ClientData>() {
                @Override public int compare(ClientData c1, ClientData c2) {
                    return c1.getNom().compareToIgnoreCase(c2.getNom());
                } });

        results.addColumn(nomColumn, new Header<String>(new TextCell() {
                @Override public void render(Cell.Context ctx, SafeHtml value, SafeHtmlBuilder sb) {
                    if (value != null) {
                        if (currentSortColumn != nomColumn)
                            sb.append(ARROWS);
                        sb.append(value);
                    }
                }
            }) {
                @Override public String getValue() {
                    return heads[Columns.NOM];
                } });

        final Column<ClientData, SafeHtml> prenomColumn =
            new AnchorColumn(new SafeHtmlCell())
            { @Override String getText(ClientData cd) { return cd.getPrenom(); } };
        prenomColumn.setSortable(true);
        resultsListHandler.setComparator(prenomColumn, new Comparator<ClientData>() {
                @Override public int compare(ClientData c1, ClientData c2) {
                    return c1.getPrenom().compareToIgnoreCase(c2.getPrenom());
                } });
        results.addColumn(prenomColumn, new Header<String>(new TextCell() {
                @Override public void render(Cell.Context ctx, SafeHtml value, SafeHtmlBuilder sb) {
                    if (value != null) {
                        if (currentSortColumn != prenomColumn)
                            sb.append(ARROWS);
                        sb.append(value);
                    }
                }
            }) {
                @Override public String getValue() {
                    return heads[Columns.PRENOM];
                } });

        final Column<ClientData, String> sexeColumn = new Column<ClientData, String>(new EditTextCell())
            { @Override public String getValue(ClientData cd) { return cd.getSexe(); } };
        sexeColumn.setSortable(true);
        resultsListHandler.setComparator(sexeColumn, new Comparator<ClientData>() {
                @Override public int compare(ClientData c1, ClientData c2) {
                    return c1.getSexe().compareTo(c2.getSexe());
                } });
        results.addColumn(sexeColumn, new Header<String>(new TextCell() {
                @Override public void render(Cell.Context ctx, SafeHtml value, SafeHtmlBuilder sb) {
                    if (value != null) {
                        if (currentSortColumn != sexeColumn)
                            sb.append(ARROWS);
                        sb.append(value);
                    }
                }
            }) {
                @Override public String getValue() {
                    return heads[Columns.SEXE];
                } });
        sexeColumn.setFieldUpdater(new FieldUpdater<ClientData, String>() {
                @Override public void update(int index, ClientData cd, String value) {
                    StringBuffer edits = new StringBuffer();
                    edits.append(cd.getID()+",Csexe," + value + ";");
                    pushEdit(edits.toString());
                }
            });

        gradeButtonCell = new ButtonGroupCell<Grade>(Arrays.asList(Constants.GRADES));
        gradeButtonCell.setShowButton(true);
        gradeColumn = new Column<ClientData, String>(gradeButtonCell)
            { @Override public String getValue(ClientData cd) { return cd.getMostRecentGrade().getGrade(); } };
        gradeColumn.setSortable(true);
        gradeColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        resultsListHandler.setComparator(gradeColumn, new Comparator<ClientData>() {
                @Override public int compare(ClientData c1, ClientData c2) {
                    return new GradeData.GradeComparator().compare
                        (c1.getMostRecentGrade(), c2.getMostRecentGrade());
                } });
        results.addColumn(gradeColumn, new Header<String>(new TextCell() {
                @Override public void render(Cell.Context ctx, SafeHtml value, SafeHtmlBuilder sb) {
                    if (value != null) {
                        sb.append(value);
                        if (currentSortColumn != gradeColumn)
                            sb.append(ARROWS);
                    }
                }
            }) {
                @Override public String getValue() {
                    return heads[Columns.GRADE];
                } });
        results.getHeader(results.getColumnCount()-1).setHeaderStyleNames("right-align");

        gradeColumn.setFieldUpdater(new FieldUpdater<ClientData, String>() {
                @Override public void update(int index, ClientData cd, String value) {
                    Grade g = null;
                    for (Grade gg : Constants.GRADES) {
                        if (gg.name.equals(value)) {
                            g = gg; break;
                        }
                    }
                    if (g == null || g.name.equals(cd.getMostRecentGrade().getGrade())) return;

                    StringBuffer edits = new StringBuffer();

                    // comments for historic reasons
                    // if the grade changed (but not the date):
                    //   delete old grade
                    // if the grade didn't change but the date changed:
                    //   delete old grade/date, but use old grade (untruncated) as new grade
                    // if the grade and date changed:
                    //   don't delete anything

                    GradeData gd = cd.getMostRecentGrade();
                    gd.setDateGrade(Constants.stdToDbDate(mostRecentGradeDate));
                    gd.setGrade(value);

                    edits.append(cd.getID()+",G," + gd.getGrade() + "|" + gd.getDateGrade() + ";");
                    pushEdit(edits.toString());
                }
            });

        dateGradeColumn = new Column<ClientData, String>(new EditTextCell())
            { @Override public String getValue(ClientData cd) {
                    String dg = cd.getMostRecentGrade().getDateGrade();
                    return dg == null ? Constants.STD_DUMMY_DATE : Constants.dbToStdDate(dg); } };
        results.addColumn(dateGradeColumn, heads[Columns.DATE_GRADE]);
        dateGradeColumn.setFieldUpdater(new FieldUpdater<ClientData, String>() {
                @Override public void update(int index, ClientData cd, String value) {
                    StringBuffer edits = new StringBuffer();
                    mostRecentGradeDate = value;

                    GradeData gd = cd.getMostRecentGrade();
                    gd.setDateGrade(Constants.stdToDbDate(value));
                    edits.append(cd.getID()+",G," + gd.getGrade() + "|" + gd.getDateGrade() + ";");
                    pushEdit(edits.toString());
                }
            });

        final Column<ClientData, String> telColumn = new Column<ClientData, String>(new EditTextCell())
            { @Override public String getValue(ClientData cd) { return cd.getTel(); } };
        telColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        results.addColumn(telColumn, heads[Columns.TEL]);
        results.getHeader(results.getColumnCount()-1).setHeaderStyleNames("right-align");
        telColumn.setFieldUpdater(new FieldUpdater<ClientData, String>() {
                @Override public void update(int index, ClientData cd, String value) {
                    StringBuffer edits = new StringBuffer();
                    edits.append(cd.getID()+",Ctel," + value + ";");
                    pushEdit(edits.toString());
                }
            });

        final Column<ClientData, String> judoQCColumn = new Column<ClientData, String>(new EditTextCell())
            { @Override public String getValue(ClientData cd) { return cd.getJudoQC(); } };
        judoQCColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_RIGHT);
        results.addColumn(judoQCColumn, heads[Columns.JUDOQC]);
        results.getHeader(results.getColumnCount()-1).setHeaderStyleNames("right-align");
        judoQCColumn.setFieldUpdater(new FieldUpdater<ClientData, String>() {
                @Override public void update(int index, ClientData cd, String value) {
                    StringBuffer edits = new StringBuffer();
                    edits.append(cd.getID()+",Caffiliation," + value + ";");
                    pushEdit(edits.toString());
                }
            });

        affEnvoyeColumn = new Column<ClientData, Boolean>(new CheckboxCell())
            { @Override public Boolean getValue(ClientData cd) {
                    if (cd.getServiceFor(currentSession) != null)
                        return cd.getServiceFor(currentSession).getVerification();
                    return false;
                } };
        affEnvoyeColumn.setSortable(true);
        affEnvoyeColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        results.addColumn(affEnvoyeColumn, new Header<String>(new TextCell() {
                @Override public void render(Cell.Context ctx, SafeHtml value, SafeHtmlBuilder sb) {
                    if (value != null) {
                        sb.append(value);
                        if (currentSortColumn != gradeColumn)
                            sb.append(ARROWS);
                    }
                }
            }) {
                @Override public String getValue() {
                    return heads[Columns.AFF_ENVOYE];
                } });
        affEnvoyeColumn.setFieldUpdater(new FieldUpdater<ClientData, Boolean>() {
                @Override public void update(int index, ClientData cd, Boolean value) {
                    StringBuffer edits = new StringBuffer();
                    edits.append(cd.getID()+",Sverification," + (value ? "1" : "0") + ";");
                    cd.getServiceFor(currentSession).setVerification(value.equals("1"));
                    pushEdit(edits.toString());
                }
            });
        resultsListHandler.setComparator(affEnvoyeColumn, new Comparator<ClientData>() {
                @Override public int compare(ClientData c1, ClientData c2) {
                    ServiceData xsd = c1.getServiceFor(currentSession);
                    int xverif = xsd != null ? (xsd.getVerification() ? 1 : 0) : 0;

                    ServiceData ysd = c2.getServiceFor(currentSession);
                    int yverif = ysd != null ? (ysd.getVerification() ? 1 : 0) : 0;
                    return yverif - xverif;
                } });

        payeColumn = new Column<ClientData, Boolean>(new CheckboxCell())
            { @Override public Boolean getValue(ClientData cd) {
                    if (cd.getServiceFor(currentSession) != null)
                        return cd.getServiceFor(currentSession).getSolde();
                    return false;
                } };
        payeColumn.setSortable(true);
        payeColumn.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
        results.addColumn(payeColumn, new Header<String>(new TextCell() {
                @Override public void render(Cell.Context ctx, SafeHtml value, SafeHtmlBuilder sb) {
                    if (value != null) {
                        sb.append(value);
                        if (currentSortColumn != gradeColumn)
                            sb.append(ARROWS);
                    }
                }
            }) {
                @Override public String getValue() {
                    return heads[Columns.PAYE];
                } });
        payeColumn.setFieldUpdater(new FieldUpdater<ClientData, Boolean>() {
                @Override public void update(int index, ClientData cd, Boolean value) {
                    StringBuffer edits = new StringBuffer();
                    edits.append(cd.getID()+",Ssolde," + (value ? "1" : "0") + ";");
                    cd.getServiceFor(currentSession).setSolde(value.equals("1"));
                    pushEdit(edits.toString());
                }
            });
        resultsListHandler.setComparator(payeColumn, new Comparator<ClientData>() {
                @Override public int compare(ClientData c1, ClientData c2) {
                    ServiceData xsd = c1.getServiceFor(currentSession);
                    int xpaye = xsd != null ? (xsd.getSolde() ? 1 : 0) : 0;

                    ServiceData ysd = c2.getServiceFor(currentSession);
                    int ypaye = ysd != null ? (ysd.getSolde() ? 1 : 0) : 0;
                    return ypaye - xpaye;
                } });

        ddnColumn = new Column<ClientData, String>(new EditTextCell())
            { @Override public String getValue(ClientData cd) {
                    Date ddn = cd.getDDN();
                    return ddn == null ? Constants.STD_DUMMY_DATE : Constants.STD_DATE_FORMAT.format(ddn); } };
        ddnColumn.setSortable(true);
        resultsListHandler.setComparator(ddnColumn, new Comparator<ClientData>() {
                @Override public int compare(ClientData c1, ClientData c2) {
                    if (c1.getDDN() == null) return -1;
                    if (c2.getDDN() == null) return 1;
                    return c1.getDDN().compareTo(c2.getDDN());
                } });
        results.addColumn(ddnColumn, new Header<String>(new TextCell() {
                @Override public void render(Cell.Context ctx, SafeHtml value, SafeHtmlBuilder sb) {
                    if (value != null) {
                        if (currentSortColumn != ddnColumn)
                            sb.append(ARROWS);
                        sb.append(value);
                    }
                }
            }) {
                @Override public String getValue() {
                    return heads[Columns.DDN];
                } });
        ddnColumn.setFieldUpdater(new FieldUpdater<ClientData, String>() {
                @Override public void update(int index, ClientData cd, String value) {
                    StringBuffer edits = new StringBuffer();
                    String converted_ddn = Constants.stdToDbDate(value);
                    cd.setDDNString(Constants.stdToDbDate(value));
                    edits.append(cd.getID()+",Cddn," + converted_ddn + ";");
                    pushEdit(edits.toString());
                }
            });

        final List<String> divSMNames = new ArrayList<>();
        divSMNames.add("Senior"); divSMNames.add("Masters");
        divisionColumn = new Column<ClientData, String>(new SelectionCell(divSMNames))
            { @Override public String getValue(ClientData cd) { return ""; }

              @Override
              public void render(Cell.Context ctx, ClientData cd, SafeHtmlBuilder s) {
                  if (divisionSMColumnVisible && (cd != null && currentSession != null &&
                                                  cd.getDDN() != null &&
                                                  (Integer.parseInt(currentSession.getYear()) -
                                                   (cd.getDDN().getYear() + 1900)) > Constants.VETERAN))
                      super.render(ctx, cd, s);
                  else {
                      if (currentSession == null)
                          return;
                      else
                          s.appendHtmlConstant(cd.getDivision(currentSession.getYear()).abbrev);
                  }
              } };
        divisionColumn.setFieldUpdater(new FieldUpdater<ClientData, String>() {
                @Override public void update(int index, ClientData cd, String value) {
                    cdToSM.put(cd, value);
                }
            });

        resultsListHandler.setComparator(divisionColumn, new Comparator<ClientData>() {
                @Override public int compare(ClientData c1, ClientData c2) {
                    int xc = c1.getDivision(currentSession.getYear()).years_ago;
                    int yc = c2.getDivision(currentSession.getYear()).years_ago;
                    if (xc == 0) xc = Integer.MAX_VALUE;
                    if (yc == 0) yc = Integer.MAX_VALUE;
                    return xc - yc;
                } });

        coursButtonCell = new ButtonGroupCell<CoursSummary>(uniqueCoursSummariesForSession);
        coursColumn = new Column<ClientData, String>(coursButtonCell)
            { @Override public String getValue(ClientData cd) {
                    int cours = -1;
                    if (cd.getServiceFor(currentSession) != null &&
                        !cd.getServiceFor(currentSession).getCours().equals(""))
                        cours = Integer.parseInt(cd.getServiceFor(currentSession).getCours());
                    return getShortDescForCoursId(cours); } };
        coursColumn.setSortable(true);
        resultsListHandler.setComparator(coursColumn, new Comparator<ClientData>() {
                @Override public int compare(ClientData c1, ClientData c2) {
                    ServiceData xsd = c1.getServiceFor(currentSession);
                    int xcours = xsd != null ? Integer.parseInt(xsd.getCours()) : -1;

                    ServiceData ysd = c2.getServiceFor(currentSession);
                    int ycours = ysd != null ? Integer.parseInt(ysd.getCours()) : -1;
                    return xcours - ycours;
                } });
        results.addColumn(coursColumn, new Header<String>(new TextCell() {
                @Override public void render(Cell.Context ctx, SafeHtml value, SafeHtmlBuilder sb) {
                    if (value != null) {
                        if (currentSortColumn != coursColumn)
                            sb.append(ARROWS);
                        sb.append(value);
                    }
                }
            }) {
                @Override public String getValue() {
                    return heads[Columns.COURS_DESC];
                } });
        coursColumn.setFieldUpdater(new FieldUpdater<ClientData, String>() {
                @Override public void update(int index, ClientData cd, String value) {
                    CoursSummary c = null;
                    for (CoursSummary cs : coursSummaries) {
                        if (cs.getValue().equals(value)) {
                            c = cs; break;
                        }
                    }
                    if (c == null) return;
                    if (cd.getServiceFor(currentSession) != null) {
                        if (c.getId().equals(cd.getServiceFor(currentSession).getCours()))
                            return;
                        cd.getServiceFor(currentSession).setCours(c.getId());
                    }

                    StringBuffer edits = new StringBuffer();
                    edits.append(cd.getID() + ",Scours,"+c.getId()+";");
                    pushEdit(edits.toString());
                }
            });

        sessionsColumn = new Column<ClientData, String>(new TextCell())
            { @Override public String getValue(ClientData cd) { return cd.getAllActiveSaisons(); } };
        results.addColumn(sessionsColumn, heads[Columns.SESSION]);
        results.redrawHeaders();

        results.addColumnSortHandler(new ColumnSortEvent.Handler() {
                @Override
                @SuppressWarnings("unchecked")
                public void onColumnSort(ColumnSortEvent event) {
                    ColumnSortList sortList = results.getColumnSortList();
                    if (sortList != null && sortList.size() > 0) {
                        currentSortColumn = (Column<ClientData, SafeHtml>)sortList.get(0).getColumn();
                    }
                }
            });
        results.getColumnSortList().push(nomColumn);
    }

    private void pushEdit(String edits) {
        guid = UUID.uuid();
        guid_on_form.setValue(guid);
        if (currentSession != null)
            currentSessionField.setValue(currentSession.getAbbrev());
        else
            currentSessionField.setValue("");
        dataToSave.setValue(edits);

        listEditForm.submit();

        pushTries = 0;
        new Timer() { public void run() {
            pushChanges(guid);
        } }.schedule(500);
    }

    // currently unused, as well as doing nothing
    private void recalc() {
        for (int i = 0; i < results.getRowCount(); i++) {
            // ClientData cd = cidToCD.get(results.getText(i, Columns.CID));
            // ServiceData sd = cd.getServiceFor(currentSession);
            // if (sd == null) continue;
            // ClubSummary cs = jdb.getClubSummaryByID(sd.getClubID());
            // ProduitSummary ps = CostCalculator.getApplicableProduit(sd, produitSummaries);;

            // // XXX getPrix on ListWidget as well
            // CostCalculator.recompute(currentSession, cd, sd, cs, sessionSummaries, coursSummaries, ps, true, null, escompteSummaries);
        }
    }

    private void addMetaData() {
        title.setValue("");
        ft_evt.setValue(evt.getValue());
        ft_date.setValue(date.getValue());
        if (currentCours != null || isFiltering) {
            multi.setValue("0");
            if (isFiltering) {
                title.setValue("");
                if (currentCours != null) {
                    short_title.setValue(currentCours.getShortDesc());
                }
                else {
                    short_title.setValue("");
                }

                String st = "";
                // TODO: add filters to title
                title.setValue(st);
            } else {
                short_title.setValue(currentCours.getShortDesc());
            }
        } else {
            // all classes
            String tt = "", shortt = "";
            multi.setValue("1");

            // TODO renumber the cours so as to eliminate blank spots
            int maxId = -1;
            for (CoursSummary cc : coursSummaries) {
                if (Integer.parseInt(cc.getId()) > maxId)
                    maxId = Integer.parseInt(cc.getId());
            }

            if (maxId > 0) {
                String[] shortDescs = new String[maxId];
                for (CoursSummary cc : coursSummaries) {
                    shortDescs[Integer.parseInt(cc.getId())] = cc.getShortDesc();
                }

                for (int i = 0; i < maxId; i++)
                    shortt += shortDescs[i] + "|";
            }
            title.setValue(tt);
            short_title.setValue(shortt);
        }
    }

    private void collectDV() {
        StringBuilder dv = new StringBuilder("|");
        for (ClientData cd : filteredClients) {
            dv.append(cd.getID()); dv.append("|");
            dv.append(cd.getNom()); dv.append("|");
            dv.append(cd.getPrenom()); dv.append("|");
            dv.append(cd.getSexe()); dv.append("|");
            dv.append(cd.getMostRecentGrade().getGrade()); dv.append("|");
            dv.append(cd.getMostRecentGrade().getDateGrade()); dv.append("|");
            dv.append(cd.getTel()); dv.append("|");
            dv.append(cd.getJudoQC()); dv.append("|");
            dv.append(cd.getDDNString()); dv.append("|");
            if (currentSession != null)
                dv.append(cd.getDivision(currentSession.getYear()).abbrev);
            dv.append("|");
            ServiceData sd = cd.getServiceFor(currentSession);
            if (sd != null && !sd.getCours().equals("")) {
                CoursSummary matchedCours = null;
                for (CoursSummary cc : coursSummaries) {
                    if (cc.getId().equals(sd.getCours())) {
                        dv.append(cc.getShortDesc());
                        matchedCours = cc;
                    }
                }
                dv.append("|");
                if (matchedCours != null) {
                    dv.append(matchedCours.getId()); dv.append("|");
                }
            }
            dv.append("*|");
        }
        data.setValue(dv.toString());
    }

    private void clearFull() {
        data_full.setValue("");
    }

    private void computeFull() {
        String dv = "";
        for (int i = 0; i < allClients.length(); i++) {
            ClientData cd = allClients.get(i);
            if (!clubFilter(cd) || !sessionFilter(cd)) continue;

            dv += toDVFullString(cd) + "*";
        }
        data_full.setValue(dv);
    }

    private String toDVFullString(ClientData cd) {
        String dv = "";

        dv += cd.getID() + "|";
        dv += cd.getNom() + "|";
        dv += cd.getPrenom() + "|";
        dv += cd.getSexe() + "|";
        dv += cd.getJudoQC() + "|";
        dv += cd.getDDNString() + "|";
        if (currentSession != null)
            dv += cd.getDivision(currentSession.getYear()).abbrev + "|";
        else
            dv += "|";
        dv += cd.getCourriel() + "|";
        dv += cd.getAdresse() + "|";
        dv += cd.getVille() + "|";
        dv += cd.getCodePostal() + "|";
        dv += cd.getTel() + "|";
        dv += cd.getCarteResident() + "|";
        dv += cd.getTelContactUrgence() + "|";
        dv += cd.getMostRecentGrade().getGrade() + "|";
        dv += cd.getMostRecentGrade().getDateGrade() + "|";
        if (currentSession != null) {
            ServiceData sd = cd.getServiceFor(currentSession);
            if (sd != null && !sd.getCours().equals("")) {
                // this is potentially slow; use a hash map instead.
                for (CoursSummary cc : coursSummaries) {
                    if (cc.getId().equals(sd.getCours()))
                        dv += cc.getShortDesc();
                }
            }
            dv += "|";
            if (sd != null) {
                ClubSummary cs = jdb.getClubSummaryByID(sd.getClubID());
                ProduitSummary ps = CostCalculator.getApplicableProduit(sd, produitSummaries);;
                CostCalculator.recompute(currentSession, cd, sd, cs, sessionSummaries, coursSummaries, ps, prorata.getValue(), prix, escompteSummaries);
                dv += sd.getFrais();
            }
        }
        else {
            dv += "|";
        }
        dv += "|";
        return dv;
    }

   private void computeImpotMailMerge() {
        String dv = "";
        ArrayList<ClientData> mmFilteredClients = new ArrayList<ClientData>();

        for (int i = 0; i < allClients.length(); i++) {
            ClientData cd = allClients.get(i);
            if (!sessionFilter(cd)) continue;

            Division d = cd.getDivision(currentSession.getYear());
            if (d.abbrev.equals("S") || d.aka.equals("S")) continue;
            mmFilteredClients.add(cd);
        }

        Collections.sort(mmFilteredClients, new Comparator<ClientData>() {
            public int compare(ClientData x, ClientData y) {
                if (!x.getNom().equals(y.getNom()))
                    return x.getNom().compareTo(y.getNom());
                return x.getPrenom().compareTo(y.getPrenom());
            }
        });

        for (ClientData cd : mmFilteredClients) {
            dv += toDVImpot(cd) + "*";
        }

        data_full.setValue(dv);
    }

   private String toDVImpot(ClientData cd) {
       String dv = "";

       dv += cd.getID() + "|";
       dv += cd.getPrenom() + " " + cd.getNom() + "|";
       dv += cd.getDDNString() + "|";
       ServiceData sd = cd.getServiceFor(currentSession);
       ClubSummary cs = jdb.getClubSummaryByID(sd.getClubID());
       ProduitSummary ps = CostCalculator.getApplicableProduit(sd, produitSummaries);
       CostCalculator.recompute(currentSession, cd, sd, cs, sessionSummaries, coursSummaries, ps, prorata.getValue(), prix, escompteSummaries);
       if (sd != null) {
           dv += Constants.currencyFormat.format(Double.parseDouble(sd.getFrais()));
       }
       dv += "|";
       return dv;
   }


    private void submit(String act) {
        addMetaData();
        listForm.setAction(JudoDB.BASE_URL+"listes"+act+".php");
        listForm.submit();
    }

    private boolean makeFT() {
        StringBuilder dv = new StringBuilder("");
        for (ClientData cd : filteredClients) {
            if (resultsSelectionModel.isSelected(cd)) {
                StringBuilder post = new StringBuilder();

                if (cdToSM.containsKey(cd)) {
                    post.append(cdToSM.get(cd));
                }
                post.append("|");
                dv.append(toDVFullString(cd) + post + "*");
            }
        }
        data.setValue("");
        data_full.setValue(dv.toString());
        if (jdb.getSelectedClubID() == null) {
            jdb.displayError("Veuillez selectionner un club.");
            new Timer() { public void run() { jdb.clearStatus(); } }.schedule(2000);
            return false;
        }
        if (currentSession == null) {
            jdb.displayError("Veuillez selectionner une session.");
            new Timer() { public void run() { jdb.clearStatus(); } }.schedule(2000);
            return false;
        }
        ClubSummary cs = jdb.getClubSummaryByID(jdb.getSelectedClubID());
        auxdata.setValue(cs.getNomShort() + "|" + cs.getNumeroClub());
        return !dv.equals("");
    }

    // --- filters ---
    public boolean filter(ClientData cd) {
        if (!sessionFilter(cd))
            return false;

        if (!coursFilter(cd))
            return false;

        if (isFiltering) {
            if (!divisionFilter(cd))
                return false;
            if (!gradeFilter(cd))
                return false;
            if (!aeFilter(cd))
                return false;
            if (!pcFilter(cd))
                return false;
        }

        return true;
    }

    /* unlike the other filters, this one can't be disabled */
    private boolean clubFilter(ClientData cd) {
        SessionSummary rs = currentSession;
        if (rs == null) {
            for (int i = 0; i < cd.getServices().length(); i++) {
                if (_clubFilter(cd.getServices().get(i)))
                    return true;
            }
            return false;
        }
        ServiceData sd = cd.getServiceFor(rs);
        return _clubFilter(sd);
    }

    private boolean _clubFilter(ServiceData sd) {
        if (jdb.getSelectedClubID() == null) return true;
        if (sd == null) return false;
        return jdb.getSelectedClubID().equals(sd.getClubID());
    }

    private boolean sessionFilter(ClientData cd) {
        if (currentSession == null) return true;

        return cd.getServiceFor(currentSession) != null;
    }

    private boolean coursFilter(ClientData cd) {
        if (currentCours == null)
            return true;

        List<CoursSummary> likeCurrentCours =
            coursSummariesByShortDesc.get(currentCours.getShortDesc());
        for (CoursSummary cc : likeCurrentCours)
            if (cc.getId().equals(cd.getServiceFor(currentSession).getCours()))
                return true;

        return false;
    }

    private boolean divisionFilter(ClientData cd) {
        String selectedDivision = division.getValue(division.getSelectedIndex());
        if (selectedDivision.equals("-1"))
            return true;

        Division c = cd.getDivision(currentSession.getYear());
        if (selectedDivision.equals(c.abbrev) || selectedDivision.equals(c.aka))
            return true;
        return false;
    }

    private boolean gradeFilter(ClientData cd) {
        String lc = grade_lower.getValue(grade_lower.getSelectedIndex());
        String uc = grade_upper.getValue(grade_upper.getSelectedIndex());
        boolean emptyLC = lc.equals(""), emptyUC = uc.equals("");

        Grade g = Constants.stringToGrade(cd.getGrade());
        if (g != null) {
            int lower_constraint = emptyLC ? 0 : Integer.parseInt(lc);
            int upper_constraint = emptyUC ? 0 : Integer.parseInt(uc);
            return (emptyLC || lower_constraint <= g.order) &&
                     (emptyUC || g.order <= upper_constraint);
        } else {
            return emptyLC && emptyUC;
        }
    }

    private boolean aeFilter(ClientData cd) {
        if (aeFilter_indifferent) return true;
        ServiceData sd = cd.getServiceFor(currentSession);
        return sd.getVerification() == aeFilter_value;
    }

    private boolean pcFilter(ClientData cd) {
        if (pcFilter_indifferent) return true;
        ServiceData sd = cd.getServiceFor(currentSession);
        return sd.getSolde() == aeFilter_value;
    }

    @SuppressWarnings("deprecation")
    public void showList() {
        boolean all = currentCours == null;
        final SessionSummary rs = currentSession;
        int count = 0, curRow;
        filteredClients.clear();

        // two passes: 1) count; 2) populate the grid
        if (allClients == null) return;

        for (int i = 0; i < allClients.length(); i++) {
            ClientData cd = allClients.get(i);
            cidToCD.put(cd.getID(), cd);
            if (!clubFilter(cd)) continue;
            if (!filter(cd)) continue;

            filteredClients.add(cd);
            count++;
        }
        nb.setText("Nombre inscrit: "+count);

        List<ClientData> list = resultsDataProvider.getList();
        list.clear();
        list.addAll(filteredClients);
        results.setVisibleRange(0, count);
    }

    /* --- process data from network --- */
    private void loadCours(JsArray<CoursSummary> coursArray) {
        coursSummaries.clear();
        dropDownCours.clear();
        currentCours = null;

        dropDownCoursButton.setText(jdb.TOUS);
        AnchorListItem tous = new AnchorListItem(jdb.TOUS);
        tous.addClickHandler(new CoursItemHandler(null));
        dropDownCours.add(tous);

        // only include non-superceded cours
        coursSummariesByShortDesc.clear();
        for (int i = 0; i < coursArray.length(); i++) {
            CoursSummary c = coursArray.get(i);
            coursSummaries.add(c);

            if (coursSummariesByShortDesc.containsKey(c.getShortDesc())) {
                List<CoursSummary> cc = coursSummariesByShortDesc.get(c.getShortDesc());
                cc.add(c);
            } else {
                List<CoursSummary> cc = new ArrayList<>();
                cc.add(c);

                coursSummariesByShortDesc.put(c.getShortDesc(), cc);
                AnchorListItem it = new AnchorListItem(c.getShortDesc());
                it.addClickHandler(new CoursItemHandler(c));
                dropDownCours.add(it);
            }
        }

        updateUniqueCoursSummariesForSession();
    }

    private void updateUniqueCoursSummariesForSession() {
        uniqueCoursSummariesForSession.clear();
        if (currentSession == null)
            uniqueCoursSummariesForSession.addAll(coursSummaries); // doesn't actually need uniqueness
        else {
            for (CoursSummary cs : coursSummaries) {
                if (cs.getSession().equals(currentSession.getSeqno()) ||
                    cs.getSession().equals(currentSession.getLinkedSeqno()))
                    uniqueCoursSummariesForSession.add(cs);
            }
        }
    }

    private void loadEscomptes(JsArray<EscompteSummary> escompteArray) {
        escompteSummaries.clear();
        escompteSummaries.add(Constants.EMPTY_ESCOMPTE);
        for (int i = 0; i < escompteArray.length(); i++) {
            EscompteSummary e = escompteArray.get(i);
            if (e.getClubId().equals("0") || e.getClubId().equals(jdb.getSelectedClubID())) {
                escompteSummaries.add(e);
            }
        }
    }

    private void loadProduits(JsArray<ProduitSummary> produitArray) {
        produitSummaries.clear();
        produitSummaries.add(Constants.EMPTY_PRODUIT);
        for (int i = 0; i < produitArray.length(); i++) {
            ProduitSummary e = produitArray.get(i);
            if (e.getClubId().equals(jdb.getSelectedClubID())) {
                produitSummaries.add(e);
            }
        }
    }

    void loadSessions(JsArray<SessionSummary> ss) {
        sessionSummaries.clear();
        dropDownSessions.clear();
        currentSession = null;

        Date today = new Date();
        TreeSet<SessionAnchorListItem> sss = new TreeSet<>();

        for (int i = 0; i < ss.length(); i++) {
            SessionSummary s = ss.get(i);
            sessionSummaries.add(s);

            try {
                Date inscrBegin = Constants.DB_DATE_FORMAT.parse(s.getFirstSignupDate());
                Date inscrEnd = Constants.DB_DATE_FORMAT.parse(s.getLastSignupDate());
                if (today.after(inscrBegin) && today.before(inscrEnd)) {
                    currentSession = s; continue;
                }
            } catch (IllegalArgumentException e) {}

            SessionAnchorListItem sali =
                new SessionAnchorListItem(s.getAbbrev(),
                                          Integer.parseInt(s.getSeqno()));
            sali.addClickHandler(new SessionItemHandler(s));
            sss.add(sali);
        }

        if (currentSession != null) {
            AnchorListItem cs = new SessionAnchorListItem(currentSession.getAbbrev(), -2);
            cs.addClickHandler(new SessionItemHandler(currentSession));
            dropDownSessions.add(cs);
        }
        selectSession(currentSession);

        AnchorListItem ts = new SessionAnchorListItem("Tous", -1);
        ts.addClickHandler(new SessionItemHandler(null));
        dropDownSessions.add(ts);

        for (AnchorListItem s : sss)
            dropDownSessions.add(s);
    }

    /* --- network functions --- */
    private boolean gotSessions = false;
    public void retrieveSessions(ClubSummary cs) {
        gotSessions = false;
        String url = JudoDB.PULL_SESSIONS_URL;
        url += "?club_id=" + ((cs == null) ? "0" : cs.getId());
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

    /* depends on jdb.retrieveClubList() and retrieveSessions() having succeeded */
    private boolean gotPrix = false;
    public void retrievePrix(final String club_id) {
        if (!gotSessions) {
            new Timer() {
                public void run() { retrievePrix(club_id); }
            }.schedule(100);
            return;
        }

        String url = JudoDB.PULL_CLUB_PRIX_URL +
            "?club_id=" + club_id;

        RequestCallback rc =
            jdb.createRequestCallback(new JudoDB.Function() {
                    public void eval(String s) {
                        gotPrix = true;
                        JsArray<Prix> cp = JsonUtils.<JsArray<Prix>>safeEval(s);
                        prix.clear();
                        for (int i = 0; i < cp.length(); i++)
                            prix.add(cp.get(i));
                    }
                });
        jdb.retrieve(url, rc);
    }

    private boolean gotCours = false;
    public void retrieveCours() {
        if (!gotSessions) {
            new Timer() {
                public void run() { retrieveCours(); }
            }.schedule(100);
            return;
        }

        coursSummaries.clear();
        String url = JudoDB.PULL_CLUB_COURS_URL;
        if (jdb.getSelectedClub() != null) {
            url += "?club_id="+jdb.getSelectedClub().getId();
        }
        RequestCallback rc =
            jdb.createRequestCallback(new JudoDB.Function() {
                    public void eval(String s) {
                        gotCours = true;
                        loadCours(JsonUtils.<JsArray<CoursSummary>>safeEval(s));
                        ListWidget.this.showList();
                    }
                });
        jdb.retrieve(url, rc);
    }

    private boolean gotEscomptes = false;
    public void retrieveEscomptes() {
        if (jdb.getSelectedClubID() == null || !gotSessions) {
            new Timer() {
                public void run() { retrieveEscomptes(); }
            }.schedule(100);
            return;
        }

        escompteSummaries.clear();
        String url = JudoDB.PULL_ESCOMPTE_URL +
            "?club_id=" + jdb.getSelectedClubID();
        RequestCallback rc =
            jdb.createRequestCallback(new JudoDB.Function() {
                    public void eval(String s) {
                        loadEscomptes
                            (JsonUtils.<JsArray<EscompteSummary>>safeEval(s));
                    }
                });
        jdb.retrieve(url, rc);
    }

    private boolean gotProduits = false;
    public void retrieveProduits() {
        if (jdb.getSelectedClubID() == null || !gotSessions) {
            new Timer() {
                public void run() { retrieveProduits(); }
            }.schedule(100);
            return;
        }

        produitSummaries.clear();
        String url = JudoDB.PULL_PRODUIT_URL +
            "?club_id=" + jdb.getSelectedClubID();
        RequestCallback rc =
            jdb.createRequestCallback(new JudoDB.Function() {
                    public void eval(String s) {
                        loadProduits
                            (JsonUtils.<JsArray<ProduitSummary>>safeEval(s));
                    }
                });
        jdb.retrieve(url, rc);
    }

    public void retrieveAllClients() {
        String url = PULL_ALL_CLIENTS_URL;
        RequestCallback rc =
            jdb.createRequestCallback(new JudoDB.Function() {
                    public void eval(String s) {
                        ListWidget.this.allClients =
                            (JsonUtils.<JsArray<ClientData>>safeEval(s));
                        new Timer() {
                            public void run() {
                                if (gotCours) {
                                    ListWidget.this.showList();
                                    cancel();
                                } else {
                                    scheduleRepeating(100);
                                }
                            } } .scheduleRepeating(10);
                    }
                });
        jdb.retrieve(url, rc);
    }

    public void pushChanges(final String guid) {
        String url = JudoDB.CONFIRM_PUSH_URL + "?guid=" + guid;
        RequestCallback rc =
            jdb.createRequestCallback(new JudoDB.Function() {
                    public void eval(String s) {
                        ConfirmResponseObject cro =
                            JsonUtils.<ConfirmResponseObject>safeEval(s);
                        String rs = cro.getResult();
                        if (rs.equals("NOT_YET")) {
                            if (pushTries >= 3) {
                                jdb.displayError("le serveur n'a pas accepté les données");
                                return;
                            }

                            new Timer() { public void run() {
                                pushChanges(guid);
                            } }.schedule(2000);
                            pushTries++;
                        } else {
                            jdb.setStatus("Sauvegardé.");
                            results.redraw();
                        }
                        new Timer() { public void run() { jdb.clearStatus(); } }.schedule(2000);
                    }
                });
        jdb.retrieve(url, rc);
    }
}
