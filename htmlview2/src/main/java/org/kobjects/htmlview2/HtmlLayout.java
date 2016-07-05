package org.kobjects.htmlview2;

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
import org.kobjects.css.CssStylableElement;
import org.kobjects.css.CssStyle;
import org.kobjects.css.CssUnit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;


public class HtmlLayout extends ViewGroup {
  private static final CssStyle EMTPY_STYLE = new CssStyle();

  private Paint borderPaint = new Paint();
  private Paint bulletPaint;
  private Paint.FontMetrics bulletMetrics;
  // Set in onMeasure
  int cssContentWidth;
  LayoutManager layoutManager;
  PageContext pageContext;

  static final LayoutManager BLOCK_LAYOUT_MANAGER = new BlockLayoutManager();
  static final LayoutManager TABLE_LAYOUT_MANAGER = new TableLayoutManager();

  public HtmlLayout(PageContext context) {
    super(context.context);
    this.pageContext = context;
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
      CssStyle style = layoutParams.style();
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
    CssStyle style = childParams.style();

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

  CssStyle getStyle() {
    ViewGroup.LayoutParams params = getLayoutParams();
    return (params instanceof LayoutParams) ? ((LayoutParams) params).style() : EMTPY_STYLE;
  }


  @Override
  public void onDraw(Canvas canvas) {
    CssEnum listStyleType = getStyle().getEnum(CssProperty.LIST_STYLE_TYPE);
    int listIndex = 1;
    for (int i = 0; i < getChildCount(); i++) {
      drawChildDecoration(canvas, i);
      CssStyle childStyle = getChildLayoutParams(i).style();
      if (childStyle.getEnum(CssProperty.DISPLAY) == CssEnum.LIST_ITEM &&
          listStyleType != CssEnum.NONE) {
        String bullet;
        switch (listStyleType) {
          case DECIMAL:
            bullet = String.valueOf(listIndex++) + ". ";
            break;
          case SQUARE:
            bullet = "\u25aa ";
            break;
          default:
            bullet = "\u2022 ";
        }
        if (bulletPaint == null) {
          bulletPaint = new Paint();
          bulletMetrics = new Paint.FontMetrics();
          //Paint.Style.FILL);
        }
        pageContext.setPaint(childStyle, bulletPaint);
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
    int width = MeasureSpec.getSize(widthMeasureSpec);

    cssContentWidth = Math.round(width / pageContext.scale);

    // "Regular" children

    CssEnum display = getStyle().getEnum(CssProperty.DISPLAY);
    LayoutManager layoutManager = display == CssEnum.TABLE
        ? TABLE_LAYOUT_MANAGER :BLOCK_LAYOUT_MANAGER;
    layoutManager.onMeasure(this, widthMeasureSpec, heightMeasureSpec);

    // Absolute positioning

    HtmlLayout container = null;
    int dx = 0;
    int dy = 0;
    for (int i = 0; i < getChildCount(); i++) {
      LayoutParams childParams = getChildLayoutParams(i);
      CssStyle childStyle = childParams.style();
      if (!childStyle.isBlock() ||
          childStyle.getEnum(CssProperty.POSITION) != CssEnum.ABSOLUTE) {
        continue;
      }
      if (container == null) {
        container = this;
        while (container.getParent() instanceof HtmlLayout) {
          if (container.getStyle().isBlock()) {
            CssEnum containerDisplay = container.getStyle().getEnum(CssProperty.DISPLAY);
            if (containerDisplay == CssEnum.ABSOLUTE || containerDisplay == CssEnum.RELATIVE) {
              break;
            }
          }
          dx += ((LayoutParams) container.getLayoutParams()).measuredX;
          dy += ((LayoutParams) container.getLayoutParams()).measuredY;
          container = (HtmlLayout) container.getParent();
        }
      }
      onMeasureAbsolute(container, i, dx, dy);
    }
  }

  void onMeasureAbsolute(HtmlLayout container, int i, int dx, int dy) {
    View child = getChildAt(i);
    HtmlLayout.LayoutParams childParams = (HtmlLayout.LayoutParams) child.getLayoutParams();
    CssStyle childStyle = childParams.style();

    int containerWidth = Math.round(container.cssContentWidth * pageContext.scale);

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
      measuredX = childLeft + Math.round(pageContext.scale * childStyle.get(CssProperty.LEFT, CssUnit.PX, container.cssContentWidth));
    } else if (childStyle.isSet(CssProperty.RIGHT)) {
      measuredX = containerWidth - child.getMeasuredWidth() - childRight - Math.round(pageContext.scale * (childStyle.get(CssProperty.RIGHT, CssUnit.PX, container.cssContentWidth)));
    } else {
      measuredX = childLeft;
    }

    int measuredY;
    if (childStyle.isSet(CssProperty.TOP)) {
      measuredY = childTop + Math.round(pageContext.scale * childStyle.get(CssProperty.TOP, CssUnit.PX, container.cssContentWidth));
  //  } else if (childParams.style.isSet(CssProperty.BOTTOM)) {
      // TODO
      // measuredY = container.getMeasuredHeight() - child.getMeasuredHeight() - childBottom - Math.round(htmlContext.scale * (childParams.style.get(CssProperty.BOTTOM, CssUnit.PX, container.cssContentWidth)));
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
    CssStyle style = getChildLayoutParams(i).style();
    return style.getEnum(CssProperty.DISPLAY) != CssEnum.NONE &&
        (!style.isBlock() || style.getEnum(CssProperty.POSITION) != CssEnum.ABSOLUTE);
  }

  public class LayoutParams extends ViewGroup.LayoutParams {
    int measuredX;
    int measuredY;

    // null for HtmlTextView
    public ViewElement element;

    public CssStyle style() {
      return element != null ? element.style : EMTPY_STYLE;
    }

   // public CssStyle style = new CssStyle();
    private Paint backgroundCache;

    public LayoutParams() {
      super(MATCH_PARENT, WRAP_CONTENT);
    }

    public int getScaledWidth(CssProperty property) {
      return Math.round(style().get(property, CssUnit.PX, cssContentWidth) * pageContext.scale);
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

      int availableWidthForMargins = ((int) (cssContentWidth * pageContext.scale))
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
