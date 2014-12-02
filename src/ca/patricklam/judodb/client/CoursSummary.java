package ca.patricklam.judodb.client;

import com.google.gwt.core.client.JavaScriptObject;

public class CoursSummary extends JavaScriptObject {
    protected CoursSummary() { }

    public final native String getId() /*-{ return this.id; }-*/;
    public final native String getShortDesc() /*-{ return this.short_desc; }-*/;
    public final native String getEntraineur() /*-{ return this.entraineur; }-*/;
}
