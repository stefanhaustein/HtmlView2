package org.kobjects.htmlview2;

import elemental.dom.Node;

class DomNode implements Node {
    DocumentImpl ownerDocument;
    DomNode parentNode;

    DomNode(DocumentImpl ownerDocument) {
        this.ownerDocument = ownerDocument;
    }

    @Override
    public DomNode getParentNode() {
        return parentNode;
    }

    @Override
    public ElementImpl getParentElement() {
        return parentNode instanceof ElementImpl ? (ElementImpl) parentNode : null;
    }

    @Override
    public DocumentImpl getOwnerDocument() {
        return ownerDocument;
    }

    @Override
    public Node appendChild(Node node) {
        throw new UnsupportedOperationException();
    }
}
