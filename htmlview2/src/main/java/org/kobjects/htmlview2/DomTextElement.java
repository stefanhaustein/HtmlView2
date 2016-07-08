package org.kobjects.htmlview2;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.method.LinkMovementMethod;
import android.text.style.*;
import android.util.Log;
import android.view.View;
import org.kobjects.css.CssEnum;
import org.kobjects.css.CssProperty;
import org.kobjects.css.CssStyle;
import org.kobjects.css.CssUnit;

import java.net.URISyntaxException;
import java.util.ArrayList;


public class DomTextElement extends DomElement implements ImageTarget {
  private HtmlTextView htmlTextView;
  int start;// = htmlTextView.content.length();
  int end;
  DomTextElement parent;
  CssStyle style;
  BitmapDrawable drawable;
  ArrayList<Object> spans = new ArrayList<>();

  DomTextElement(DomDocument ownerDocument, String name) {
    super(ownerDocument, name);
  }


  public void sync(HtmlTextView htmlTextView) {
    this.htmlTextView = htmlTextView;
    start = htmlTextView.content.length();

    if (getName().equals("img")) {
      htmlTextView.appendNormalized("\u2327");
      String src = getAttribute("src");
      if (src != null) {
        try {
          htmlTextView.pageContext.requestHandler.requestImage(this, htmlTextView.pageContext.createUri(src));
        } catch (URISyntaxException e) {
          Log.e(HtmlTextView.TAG, "Error constructing image URL from '" + src + "'", e);
        }
      }
    } else if (getName().equals("br")) {
      htmlTextView.hasLineBreaks = true;
      htmlTextView.appendRaw("\u200b");
      htmlTextView.pendingBreakPosition = htmlTextView.content.length() - 1;
    } else {
      DomNode child = getFirstChild();
      while (child != null) {
        if (child instanceof DomTextNode) {
          htmlTextView.appendNormalized(((DomTextNode) child).text);
        } else {
          ((DomTextElement) child).sync(htmlTextView);
        }
        child = child.getNextSibling();
      }
    }
    end = htmlTextView.content.length();
  }

  @Override
  public void setComputedStyle(CssStyle style) {
    // System.out.println("applyStyle to '" + content.toString().substring(start, end) + "': " + style);
    this.style = style;
    CssStyle parentStyle = parent == null ? ((HtmlViewGroup.LayoutParams) htmlTextView.getLayoutParams()).style() : parent.style;
    for (Object span : spans) {
      htmlTextView.content.removeSpan(span);
    }
    spans.clear();
    if (drawable != null) {
      Bitmap bitmap = drawable.getBitmap();
      float imageWidth = bitmap.getWidth();
      float imageHeight = bitmap.getHeight();
      int cssContentWidth = ((HtmlViewGroup) htmlTextView.getParent()).cssContentWidth;
      if (style.isSet(CssProperty.WIDTH)) {
        imageWidth = style.get(CssProperty.WIDTH, CssUnit.PX, cssContentWidth);
        if (style.isSet((CssProperty.HEIGHT))) {
          imageHeight = style.get(CssProperty.WIDTH, CssUnit.PX, cssContentWidth);
        } else {
          imageHeight *= imageWidth / bitmap.getWidth();
        }
      } else if (style.isSet(CssProperty.HEIGHT)) {
        imageHeight = style.get(CssProperty.HEIGHT, CssUnit.PX, cssContentWidth);
        imageWidth *= imageHeight / bitmap.getHeight();
      }
      drawable.setBounds(0, 0, Math.round(imageWidth * htmlTextView.pageContext.scale),
              Math.round(imageHeight * htmlTextView.pageContext.scale));

      spans.add(new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE));
    }
    int typefaceFlags = CssStyles.getTextStyle(style);
    if (typefaceFlags != CssStyles.getTextStyle(parentStyle)) {
      spans.add(new StyleSpan(typefaceFlags));
    }
    String typefaceName = CssStyles.getFontFamilyName(style);
    if (!typefaceName.equals(CssStyles.getFontFamilyName(parentStyle))) {
      spans.add(new TypefaceSpan(typefaceName));
    }
    int size = htmlTextView.pageContext.getTextSize(style);
    if (size != htmlTextView.pageContext.getTextSize(parentStyle)) {
      spans.add(new AbsoluteSizeSpan(size));
    }
    int color = style.getColor(CssProperty.COLOR);
    if (color != parentStyle.getColor(CssProperty.COLOR)) {
      spans.add(new ForegroundColorSpan(color));
    }
    CssEnum textDecoration = style.getEnum(CssProperty.TEXT_DECORATION);
    if (textDecoration != parentStyle.getEnum(CssProperty.TEXT_DECORATION)) {
      switch (textDecoration) {
        case UNDERLINE:
          spans.add(new UnderlineSpan());
          break;
        case LINE_THROUGH:
          spans.add(new StrikethroughSpan());
          break;
      }
    }
    CssEnum verticalAlign = style.getEnum(CssProperty.VERTICAL_ALIGN);
    if (verticalAlign != parentStyle.getEnum(CssProperty.VERTICAL_ALIGN)) {
      switch (verticalAlign) {
        case SUB:
          spans.add(new SubscriptSpan());
          break;
        case SUPER:
          spans.add(new SuperscriptSpan());
          break;
      }
    }
    if (getName().equals("a") && getAttribute("href") != null) {
      htmlTextView.setMovementMethod(LinkMovementMethod.getInstance());
      spans.add(new ClickableSpan() {
        @Override
        public void onClick(View widget) {
          try {
            htmlTextView.pageContext.requestHandler.openLink(
                    DomTextElement.this, htmlTextView.pageContext.createUri(getAttribute("href")));
          } catch (URISyntaxException e) {
            Log.e(HtmlTextView.TAG, "URI Syntax error", e);
          }
        }
      });
    }
/*
  if (start == 0 && end == length()) {
    setTextSize(params.getScaledWidth(CssProperty.FONT_SIZE) );
  }
*/
    //  content.setSpan(new ForegroundColorSpan(((int) Math.round(Math.random() * 0x0ffffff)) | 0x0ff000000), start, end, 0);
    //  changed = true;

    if (spans.size() > 0) {
      for (Object span : spans) {
        htmlTextView.content.setSpan(span, start, end, 0);
      }
      htmlTextView.setText(htmlTextView.content);
    }
  }

  @Override
  public void setImage(Bitmap bitmap) {
    if (drawable == null) {
      if (htmlTextView.images == null) {
        htmlTextView.images = new ArrayList<>();
      }
      htmlTextView.images.add(this);
    }
    drawable = new BitmapDrawable(htmlTextView.pageContext.getContext().getResources(), bitmap);
    if (style != null) {
      setComputedStyle(style);
    }
  }
}
