package org.kobjects.htmlview2;

import elemental.dom.Node;

abstract class DomParentNode extends DomNode {
    DomNode firstChild;
    DomNode lastChild;

    DomParentNode(DomDocument ownerDocument) {
        super(ownerDocument);
    }

    public DomNode getFirstChild() {
        return firstChild;
    }

    public DomNode getLastChild() {
        return lastChild;
    }

    public DomNode appendChild(Node node) {
        DomNode domNode = (DomNode) node;
        if (lastChild == null) {
            lastChild = firstChild = domNode;
        } else {
            lastChild.nextSibling = domNode;
            domNode.previousSibling = lastChild;
            lastChild = domNode;
        }
        return domNode;
    }
}
