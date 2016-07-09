package org.kobjects.htmlview2;

import elemental.dom.Element;
import org.kobjects.css.CssStylableElement;
import org.kobjects.css.CssStyle;

import java.util.Iterator;
import java.util.LinkedHashMap;

class HvElement extends HvParentNode implements Element, CssStylableElement {
  private final String name;
  LinkedHashMap<String,String> attributes = new LinkedHashMap<>();
  CssStyle style;

  public HvElement(HvDocument ownerDocument, String name) {
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
      HvElement next = getFirstElementChild();

      @Override
      public boolean hasNext() {
        return next != null;
      }

      @Override
      public CssStylableElement next() {
        HvElement result = next;
        next = next.getNextElementSibling();
        return result;
      }
    };
  }

  @Override
  public HvElement getFirstElementChild() {
    HvNode result = getFirstChild();
    while (result != null && !(result instanceof HvElement)) {
      result = result.getNextSibling();
    }
    return (HvElement) result;
  }

  @Override
  public HvElement getLastElementChild() {
    HvNode result = getLastChild();
    while (result != null && !(result instanceof HvElement)) {
      result = result.getPreviousSibling();
    }
    return (HvElement) result;
  }

  @Override
  public HvElement getNextElementSibling() {
    HvNode result = getNextSibling();
    while (result != null && !(result instanceof HvElement)) {
      result = result.getNextSibling();
    }
    return (HvElement) result;
  }

  @Override
  public HvElement getPreviousElementSibling() {
    HvNode result = getPreviousSibling();
    while (result != null && !(result instanceof HvElement)) {
      result = result.getNextSibling();
    }
    return (HvElement) result;
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
