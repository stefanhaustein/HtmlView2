package org.kobjects.htmlview2;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import elemental.dom.Node;
import org.kobjects.css.CssStyle;

public class DomViewElement extends DomElement {
    private final DomDocument document;
    private View view;

    public DomViewElement(DomDocument document, String name, View view) {
        super(name);
        this.view = view;
        this.document = document;
    }

    @Override
    public void setComputedStyle(CssStyle style) {
        super.setComputedStyle(style);

        if (view instanceof HtmlViewGroup) {
            HtmlViewGroup htmlLayout = (HtmlViewGroup) view;
            for (int i = 0; i < htmlLayout.getChildCount(); i++) {
                if (htmlLayout.getChildAt(i) instanceof HtmlTextView) {
                    ((HtmlTextView) htmlLayout.getChildAt(i)).setComputedStyle(style);
                }
            }
        }
    }

    public View getView() {
        if (view == null) {
            String type = getAttribute("type");
            String value = getAttribute("value");
            if ("button".equals(type) || "submit".equals(type) || "reset".equals(type)) {
                view = new Button(document.htmlContext.getContext());
            } else if ("checkbox".equals(type)) {
                view = new CheckBox(document.htmlContext.getContext());
            } else {
                view = new EditText(document.htmlContext.getContext());
            }
            if (value != null) {
                ((TextView) view).setText(value);
            }
        }
        return view;
    }

    @Override
    public Node appendChild(Node child) {
        super.appendChild(child);

    }
}
