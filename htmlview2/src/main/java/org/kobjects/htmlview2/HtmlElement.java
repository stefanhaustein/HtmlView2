package org.kobjects.htmlview2;

import org.kobjects.css.CssStylableElement;

public interface HtmlElement extends CssStylableElement {

  void setAttribute(String name, String value);
  void add(HtmlElement element);

}
