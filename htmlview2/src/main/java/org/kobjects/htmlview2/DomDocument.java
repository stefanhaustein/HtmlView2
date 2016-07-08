package org.kobjects.htmlview2;


import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import elemental.dom.Document;
import elemental.dom.Element;
import elemental.dom.Node;
import org.kobjects.html.HtmlParser;

public class DomDocument extends DomNode implements Document {
    protected final HtmlView htmlContext;

    protected DomDocument(HtmlView context) {
        super(null);
        this.htmlContext = context;
    }

    public Element createElement(String name) {
        if (HtmlParser.hasElementProperty(name, HtmlParser.ElementProperty.LOGICAL)) {
            return new ElementImpl(this, name);
        }
        if (HtmlParser.hasElementProperty(name, HtmlParser.ElementProperty.TEXT)) {
            return new TextElement(this, name);
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
        return new ViewElement(this, name, view);
    }

    @Override
    public TextNode createTextNode(String text) {
        return new TextNode(this, text);
    }

    @Override
    public Node getParentNode() {
        return null;
    }

    @Override
    public Element getParentElement() {
        return null;
    }

    @Override
    public Document getOwnerDocument() {
        return null;
    }

    @Override
    public Node appendChild(Node node) {
        return null;
    }
}
