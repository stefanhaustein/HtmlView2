package org.kobjects.htmlview2;

import elemental.dom.Text;

class DomTextNode extends DomNode implements Text {
    String text;

    DomTextNode(DomDocument ownerDocument, String text) {
        super(ownerDocument);
        this.text = text;
    }

    @Override
    public String getData() {
        return text;
    }

}
