package ca.patricklam.judodb.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

public class ConfigWidget extends Composite {
	interface MyUiBinder extends UiBinder<Widget, ConfigWidget> {}
	public static MyUiBinder uiBinder = GWT.create(MyUiBinder.class);
	
	private final JudoDB jdb;
	private static final String PULL_CONFIG_URL = JudoDB.BASE_URL + "pull_config.php";
	//private static final String PUSH_CONFIG_URL = JudoDB.BASE_URL + "push_config.php";
	private static final String CALLBACK_URL_SUFFIX_Q = "?callback=";

	public ConfigWidget(JudoDB jdb) {
		this.jdb = jdb;
		initWidget(uiBinder.createAndBindUi(this));
		jdb.pleaseWait();
		getJson(jdb.jsonRequestId++, PULL_CONFIG_URL + CALLBACK_URL_SUFFIX_Q, this);
	}
	
	/**
	 * Make call to remote server.
	 */
	public native static void getJson(int requestId, String url,
	      ConfigWidget handler) /*-{
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
			jdb.displayError("Couldn't retrieve JSON (config)");
			return;
		}

	    //this.config = jso.cast();
	    jdb.clearStatus();
	}
}
