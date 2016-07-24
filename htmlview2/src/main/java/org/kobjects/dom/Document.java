package org.kobjects.dom;

public interface Document extends Node {
  Element createElement(String name);
  Text createTextNode(String text);

  Element getDocumentElement();
}