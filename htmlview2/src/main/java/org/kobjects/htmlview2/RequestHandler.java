package org.kobjects.htmlview2;


import java.net.URI;

public interface RequestHandler {
  void requestStyleSheet(HtmlLayout rootElement, URI uri);
  void requestImage(ImageTarget target, URI uri);
  void openLink(HtmlElement element, URI uri);
}
