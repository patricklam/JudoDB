package ca.patricklam.judodb.client;

import com.google.gwt.core.client.JavaScriptObject;

public class ClubSummary extends JavaScriptObject {
	protected ClubSummary() { }
	
	public final native String getId() /*-{ return this.id; }-*/;
	public final native String getNom() /*-{ return this.nom; }-*/;
	public final native String getNumeroClub() /*-{ return this.numero_club; }-*/;
}
