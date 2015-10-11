// -*-  indent-tabs-mode:nil; c-basic-offset:4; -*-
package ca.patricklam.judodb.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.TextBox;
import org.gwtbootstrap3.client.ui.DropDownMenu;

public class MainLayoutPanel extends Composite {
    interface MyUiBinder extends UiBinder<Widget, MainLayoutPanel> {}
    public static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiField DockLayoutPanel dock;

    // main column
    @UiField TextBox searchTextBox;
    @UiField Button nouveauButton;
    @UiField Button searchButton;
    @UiField Button listeButton;
    @UiField Button configButton;
    @UiField Button logoutButton; // logout.php

    // results column
    @UiField VerticalPanel searchResultsPanel = new VerticalPanel();
    @UiField FlexTable searchResults = new FlexTable();

    @UiField HorizontalPanel searchNavPanel;
    @UiField Button nextResultsButton;
    @UiField Button prevResultsButton;

    // misc
    @UiField Label statusLabel;
    @UiField Label versionLabel;
    @UiField ButtonGroup dropDownUserClubsButtonGroup;
    @UiField Button dropDownUserClubsButton;
    @UiField DropDownMenu dropDownUserClubs;

    @UiField FlowPanel mainPanel;
    @UiField ScrollPanel editClient;
    @UiField ScrollPanel lists;
    @UiField LayoutPanel config;
    @UiField FlowPanel rightbar;
    @UiField FlowPanel listActions;
    @UiField FlowPanel configActions;

    public MainLayoutPanel() {
        initWidget(uiBinder.createAndBindUi(this));
    }
}
