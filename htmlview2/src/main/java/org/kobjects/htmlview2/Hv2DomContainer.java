package org.kobjects.htmlview2;

import org.kobjects.dom.Node;

abstract class Hv2DomContainer extends Hv2DomNode {
  enum SyncState {
      SYNCED, CHILD_INVALID, INVALID
  }
  enum ComponentType {
      TEXT,                // span, b, i, ...
      PHYSICAL_CONTAINER,  // div, p, all other elements
      LOGICAL_CONTAINER,   // tr, tbody
      INVISIBLE,           // head, title, ...
      IMAGE,               // img
      LEAF_COMPONENT       // input, select, ...
  }
  ComponentType componentType;
  SyncState syncState;
  Hv2DomNode firstChild;
  Hv2DomNode lastChild;

  Hv2DomContainer(Hv2DomDocument ownerDocument, ComponentType componentType) {
    super(ownerDocument);
    this.componentType = componentType;
  }

  public Hv2DomNode getFirstChild() {
        return firstChild;
    }

  public Hv2DomNode getLastChild() {
        return lastChild;
    }

  public Hv2DomNode insertBefore(Node newNode, Node referenceNode) {
    Hv2DomNode newHvNode = (Hv2DomNode) newNode;
    if (referenceNode == null) {
      if (lastChild == null) {
        lastChild = firstChild = newHvNode;
      } else {
        lastChild.nextSibling = newHvNode;
        newHvNode.previousSibling = lastChild;
        lastChild = newHvNode;
      }
    } else if (referenceNode.getParentNode() != this) {
      throw new IllegalArgumentException("parent mismatch");
    } else {
      Hv2DomNode ref = (Hv2DomNode) referenceNode;
      if (ref.previousSibling == null) {
        firstChild = newHvNode;
        newHvNode.nextSibling = ref;
        ref.previousSibling = newHvNode;
      } else {
        newHvNode.previousSibling = ref.previousSibling;
        newHvNode.nextSibling = ref;
        ref.previousSibling.nextSibling = newHvNode;
        ref.previousSibling = newHvNode;
      }
    }
    newHvNode.parentNode = this;

    Hv2DomContainer parent = parentNode;
    syncState = SyncState.INVALID;
    while(parent != null && parent.syncState == SyncState.SYNCED) {
      parent.syncState = SyncState.CHILD_INVALID;
    }

    return newHvNode;
  }

  public String getTextContent() {
    StringBuilder sb = new StringBuilder();
    Node child = getFirstChild();
    while (child != null) {
      sb.append(child.getTextContent());
      child = child.getNextSibling();
    }
    return sb.toString();
  }

  public Hv2DomNode appendChild(Node node) {
        return insertBefore(node, null);
    }
}
