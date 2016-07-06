package org.kobjects.htmlview2;


import elemental.dom.Element;

import java.net.URI;

public interface RequestHandler {
  void requestStyleSheet(HtmlViewGroup rootElement, URI uri);
  void requestImage(ImageTarget target, URI uri);
  void openLink(Element element, URI uri);
}
