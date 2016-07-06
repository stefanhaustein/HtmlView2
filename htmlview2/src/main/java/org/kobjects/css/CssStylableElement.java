package org.kobjects.css;

import java.util.Iterator;

public interface CssStylableElement {

  String getAttribute(String name);

  String getName();

  Iterator<? extends CssStylableElement> getChildElementIterator();
  
  void setComputedStyle(CssStyle style);
}
