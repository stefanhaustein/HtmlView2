package org.kobjects.htmlview2;


import org.kobjects.dom.Document;
import org.kobjects.dom.Element;
import org.kobjects.dom.Node;

class Hv2DomDocument extends Hv2DomContainer implements Document {
    protected final HtmlView htmlContext;

    protected Hv2DomDocument(HtmlView context) {
        super(null, ComponentType.PHYSICAL_CONTAINER);
        this.htmlContext = context;
    }

    public Hv2DomElement createElement(String name) {
        return new Hv2DomElement(this, name, null);
    }

    @Override
    public Hv2DomText createTextNode(String text) {
        return new Hv2DomText(this, text);
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
