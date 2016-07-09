package org.kobjects.htmlview2;

import elemental.dom.Text;

class HvTextNode extends HvNode implements Text {
    String text;

    HvTextNode(HvDocument ownerDocument, String text) {
        super(ownerDocument);
        this.text = text;
    }

    @Override
    public String getData() {
        return text;
    }

}
