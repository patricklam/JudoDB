package ca.patricklam.judodb.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class ConfigWidget extends Composite {
	interface MyUiBinder extends UiBinder<Widget, ConfigWidget> {}
	public static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
	
	private final JudoDB jdb;

	public ConfigWidget(JudoDB jdb) {
		this.jdb = jdb;
		initWidget(uiBinder.createAndBindUi(this));
	}
}
