package ca.patricklam.judodb.client;

import com.google.gwt.core.client.JavaScriptObject;

public class GradeData extends JavaScriptObject {
	protected GradeData() {}
	
	public final native String getGrade() /*-{ return this.grade; }-*/;
	public final native String getDateGrade() /*-{ return this.date_grade; }-*/;
}
