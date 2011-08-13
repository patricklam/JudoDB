package ca.patricklam.judodb.client;

import com.google.gwt.core.client.JavaScriptObject;

public class ServiceData extends JavaScriptObject {
	protected ServiceData() {}

	public final native String getID() /*-{ return this.id; }-*/;
	public final native String getDateInscription() /*-{ return this.date_inscriptions; }-*/;
	public final native String getSaisons() /*-{ return this.saisons; }-*/;
	public final native boolean getSansAffiliation() /*-{ return this.sans_affiliation; }-*/;
	public final native int getCours() /*-{ return this.cours; }-*/;
	public final native int getSessions() /*-{ return this.sessions; }-*/;
	public final native boolean getPasseport() /*-{ return this.passeport; }-*/;
	public final native boolean getNonAnjou() /*-{ return this.non_anjou; }-*/;

}
