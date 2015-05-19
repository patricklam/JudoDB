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
}
