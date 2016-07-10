package org.kobjects.htmlview2;

import android.content.Context;
import android.view.View;
import android.widget.*;
import elemental.dom.Element;
import org.kobjects.css.CssStylableElement;
import org.kobjects.css.CssStyleDeclaration;

import java.util.Iterator;
import java.util.LinkedHashMap;

class HvDomElement extends HvDomContainer implements Element, CssStylableElement {

  private final String name;
  LinkedHashMap<String,String> attributes = new LinkedHashMap<>();
  CssStyleDeclaration style = new CssStyleDeclaration();
  CssStyleDeclaration computedStyle;
  SpanCollection sections;

  /**
   * The view corresponding to this element.
   */
  private View view;

  public HvDomElement(HvDomDocument ownerDocument, String name) {
    this(ownerDocument, name, null);
  }

  public HvDomElement(HvDomDocument ownerDocument, String name, View view) {
    super(ownerDocument, HtmlProcessor.getElementType(name));
    this.name = name;
    if (view == null && componentType == ComponentType.PHYSICAL_CONTAINER) {
      this.view = new HtmlViewGroup(ownerDocument.htmlContext.getContext(), ownerDocument.htmlContext, this);
    } else {
      this.view = view;
    }
  }

  @Override
  public String getAttribute(String name) {
    return attributes.get(name);
  }

  @Override
  public String getLocalName() {
    return name;
  }

  @Override
  public CssStyleDeclaration getComputedStyle() {
    return computedStyle;
  }

  @Override
  public CssStyleDeclaration getStyle() {
    return style;
  }

  public View getView() {
    if (view == null && (componentType == ComponentType.LEAF_COMPONENT)) {
      Context context = ownerDocument.htmlContext.getContext();
      if ("textarea".equals(name)) {
        this.view = new EditText(context);
      } else if ("select".equals(name)) {
        this.view = new Spinner(context);
      } else if ("input".equals(name)) {
        String type = getAttribute("type");
        String value = getAttribute("value");
        if ("button".equals(type) || "submit".equals(type) || "reset".equals(type)) {
          view = new Button(context);
        } else if ("checkbox".equals(type)) {
          view = new CheckBox(context);
        } else {
          view = new EditText(context);
        }
        if (value != null) {
          ((TextView) view).setText(value);
        }
      }
    }
    return view;
  }

  @Override
  public Iterator<? extends CssStylableElement> getChildElementIterator() {
    return new Iterator<CssStylableElement>() {
      HvDomElement next = getFirstElementChild();

      @Override
      public boolean hasNext() {
        return next != null;
      }

      @Override
      public CssStylableElement next() {
        HvDomElement result = next;
        next = next.getNextElementSibling();
        return result;
      }
    };
  }

  @Override
  public HvDomElement getFirstElementChild() {
    HvDomNode result = getFirstChild();
    while (result != null && !(result instanceof HvDomElement)) {
      result = result.getNextSibling();
    }
    return (HvDomElement) result;
  }

  @Override
  public HvDomElement getLastElementChild() {
    HvDomNode result = getLastChild();
    while (result != null && !(result instanceof HvDomElement)) {
      result = result.getPreviousSibling();
    }
    return (HvDomElement) result;
  }

  @Override
  public HvDomElement getNextElementSibling() {
    HvDomNode result = getNextSibling();
    while (result != null && !(result instanceof HvDomElement)) {
      result = result.getNextSibling();
    }
    return (HvDomElement) result;
  }

  @Override
  public HvDomElement getPreviousElementSibling() {
    HvDomNode result = getPreviousSibling();
    while (result != null && !(result instanceof HvDomElement)) {
      result = result.getNextSibling();
    }
    return (HvDomElement) result;
  }

  @Override
  public void setAttribute(String name, String value) {
    attributes.put(name, value);
    if (name.equals("style")) {
      style.read(ownerDocument.htmlContext.baseUri, value);
    }
  }

  @Override
  public void setComputedStyle(CssStyleDeclaration style) {
    this.computedStyle = style;

    if (sections != null) {
      sections.updateStyle();
    } else if (componentType == ComponentType.PHYSICAL_CONTAINER) {
      HtmlViewGroup htmlLayout = (HtmlViewGroup) view;
      for (int i = 0; i < htmlLayout.getChildCount(); i++) {
        if (htmlLayout.getChildAt(i) instanceof HtmlTextView) {
          ((HtmlTextView) htmlLayout.getChildAt(i)).setComputedStyle(style);
        }
      }
    }
  }
}
