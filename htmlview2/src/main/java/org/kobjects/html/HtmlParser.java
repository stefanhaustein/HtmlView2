package org.kobjects.html;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;


/**
 * Turns the token stream from an xml pull parser in relaxed mode into well formed XML,
 * taking implicitly closed and empty HTML elements into account.
 */
public class HtmlParser {

  public enum ElementProperty {
    SELF_CLOSING,  // Automatically close this element
    TEXT,          // Element contains text, will be parsed to HtmlTextView.
    LOGICAL,
  }

  private static final String[] HTML_ENTITY_TABLE = {
      "acute", "\u00B4",
      "apos", "\u0027",
      "Auml", "\u00C4", "auml", "\u00E4",
      "nbsp", "\u00a0",
      "Ouml", "\u00D6", "ouml", "\u00F6",
      "szlig", "\u00DF",
      "Uuml", "\u00DC", "uuml", "\u00FC",
  };
  private final static ElementData EMPTY_ELEMENT_DATA = new ElementData(EnumSet.noneOf(ElementProperty.class));
  private final static LinkedHashMap<String, ElementData> DTD = new LinkedHashMap<>();
  static {
    DTD.put("a", new ElementData(EnumSet.of(ElementProperty.TEXT)));
    DTD.put("area", new ElementData(EnumSet.of(ElementProperty.SELF_CLOSING)));
    DTD.put("b", new ElementData(EnumSet.of(ElementProperty.TEXT)));
    DTD.put("base", new ElementData(EnumSet.of(ElementProperty.SELF_CLOSING)));
    DTD.put("big", new ElementData(EnumSet.of(ElementProperty.TEXT)));
    DTD.put("br", new ElementData(EnumSet.of(ElementProperty.SELF_CLOSING, ElementProperty.TEXT)));
    DTD.put("col", new ElementData(EnumSet.of(ElementProperty.SELF_CLOSING)));
    DTD.put("command", new ElementData(EnumSet.of(ElementProperty.SELF_CLOSING)));
    DTD.put("del", new ElementData(EnumSet.of(ElementProperty.TEXT)));
    DTD.put("em", new ElementData(EnumSet.of(ElementProperty.TEXT)));
    DTD.put("embed", new ElementData(EnumSet.of(ElementProperty.SELF_CLOSING)));
    DTD.put("font", new ElementData(EnumSet.of(ElementProperty.TEXT)));
    DTD.put("hr", new ElementData(EnumSet.of(ElementProperty.SELF_CLOSING)));
    DTD.put("i", new ElementData(EnumSet.of(ElementProperty.TEXT)));
    DTD.put("img", new ElementData(EnumSet.of(ElementProperty.SELF_CLOSING, ElementProperty.TEXT)));
    DTD.put("input", new ElementData(EnumSet.of(ElementProperty.SELF_CLOSING)));
    DTD.put("ins", new ElementData(EnumSet.of(ElementProperty.TEXT)));
    DTD.put("keygen", new ElementData(EnumSet.of(ElementProperty.SELF_CLOSING)));
    DTD.put("li", new ElementData(EnumSet.noneOf(ElementProperty.class), "li"));
    DTD.put("link", new ElementData(EnumSet.of(ElementProperty.SELF_CLOSING)));
    DTD.put("meta", new ElementData(EnumSet.of(ElementProperty.SELF_CLOSING)));
    DTD.put("p", new ElementData(EnumSet.noneOf(ElementProperty.class),
        "address", "article", "aside", "blockquote", "dir", "div", "dl", "fieldset", "footer",
        "form", "h1", "h2", "h3", "h4", "h5", "h6", "header", "hr", "menu", "nav", "ol", "p", "pre",
        "section", "table", "or", "ul"));
    DTD.put("param", new ElementData(EnumSet.of(ElementProperty.SELF_CLOSING)));
    DTD.put("small", new ElementData(EnumSet.of(ElementProperty.TEXT)));
    DTD.put("source", new ElementData(EnumSet.of(ElementProperty.SELF_CLOSING)));
    DTD.put("span", new ElementData(EnumSet.of(ElementProperty.TEXT)));
    DTD.put("strike", new ElementData(EnumSet.of(ElementProperty.TEXT)));
    DTD.put("strong", new ElementData(EnumSet.of(ElementProperty.TEXT)));
    DTD.put("sub", new ElementData(EnumSet.of(ElementProperty.TEXT)));
    DTD.put("sup", new ElementData(EnumSet.of(ElementProperty.TEXT)));
    DTD.put("tbody", new ElementData(EnumSet.of(ElementProperty.LOGICAL)));
    DTD.put("thead", new ElementData(EnumSet.of(ElementProperty.LOGICAL)));
    DTD.put("td", new ElementData(EnumSet.noneOf(ElementProperty.class), "td", "th", "tr"));
    DTD.put("th", new ElementData(EnumSet.noneOf(ElementProperty.class), "td", "th", "tr"));
    DTD.put("tt", new ElementData(EnumSet.of(ElementProperty.TEXT)));
    DTD.put("tr", new ElementData(EnumSet.of(ElementProperty.LOGICAL), "tr"));
    DTD.put("track", new ElementData(EnumSet.of(ElementProperty.SELF_CLOSING)));
    DTD.put("u", new ElementData(EnumSet.of(ElementProperty.TEXT)));
    DTD.put("wbr", new ElementData(EnumSet.of(ElementProperty.SELF_CLOSING)));
  }

  static private ElementData getElementData(String name) {
    ElementData result = DTD.get(name);
    return result == null ? EMPTY_ELEMENT_DATA : result;
  }

  public static boolean hasElementProperty(String name, ElementProperty property) {
    return getElementData(name).properties.contains(property);
  }

  private XmlPullParser parser;
  private int currentEvent;
  private String currentName;
  private boolean insertedEvent;
  private ArrayList<String> openTags = new ArrayList<>();

  public HtmlParser() throws XmlPullParserException {
    parser = XmlPullParserFactory.newInstance().newPullParser();
    parser.setFeature("http://xmlpull.org/v1/doc/features.html#relaxed", true);
  }

  public int getAttributeCount() {
    return parser.getAttributeCount();
  }

  public String getAttributeName(int index) {
    return parser.getAttributeName(index);
  }

  public String getAttributeValue(int index) {
    return parser.getAttributeValue(index);
  }

  public String getAttributeValue(String name) {
    return parser.getAttributeValue(null, name);
  }

  public int getEventType() throws XmlPullParserException {
    return currentEvent;
  }

  public String getName() {
    return currentName;
  }

  public String getText() {
    return parser.getText();
  }

  public String getPositionDescription() {
    return parser.getPositionDescription();
  }

  public int next() throws IOException, XmlPullParserException {
    // Insert close tags for self-closing elements.
    if (currentEvent == XmlPullParser.START_TAG &&
            hasElementProperty(currentName, ElementProperty.SELF_CLOSING)) {
      currentEvent = XmlPullParser.END_TAG;
      insertedEvent = true;
      openTags.remove(openTags.size() - 1);
      parser.next();
      return currentEvent;
    }

    if (insertedEvent) {
      insertedEvent = false;
      currentEvent = parser.getEventType();
    } else {
      currentEvent = parser.next();
    }

    // Skip close tags for self-closing elements.
    while (currentEvent == XmlPullParser.END_TAG
        && getElementData(parser.getName()).properties.contains(ElementProperty.SELF_CLOSING)) {
      currentEvent = parser.next();
    }

    // Auto-close elements which are closed by the current start tag.
    if (currentEvent == XmlPullParser.START_TAG && openTags.size() > 0
        && Arrays.binarySearch(getElementData(openTags.get(openTags.size() - 1)).closedBy,
                               parser.getName()) >= 0) {
      currentEvent = XmlPullParser.END_TAG;
      currentName = openTags.get(openTags.size() - 1);
      openTags.remove(openTags.size() - 1);
      insertedEvent = true;
      return currentEvent;
    }

    if (currentEvent == XmlPullParser.START_TAG) {
      currentName = parser.getName();
      openTags.add(currentName);
    } else if (currentEvent == XmlPullParser.END_TAG) {
      currentName = parser.getName();
      int i = openTags.lastIndexOf(currentName);
      if (i == -1) {
        System.err.println("Ignoring </" + currentName + ">: opening tag not found in " + openTags + " at " + parser.getPositionDescription());
        return next();
      }
      if (i != openTags.size() - 1) {
        insertedEvent = true;
        currentName = openTags.get(openTags.size() - 1);
      }
      openTags.remove(openTags.get(openTags.size() - 1));
    } else if (currentEvent == XmlPullParser.END_DOCUMENT && openTags.size() > 0) {
      currentName = openTags.get(openTags.size() - 1);
      currentEvent = XmlPullParser.END_TAG;
      openTags.remove(openTags.get(openTags.size() - 1));
      insertedEvent = true;
    }
    return currentEvent;
  }

  public void setInput(Reader reader) throws XmlPullParserException {
    parser.setInput(reader);
    for (int i = 0; i < HTML_ENTITY_TABLE.length; i += 2) {
      parser.defineEntityReplacementText(HTML_ENTITY_TABLE[i], HTML_ENTITY_TABLE[i + 1]);
    }
  }

  private static class ElementData {
    private final EnumSet<ElementProperty> properties;
    private final String[] closedBy;

    private ElementData(EnumSet<ElementProperty> properties, String... closedBy) {
      this.properties = properties;
      this.closedBy = closedBy;
    }
  }
}
