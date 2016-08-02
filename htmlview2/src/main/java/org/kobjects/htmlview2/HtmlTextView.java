package org.kobjects.htmlview2;

import android.text.SpannableStringBuilder;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;

import org.kobjects.css.CssProperty;
import org.kobjects.css.CssStyleDeclaration;
import org.kobjects.css.CssUnit;

import java.util.ArrayList;


public class HtmlTextView extends TextView {
  static final String TAG = "HtmlTextView";

  CssStyleDeclaration computedStyle;
  SpannableStringBuilder content = new SpannableStringBuilder("");
  HtmlView htmlView;
  ArrayList<SpanCollection> images;
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
      for(SpanCollection image: images) {
        image.updateStyle();
      }
    }
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }

  /*
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
  }*/

  void append(String text) {
    if (pendingBreakPosition != -1) {
      content.replace(pendingBreakPosition, pendingBreakPosition + 1, "\n");
      pendingBreakPosition = -1;
    }
    // System.out.println("AppendText '" + text + "' to '" + getText() + "'");
    content.append(text);
    setText(content);
  }


  public void setComputedStyle(CssStyleDeclaration computedStyle) {
    this.computedStyle = computedStyle;
    // System.out.println("applyRootStyle to '" + content + "': " + computedStyle);
    float scale = htmlView.scale;
    setTextSize(TypedValue.COMPLEX_UNIT_PX, this.computedStyle.get(CssProperty.FONT_SIZE, CssUnit.PX) * scale);
    setTextColor(this.computedStyle.getColor(CssProperty.COLOR));
    setTypeface(CssConversion.getTypeface(this.computedStyle)); // , CssConversion.getTextStyle(this.computedStyle));
    setPaintFlags((getPaintFlags() & HtmlView.PAINT_MASK) | CssConversion.getPaintFlags(this.computedStyle));
    switch (this.computedStyle.getEnum(CssProperty.TEXT_ALIGN)) {
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
