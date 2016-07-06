package org.kobjects.htmlview2.parser;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;


import elemental.dom.Element;
import org.kobjects.css.CssStyleSheet;
import org.kobjects.htmlview2.*;
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

  public void parse(Reader reader, HtmlView htmlView) {
    this.htmlView = htmlView;
    try {
      if (parser == null) {
        parser = new HtmlParser();
      }
      parser.setInput(reader);

      parser.next();
      parseContainerContent(htmlView, null);
      CssStyleSheet styleSheet = htmlView.getStyleSheet();
      for (int i = 0; i < htmlView.getChildCount(); i++) {
        styleSheet.apply(((HtmlViewGroup.LayoutParams) htmlView.getChildAt(i).getLayoutParams()).element, null);
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
  private void parseHtmlText(HtmlTextView htmlTextView, Element logicalContainer, List<HtmlTextView.TextElement> elementStack) throws XmlPullParserException, IOException {
    HtmlTextView.TextElement element = null;
    // Reconstruct elements
    if (elementStack.size() > 0) {
      element = htmlTextView.addElement(logicalContainer, elementStack.get(0).getName());
      for (int i = 1; i < elementStack.size(); i++) {
        element = element.addChild(elementStack.get(i).getName());
      }
    }
    // Resume parsing
    if (parser.getEventType() == XmlPullParser.TEXT) {
      if (element == null) {
        htmlTextView.appendNormalized(parser.getText());
      } else {
        element.appendNormalized(parser.getText());
      }
    } else {  // Must be on START_TAG here
      if (element == null) {
        element = htmlTextView.addElement(logicalContainer, parser.getName());
      } else {
        element = element.addChild(parser.getName());
      }
      parseTextElement(element, elementStack);
    }
  }


  // Precondition: on text element start tag
  private void parseTextElement(HtmlTextView.TextElement element, List<HtmlTextView.TextElement> elementStack) throws IOException, XmlPullParserException {
    // System.out.println("parseTextElement: " + parser.getName());
    elementStack.add(element);
    for (int i = 0; i < parser.getAttributeCount(); i++) {
      element.setAttribute(parser.getAttributeName(i), parser.getAttributeValue(i));
    }

    parser.next();
    while (parser.getEventType() != XmlPullParser.END_TAG) {
      switch (parser.getEventType()) {
        case XmlPullParser.START_TAG:
          if (parser.elementProperty(HtmlParser.ElementProperty.TEXT) || parser.getName().equals("img")) {
            parseTextElement(element.addChild(parser.getName()), elementStack);
          } else {
            // Fall back to parseContainerContent, preserving the open element stack
            element.end();
            return;
          }
          break;

        case XmlPullParser.TEXT:
          element.appendNormalized(parser.getText());
          parser.next();
          break;

        default:
          throw new RuntimeException("Unexpected: " + parser.getPositionDescription());
      }
    }
    element.end();
    elementStack.remove(elementStack.size() - 1);
    parser.next();
  }

  private void parseContainerContent(HtmlViewGroup physicalContainer, VirtualElement logicalContainer) throws IOException, XmlPullParserException {
    HtmlTextView pendingText = null;
    // System.out.println("parseContainerContent " + name);
    ArrayList<HtmlTextView.TextElement> textElementStack = null;
    while (parser.getEventType() != XmlPullParser.END_DOCUMENT
        && parser.getEventType() != XmlPullParser.END_TAG) {
      switch (parser.getEventType()) {
        case XmlPullParser.START_TAG: {
          String childName = parser.getName();
          if (childName.equals("html")) { // || childName.equals("head")) {
            parser.next();
            parseContainerContent(physicalContainer, logicalContainer);
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
          } else if (parser.elementProperty(HtmlParser.ElementProperty.LOGICAL)) {
            VirtualElement logicalChild = new VirtualElement(parser.getName());
            logicalContainer.appendChild(logicalChild);
            parser.next();
            parseContainerContent(physicalContainer, logicalChild);
            parser.next();
          } else if (parser.elementProperty(HtmlParser.ElementProperty.TEXT) || parser.getName().equals("img")) {
            if (pendingText == null) {
              pendingText = new HtmlTextView(htmlView);
              physicalContainer.addView(pendingText);
            }
            if (textElementStack == null) {
              textElementStack = new ArrayList<>();
            }
            parseHtmlText(pendingText, logicalContainer, textElementStack);
          } else {
            pendingText = null;
            ViewElement viewElement = (ViewElement) htmlView.getDocument().createElement(parser.getName());
            for (int i = 0; i < parser.getAttributeCount(); i++) {
              viewElement.setAttribute(parser.getAttributeName(i), parser.getAttributeValue(i));
            }
            View child = viewElement.getView();
            physicalContainer.addView(child);
            ((HtmlViewGroup.LayoutParams) child.getLayoutParams()).element = viewElement;
            if (logicalContainer != null) {
              logicalContainer.appendChild(viewElement);
            }
            parser.next();
            if (child instanceof HtmlViewGroup) {
              parseContainerContent((HtmlViewGroup) child, viewElement);
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
          if (containsText(parser.getText()) && logicalContainer != null) {
            if (pendingText == null) {
              pendingText = new HtmlTextView(htmlView);
              physicalContainer.addView(pendingText);
            }
            if (textElementStack != null && textElementStack.size() != 0) {
              parseHtmlText(pendingText, logicalContainer, textElementStack);
            } else {
              pendingText.appendNormalized(parser.getText());
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
