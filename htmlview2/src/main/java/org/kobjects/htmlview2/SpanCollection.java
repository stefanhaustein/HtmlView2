package org.kobjects.htmlview2;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.method.LinkMovementMethod;
import android.text.style.*;
import android.util.Log;
import android.view.View;
import org.kobjects.css.CssEnum;
import org.kobjects.css.CssProperty;
import org.kobjects.css.CssStyleDeclaration;
import org.kobjects.css.CssUnit;

import java.net.URISyntaxException;
import java.util.ArrayList;

class SpanCollection implements ImageTarget {
  Hv2DomElement element;
  BitmapDrawable drawable;
  HtmlTextView htmlTextView;
  int start;
  int end;
  ArrayList<Object> spans = new ArrayList<>();
  SpanCollection previous;

  SpanCollection(Hv2DomElement element, HtmlTextView htmlTextView) {
    this.element = element;
    this.htmlTextView = htmlTextView;
    this.start = htmlTextView.content.length();
  }

  void updateStyle() {
    if (previous != null) {
      previous.updateStyle();
    }
    CssStyleDeclaration parentStyle;

    if (element.parentNode.componentType == Hv2DomContainer.ComponentType.TEXT) {
      parentStyle = ((Hv2DomElement) element.parentNode).computedStyle;
    } else {
      if (htmlTextView.computedStyle == null) {
        htmlTextView.computedStyle = new CssStyleDeclaration();
      }
      parentStyle = htmlTextView.computedStyle;
    }

    for (Object span : spans) {
      htmlTextView.content.removeSpan(span);
    }

    spans.clear();
    if (drawable != null) {
      Bitmap bitmap = drawable.getBitmap();
      float imageWidth = bitmap.getWidth();
      float imageHeight = bitmap.getHeight();
      int cssContentWidth = ((HtmlViewGroup) htmlTextView.getParent()).cssContentWidth;
      if (element.computedStyle.isSet(CssProperty.WIDTH)) {
        imageWidth = element.computedStyle.get(CssProperty.WIDTH, CssUnit.PX, cssContentWidth);
        if (element.computedStyle.isSet((CssProperty.HEIGHT))) {
          imageHeight = element.computedStyle.get(CssProperty.WIDTH, CssUnit.PX, cssContentWidth);
        } else {
          imageHeight *= imageWidth / bitmap.getWidth();
        }
      } else if (element.computedStyle.isSet(CssProperty.HEIGHT)) {
        imageHeight = element.computedStyle.get(CssProperty.HEIGHT, CssUnit.PX, cssContentWidth);
        imageWidth *= imageHeight / bitmap.getHeight();
      }
      drawable.setBounds(0, 0, Math.round(imageWidth * htmlTextView.htmlView.scale),
              Math.round(imageHeight * htmlTextView.htmlView.scale));

      spans.add(new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE));
    }
    String typefaceName = CssConversion.getFontFamilyName(element.computedStyle);
    if (!typefaceName.equals(CssConversion.getFontFamilyName(parentStyle))) {
      spans.add(new TypefaceSpan(typefaceName));
    }
    int typefaceFlags = CssConversion.getTextStyle(element.computedStyle);
    if (typefaceFlags != CssConversion.getTextStyle(parentStyle)) {
      spans.add(new StyleSpan(typefaceFlags));
    }
    int size = htmlTextView.htmlView.getTextSize(element.computedStyle);
    if (size != htmlTextView.htmlView.getTextSize(parentStyle)) {
      spans.add(new AbsoluteSizeSpan(size));
    }
    int color = element.computedStyle.getColor(CssProperty.COLOR);
    if (color != parentStyle.getColor(CssProperty.COLOR)) {
      spans.add(new ForegroundColorSpan(color));
    }
    int backgroundColor = element.computedStyle.getColor(CssProperty.BACKGROUND_COLOR);
    if (backgroundColor != parentStyle.getColor(CssProperty.BACKGROUND_COLOR)) {
      spans.add(new BackgroundColorSpan(backgroundColor));
    }
    CssEnum textDecoration = element.computedStyle.getEnum(CssProperty.TEXT_DECORATION);
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
    CssEnum verticalAlign = element.computedStyle.getEnum(CssProperty.VERTICAL_ALIGN);
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
    if (element.getLocalName().equals("a") && element.getAttribute("href") != null) {
      htmlTextView.setMovementMethod(LinkMovementMethod.getInstance());
      spans.add(new ClickableSpan() {
        @Override
        public void onClick(View widget) {
          try {
            htmlTextView.htmlView.openLink(
                    element, htmlTextView.htmlView.createUri(element.getAttribute("href")));
          } catch (URISyntaxException e) {
            Log.e(HtmlTextView.TAG, "URI Syntax error", e);
          }
        }
      });
    }
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
    drawable = new BitmapDrawable(htmlTextView.htmlView.getContext().getResources(), bitmap);
    if (element.style != null) {
      element.setComputedStyle(element.style);
    }
  }

}
