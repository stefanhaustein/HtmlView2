package org.kobjects.htmlview2;

public abstract class DomParentNode extends DomNode {
    DomNode firstChild;
    DomNode lastChild;

    public DomNode getFirstChild() {
        return firstChild;
    }

    public DomNode getLastChild() {
        return lastChild;
    }
}
