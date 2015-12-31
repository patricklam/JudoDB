// -*-  indent-tabs-mode:nil; c-basic-offset:4; -*-
package ca.patricklam.judodb.client;

import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.user.client.ui.ScrollPanel;

public class OnlyYScrollPanel extends ScrollPanel {
    public OnlyYScrollPanel() {
        getScrollableElement().getStyle().setOverflowX(Overflow.HIDDEN);
        getScrollableElement().getStyle().setOverflowY(Overflow.VISIBLE);
    }
}
