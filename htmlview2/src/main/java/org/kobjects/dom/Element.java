package org.kobjects.dom;

public interface Element extends Node {
  String getLocalName();
  void setAttribute(String name, String value);
  String getAttribute(String name);

  Element getFirstElementChild();
  Element getLastElementChild();
  Element getNextElementSibling();
  Element getPreviousElementSibling();

  CSSStyleDeclaration getStyle();
  CSSStyleDeclaration getComputedStyle();
}
