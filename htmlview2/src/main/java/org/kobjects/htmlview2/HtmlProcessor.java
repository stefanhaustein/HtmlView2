package org.kobjects.htmlview2;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;


import elemental.dom.Element;
import elemental.dom.Node;
import elemental.dom.Text;
import org.kobjects.css.CssStylableElement;
import org.kobjects.css.CssStyleSheet;
import org.kobjects.html.HtmlParser;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


/**
 * Uses a HtmlParser to generate a widget tree that corresponds to the HTML code.
 *
 * Can be re-used, but is not thread safe.
 */
public class HtmlProcessor {
  private static final String TAG = "HtmlProcessor";
  private HtmlParser parser;
  private HtmlView htmlView;
  private DomDocument document;

  public void parse(Reader reader, HtmlView htmlView) {
    this.htmlView = htmlView;
    try {
      if (parser == null) {
        parser = new HtmlParser();
      }
      parser.setInput(reader);
      parser.next();
      document = htmlView.getDocument();

      parseContainerContent(document);

      TreeSync.sync(htmlView, document);

      CssStyleSheet styleSheet = htmlView.getStyleSheet();
      Node child = document.getFirstChild();
      while (child != null) {
        if (child instanceof CssStylableElement) {
          styleSheet.apply((CssStylableElement) child, null);
        }
        child = child.getNextSibling();
      }

    } catch (XmlPullParserException e) {
      throw new RuntimeException(e);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Parse the text content of an element.
   * Precondition: behind the opening tag
   * Postcondition: on the closing tag
   */
  private String parseTextContent() throws IOException, XmlPullParserException {
    StringBuilder sb = new StringBuilder();
    while (parser.getEventType() != XmlPullParser.END_TAG) {
      switch(parser.getEventType()) {
        case XmlPullParser.START_TAG:
          parser.next();
          sb.append(parseTextContent());
          parser.next();
          break;

        case XmlPullParser.TEXT:
          sb.append(parser.getText());
          parser.next();
          break;

        default:
          throw new RuntimeException("Unexpected event: " + parser.getPositionDescription());
      }
    }
    return sb.toString();
  }

  /**
   * Adds text or text elements to the given HtmlTextView, opening elements from the given
   * elementStack. The element stack is used when a previous HtmlTextView was interrupted
   * because of block content.
   */
  private void parseHtmlText(Node container, List<Element> elementStack) throws XmlPullParserException, IOException {
    Element element = null;
    // Reconstruct elements
    if (elementStack.size() > 0) {
      element = document.createElement(elementStack.get(0).getLocalName());
      for (int i = 1; i < elementStack.size(); i++) {
        DomTextElement child = (DomTextElement) htmlView.getDocument().createElement(elementStack.get(i).getLocalName());
        element.appendChild(child);
        element = child;
      }
    }
    // Resume parsing
    if (parser.getEventType() == XmlPullParser.TEXT) {
      Text text = document.createTextNode(parser.getText());
      if (element == null) {
        container.appendChild(text);
      } else {
        element.appendChild(text);
      }
    } else {  // Must be on START_TAG here
      if (element == null) {
        element = document.createElement(parser.getName());
        container.appendChild(element);
      } else {
        DomTextElement child = (DomTextElement) htmlView.getDocument().createElement(parser.getName());
        element.appendChild(child);
        element = child;
      }
      parseTextElement(element, elementStack);
    }
  }


  // Precondition: on text element start tag
  private void parseTextElement(Element element, List<Element> elementStack) throws IOException, XmlPullParserException {
    // System.out.println("parseTextElement: " + parser.getName());
    elementStack.add(element);
    for (int i = 0; i < parser.getAttributeCount(); i++) {
      element.setAttribute(parser.getAttributeName(i), parser.getAttributeValue(i));
    }

    parser.next();
    while (parser.getEventType() != XmlPullParser.END_TAG) {
      switch (parser.getEventType()) {
        case XmlPullParser.START_TAG:
          if (parser.hasElementProperty(parser.getName(), HtmlParser.ElementProperty.TEXT) || parser.getName().equals("img")) {
            DomTextElement child = (DomTextElement) htmlView.getDocument().createElement(parser.getName());
            element.appendChild(child);
            parseTextElement(child, elementStack);
          } else {
            // Fall back to parseContainerContent, preserving the open element stack
            return;
          }
          break;

        case XmlPullParser.TEXT:
          element.appendChild(htmlView.getDocument().createTextNode(parser.getText()));
          parser.next();
          break;

        default:
          throw new RuntimeException("Unexpected: " + parser.getPositionDescription());
      }
    }
    elementStack.remove(elementStack.size() - 1);
    parser.next();
  }

  private void parseContainerContent(Node container) throws IOException, XmlPullParserException {
    // System.out.println("parseContainerContent " + name);
    ArrayList<Element> textElementStack = null;
    while (parser.getEventType() != XmlPullParser.END_DOCUMENT
        && parser.getEventType() != XmlPullParser.END_TAG) {
      switch (parser.getEventType()) {
        case XmlPullParser.START_TAG: {
          String childName = parser.getName();
          if (childName.equals("html")) { // || childName.equals("head")) {
            parser.next();
            parseContainerContent(container);
            parser.next();
          } else if (childName.equals("link")) {
            if ("stylesheet".equals(parser.getAttributeValue("rel"))) {
              String href = parser.getAttributeValue("href");
              if (href != null) {
                try {
                  htmlView.getRequestHandler().requestStyleSheet(htmlView,
                      htmlView.createUri(parser.getAttributeValue("href")));
                } catch (URISyntaxException e) {
                  Log.e(TAG, "Error resolving stylesheet URL " + href, e);
                }
              }
            }
            parser.next();
            parseTextContent();
            parser.next();
          } else if (childName.equals("script") || childName.equals("title")) {
            parser.next();
            parseTextContent();
            parser.next();
          } else if (childName.equals("style")) {
            parser.next();
            String styleText = parseTextContent();
            htmlView.getStyleSheet().read(styleText, htmlView.getBaseUri(), null, null, null);
            parser.next();
          } else if (parser.hasElementProperty(parser.getName(), HtmlParser.ElementProperty.LOGICAL)) {
            Element child = document.createElement(parser.getName());
            container.appendChild(child);
            parser.next();
            parseContainerContent(child);
            parser.next();
          } else if (parser.hasElementProperty(parser.getName(), HtmlParser.ElementProperty.TEXT) || parser.getName().equals("img")) {
            if (textElementStack == null) {
              textElementStack = new ArrayList<>();
            }
            parseHtmlText(container, textElementStack);
          } else {
            DomViewElement viewElement = (DomViewElement) htmlView.getDocument().createElement(parser.getName());
            for (int i = 0; i < parser.getAttributeCount(); i++) {
              viewElement.setAttribute(parser.getAttributeName(i), parser.getAttributeValue(i));
            }
            container.appendChild(viewElement);
            parser.next();
            View child = viewElement.getView();
            if (child instanceof HtmlViewGroup) {
              parseContainerContent(viewElement);
            } else if ("select".equals(childName)) {
              parseSelectContent((Spinner) child);
            } else {
              String viewContent = parseTextContent();
              if ("textarea".equals(childName)) {
                ((TextView) child).setText(viewContent);
              } else {
                System.out.println("Ignored view content for now: " + viewContent);
              }
            }
            parser.next();
          }
          break;
        }
        case XmlPullParser.TEXT:
          if (containsText(parser.getText())) {
            if (textElementStack != null && textElementStack.size() != 0) {
              parseHtmlText(container, textElementStack);
            } else {
              container.appendChild(document.createTextNode(parser.getText()));
            }
          }
          parser.next();
          break;

        default:
          throw new RuntimeException("Unexpected token: " + parser.getPositionDescription());
      }
    }
  }

  private void parseSelectContent(Spinner spinner) throws XmlPullParserException, IOException {
    ArrayAdapter<String> options = new ArrayAdapter<String>(htmlView.getContext(),
        android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(options);
    while (parser.getEventType() != XmlPullParser.END_TAG) {
      switch(parser.getEventType()) {
        case XmlPullParser.START_TAG:
          String name = parser.getName();
          boolean selected = parser.getAttributeValue("selected") != null;
          parser.next();
          String content = parseTextContent();
          parser.next();
          if (name.equals("option")) {
            options.add(content);
            if (selected) {
              spinner.setSelection(options.getCount() - 1);
            }
          }
          break;

        case XmlPullParser.TEXT:
          // Ignore
          parser.next();
          break;

        default:
          throw new RuntimeException("Unexpected event: " + parser.getPositionDescription());
      }
    }
  }

  private static boolean containsText(String text) {
    for (int i = 0; i < text.length(); i++) {
      if (text.charAt(i) > ' ') {
        return true;
      }
    }
    return false;
  }
}
