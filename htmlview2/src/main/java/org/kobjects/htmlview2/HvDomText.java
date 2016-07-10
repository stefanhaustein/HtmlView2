package org.kobjects.htmlview2;

import elemental.dom.Text;

class HvDomText extends HvDomNode implements Text {
  String text;

  HvDomText(HvDomDocument ownerDocument, String text) {
    super(ownerDocument);
    this.text = text;
  }

  @Override
  public String getData() {
        return text;
    }

  @Override
  public HvDomNode getFirstChild() {
        return null;
    }

  @Override
  public HvDomNode getLastChild() {
        return null;
    }

  @Override
  public String getTextContent() {
    return text;
  }
}
