package org.kobjects.htmlview2;


import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import elemental.dom.Document;
import elemental.dom.Element;
import elemental.dom.Node;
import org.kobjects.html.HtmlParser;

public class DomDocument extends DomParentNode implements Document {
    protected final HtmlView htmlContext;

    protected DomDocument(HtmlView context) {
        super(null);
        this.htmlContext = context;
    }

    public DomElement createElement(String name) {
        if (HtmlParser.hasElementProperty(name, HtmlParser.ElementProperty.LOGICAL)) {
            return new DomElement(this, name);
        }
        if (HtmlParser.hasElementProperty(name, HtmlParser.ElementProperty.TEXT)) {
            return new DomTextElement(this, name);
        }

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
        return new DomViewElement(this, name, view);
    }

    @Override
    public DomTextNode createTextNode(String text) {
        return new DomTextNode(this, text);
    }

    @Override
    public Element getDocumentElement() {
        Node result = getFirstChild();
        while (result != null && !(result instanceof Element)) {
            result = result.getNextSibling();
        }
        return (Element) result;
    }

}
