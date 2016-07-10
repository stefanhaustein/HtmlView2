package org.kobjects.htmlview2;

import elemental.dom.Node;

abstract class HvDomContainer extends HvDomNode {
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
  HvDomNode firstChild;
  HvDomNode lastChild;

  HvDomContainer(HvDomDocument ownerDocument, ComponentType componentType) {
    super(ownerDocument);
    this.componentType = componentType;
  }

  public HvDomNode getFirstChild() {
        return firstChild;
    }

  public HvDomNode getLastChild() {
        return lastChild;
    }

  public HvDomNode insertBefore(Node newNode, Node referenceNode) {
    HvDomNode newHvNode = (HvDomNode) newNode;
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
      HvDomNode ref = (HvDomNode) referenceNode;
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

    HvDomContainer parent = parentNode;
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

  public HvDomNode appendChild(Node node) {
        return insertBefore(node, null);
    }
}
