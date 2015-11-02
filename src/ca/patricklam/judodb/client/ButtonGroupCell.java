// -*-  indent-tabs-mode:nil; c-basic-offset:4; -*-
package ca.patricklam.judodb.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.gwt.cell.client.AbstractInputCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.SpanElement;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.client.SafeHtmlTemplates.Template;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.TakesValue;

public class ButtonGroupCell<T extends TakesValue<String>> extends AbstractInputCell<String, String> {
    interface Template extends SafeHtmlTemplates {
        @Template("<div class=\"btn-group\" role=\"group\"> <button type=\"button\" class=\"btn btn-default btn-xs dropdown-toggle\" data-toggle=\"dropdown\" aria-haspopup=\"true\" aria-expanded=\"false\"><span>{0}</span><span class=\"caret\"></span></button><ul class=\"dropdown-menu\">")
        SafeHtml openButtonGroup(String button);

        @Template("</ul></div>")
        SafeHtml closeButtonGroup();

        @Template("<li><a name=\"{0}\" onClick=\"this.parentNode.parentNode.previousSibling.firstChild.innerHTML=this.name;\">{0}</a></li>")
        SafeHtml li(String option);
    }

    private static Template template;
    private final HashMap<String, Integer> indexForOption = new HashMap<String, Integer>();
    private final List<T> options;
    private boolean showButton;

    public ButtonGroupCell(List<T> options) {
        super("click");
        if (template == null) {
            template = GWT.create(Template.class);
        }
        this.options = options;
        this.showButton = false;
    }

    public void setShowButton(boolean showButton) {
        this.showButton = showButton;
    }

    @Override public void onBrowserEvent(Context context, Element parent, String value,
				   NativeEvent event, ValueUpdater<String> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
        String type = event.getType();
        if ("click".equals(type)) {
            Element span = parent.getFirstChildElement().getFirstChildElement();
            String actualValue = span.getInnerText();
            if (valueUpdater != null) {
                 valueUpdater.update(actualValue);
            }
        }
    }

    @Override public void render(Context context, String value, SafeHtmlBuilder sb) {
        if (!showButton) {
            sb.appendEscaped(value);
            return;
        }

        sb.append(template.openButtonGroup(value));
        for (T option : options) {
            sb.append(template.li(option.getValue()));
        }
        sb.append(template.closeButtonGroup());
    }
}
