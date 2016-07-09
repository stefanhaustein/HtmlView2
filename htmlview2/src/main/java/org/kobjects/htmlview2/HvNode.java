package org.kobjects.htmlview2;

import elemental.dom.Node;

class HvNode implements Node {
    HvDocument ownerDocument;
    HvNode parentNode;
    HvNode previousSibling;
    HvNode nextSibling;

    HvNode(HvDocument ownerDocument) {
        this.ownerDocument = ownerDocument;
    }

    @Override
    public HvNode getParentNode() {
        return parentNode;
    }

    @Override
    public HvElement getParentElement() {
        return parentNode instanceof HvElement ? (HvElement) parentNode : null;
    }

    @Override
    public HvDocument getOwnerDocument() {
        return ownerDocument;
    }

    @Override
    public HvNode getFirstChild() {
        return null;
    }

    @Override
    public HvNode getLastChild() {
        return null;
    }

    @Override
    public HvNode getNextSibling() {
        return nextSibling;
    }

    @Override
    public HvNode getPreviousSibling() {
        return previousSibling;
    }

    @Override
    public HvNode appendChild(Node node) {
        throw new UnsupportedOperationException();
    }
}
