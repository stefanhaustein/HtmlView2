package org.kobjects.htmlview2;


import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import elemental.dom.Document;
import elemental.dom.Element;
import elemental.dom.Node;
import org.kobjects.html.HtmlParser;

class HvDocument extends HvParentNode implements Document {
    protected final HtmlView htmlContext;

    protected HvDocument(HtmlView context) {
        super(null);
        this.htmlContext = context;
    }

    public HvElement createElement(String name) {
        if (HtmlParser.hasElementProperty(name, HtmlParser.ElementProperty.LOGICAL)) {
            return new HvElement(this, name);
        }
        if (HtmlParser.hasElementProperty(name, HtmlParser.ElementProperty.TEXT)) {
            return new HvTextElement(this, name);
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
        return new HvViewElement(this, name, view);
    }

    @Override
    public HvTextNode createTextNode(String text) {
        return new HvTextNode(this, text);
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
