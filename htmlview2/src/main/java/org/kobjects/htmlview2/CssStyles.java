package org.kobjects.htmlview2;

import android.graphics.Paint;
import android.graphics.Typeface;
import org.kobjects.css.*;


public class CssStyles {
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

    static Typeface getTypeface(CssStyle style) {
      int flags = getTextStyle(style);
      if (!style.isSet(CssProperty.FONT_FAMILY)) {
        return Typeface.defaultFromStyle(flags);
      }
      return Typeface.create(getFontFamilyName(style), flags);
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
}
