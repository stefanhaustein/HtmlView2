// Copyright 2010 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.kobjects.css;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This class represents a CSS style sheet. It is also used to represent parts of a style sheet in 
 * a tree structure, where the depth of the tree equals the length of the longest selector.
 * 
 * @author Stefan Haustein
 */
public class CssStyleSheet {
  private static final char SELECT_ATTRIBUTE_NAME = 7;
  private static final char SELECT_ATTRIBUTE_VALUE = 8;
  private static final char SELECT_ATTRIBUTE_INCLUDES = 9;
  private static final char SELECT_ATTRIBUTE_DASHMATCH = 10;


  /**
   * A table mapping element names to sub-style sheets for the corresponding
   * selection path.
   */
  public HashMap<String, CssStyleSheet> selectElementName;

  /**
   * A table mapping pseudoclass names to sub-style sheets for the corresponding
   * selection path.
   */
  private HashMap<String, CssStyleSheet> selectPseudoclass;

  /**
   * A list of attribute names for selectors. Forms attribute selectors together
   * with selectAttributeOperation and selectAttributeValue.
   */
  private ArrayList<String> selectAttributeName;

  /**
   * A list of attribute operations for selectors (one of the
   * SELECT_ATTRIBUTE_XX constants). Forms attribute selectors together with
   * selectAttributeName and selectAttributeValue.
   */
  private StringBuilder selectAttributeOperation;

  /**
   * A list of Hashtables, mapping attribute values to sub-style sheets for the
   * corresponding selection path. Forms attribute selectors together with
   * selectAttributeName and selectAttributeOperation.
   */
  private ArrayList<HashMap<String, CssStyleSheet>> selectAttributeValue;

  /**
   * Reference to child selector selector sub-style sheet.
   */
  private CssStyleSheet selectChild;

  /**
   * Reference to descendant selector sub-style sheet.
   */
  private CssStyleSheet selectDescendants;

  /**
   * Properties for * rules 
   */
  private ArrayList<CssStyle> properties;

  /**
   * Creates a new style sheet with default rules for HTML.
   */
  public static CssStyleSheet createDefault() {
    CssStyleSheet s = new CssStyleSheet();
    // Set default indent with to sufficient space for ordered lists with
    // two digits and the default paragraph spacing to 50% of the font height
    // (so top and bottom spacing adds up to a full line)
    int defaultIndent = 40;
    int defaultParagraphSpace = 12;
    
    s.put(":link", new CssStyle()
        .set(CssProperty.COLOR, 0x0ff0000ff, CssUnit.ARGB)
        .setEnum(CssProperty.TEXT_DECORATION, CssEnum.UNDERLINE));
    s.put("address", new CssStyle()
        .setEnum(CssProperty.DISPLAY, CssEnum.BLOCK));
    s.put("b", new CssStyle().set(CssProperty.FONT_WEIGHT, 700000, CssUnit.NUMBER));
    CssStyle tt = new CssStyle();
    tt.fontFamily = "monospace";
    s.put("tt", tt);
    s.put("big", new CssStyle().set(CssProperty.FONT_SIZE, 16, CssUnit.PT));
    s.put("blockquote", new CssStyle()
        .setEnum(CssProperty.DISPLAY, CssEnum.BLOCK)
        .set(CssProperty.MARGIN_TOP, defaultParagraphSpace, CssUnit.PX)
        .set(CssProperty.MARGIN_RIGHT, defaultIndent, CssUnit.PX)
        .set(CssProperty.MARGIN_BOTTOM, defaultParagraphSpace, CssUnit.PX)
        .set(CssProperty.MARGIN_LEFT, defaultIndent, CssUnit.PX));
    s.put("body", new CssStyle()
        .setEnum(CssProperty.DISPLAY, CssEnum.BLOCK)
        .set(CssProperty.PADDING, defaultParagraphSpace / 2, CssUnit.PX, 0));
    s.put("button", new CssStyle().
        setEnum(CssProperty.DISPLAY, CssEnum.INLINE_BLOCK).
        set(CssProperty.PADDING, 30, CssUnit.PX));
    s.put("center", new CssStyle()
        .setEnum(CssProperty.DISPLAY, CssEnum.BLOCK)
        .set(CssProperty.MARGIN_TOP, defaultParagraphSpace, CssUnit.PX)
        .set(CssProperty.MARGIN_BOTTOM, defaultParagraphSpace, CssUnit.PX)
        .setEnum(CssProperty.TEXT_ALIGN, CssEnum.CENTER));
    s.put("dd", new CssStyle()
        .setEnum(CssProperty.DISPLAY, CssEnum.BLOCK)
        .set(CssProperty.MARGIN_LEFT, defaultIndent, CssUnit.PX));
    s.put("del", new CssStyle().setEnum(CssProperty.TEXT_DECORATION, CssEnum.LINE_THROUGH));
    s.put("dir", new CssStyle()
        .setEnum(CssProperty.DISPLAY, CssEnum.BLOCK)
        .set(CssProperty.MARGIN_TOP, defaultParagraphSpace, CssUnit.PX)
        .set(CssProperty.MARGIN_BOTTOM, defaultParagraphSpace, CssUnit.PX)
        .set(CssProperty.MARGIN_LEFT, defaultIndent, CssUnit.PX)
        .setEnum(CssProperty.LIST_STYLE_TYPE, CssEnum.SQUARE));
    s.put("div", new CssStyle().setEnum(CssProperty.DISPLAY, CssEnum.BLOCK));
    s.put("dl", new CssStyle().setEnum(CssProperty.DISPLAY, CssEnum.BLOCK));
    s.put("dt", new CssStyle().setEnum(CssProperty.DISPLAY, CssEnum.BLOCK));
    s.put("form", new CssStyle().setEnum(CssProperty.DISPLAY, CssEnum.BLOCK));
    for (int i = 1; i <= 6; i++) {
      s.put("h" + i, new CssStyle()
          .setEnum(CssProperty.DISPLAY, CssEnum.BLOCK)
          .set(CssProperty.FONT_WEIGHT, 700, CssUnit.NUMBER)
          .set(CssProperty.MARGIN_TOP, defaultParagraphSpace, CssUnit.PX)
          .set(CssProperty.MARGIN_BOTTOM, defaultParagraphSpace, CssUnit.PX)
          .set(CssProperty.FONT_SIZE, 20 - 2 * i, CssUnit.PT));
    }
    s.put("hr", new CssStyle()
        .setEnum(CssProperty.DISPLAY, CssEnum.BLOCK)
        .setEnum(CssProperty.BORDER_TOP_STYLE, CssEnum.SOLID)
        .set(CssProperty.BORDER_TOP_COLOR, 0x0ff888888, CssUnit.ARGB)
        .set(CssProperty.MARGIN_TOP, defaultParagraphSpace, CssUnit.PX)
        .set(CssProperty.MARGIN_BOTTOM, defaultParagraphSpace, CssUnit.PX));
    CssStyle italic = new CssStyle().setEnum(CssProperty.FONT_STYLE, CssEnum.ITALIC);
    s.put("i", italic);
    s.put("em", italic);
    s.put("img", new CssStyle().setEnum(CssProperty.DISPLAY, CssEnum.INLINE_BLOCK));
    s.put("input", new CssStyle()
        .setEnum(CssProperty.DISPLAY, CssEnum.INLINE_BLOCK));
    s.put("ins", new CssStyle().setEnum(CssProperty.TEXT_DECORATION, CssEnum.UNDERLINE));
    s.put("li", new CssStyle()
        .setEnum(CssProperty.DISPLAY, CssEnum.LIST_ITEM)
        .set(CssProperty.MARGIN_TOP, defaultParagraphSpace, CssUnit.PX)
        .set(CssProperty.MARGIN_BOTTOM, defaultParagraphSpace, CssUnit.PX));
    s.put("marquee", new CssStyle().setEnum(CssProperty.DISPLAY, CssEnum.BLOCK));
    s.put("menu", new CssStyle()
        .setEnum(CssProperty.DISPLAY, CssEnum.BLOCK).
        set(CssProperty.MARGIN_TOP, defaultParagraphSpace, CssUnit.PX).
        set(CssProperty.MARGIN_BOTTOM, defaultParagraphSpace, CssUnit.PX).
        set(CssProperty.MARGIN_LEFT, defaultIndent, CssUnit.PX).
        setEnum(CssProperty.LIST_STYLE_TYPE, CssEnum.SQUARE));
    s.put("ol", new CssStyle()
        .setEnum(CssProperty.DISPLAY, CssEnum.BLOCK)
        .set(CssProperty.MARGIN_LEFT, defaultIndent, CssUnit.PX)
        .setEnum(CssProperty.LIST_STYLE_TYPE, CssEnum.DECIMAL));
    s.put("p", new CssStyle()
        .setEnum(CssProperty.DISPLAY, CssEnum.BLOCK)
        .set(CssProperty.MARGIN_TOP, defaultParagraphSpace, CssUnit.PX)
        .set(CssProperty.MARGIN_BOTTOM, defaultParagraphSpace, CssUnit.PX));
    CssStyle pre = new CssStyle()
        .setEnum(CssProperty.DISPLAY, CssEnum.BLOCK)
        .setEnum(CssProperty.WHITE_SPACE, CssEnum.PRE)
        .set(CssProperty.MARGIN_TOP, defaultParagraphSpace, CssUnit.PX)
        .set(CssProperty.MARGIN_BOTTOM, defaultParagraphSpace, CssUnit.PX);
    pre.fontFamily = "monospace";
    s.put("pre", pre);
    s.put("script", new CssStyle().setEnum(CssProperty.DISPLAY, CssEnum.NONE));
    s.put("small", new CssStyle().set(CssProperty.FONT_SIZE, 9, CssUnit.PT));
    s.put("strike", new CssStyle().setEnum(CssProperty.TEXT_DECORATION, CssEnum.LINE_THROUGH));
    s.put("strong", new CssStyle()
        .set(CssProperty.FONT_WEIGHT, CssStyle.FONT_WEIGHT_BOLD, CssUnit.NUMBER));
    s.put("style", new CssStyle().setEnum(CssProperty.DISPLAY, CssEnum.NONE));

    s.put("sup", new CssStyle()
        .set(CssProperty.FONT_SIZE, 9, CssUnit.PT)
        .setEnum(CssProperty.VERTICAL_ALIGN, CssEnum.SUPER));
    s.put("sub", new CssStyle()
        .set(CssProperty.FONT_SIZE, 9, CssUnit.PT)
        .setEnum(CssProperty.VERTICAL_ALIGN, CssEnum.SUB));

    s.put("table", new CssStyle()
        .set(CssProperty.BORDER_SPACING, 2, CssUnit.PX)
        .setEnum(CssProperty.DISPLAY, CssEnum.TABLE)
        .setEnum(CssProperty.CLEAR, CssEnum.BOTH));
    s.put("td", new CssStyle()
        .setEnum(CssProperty.DISPLAY, CssEnum.TABLE_CELL)
        .set(CssProperty.PADDING, 10, CssUnit.PX)
        .setEnum(CssProperty.BORDER_STYLE, CssEnum.SOLID)
        .setEnum(CssProperty.TEXT_ALIGN, CssEnum.LEFT));
    s.put("th", new CssStyle()
        .setEnum(CssProperty.DISPLAY, CssEnum.TABLE_CELL)
        .set(CssProperty.FONT_WEIGHT, 700, CssUnit.NUMBER)
        .set(CssProperty.PADDING, 10, CssUnit.PX)
        .setEnum(CssProperty.BORDER_STYLE, CssEnum.SOLID)
        .setEnum(CssProperty.TEXT_ALIGN, CssEnum.CENTER));
    s.put("tr", new CssStyle().setEnum(CssProperty.DISPLAY, CssEnum.TABLE_ROW));
    s.put("u", new CssStyle().setEnum(CssProperty.TEXT_DECORATION, CssEnum.UNDERLINE));
    s.put("ul", new CssStyle()
        .setEnum(CssProperty.DISPLAY, CssEnum.BLOCK)
        .set(CssProperty.MARGIN_LEFT, defaultIndent, CssUnit.PX)
        .setEnum(CssProperty.LIST_STYLE_TYPE, CssEnum.SQUARE));
    s.put("ul ul", new CssStyle().setEnum(CssProperty.LIST_STYLE_TYPE, CssEnum.CIRCLE));
    s.put("ul ul ul", new CssStyle().setEnum(CssProperty.LIST_STYLE_TYPE, CssEnum.DISC));
    return s;
  }

  /**
   * Returns true if s matches any type in the given media type array. null, the empty string
   * and all are always matched. s is converted to lower case for the match.
   */
  public static boolean matchesMediaType(String s, String[] mediaTypes) {
    if (s == null) {
      return true;
    }
    s = s.trim().toLowerCase(Locale.US);
    return s.length() == 0 || s.equals("all") || Css.indexOfIgnoreCase(mediaTypes, s) != -1;
  }
  
  
  /**
   * Reads a style sheet from the given css string and merges it into this style sheet.
   * @param css the CSS string to load the style sheet from
   * @param url URL of this style sheet (or the containing document)
   * @param nesting The nesting of this style sheet in other style sheets.
   * 
   * @return this
   */
  public CssStyleSheet read(String css, URI url, int[] nesting, String[] mediaTypes, List<Dependency> dependencies) {
    CssTokenizer ct = new CssTokenizer(url, css);
    int position = 0;
    boolean inMedia = false;
    while (ct.ttype != CssTokenizer.TT_EOF) {
      if (ct.ttype == CssTokenizer.TT_ATKEYWORD) {
        if ("media".equals(ct.sval)) {
          ct.nextToken(false);
          inMedia = false;
          do {
            if(ct.ttype != ',') {
              inMedia |= matchesMediaType(ct.sval, mediaTypes);
            }
            ct.nextToken(false);
          } while (ct.ttype != '{' && ct.ttype != CssTokenizer.TT_EOF);
          ct.nextToken(false);
          if (!inMedia) {
            int level = 1;
            do {
              switch (ct.ttype) {
              case '}': 
                level--;
                break;
              case '{':
                level++;
                break;
              case CssTokenizer.TT_EOF:
                return this;
              }
              ct.nextToken(false);
            } while (level > 0);
          }
        } else if ("import".equals(ct.sval)){
          ct.nextToken(false);
          String importUrl = ct.sval;
          ct.nextToken(false);
          StringBuilder media = new StringBuilder();
          while (ct.ttype != ';' && ct.ttype != CssTokenizer.TT_EOF) {
            media.append(ct.sval);
            ct.nextToken(false);
          }
          if (matchesMediaType(media.toString(), mediaTypes)) {
            int [] dependencyNesting = new int[nesting.length + 1];
            System.arraycopy(nesting, 0, dependencyNesting, 0, nesting.length);
            dependencyNesting[nesting.length] = position;
            dependencies.add(new Dependency(url.resolve(importUrl), dependencyNesting));
          }
          ct.nextToken(false);
          position++;
        } else {
          ct.debug("unsupported @" + ct.sval);
          ct.nextToken(false);
        }
      } else if (ct.ttype == '}') {
        if (!inMedia) {
          ct.debug("unexpected }");
        }
        inMedia = false;
        ct.nextToken(false);
      } else {
        // no @keyword or } -> regular selector
        ArrayList<CssStyle> targets = new ArrayList<CssStyle>();
        targets.add(parseSelector(ct));
        while (ct.ttype == ',') {
          ct.nextToken(false);
          targets.add(parseSelector(ct));
        }

        CssStyle style = new CssStyle();
        if (ct.ttype == '{') {
          ct.nextToken(false);
          style.read(ct);
          ct.assertTokenType('}');
        } else {
          ct.debug("{ expected");
        }

        for (int i = 0; i < targets.size(); i++) {
          CssStyle target = targets.get(i);
          if (target == null) {
            continue;
          }
          target.position = position;
          target.nesting = nesting;
          target.setFrom(style);
        }
        ct.nextToken(false);
        position++;
      }
    }
    return this;
  }

  /**
   * Parse a selector. The tokenizer must be at the first token of the selector.
   * When returning, the current token will be ',' or '{'.
   * <p>
   * This method brings selector paths into the tree form described in the class
   * documentation.
   * 
   * @param ct the css tokenizer
   * @return the node at the end of the tree path denoted by this selector,
   *         where the corresponding CSS properties will be stored
   */
  private CssStyle parseSelector(CssTokenizer ct) {

    boolean error = false;
    
    int specificity = 0;
    CssStyleSheet result = this;

    loop : while (true) {
      switch (ct.ttype) {
        case CssTokenizer.TT_IDENT: {
          if (result.selectElementName == null) {
            result.selectElementName = new HashMap<String, CssStyleSheet>();
          }
          result = descend(result.selectElementName, 
              Css.identifierToLowerCase(ct.sval));
          specificity += Css.SPECIFICITY_D;
          ct.nextToken(true);
          break;
        }
        case '*': {
          // no need to do anything...
          ct.nextToken(true);
          continue;
        }
        case '[': {
          ct.nextToken(false);
          String name = Css.identifierToLowerCase(ct.sval);
          ct.nextToken(false);
          char type;
          String value = "";
          
          if (ct.ttype == ']') {
            type = SELECT_ATTRIBUTE_NAME;
          } else {
            switch (ct.ttype) {
              case CssTokenizer.TT_INCLUDES:
                type = SELECT_ATTRIBUTE_INCLUDES;
                break;
              case '=':
                type = SELECT_ATTRIBUTE_VALUE;
                break;
              case CssTokenizer.TT_DASHMATCH:
                type = SELECT_ATTRIBUTE_DASHMATCH;
                break;
              default:
                error = true;
                break loop;
            }
            ct.nextToken(false);
            if (ct.ttype != CssTokenizer.TT_STRING) {
              error = true;
              break loop;
            }
            value = ct.sval;
            ct.nextToken(false);
            ct.assertTokenType(']');
            specificity += Css.SPECIFICITY_C;
          }
          result = result.createAttributeSelector(type, name, value);
          ct.nextToken(true);
          break;
        }
        case '.':
          ct.nextToken(false);
          error = ct.ttype != CssTokenizer.TT_IDENT;
          result = result.createAttributeSelector(SELECT_ATTRIBUTE_INCLUDES, "class", ct.sval);
          specificity += Css.SPECIFICITY_C;
          ct.nextToken(true);
          break;

        case CssTokenizer.TT_HASH:
          result = result.createAttributeSelector(SELECT_ATTRIBUTE_VALUE, "id", ct.sval);
          specificity += Css.SPECIFICITY_B;
          ct.nextToken(true);
          break;

        case ':': 
          ct.nextToken(false);
          error = ct.ttype != CssTokenizer.TT_IDENT;
          if (result.selectPseudoclass == null) {
            result.selectPseudoclass = new HashMap<String, CssStyleSheet>();
          }
          result = descend(result.selectPseudoclass, ct.sval);
          specificity += Css.SPECIFICITY_C;
          ct.nextToken(true);
          break;

        case CssTokenizer.TT_S:
          ct.nextToken(false);
          if (ct.ttype == '{' || ct.ttype == ',' || ct.ttype == -1) {
            break loop;
          }
          if (ct.ttype == '>') {
            if (result.selectChild == null) {
              result.selectChild = new CssStyleSheet();
            }
            result = result.selectChild;
            ct.nextToken(false);
          } else {
            if (result.selectDescendants == null) {
              result.selectDescendants = new CssStyleSheet();
            }
            result = result.selectDescendants;
          }
          break;

        case '>':
          if (result.selectChild == null) {
            result.selectChild = new CssStyleSheet();
          }
          result = result.selectChild;
          ct.nextToken(false);
          break;

        default: // unknown
          break loop;
      }
    }

    // state: behind all recognized tokens -- check for unexpected stuff
    if (error || (ct.ttype != ',' && ct.ttype != '{')) {
      ct.debug("Unrecognized selector");
      // parse to '{', ',' or TT_EOF to get to a well-defined state
      while (ct.ttype != ',' && ct.ttype != CssTokenizer.TT_EOF
          && ct.ttype != '{') {
        ct.nextToken(false);
      }
      return null;
    }

    CssStyle style = new CssStyle();
    style.specificity = specificity;
    if (result.properties == null) {
      result.properties = new ArrayList<CssStyle>();
    }
    result.properties.add(style);
    
    return style;
  }

  private CssStyleSheet createAttributeSelector(char type, String name, String value) {
    int index = -1;
    if (selectAttributeOperation == null) {
      selectAttributeOperation = new StringBuilder();
      selectAttributeName = new ArrayList<String>();
      selectAttributeValue = new ArrayList<HashMap<String, CssStyleSheet>>();
    } else {
      for (int j = 0; j < selectAttributeOperation.length(); j++) {
        if (selectAttributeOperation.charAt(j) == type
            && selectAttributeName.get(j).equals(name)) {
          index = j;
        }
      }
    }

   if (index == -1) {
      index = selectAttributeOperation.length();
      selectAttributeOperation.append(type);
      selectAttributeName.add(name);
      selectAttributeValue.add(new HashMap<String, CssStyleSheet>());
    }
    return descend(selectAttributeValue.get(index), value);
  }

  /**
   * Returns the style sheet denoted by the given key from the hashtable. If not
   * yet existing, a corresponding entry is created.
   */
  private static CssStyleSheet descend(Map<String, CssStyleSheet> h, String key) {
    CssStyleSheet s = h.get(key);
    if (s == null) {
      s = new CssStyleSheet();
      h.put(key, s);
    }
    return s;
  }

  /**
   * Helper method for collectStyles(). Determines whether the given key is 
   * in the given map. If so, the style search continues in the corresponding 
   * style sheet.
   * 
   * @param element the element under consideration (may be the target element
   *            or any parent)
   * @param map corresponding sub style sheet map
   * @param key element name or attribute value
   * @param queue queue of matching rules to be processed further
   */
  private static void collectStyles(CssStylableElement element, Map<String, CssStyleSheet> map, String key,
                                    List<CssStyle> queue, List<CssStyleSheet> children, List<CssStyleSheet> descendants) {
    if (key == null || map == null) {
      return;
    }
    CssStyleSheet sh = map.get(key);
    if (sh != null) {
      sh.collectStyles(element, queue, children, descendants);
    }
  }

  /**
   * Performs a depth first search of all matching selectors and enqueues the
   * corresponding style information.
   */
  public void collectStyles(CssStylableElement element, List<CssStyle> queue,
                            List<CssStyleSheet> children, List<CssStyleSheet> descendants) {
    
    if (properties != null) {
      // enqueue the style at the current node according to its specificity

      for (int i = 0; i < properties.size(); i++) {
        CssStyle p = properties.get(i);
        int index = queue.size();
        while (index > 0) {
          CssStyle s = queue.get(index - 1);
          if (s.compareSpecificity(p) < 0) {
            break;
          }
          if (s == p) {
            index = -1;
            break;
          }
          index--;
        }
        if (index != -1) {
          queue.add(index, p);
        }
      }
    }
      
    if (selectAttributeOperation != null) {
      for (int i = 0; i < selectAttributeOperation.length(); i++) {
        int type = selectAttributeOperation.charAt(i);
        String name = selectAttributeName.get(i);
        String value = element.getAttribute(name);
        if (value == null) {
          continue;
        }
        HashMap<String, CssStyleSheet> valueMap = selectAttributeValue.get(i);
        if (type == SELECT_ATTRIBUTE_NAME) {
          collectStyles(element, valueMap, "", queue, children, descendants);
        } else if (type == SELECT_ATTRIBUTE_VALUE) {
          collectStyles(element, valueMap, value, queue, children, descendants);
        } else {
          String[] values = Css.split(value,
              type == SELECT_ATTRIBUTE_INCLUDES ? ' ' : ',');
          for (int j = 0; j < values.length; j++) {
            collectStyles(element, valueMap, values[j], queue, children, descendants);
          }
        }
      }
    }

    if (selectElementName != null) {
      collectStyles(element, selectElementName, element.getName(), queue, children, descendants);
    }

    if (selectChild != null) {
      children.add(selectChild);
    }

/*    if (selectPseudoclass != null && element.isLink()) {
      collectStyles(element, selectPseudoclass, "link", queue, children, descendants);
    }*/
    
    if (selectDescendants != null) {
      descendants.add(selectDescendants);
    }
  }

  /**
   * Internal method used to simplify building the default style sheet.
   * 
   * @param selector element name
   * @param style default style for the element
   */
  private void put(String selector, CssStyle style) {
    if (selectElementName == null) {
      selectElementName = new HashMap<String, CssStyleSheet>();
    }
    
    boolean simple = true;
    for (int i = 0; i < selector.length(); i++){
      char c = selector.charAt(i);
      if (c < 'a' || c > 'z') {
        simple = false;
        break;
      }
    }

    if (simple) {
      CssStyleSheet s = new CssStyleSheet();
      s.properties = new ArrayList<CssStyle>();
      s.properties.add(style);
      style.specificity = Css.SPECIFICITY_D - Css.SPECIFICITY_IMPORTANT;
      selectElementName.put(selector, s);
    } else {
      CssTokenizer ct = new CssTokenizer(null, selector + "{");
      CssStyle target = parseSelector(ct);
      target.setFrom(style);
      // copy important
      target.specificity += style.specificity - Css.SPECIFICITY_IMPORTANT;
    }
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    toString("", sb);
    return sb.toString();
  }

  public void toString(String current, StringBuilder sb) {
    if (properties != null) {
      sb.append(current.length() == 0 ? "*" : current);
      sb.append(" {");
      for (int i = 0; i < properties.size(); i++) {
        properties.get(i).toString("", sb);
      }
      sb.append("}\n");
    }

    if (selectElementName != null) {
      for (Map.Entry<String, CssStyleSheet> entry: selectElementName.entrySet()) {
        entry.getValue().toString(entry.getKey() + current, sb);
      }
    }

    if (selectAttributeOperation != null) {
      for (int i = 0; i < selectAttributeOperation.length(); i++) {
        int type = selectAttributeOperation.charAt(i);
        StringBuilder p = new StringBuilder(current);
        p.append('[');
        p.append(selectAttributeName.get(i));

        if (type == SELECT_ATTRIBUTE_NAME) {
          p.append(']');
          selectAttributeValue.get(i).get("").toString(p.toString(), sb);
        } else {
          switch (type) {
            case SELECT_ATTRIBUTE_VALUE:
              p.append('=');
              break;
            case SELECT_ATTRIBUTE_INCLUDES:
              p.append("~=");
              break;
            case SELECT_ATTRIBUTE_DASHMATCH:
              p.append("|=");
              break;
          }
          HashMap<String, CssStyleSheet> valueMap = selectAttributeValue.get(i);
          for (Map.Entry<String, CssStyleSheet> e : valueMap.entrySet()) {
            e.getValue().toString(p.toString() + '"' + e.getKey() + "\"]", sb);
          }
        }
      }
    }

    if (selectDescendants != null) {
      selectDescendants.toString(current + " ", sb);
    }

    if (selectChild != null) {
      selectChild.toString(current + " > ", sb);
    }
  }

  public void apply(CssStylableElement element, URI baseUri) {
    ArrayList<CssStyleSheet> applyAnywhere = new ArrayList<>();
    applyAnywhere.add(this);
    CssStyleSheet.apply(element, baseUri, null, new ArrayList<CssStyleSheet>(), applyAnywhere);
    
  }

  /**
   * Applies the given style sheet to this element and recursively to all child
   * elements, setting the computedStyle field to the computed CSS values.
   * <p>
   * Technically, it builds a queue of applicable styles and then applies them 
   * in the order of ascending specificity. After the style sheet has been 
   * applied, the inheritance rules and finally the style attribute are taken 
   * into account.
   */
  private static void apply(CssStylableElement element, URI baseUri, CssStyle inherit,
                            List<CssStyleSheet> applyHere, List<CssStyleSheet> applyAnywhere) {
    CssStyle style = new CssStyle();

    ArrayList<CssStyle> queue = new ArrayList<>();
    ArrayList<CssStyleSheet> childStyles = new ArrayList<>();
    ArrayList<CssStyleSheet> descendantStyles = new ArrayList<>();
  
    int size = applyHere.size();
    for (int i = 0; i < size; i++) {
      CssStyleSheet styleSheet = applyHere.get(i);
      styleSheet.collectStyles(element, queue, childStyles, descendantStyles);
    }
    size = applyAnywhere.size();
    for (int i = 0; i < size; i++) {
      CssStyleSheet styleSheet = applyAnywhere.get(i);
      descendantStyles.add(styleSheet);
      styleSheet.collectStyles(element, queue, childStyles, descendantStyles);
    }
  
    for (int i = 0; i < queue.size(); i++) {
      style.setFrom((queue.get(i)));
    }
  
    String styleAttr = element.getAttribute("style");
    if (styleAttr != null) {
      style.read(baseUri, styleAttr);
    }
  
    if (inherit != null) {
      style.inherit(inherit);
    }
  
    element.setComputedStyle(style);
    // recurse....
    Iterator<? extends CssStylableElement> iterator = element.getChildElementIterator();
    while(iterator.hasNext()) {
      apply(iterator.next(), baseUri, style, childStyles, descendantStyles);
    }
  }

  /**
   * Helper class to keep track of and resolve imports.
   */
  public static class Dependency {
    private final URI url;
    private final int[] nesting;

    Dependency(URI url, int[] nesting) {
      this.url = url;
      this.nesting = nesting;
    }

    /** 
     * Returns the URL of the nested style sheet to load.
     */
    public URI getUrl() {
      return url;
    }

    /**
     * Returns the nesting positions of the style sheet to load.
     * Used for specificity calculation.
     */
    public int[] getNestingPositions() {
      return nesting;
    }
  }
}