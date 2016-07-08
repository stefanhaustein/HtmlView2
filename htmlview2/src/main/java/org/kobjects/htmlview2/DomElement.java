package org.kobjects.htmlview2;

import elemental.dom.Element;
import elemental.dom.Node;
import org.kobjects.css.CssStylableElement;
import org.kobjects.css.CssStyle;

import java.util.Iterator;
import java.util.LinkedHashMap;

public class DomElement extends DomParentNode implements Element, CssStylableElement {
  private final String name;
  LinkedHashMap<String,String> attributes = new LinkedHashMap<>();
  CssStyle style;

  public DomElement(DomDocument ownerDocument, String name) {
    super(ownerDocument);
    this.name = name;
  }

  @Override
  public String getAttribute(String name) {
    return attributes.get(name);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getLocalName() {
    return name;
  }

  @Override
  public Iterator<? extends CssStylableElement> getChildElementIterator() {
    return new Iterator<CssStylableElement>() {
      DomElement next = getFirstElementChild();

      @Override
      public boolean hasNext() {
        return next != null;
      }

      @Override
      public CssStylableElement next() {
        DomElement result = next;
        next = next.getNextElementSibling();
        return result;
      }
    };
  }

  @Override
  public DomElement getFirstElementChild() {
    DomNode result = getFirstChild();
    while (result != null && !(result instanceof DomElement)) {
      result = result.getNextSibling();
    }
    return (DomElement) result;
  }

  @Override
  public DomElement getLastElementChild() {
    DomNode result = getLastChild();
    while (result != null && !(result instanceof DomElement)) {
      result = result.getPreviousSibling();
    }
    return (DomElement) result;
  }

  @Override
  public DomElement getNextElementSibling() {
    DomNode result = getNextSibling();
    while (result != null && !(result instanceof DomElement)) {
      result = result.getNextSibling();
    }
    return (DomElement) result;
  }

  @Override
  public DomElement getPreviousElementSibling() {
    DomNode result = getPreviousSibling();
    while (result != null && !(result instanceof DomElement)) {
      result = result.getNextSibling();
    }
    return (DomElement) result;
  }

  @Override
  public void setAttribute(String name, String value) {
    attributes.put(name, value);
  }

  @Override
  public void setComputedStyle(CssStyle style) {
    this.style = style;
  }
}
