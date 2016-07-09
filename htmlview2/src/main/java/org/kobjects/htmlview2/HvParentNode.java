package org.kobjects.htmlview2;

import elemental.dom.Node;

abstract class HvParentNode extends HvNode {
    HvNode firstChild;
    HvNode lastChild;

    HvParentNode(HvDocument ownerDocument) {
        super(ownerDocument);
    }

    public HvNode getFirstChild() {
        return firstChild;
    }

    public HvNode getLastChild() {
        return lastChild;
    }

    public HvNode appendChild(Node node) {
        HvNode domNode = (HvNode) node;
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
