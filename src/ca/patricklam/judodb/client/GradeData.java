package ca.patricklam.judodb.client;

import java.util.Comparator;
import java.util.Date;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.i18n.client.DateTimeFormat;

public class GradeData extends JavaScriptObject {
	protected GradeData() {}
	
	public final native String getGrade() /*-{ return this.grade; }-*/;
	public final native void setGrade(String grade) /*-{ this.grade = grade; }-*/;
	public final native String getDateGrade() /*-{ return this.date_grade; }-*/;
	public final native void setDateGrade(String date_grade) /*-{ this.date_grade = date_grade; }-*/;
	
	static class GradeComparator implements Comparator<GradeData> {
		@SuppressWarnings("deprecation")
		public final int compare(GradeData g0, GradeData g1) {
			DateTimeFormat df = DateTimeFormat.getFormat("yyyy-MM-dd");
		
			if (g0.getDateGrade().equals(g1.getDateGrade())) return 0;
			if (g0.getDateGrade().equals("") || g0.getDateGrade().equals("0000-00-00")) return -1;
			if (g1.getDateGrade().equals("") || g1.getDateGrade().equals("0000-00-00")) return 1;
		
			Date d0 = df.parse(g0.getDateGrade());
			Date d1 = df.parse(g1.getDateGrade());
		
			if (d0.getYear() != d1.getYear())
				return d0.getYear() - d1.getYear();
			
			if (d0.getMonth() != d1.getMonth())
				return d0.getMonth() - d1.getMonth();
			
			if (d0.getDate() != d1.getDate())
				return d0.getDate() - d1.getDate();
			
			return 0;
		}
	}
}
