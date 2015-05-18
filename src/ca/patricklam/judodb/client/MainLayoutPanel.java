package ca.patricklam.judodb.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class MainLayoutPanel extends Composite {
    interface MyUiBinder extends UiBinder<Widget, MainLayoutPanel> {}
    public static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

    @UiField Label statusLabel;
    @UiField Label versionLabel;

    public MainLayoutPanel() {
        initWidget(uiBinder.createAndBindUi(this));
    }
}