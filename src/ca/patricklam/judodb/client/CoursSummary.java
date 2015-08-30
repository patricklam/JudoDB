package ca.patricklam.judodb.client;

import com.google.gwt.core.client.JavaScriptObject;

public class CoursSummary extends JavaScriptObject {
    protected CoursSummary() { }

    public final native String getId() /*-{ return this.id; }-*/;
    public final native void setId(String id) /*-{ this.id = id; }-*/;
    public final native String getClubId() /*-{ return this.club_id; }-*/;
    public final native void setClubId(String club_id) /*-{ this.club_id = club_id; }-*/;
    public final native String getSession() /*-{ return this.session_seqno; }-*/;
    public final native void setSession(String session_seqno) /*-{ this.session_seqno = session_seqno; }-*/;
    public final native String getShortDesc() /*-{ return this.short_desc; }-*/;
    public final native void setShortDesc(String short_desc) /*-{ this.short_desc = short_desc; }-*/;

    public final String get(String key) {
	if (key.equals("id")) return getId();
	if (key.equals("club_id")) return getClubId();
	if (key.equals("session")) return getSession();
	if (key.equals("short_desc")) return getShortDesc();
	throw new RuntimeException("get: bad key " + key);
    }

    public final void set(String key, String value) {
	// seqno, id read-only
	// if (key.equals("seqno")) setSeqno(value);
	// if (key.equals("id")) setId(value);

	if (key.equals("club_id")) setClubId(value);
	else if (key.equals("short_desc")) setShortDesc(value);
	else if (key.equals("session")) setSession(value);
	else throw new RuntimeException("set: bad key " + key);
    }
}
