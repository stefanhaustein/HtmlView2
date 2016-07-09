package org.kobjects.htmlview2;

import android.text.SpannableStringBuilder;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

import org.kobjects.css.CssProperty;
import org.kobjects.css.CssStyle;
import org.kobjects.css.CssUnit;

import java.util.ArrayList;


public class HtmlTextView extends TextView {
  static final String TAG = "HtmlTextView";

  CssStyle style;
  SpannableStringBuilder content = new SpannableStringBuilder("");
  HtmlView htmlView;
  ArrayList<HvTextElement> images;
  boolean hasLineBreaks = false;
  int pendingBreakPosition = -1;

  public HtmlTextView(HtmlView pageContext) {
    super(pageContext.getContext());
   // this.setSingleLine(false);
    this.htmlView = pageContext;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    if (images != null) {
      for(HvTextElement image: images) {
        image.setComputedStyle(image.style);
      }
    }
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }

  void appendNormalized(String text) {
    // System.out.println("AppendText '" + text + "' to '" + getText() + "'");

    if (pendingBreakPosition != -1) {
      content.replace(pendingBreakPosition, pendingBreakPosition + 1, "\n");
      pendingBreakPosition = -1;
    }

    StringBuilder sb = new StringBuilder();
    boolean wasSpace = sb.length() == 0 || sb.charAt(sb.length() - 1) <= ' ';
    for (int i = 0; i < text.length(); i++) {
      char c = text.charAt(i);
      if (c <= ' ') {
        if (!wasSpace) {
          sb.append(' ');
          wasSpace = true;
        }
      } else {
        sb.append(c);
        wasSpace = false;
      }
    }
    content.append(sb);
    setText(content);
  }

  void appendRaw(String text) {
    if (pendingBreakPosition != -1) {
      content.replace(pendingBreakPosition, pendingBreakPosition + 1, "\n");
      pendingBreakPosition = -1;
    }
    // System.out.println("AppendText '" + text + "' to '" + getText() + "'");
    content.append(text);
    setText(content);
  }


  public void setComputedStyle(CssStyle style) {
    this.style = style;
    // System.out.println("applyRootStyle to '" + content + "': " + style);
    float scale = htmlView.scale;
    setTextSize(TypedValue.COMPLEX_UNIT_PX, style.get(CssProperty.FONT_SIZE, CssUnit.PX) * scale);
    setTextColor(style.getColor(CssProperty.COLOR));
    setTypeface(CssStyles.getTypeface(style), CssStyles.getTextStyle(style));
    setPaintFlags((getPaintFlags() & HtmlView.PAINT_MASK) | CssStyles.getPaintFlags(style));
    switch (style.getEnum(CssProperty.TEXT_ALIGN)) {
      case RIGHT:
        setGravity(Gravity.RIGHT);
        break;
      case CENTER:
        setGravity(Gravity.CENTER);
        break;
      default:
        setGravity(Gravity.LEFT);
        break;
    }
  }


}
