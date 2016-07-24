package org.kobjects.htmlview2;

import org.kobjects.dom.Node;

abstract class Hv2DomNode implements Node {
    Hv2DomDocument ownerDocument;
    Hv2DomContainer parentNode;
    Hv2DomNode previousSibling;
    Hv2DomNode nextSibling;

    Hv2DomNode(Hv2DomDocument ownerDocument) {
        this.ownerDocument = ownerDocument;
    }

    @Override
    public Hv2DomNode getParentNode() {
        return parentNode;
    }

    @Override
    public Hv2DomElement getParentElement() {
        return parentNode instanceof Hv2DomElement ? (Hv2DomElement) parentNode : null;
    }

    @Override
    public Hv2DomDocument getOwnerDocument() {
        return ownerDocument;
    }

    @Override
    public Hv2DomNode getNextSibling() {
        return nextSibling;
    }

    @Override
    public Hv2DomNode getPreviousSibling() {
        return previousSibling;
    }

    @Override
    public Hv2DomNode appendChild(Node node) {
        throw new UnsupportedOperationException();
    }
}
