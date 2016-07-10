package org.kobjects.htmlview2;

import elemental.dom.Node;

abstract class HvDomNode implements Node {
    HvDomDocument ownerDocument;
    HvDomContainer parentNode;
    HvDomNode previousSibling;
    HvDomNode nextSibling;

    HvDomNode(HvDomDocument ownerDocument) {
        this.ownerDocument = ownerDocument;
    }

    @Override
    public HvDomNode getParentNode() {
        return parentNode;
    }

    @Override
    public HvDomElement getParentElement() {
        return parentNode instanceof HvDomElement ? (HvDomElement) parentNode : null;
    }

    @Override
    public HvDomDocument getOwnerDocument() {
        return ownerDocument;
    }

    @Override
    public HvDomNode getNextSibling() {
        return nextSibling;
    }

    @Override
    public HvDomNode getPreviousSibling() {
        return previousSibling;
    }

    @Override
    public HvDomNode appendChild(Node node) {
        throw new UnsupportedOperationException();
    }
}
