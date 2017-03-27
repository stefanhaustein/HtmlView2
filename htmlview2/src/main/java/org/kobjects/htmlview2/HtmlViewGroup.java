package org.kobjects.htmlview2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import org.kobjects.css.CssEnum;
import org.kobjects.css.CssProperty;
import org.kobjects.css.CssStyleDeclaration;
import org.kobjects.css.CssUnit;


public class HtmlViewGroup extends ViewGroup {
  private static final CssStyleDeclaration EMTPY_STYLE = new CssStyleDeclaration();

  private Paint borderPaint = new Paint();
  private Paint bulletPaint;
  private Paint.FontMetrics bulletMetrics;
  // Set in onMeasure
  int cssContentWidth;
  HtmlView htmlView;
  Hv2DomContainer node;

  static final LayoutManager BLOCK_LAYOUT_MANAGER = new BlockLayoutManager();
  static final LayoutManager TABLE_LAYOUT_MANAGER = new TableLayoutManager();

  // Visible for testing
  static final String toLetters(int n, char c0, int base) {
    StringBuilder sb = new StringBuilder();
    do {
      n--; // 1->0=a
      sb.insert(0, (char) (c0 + n % base));
      n /= base;
    } while (n != 0);
    return sb.toString();
  }

  private static final String[] ROMAN_DIGITS = {"M", "CM","D","CD","C", "XC", "L", "XL", "X","IX","V","IV","I"};
  private static final int[] ROMAN_VALUES = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};

  static final String toRoman(int n) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < ROMAN_VALUES.length; i++) {
      while (n % ROMAN_VALUES[i] < n) {
        sb.append(ROMAN_DIGITS[i]);
        n -= ROMAN_VALUES[i];
      }
    }
    return sb.toString();
  }


  public HtmlViewGroup(Context context, HtmlView htmlView, Hv2DomContainer node) {
    super(context);
    this.htmlView = htmlView;
    this.node = node;
    setWillNotDraw(false);
    setClipChildren(false);
  }

  private void drawBackgroud(Canvas canvas, LayoutParams layoutParams,
                             int left, int top, int right, int bottom) {
    Paint bg = layoutParams.getBackgroundPaint();
    if (bg != null) {
      canvas.drawRect(left + 1, top + 1, right, bottom, bg);
    }

    Bitmap img = layoutParams.getBackgroundBitmap();
    if (img != null) {
      CssStyleDeclaration style = layoutParams.style();
      Rect clipBounds = new Rect();
      canvas.getClipBounds(clipBounds);
      canvas.save();
      canvas.clipRect(left + 1, top + 1, right, bottom);
      CssEnum repeat = style.getEnum(CssProperty.BACKGROUND_REPEAT);

      int bgX = layoutParams.getBackgroundOffset(true, right - left, img.getWidth(), repeat);
      int bgY = layoutParams.getBackgroundOffset(
          false, bottom - top, img.getHeight(), repeat);

      canvas.clipRect(left + 1, top + 1, right, bottom);
      if (repeat == CssEnum.REPEAT_Y || repeat == CssEnum.REPEAT) {
        do {
          if (repeat == CssEnum.REPEAT) {
            int currentBgX = bgX;
            do {
              canvas.drawBitmap(img, left + 1 + currentBgX, top + 1 + bgY, null);
              currentBgX += img.getWidth();
            } while (currentBgX < right - left);
          } else {
            canvas.drawBitmap(img, left + 1 + bgX, top + 1 + bgY, null);
          }
          bgY += img.getHeight();
        } while (bgY < bottom - top);
      } else if (repeat == CssEnum.REPEAT_X) {
        do {
          canvas.drawBitmap(img, left + 1 + bgX, top + 1 + bgY, null);
          bgX += img.getWidth();
        } while (bgX < right - left);
      } else {
        canvas.drawBitmap(img, left + 1 + bgX, top + 1 + bgY, null);
      }
      canvas.restore();
    }
  }

  public void drawChildDecoration(Canvas canvas, int index) {
    View child = getChildAt(index);
    if (child instanceof HtmlTextView) {
      return;
    }
    LayoutParams childParams = (LayoutParams) getChildAt(index).getLayoutParams();
    CssStyleDeclaration style = childParams.style();

    int x0 = child.getLeft() - childParams.getPaddingLeft();
    int y0 = child.getTop() - childParams.getPaddingTop();
    int x1 = child.getRight() + childParams.getPaddingRight();
    int y1 = child.getBottom() + childParams.getPaddingBottom();

    int borderLeft = childParams.getBorderLeft();
    int borderRight = childParams.getBorderRight();
    int borderTop = childParams.getBorderTop();
    int borderBottom = childParams.getBorderBottom();

    // Background paint area is specified using 'background-clip' property, and default value of it
    // is 'border-box'
    drawBackgroud(canvas, childParams, x0 - borderLeft, y0 - borderTop,
        x1 + borderRight, y1 + borderBottom);

    if (borderTop > 0) {
      borderPaint.setColor(style.getColor(CssProperty.BORDER_TOP_COLOR));
      int dLeft = (borderLeft << 8) / borderTop;
      int dRight = (borderRight << 8) / borderTop;
      for (int i = 0; i < borderTop; i++) {
        canvas.drawLine(
            x0 - ((i * dLeft) >> 8), y0 - i,
            x1 + ((i * dRight) >> 8), y0 - i, borderPaint);
      }
    }
    if (borderRight > 0) {
      borderPaint.setColor(style.getColor(CssProperty.BORDER_RIGHT_COLOR));
      int dTop = (borderTop << 8) / borderRight;
      int dBottom = (borderBottom << 8) / borderRight;
      for (int i = 0; i < borderRight; i++) {
        canvas.drawLine(
            x1 + i, y0 - ((i * dTop) >> 8),
            x1 + i, y1 + ((i * dBottom) >> 8), borderPaint);
      }
    }
    if (borderBottom > 0) {
      borderPaint.setColor(style.getColor(CssProperty.BORDER_BOTTOM_COLOR));
      int dLeft = (borderLeft << 8) / borderBottom;
      int dRight = (borderRight << 8) / borderBottom;
      for (int i = 0; i < borderBottom; i++) {
        canvas.drawLine(
            x0 - ((i * dLeft) >> 8) + 1, y1 + i,
            x1 + ((i * dRight) >> 8) + 1, y1 + i, borderPaint);
      }
    }
    if (borderLeft > 0) {
      borderPaint.setColor(style.getColor(CssProperty.BORDER_LEFT_COLOR));
      int dTop = (borderTop << 8) / borderLeft;
      int dBottom = (borderBottom << 8) / borderLeft;
      for (int i = 0; i < borderLeft; i++) {
        canvas.drawLine(
            x0 - i, y0 - ((i * dTop) >> 8) + 1,
            x0 - i, y1 + ((i * dBottom) >> 8) + 1, borderPaint);
      }
    }
  }

  @Override
  public LayoutParams generateLayoutParams(AttributeSet attrs) {
    return new LayoutParams();
  }

  @Override
  protected LayoutParams generateDefaultLayoutParams() {
    return new LayoutParams();
  }

  @Override
  protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
    return new LayoutParams();
  }

  LayoutParams getChildLayoutParams(int i) {
    return (LayoutParams) getChildAt(i).getLayoutParams();
  }

  CssStyleDeclaration getStyle() {
    ViewGroup.LayoutParams params = getLayoutParams();
    return (params instanceof LayoutParams) ? ((LayoutParams) params).style() : EMTPY_STYLE;
  }


  @Override
  public void onDraw(Canvas canvas) {
    CssEnum listStyleType = getStyle().getEnum(CssProperty.LIST_STYLE_TYPE);
    int listIndex = 1;
    for (int i = 0; i < getChildCount(); i++) {
      drawChildDecoration(canvas, i);
      CssStyleDeclaration childStyle = getChildLayoutParams(i).style();
      if (childStyle.getEnum(CssProperty.DISPLAY) == CssEnum.LIST_ITEM &&
          listStyleType != CssEnum.NONE) {
        String bullet;
        switch (listStyleType) {
          case DECIMAL:
            bullet = String.valueOf(listIndex) + ". ";
            break;
          case LOWER_LATIN:
            bullet = toLetters(listIndex, 'a', 26) + ". ";
            break;
          case LOWER_GREEK:
            bullet = toLetters(listIndex, '\u03b1', 25) + ". ";
            break;
          case LOWER_ROMAN:
            bullet = toRoman(listIndex) + ". ";
            break;
          case UPPER_LATIN:
            bullet = toLetters(listIndex, 'a', 26) + ". ";
            break;
          case UPPER_ROMAN:
            bullet = toRoman(listIndex).toUpperCase() + ". ";
            break;
          case SQUARE:
            bullet = "\u25aa ";
            break;
          default:
            bullet = "\u2022 ";
        }
        listIndex++;
        if (bulletPaint == null) {
          bulletPaint = new Paint();
          bulletMetrics = new Paint.FontMetrics();
          //Paint.Style.FILL);
        }
        htmlView.setPaint(childStyle, bulletPaint);
        bulletPaint.getFontMetrics(bulletMetrics);
        canvas.drawText(bullet, -bulletPaint.measureText(bullet), getChildLayoutParams(i).measuredY - bulletMetrics.top, bulletPaint);
      }
    }
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    for (int i = 0; i < getChildCount(); i++) {
      View childView = getChildAt(i);
      LayoutParams childLayoutParams = getChildLayoutParams(i);
      childView.layout(
          childLayoutParams.measuredX,
          childLayoutParams.measuredY,
          childLayoutParams.measuredX + childView.getMeasuredWidth(),
          childLayoutParams.measuredY + childView.getMeasuredHeight());
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    if (node.syncState != Hv2DomContainer.SyncState.SYNCED) {
      TreeSync.syncContainer(this, node, true);
    }

    int width = MeasureSpec.getSize(widthMeasureSpec);

    cssContentWidth = Math.round(width / htmlView.scale);

    // "Regular" children

    CssEnum display = getStyle().getEnum(CssProperty.DISPLAY);
    LayoutManager layoutManager = display == CssEnum.TABLE
        ? TABLE_LAYOUT_MANAGER :BLOCK_LAYOUT_MANAGER;
    layoutManager.onMeasure(this, widthMeasureSpec, heightMeasureSpec);

    // Absolute positioning

    HtmlViewGroup container = null;
    int dx = 0;
    int dy = 0;
    for (int i = 0; i < getChildCount(); i++) {
      LayoutParams childParams = getChildLayoutParams(i);
      CssStyleDeclaration childStyle = childParams.style();
      if (!childStyle.isBlock() ||
          childStyle.getEnum(CssProperty.POSITION) != CssEnum.ABSOLUTE) {
        continue;
      }
      if (container == null) {
        container = this;
        while (container.getParent() instanceof HtmlViewGroup) {
          if (container.getStyle().isBlock()) {
            CssEnum containerDisplay = container.getStyle().getEnum(CssProperty.DISPLAY);
            if (containerDisplay == CssEnum.ABSOLUTE || containerDisplay == CssEnum.RELATIVE) {
              break;
            }
          }
          dx += ((LayoutParams) container.getLayoutParams()).measuredX;
          dy += ((LayoutParams) container.getLayoutParams()).measuredY;
          container = (HtmlViewGroup) container.getParent();
        }
      }
      onMeasureAbsolute(container, i, dx, dy);
    }
  }

  void onMeasureAbsolute(HtmlViewGroup container, int i, int dx, int dy) {
    View child = getChildAt(i);
    HtmlViewGroup.LayoutParams childParams = (HtmlViewGroup.LayoutParams) child.getLayoutParams();
    CssStyleDeclaration childStyle = childParams.style();

    int containerWidth = Math.round(container.cssContentWidth * htmlView.scale);

    int childLeft = childParams.getMarginLeft() + childParams.getBorderLeft() + childParams.getPaddingLeft();
    int childRight = childParams.getMarginRight() + childParams.getBorderRight() + childParams.getPaddingRight();
    int childTop = childParams.getBorderTop() + childParams.getPaddingTop();
    int childBottom = childParams.getBorderBottom() + childParams.getPaddingBottom();
    int maxChildContentWidth = Math.max(Math.round(containerWidth) - childLeft - childRight, 0);

    if (childStyle.isSet(CssProperty.WIDTH)) {
      child.measure(View.MeasureSpec.EXACTLY | childParams.getScaledWidth(CssProperty.WIDTH),
          View.MeasureSpec.UNSPECIFIED);
    } else {
      if (childStyle.isSet(CssProperty.MAX_WIDTH)) {
        maxChildContentWidth = Math.min(maxChildContentWidth, childParams.getScaledWidth(CssProperty.MAX_WIDTH));
      }
      child.measure(MeasureSpec.AT_MOST | maxChildContentWidth, View.MeasureSpec.UNSPECIFIED);
    }

    int measuredX;
    if (childStyle.isSet(CssProperty.LEFT)) {
      measuredX = childLeft + Math.round(htmlView.scale * childStyle.get(CssProperty.LEFT, CssUnit.PX, container.cssContentWidth));
    } else if (childStyle.isSet(CssProperty.RIGHT)) {
      measuredX = containerWidth - child.getMeasuredWidth() - childRight - Math.round(htmlView.scale * (childStyle.get(CssProperty.RIGHT, CssUnit.PX, container.cssContentWidth)));
    } else {
      measuredX = childLeft;
    }

    int measuredY;
    if (childStyle.isSet(CssProperty.TOP)) {
      measuredY = childTop + Math.round(htmlView.scale * childStyle.get(CssProperty.TOP, CssUnit.PX, container.cssContentWidth));
  //  } else if (childParams.computedStyle.isSet(CssProperty.BOTTOM)) {
      // TODO
      // measuredY = container.getMeasuredHeight() - child.getMeasuredHeight() - childBottom - Math.round(htmlContext.scale * (childParams.computedStyle.get(CssProperty.BOTTOM, CssUnit.PX, container.cssContentWidth)));
    } else {
      measuredY = childTop;
    }

    childParams.setMeasuredPosition(measuredX - dx, measuredY - dy);
  }


  /**
   * Called from the layout manager.
   */
  void setMeasuredSize(int measuredWidth, int measuredHeight) {
    setMeasuredDimension(measuredWidth, measuredHeight);
  }

  public boolean isRegularLayout(int i) {
    CssStyleDeclaration style = getChildLayoutParams(i).style();
    return style.getEnum(CssProperty.DISPLAY) != CssEnum.NONE &&
        (!style.isBlock() || style.getEnum(CssProperty.POSITION) != CssEnum.ABSOLUTE);
  }

  public class LayoutParams extends ViewGroup.LayoutParams {
    int measuredX;
    int measuredY;

    // null for HtmlTextView
    public Hv2DomElement element;

    public CssStyleDeclaration style() {
      return element != null ? element.computedStyle : EMTPY_STYLE;
    }

   // public CssStyleDeclaration computedStyle = new CssStyleDeclaration();
    private Paint backgroundCache;

    public LayoutParams() {
      super(MATCH_PARENT, WRAP_CONTENT);
    }

    public int getScaledWidth(CssProperty property) {
      return Math.round(style().get(property, CssUnit.PX, cssContentWidth) * htmlView.scale);
    }

    public int getBorderTop() {
      return style().getEnum(CssProperty.BORDER_TOP_STYLE) == CssEnum.NONE
          ? 0 : getScaledWidth(CssProperty.BORDER_TOP_WIDTH);
    }
    public int getBorderRight() {
      return style().getEnum(CssProperty.BORDER_RIGHT_STYLE) == CssEnum.NONE
          ? 0 : getScaledWidth(CssProperty.BORDER_RIGHT_WIDTH);
    }
    public int getBorderBottom() {
      return style().getEnum(CssProperty.BORDER_BOTTOM_STYLE) == CssEnum.NONE
          ? 0 : getScaledWidth(CssProperty.BORDER_BOTTOM_WIDTH);
    }
    public int getBorderLeft() {
      return style().getEnum(CssProperty.BORDER_LEFT_STYLE) == CssEnum.NONE
          ? 0 : getScaledWidth(CssProperty.BORDER_LEFT_WIDTH);
    }

    public int getMarginTop() {
      return getScaledWidth(CssProperty.MARGIN_TOP);
    }
    public int getMarginBottom() {
      return getScaledWidth(CssProperty.MARGIN_BOTTOM);
    }

    public int getMarginLeft() {
      return computeMargin(CssProperty.MARGIN_LEFT, CssProperty.MARGIN_RIGHT);
    }

    public int getMarginRight() {
      return computeMargin(CssProperty.MARGIN_RIGHT, CssProperty.MARGIN_LEFT);
    }

    public int computeMargin(CssProperty marginToCompute, CssProperty oppositeMargin) {
      if (style().getEnum(CssProperty.DISPLAY) != CssEnum.BLOCK) {
        return 0;
      }
      int margin = getScaledWidth(marginToCompute);
      if (!style().isLengthFixedOrPercent(CssProperty.WIDTH)) {
        return margin;
      }

      int availableWidthForMargins = ((int) (cssContentWidth * htmlView.scale))
          - getScaledWidth(CssProperty.WIDTH) - getPaddingLeft() - getPaddingRight()
          - getBorderLeft() - getBorderRight();

      if (style().getEnum(marginToCompute) == CssEnum.AUTO) {
        if (style().getEnum(oppositeMargin) == CssEnum.AUTO) {
          margin = availableWidthForMargins / 2;
        } else {
          margin = availableWidthForMargins - getScaledWidth(oppositeMargin);
        }
      }

      return margin;
    }


    public int getPaddingTop() {
      return getScaledWidth(CssProperty.PADDING_TOP);
    }
    public int getPaddingRight() {
      return getScaledWidth(CssProperty.PADDING_RIGHT);
    }
    public int getPaddingBottom() {
      return getScaledWidth(CssProperty.PADDING_BOTTOM);
    }
    public int getPaddingLeft() {
      return getScaledWidth(CssProperty.PADDING_LEFT);
    }

    public void setMeasuredPosition(int x, int y) {
      measuredX = x;
      measuredY = y;
    }


    /**
     * Returns background paint object for given element.
     */
    public Paint getBackgroundPaint() {
      if (backgroundCache == null) {
        int color = style().getColor(CssProperty.BACKGROUND_COLOR);
        if ((color & 0x0ff000000) != 0) {
          backgroundCache = new Paint();
          backgroundCache.setStyle(android.graphics.Paint.Style.FILL);
          backgroundCache.setColor(color);
        }
      }
      return backgroundCache;
    }

    private Bitmap getBackgroundBitmap() {
      Bitmap img = null;


      if (style().isSet(CssProperty.BACKGROUND_IMAGE)) {
        String backgroundImage = style().getString(CssProperty.BACKGROUND_IMAGE);
        Uri backgroundImageUri = Uri.parse(backgroundImage);
/*
        if (ImageUtil.isDataUri(backgroundImageUri)) {
          img = decodeBase64Image(backgroundImageUri);
        } else {
          // TODO: Fetch image from network.
          img = null;
        }
        */
      }
      return img;
    }


    /**
     * Computes background position offset for given direction based on specified inputs.
     *
     * @param isHorizontalOffset If true we compute horizontal offset, else vertical offset.
     * @param containerSize border box size in {@code dp} of the container that has this element.
     * @param imageSize size in {@code dp} of the background image.
     * @param cssRepeatPropertyValue value of the repeat property.
     */
    private int getBackgroundOffset(boolean isHorizontalOffset,
                                    int containerSize, int imageSize,
                                    CssEnum cssRepeatPropertyValue) {
      // Background positioning area is specified using 'background-origin' property, and the
      // default value of it is 'padding-box'
      CssProperty cssPositionProperty;
      CssEnum cssRepeatDirection;
      int borderOffset;
      int borderWidth;

      if (isHorizontalOffset) {
        cssPositionProperty = CssProperty.BACKGROUND_POSITION_X;
        cssRepeatDirection = CssEnum.REPEAT_X;
        borderOffset = getBorderLeft();
        borderWidth = getBorderLeft() + getBorderRight();
      } else {
        cssPositionProperty = CssProperty.BACKGROUND_POSITION_Y;
        cssRepeatDirection = CssEnum.REPEAT_Y;
        borderOffset = getBorderTop();
        borderWidth = getBorderTop() + getBorderBottom();
      }

      int backgroundOffset = borderOffset + style().getBackgroundReferencePoint(
          cssPositionProperty, containerSize - borderWidth, imageSize);

      if (cssRepeatPropertyValue == cssRepeatDirection
          || cssRepeatPropertyValue == CssEnum.REPEAT) {
        backgroundOffset = (backgroundOffset > 0 ? backgroundOffset % imageSize : backgroundOffset)
            - imageSize;
      }

      return backgroundOffset;
    }

  }

}
