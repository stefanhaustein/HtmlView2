package org.kobjects.htmlview2;


import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import elemental.dom.Document;
import elemental.dom.Element;
import elemental.dom.Node;

class HvDomDocument extends HvDomContainer implements Document {
    protected final HtmlView htmlContext;

    protected HvDomDocument(HtmlView context) {
        super(null, ComponentType.PHYSICAL_CONTAINER);
        this.htmlContext = context;
    }

    public HvDomElement createElement(String name) {
        return new HvDomElement(this, name, null);
    }

    @Override
    public HvDomText createTextNode(String text) {
        return new HvDomText(this, text);
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
