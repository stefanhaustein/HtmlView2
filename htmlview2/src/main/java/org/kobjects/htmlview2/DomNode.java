package org.kobjects.htmlview2;

import elemental.dom.Node;

class DomNode implements Node {
    DomDocument ownerDocument;
    DomNode parentNode;
    DomNode previousSibling;
    DomNode nextSibling;

    DomNode(DomDocument ownerDocument) {
        this.ownerDocument = ownerDocument;
    }

    @Override
    public DomNode getParentNode() {
        return parentNode;
    }

    @Override
    public DomElement getParentElement() {
        return parentNode instanceof DomElement ? (DomElement) parentNode : null;
    }

    @Override
    public DomDocument getOwnerDocument() {
        return ownerDocument;
    }

    @Override
    public DomNode getFirstChild() {
        return null;
    }

    @Override
    public DomNode getLastChild() {
        return null;
    }

    @Override
    public DomNode getNextSibling() {
        return nextSibling;
    }

    @Override
    public DomNode getPreviousSibling() {
        return previousSibling;
    }

    @Override
    public DomNode appendChild(Node node) {
        throw new UnsupportedOperationException();
    }
}
