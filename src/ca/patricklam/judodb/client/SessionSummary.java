package ca.patricklam.judodb.client;

import com.google.gwt.core.client.JavaScriptObject;

public class SessionSummary extends JavaScriptObject {
    protected SessionSummary() { }

    public final native String getSeqno() /*-{ return this.seqno; }-*/;
    public final native String getLinkedSeqno() /*-{ return this.linked_seqno; }-*/;
    public final native void setLinkedSeqno(String linked_seqno) /*-{ this.linked_seqno = linked_seqno; }-*/;
    public final native String getName() /*-{ return this.name; }-*/;
    public final native void setName(String name) /*-{ this.name = name; }-*/;
    public final native String getYear() /*-{ return this.year; }-*/;
    public final native void setYear(String year) /*-{ this.year = year; }-*/;
    public final native String getAbbrev() /*-{ return this.abbrev; }-*/;
    public final native void setAbbrev(String abbrev) /*-{ this.abbrev = abbrev; }-*/;
    public final native String getId() /*-{ return this.id; }-*/;
    public final native String getClub() /*-{ return this.club; }-*/;
    public final native void setClub(String club) /*-{ this.club = club; }-*/;
    public final native String getFirstClassDate() /*-{ return this.first_class_date; }-*/;
    public final native void setFirstClassDate(String firstClassDate) /*-{ this.firstClassDate = firstClassDate; }-*/;
    public final native String getFirstSignupDate() /*-{ return this.first_signup_date; }-*/;
    public final native void setFirstSignupDate(String firstSignupDate) /*-{ this.firstSignupDate = firstSignupDate; }-*/;
    public final native String getLastClassDate() /*-{ return this.last_class_date; }-*/;
    public final native void setLastClassDate(String lastClassDate) /*-{ this.lastClassDate = lastClassDate; }-*/;
    public final native String getLastSignupDate() /*-{ return this.last_signup_date; }-*/;
    public final native void setLastSignupDate(String lastSignupDate) /*-{ this.lastSignupDate = lastSignupDate; }-*/;

    public final String get(String key) {
	if (key.equals("seqno")) return getSeqno();
	if (key.equals("linkedSeqno")) return getLinkedSeqno();
	if (key.equals("name")) return getName();
	if (key.equals("year")) return getYear();
	if (key.equals("abbrev")) return getAbbrev();
	if (key.equals("id")) return getId();
	if (key.equals("club")) return getClub();
	if (key.equals("firstClassDate")) return getFirstClassDate();
	if (key.equals("firstSignupDate")) return getFirstSignupDate();
	if (key.equals("lastClassDate")) return getLastClassDate();
	if (key.equals("lastSignupDate")) return getLastSignupDate();
	throw new RuntimeException("bad key");
    }

    public final String set(String key, String value) {
	// seqno, id read-only
	// if (key.equals("seqno")) setSeqno(value);
	// if (key.equals("id")) setId(value);
	if (key.equals("linkedSeqno")) setLinkedSeqno(value);
	if (key.equals("name")) setName(value);
	if (key.equals("year")) setYear(value);
	if (key.equals("abbrev")) setAbbrev(value);
	if (key.equals("club")) setClub(value);
	if (key.equals("firstClassDate")) setFirstClassDate(value);
	if (key.equals("firstSignupDate")) setFirstSignupDate(value);
	if (key.equals("lastClassDate")) setLastClassDate(value);
	if (key.equals("lastSignupDate")) setLastSignupDate(value);
	throw new RuntimeException("bad key");
    }
}
