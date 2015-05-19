package ca.patricklam.judodb.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;

public class MainLayoutPanel extends Composite {
    interface MyUiBinder extends UiBinder<Widget, MainLayoutPanel> {}
    public static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiField DockLayoutPanel dock;

    @UiField Label statusLabel;
    @UiField Label versionLabel;

    @UiField FlowPanel search;
    @UiField ScrollPanel editClient;
    @UiField ScrollPanel lists;
    @UiField FlowPanel config;
    @UiField FlowPanel rightbar;
    @UiField FlowPanel listActions;
    @UiField FlowPanel mainActions;

    public MainLayoutPanel() {
        initWidget(uiBinder.createAndBindUi(this));
    }
}