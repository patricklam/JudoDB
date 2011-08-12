package ca.patricklam.judodb.client;

import com.google.gwt.core.client.JavaScriptObject;

public class ClientSummary extends JavaScriptObject {
	protected ClientSummary() { }
	
	public final native String getId() /*-{ return this.id; }-*/;
	public final native String getNom() /*-{ return this.nom; }-*/;
	public final native String getPrenom() /*-{ return this.prenom; }-*/;
	public final native String getSaisons() /*-{ return this.saisons; }-*/;
}
