package org.kobjects.htmlview2;


import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import elemental.dom.Document;
import elemental.dom.Element;

public class HtmlDocument implements Document {
    protected final HtmlView htmlContext;

    protected HtmlDocument(HtmlView context) {
        this.htmlContext = context;
    }

    public Element createElement(String name) {
        View view;
        if ("input".equals(name)) {
            view = null;
        } else if ("textarea".equals(name)) {
            view = new EditText(htmlContext.getContext());
        } else if ("select".equals(name)) {
            view = new Spinner(htmlContext.getContext());
        } else {
            view = new HtmlViewGroup(htmlContext.getContext(), htmlContext);
        }
        return new ViewElement(this, name, view);
    }
}
