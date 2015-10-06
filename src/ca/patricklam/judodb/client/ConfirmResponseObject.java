// -*-  indent-tabs-mode:nil; c-basic-offset:4; -*-
package ca.patricklam.judodb.client;

import com.google.gwt.core.client.JavaScriptObject;

class ConfirmResponseObject extends JavaScriptObject {
    protected ConfirmResponseObject() {}

    public final native String getResult() /*-{ return this.result; }-*/;
    public final native int getSid() /*-{ return this.sid; }-*/;
}
