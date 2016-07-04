package org.kobjects.htmlview2;

import org.kobjects.css.CssStylableElement;
import org.kobjects.css.CssStyle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class NonViewElement implements HtmlElement {
  private final String name;
  ArrayList<HtmlElement> children = new ArrayList<>();
  LinkedHashMap<String,String> attributes = new LinkedHashMap<>();
  CssStyle style;

  public NonViewElement(String name) {
    this.name = name;
  }

  @Override
  public void add(HtmlElement element) {
    children.add(element);
  }

  @Override
  public String getAttributeValue(String name) {
    return attributes.get(name);
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public Iterator<? extends CssStylableElement> getChildElementIterator() {
    return children.iterator();
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
