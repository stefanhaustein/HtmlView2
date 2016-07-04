package org.kobjects.htmlview2;


import android.content.Context;
import android.graphics.Paint;
import android.graphics.Typeface;

import org.kobjects.css.Css;
import org.kobjects.css.CssEnum;
import org.kobjects.css.CssProperty;
import org.kobjects.css.CssStyle;
import org.kobjects.css.CssStyleSheet;
import org.kobjects.css.CssUnit;

import java.net.URI;
import java.net.URISyntaxException;


/** 
 * Context information for a HTML document; holds references to the base URI and the style
 * sheet.
 */
public class PageContext {
  static final int PAINT_MASK = ~(Paint.STRIKE_THRU_TEXT_FLAG | Paint.UNDERLINE_TEXT_FLAG);

  static int getTextStyle(CssStyle style) {
    int flags = 0;
    if (style.get(CssProperty.FONT_WEIGHT, CssUnit.NUMBER) > 600) {
      flags |= Typeface.BOLD;
    }
    if (style.getEnum(CssProperty.FONT_STYLE) == CssEnum.ITALIC) {
      flags |= Typeface.ITALIC;
    }
    return flags;
  }

  static int getPaintFlags(CssStyle style) {
    switch (style.getEnum(CssProperty.TEXT_DECORATION)) {
      case UNDERLINE:
        return Paint.UNDERLINE_TEXT_FLAG;
      case LINE_THROUGH:
        return Paint.STRIKE_THRU_TEXT_FLAG;
      default:
        return 0;
    }
  }

  static String getFontFamilyName(CssStyle style) {
    if (!style.isSet(CssProperty.FONT_FAMILY)) {
      return "";
    }
    String fontFamily = Css.identifierToLowerCase(style.getString(CssProperty.FONT_FAMILY));
    int cut = fontFamily.lastIndexOf(',');
    return fontFamily.substring(cut + 1).trim();
  }


  final CssStyleSheet styleSheet = CssStyleSheet.createDefault();
  final Context context;
  final RequestHandler requestHandler;
  float scale;
  public URI baseUri;

  public PageContext(Context context, RequestHandler requestHandler, URI baseUri) {
    this.context = context;
    this.requestHandler = requestHandler;
    this.baseUri = baseUri;
    scale = context.getResources().getDisplayMetrics().density;
  }

  int getTextSize(CssStyle style) {
    return Math.round(scale * style.get(CssProperty.FONT_SIZE, CssUnit.PX));
  }

  static Typeface getTypeface(CssStyle style) {
    int flags = getTextStyle(style);
    if (!style.isSet(CssProperty.FONT_FAMILY)) {
      return Typeface.defaultFromStyle(flags);
    }
    return Typeface.create(getFontFamilyName(style), flags);
  }

  public Context getContext() {
    return context;
  }

  void setPaint(CssStyle style, Paint paint) {
    paint.setTextSize(getTextSize(style));
    paint.setTypeface(getTypeface(style));
    paint.setFlags((paint.getFlags() & PAINT_MASK) | getPaintFlags(style));
  }

  public URI createUri(String uri) throws URISyntaxException {
    return baseUri.resolve(uri);
  }

  public RequestHandler getRequestHandler() { return requestHandler; }

  public CssStyleSheet getStyleSheet() {
    return styleSheet;
  }

  public URI getBaseUri() {
    return baseUri;
  }
}
