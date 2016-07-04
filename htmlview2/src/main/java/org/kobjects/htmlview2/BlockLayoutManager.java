package org.kobjects.htmlview2;

import android.view.View;

import org.kobjects.css.CssProperty;
import org.kobjects.css.CssEnum;

public class BlockLayoutManager implements LayoutManager {

  void adjustLastLine(HtmlLayout htmlLayout, int firstChildIndex, int to, int usedSpace, int availableSpace) {
    if (!(htmlLayout.getLayoutParams() instanceof HtmlLayout.LayoutParams)) {
      return;
    }
    HtmlLayout.LayoutParams params = (HtmlLayout.LayoutParams) htmlLayout.getLayoutParams();
    CssEnum align = params.style.getEnum(CssProperty.TEXT_ALIGN);
    int addOffset = 0;
    if (align == CssEnum.RIGHT) {
      addOffset = availableSpace - usedSpace;
    } else if (align == CssEnum.CENTER) {
      addOffset = (availableSpace - usedSpace) / 2;
    }
    if (addOffset == 0) {
      return;
    }
    for (int i = firstChildIndex; i < to; i++) {
      htmlLayout.getChildLayoutParams(i).measuredX += addOffset;
    }
  }

  @Override
  public void onMeasure(HtmlLayout htmlLayout, int widthMeasureSpec, int heightMeasureSpec) {
    int width = View.MeasureSpec.getSize(widthMeasureSpec);
    int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
    int height = View.MeasureSpec.getSize(heightMeasureSpec);
    int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);

    if (width == 0) {
      // TODO: Is that true even for absolute positioning?
      throw new RuntimeException("HTML layout requires a maximum width.");
    }
    int currentX = 0;
    int currentY = 0;
    int currentLineHeight = 0;
    int pendingMargin = 0;
    int firstChildIndex = 0;
    int maxWidth = 0;

    for (int i = 0; i < htmlLayout.getChildCount(); i++) {
      if (!htmlLayout.isRegularLayout(i)) {
        continue;
      }

      View child = htmlLayout.getChildAt(i);
      HtmlLayout.LayoutParams childParams = (HtmlLayout.LayoutParams) child.getLayoutParams();

      int childLeft = childParams.getMarginLeft() + childParams.getBorderLeft() + childParams.getPaddingLeft();
      int childRight = childParams.getMarginRight() + childParams.getBorderRight() + childParams.getPaddingRight();
      int childTop = childParams.getBorderTop() + childParams.getPaddingTop();
      int childBottom = childParams.getBorderBottom() + childParams.getPaddingBottom();
      int maxChildContentWidth = Math.max(width - childLeft - childRight, 0);

      int measuredX;
      int measuredY;

      if (childParams.style.isBlock() ||
          (child instanceof HtmlTextView && ((HtmlTextView) child).hasLineBreaks)) {
        if (currentX > 0) {
          adjustLastLine(htmlLayout, firstChildIndex, i, currentX, width);
          currentY += currentLineHeight;
          currentX = 0;
          currentLineHeight = 0;
        }
        currentY += Math.max(pendingMargin, childParams.getMarginTop());

        if (childParams.style.isSet(CssProperty.WIDTH)) {
          child.measure(View.MeasureSpec.EXACTLY | childParams.getScaledWidth(CssProperty.WIDTH),
              View.MeasureSpec.UNSPECIFIED);
        } else {
          if (childParams.style.isSet(CssProperty.MAX_WIDTH)) {
            maxChildContentWidth = Math.min(maxChildContentWidth, childParams.getScaledWidth(CssProperty.MAX_WIDTH));
          }
          child.measure(widthMode | maxChildContentWidth, View.MeasureSpec.UNSPECIFIED);
        }

        measuredX = currentX + childLeft;
        measuredY = currentY + childTop;

        currentY += childTop + child.getMeasuredHeight() + childBottom;
        pendingMargin = childParams.getMarginBottom();
      } else {
        if (currentX == 0) {
          firstChildIndex = i;
          currentY += pendingMargin;
          pendingMargin = 0;
        }
        child.measure(View.MeasureSpec.AT_MOST | maxChildContentWidth, View.MeasureSpec.UNSPECIFIED);
        int childWidth = childLeft + child.getMeasuredWidth() + childRight;
        int childHeight = childTop + child.getMeasuredHeight() + childBottom;
        if (currentX > 0 && currentX + childWidth > width) {
          adjustLastLine(htmlLayout, firstChildIndex, i, currentX, width);
          currentY += currentLineHeight;
          currentX = 0;
        }
        measuredX = currentX + childLeft;
        measuredY = currentY + childTop;
        currentLineHeight = Math.max(currentLineHeight, childHeight);
        currentX += childWidth;
      }

      if (childParams.style.getEnum(CssProperty.POSITION) == CssEnum.RELATIVE) {
        measuredX += childParams.getScaledWidth(CssProperty.LEFT)
            - childParams.getScaledWidth(CssProperty.RIGHT);
        measuredY += childParams.getScaledWidth(CssProperty.TOP)
            - childParams.getScaledWidth(CssProperty.BOTTOM);
      }
      childParams.setMeasuredPosition(measuredX, measuredY);

      maxWidth = Math.max(maxWidth, child.getMeasuredWidth() + childLeft + childRight);
    }
    if (currentX > 0) {
      adjustLastLine(htmlLayout, firstChildIndex, htmlLayout.getChildCount(), currentX, width);
      currentY += currentLineHeight;
    }
    currentY += pendingMargin;

    if (widthMode == View.MeasureSpec.AT_MOST) {
      onMeasure(htmlLayout, maxWidth | View.MeasureSpec.EXACTLY, heightMeasureSpec);
    } else {
      if (heightMode == View.MeasureSpec.EXACTLY) {
        currentY = height;
      }
      htmlLayout.setMeasuredSize(width, currentY);
    }
  }
}
