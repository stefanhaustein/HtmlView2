package org.kobjects.htmllayout;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.SubscriptSpan;
import android.text.style.SuperscriptSpan;
import android.text.style.TypefaceSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;

import org.kobjects.css.CssEnum;
import org.kobjects.css.CssProperty;
import org.kobjects.css.CssStyle;
import org.kobjects.css.CssUnit;

import java.net.URISyntaxException;
import java.util.ArrayList;


public class HtmlTextView extends TextView {
  private static final String TAG = "HtmlTextView";

  CssStyle style;
  SpannableStringBuilder content = new SpannableStringBuilder("");
  PageContext pageContext;
  ArrayList<TextElement> images;
  boolean hasLineBreaks = false;
  int pendingBreakPosition = -1;

  public HtmlTextView(PageContext pageContext) {
    super(pageContext.context);
   // this.setSingleLine(false);
    this.pageContext = pageContext;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    if (images != null) {
      for(TextElement image: images) {
        image.setComputedStyle(image.style);
      }
    }
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
  }

  public TextElement addElement(HtmlElement logicalContainer, String name) {
    TextElement element = new TextElement(name);
    logicalContainer.add(element);
    return element;
  }

  public void appendNormalized(String text) {
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

  public void appendRaw(String text) {
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
    float scale = pageContext.scale;
    setTextSize(TypedValue.COMPLEX_UNIT_PX, style.get(CssProperty.FONT_SIZE, CssUnit.PX) * scale);
    setTextColor(style.getColor(CssProperty.COLOR));
    setTypeface(PageContext.getTypeface(style), PageContext.getTextStyle(style));
    setPaintFlags((getPaintFlags() & PageContext.PAINT_MASK) | PageContext.getPaintFlags(style));
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


  public class TextElement extends NonViewElement implements ImageTarget {
    int start = content.length();
    int end;
    TextElement parent;
    CssStyle style;
    BitmapDrawable drawable;
    ArrayList<Object> spans = new ArrayList<>();

    TextElement(String name) {
      super(name);
    }

    public TextElement addChild(String name) {
      TextElement child = new TextElement(name);
      children.add(child);
      return child;
    }

    public void appendNormalized(String text) {
      HtmlTextView.this.appendNormalized(text);
    }

    public void appendRaw(String text) {
      HtmlTextView.this.appendRaw(text);
    }

    public void end() {
      if (getName().equals("img")) {
        appendNormalized("\u2327");
        String src = getAttributeValue("src");
        if (src != null) {
          try {
            pageContext.requestHandler.requestImage(this, pageContext.createUri(src));
          } catch (URISyntaxException e) {
            Log.e(TAG, "Error constructing image URL from '" + src + "'", e);
          }
        }
      } else if (getName().equals("br")) {
        hasLineBreaks = true;
        appendRaw("\u200b");
        pendingBreakPosition = content.length() - 1;
      }
      end = content.length();
    }

    @Override
    public void setComputedStyle(CssStyle style) {
      // System.out.println("applyStyle to '" + content.toString().substring(start, end) + "': " + style);
      this.style = style;
      CssStyle parentStyle = parent == null ? ((HtmlLayout.LayoutParams) getLayoutParams()).style : parent.style;
      for (Object span : spans) {
        content.removeSpan(span);
      }
      spans.clear();
      if (drawable != null) {
        Bitmap bitmap = drawable.getBitmap();
        float imageWidth = bitmap.getWidth();
        float imageHeight = bitmap.getHeight();
        int cssContentWidth = ((HtmlLayout) getParent()).cssContentWidth;
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
        drawable.setBounds(0, 0, Math.round(imageWidth * pageContext.scale),
            Math.round(imageHeight * pageContext.scale));

        spans.add(new ImageSpan(drawable, ImageSpan.ALIGN_BASELINE));
      }
      int typefaceFlags = PageContext.getTextStyle(style);
      if (typefaceFlags != PageContext.getTextStyle(parentStyle)) {
        spans.add(new StyleSpan(typefaceFlags));
      }
      String typefaceName = PageContext.getFontFamilyName(style);
      if (!typefaceName.equals(PageContext.getFontFamilyName(parentStyle))) {
        spans.add(new TypefaceSpan(typefaceName));
      }
      int size = pageContext.getTextSize(style);
      if (size != pageContext.getTextSize(parentStyle)) {
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
      if (getName().equals("a") && getAttributeValue("href") != null) {
        setMovementMethod(LinkMovementMethod.getInstance());
        spans.add(new ClickableSpan() {
          @Override
          public void onClick(View widget) {
            try {
              pageContext.requestHandler.openLink(
                  TextElement.this, pageContext.createUri(getAttributeValue("href")));
            } catch (URISyntaxException e) {
              Log.e(TAG, "URI Syntax error", e);
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
          content.setSpan(span, start, end, 0);
        }
        setText(content);
      }
    }

    @Override
    public void setImage(Bitmap bitmap) {
      if (drawable == null) {
        if (images == null) {
          images = new ArrayList<>();
        }
        images.add(this);
      }
      drawable = new BitmapDrawable(pageContext.context.getResources(), bitmap);
      if (style != null) {
        setComputedStyle(style);
      }
    }
  }
}
