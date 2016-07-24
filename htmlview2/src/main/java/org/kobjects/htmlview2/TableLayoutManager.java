package org.kobjects.htmlview2;


import android.view.View;

import org.kobjects.dom.Element;
import org.kobjects.css.CssProperty;
import org.kobjects.css.CssStylableElement;
import org.kobjects.css.CssUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class TableLayoutManager implements LayoutManager {

  static int getColSpan(Element cell) {
    String colSpanStr = cell.getAttribute("colspan");
    if (colSpanStr != null && !colSpanStr.isEmpty()) {
      try {
        return Integer.parseInt(colSpanStr.trim());
      } catch (Exception e) {
      }
    }
    return 1;
  }

  static int getRowSpan(Element cell) {
    String colSpanStr = cell.getAttribute("rowspan");
    if (colSpanStr != null && !colSpanStr.isEmpty()) {
      try {
        return Integer.parseInt(colSpanStr.trim());
      } catch (Exception e) {
      }
    }
    return 1;
  }

  @Override
  public void onMeasure(HtmlViewGroup htmlLayout, int widthMeasureSpec, int heightMeasureSpec) {
    int width = View.MeasureSpec.getSize(widthMeasureSpec);
    int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
    int height = View.MeasureSpec.getSize(heightMeasureSpec);
    int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);

    ArrayList<ColumnData> columnList = new ArrayList<>();
    List<List<View>> rows = collectRows(htmlLayout);

    // Phase one: Measure
    int columnCount = 0;
    for (List<View> row : rows) {
      int columnIndex = 0;
      for (View cell: row) {
        HtmlViewGroup.LayoutParams cellParams = (HtmlViewGroup.LayoutParams) cell.getLayoutParams();
        ColumnData columnData;
        while (true) {
          while (columnList.size() <= columnIndex) {
            columnList.add(new ColumnData());
          }
          columnData = columnList.get(columnIndex);
          if (columnData.remainingRowSpan == 0) {
            break;
          }
          columnData.remainingRowSpan--;
          columnIndex++;
        }
        cell.measure(View.MeasureSpec.AT_MOST | width, View.MeasureSpec.UNSPECIFIED);
        int colSpan = getColSpan(cellParams.element);
        int rowSpan = getRowSpan(cellParams.element);
        int cellWidth = cell.getMeasuredWidth() + cellParams.getBorderLeft() + cellParams.getPaddingLeft()
            + cellParams.getPaddingRight() + cellParams.getBorderRight();

        if (colSpan == 1) {
          columnData.maxMeasuredWidth = Math.max(columnData.maxMeasuredWidth, cellWidth);
        } else {
          if (columnData.maxWidthForColspan == null) {
            columnData.maxWidthForColspan = new HashMap<>();
          }
          Integer old = columnData.maxWidthForColspan.get(colSpan);
          columnData.maxWidthForColspan.put(colSpan, old == null ? cellWidth : Math.max(old, cellWidth));
          while (columnList.size() < columnIndex + colSpan) {
            columnList.add(new ColumnData());
          }
        }
        if (rowSpan > 0) {
          for (int i = 0; i < colSpan; i++) {
            columnList.get(columnIndex + i).remainingRowSpan = rowSpan;
          }
        }
        columnIndex += colSpan;
      }
      columnCount = Math.max(columnCount, columnIndex);
    }
    while (columnList.size() <= columnCount) {
      columnList.add(new ColumnData());
    }

    int borderSpacing = Math.round(htmlLayout.getStyle().get(CssProperty.BORDER_SPACING, CssUnit.PX, htmlLayout.cssContentWidth));
    // Phase two: Resolve

    int availableWidth = width - columnCount * borderSpacing;
    int totalWidth = 0;

    for (int i = 0; i < columnList.size(); i++) {
      ColumnData columnData = columnList.get(i);
      columnData.remainingRowSpan = 0;
      if (columnData.maxWidthForColspan != null) {
        for (Map.Entry<Integer,Integer> e : columnData.maxWidthForColspan.entrySet()) {
          int span = e.getKey();
          int spanWidth = e.getValue();
          int curWidth = 0;
          for (int j = i; j < i + span; j++) {
            curWidth += columnList.get(j).maxMeasuredWidth;
          }
          if (curWidth < spanWidth) {
            int distribute = (spanWidth - curWidth) / span;
            for (int j = i; j < i + span; j++) {
              columnList.get(j).maxMeasuredWidth += distribute;
            }
          }
        }
      }

      totalWidth += columnData.maxMeasuredWidth;
    }

    System.out.println("Table desired total width: " + totalWidth + " max avail: " + availableWidth);

    if (totalWidth > availableWidth || widthMeasureSpec == View.MeasureSpec.EXACTLY) {
      for (ColumnData columnData : columnList) {
        System.out.println("Reducing column width " + columnData.maxMeasuredWidth + " to " + (columnData.maxMeasuredWidth * availableWidth / totalWidth));
        columnData.maxMeasuredWidth = columnData.maxMeasuredWidth * availableWidth / totalWidth;
      }
      totalWidth = width;
    }

    // Phase three: Layout

    int currentY = 0;
    for (List<View> row : rows) {
      int columnIndex = 0;
      int rowHeight = 0;
      int currentX = 0;
      for (View cell: row) {
        HtmlViewGroup.LayoutParams cellParams = (HtmlViewGroup.LayoutParams) cell.getLayoutParams();
        Hv2DomElement cellElement = cellParams.element;
        ColumnData columnData;
        while (true) {
          // Skip columns with remaining rowspan
          while (columnList.size() <= columnIndex) {
            columnList.add(new ColumnData());
          }
          columnData = columnList.get(columnIndex);
          if (columnData.remainingRowSpan == 0) {
            break;
          }
          currentX += columnData.maxMeasuredWidth + borderSpacing;
          columnIndex++;
        }
        int topOffset = cellParams.getBorderTop() + cellParams.getPaddingTop();
        int bottomOffset = cellParams.getBorderBottom() + cellParams.getPaddingBottom();
        int leftOffset = cellParams.getBorderLeft() + cellParams.getPaddingLeft();
        int rightOffset = cellParams.getBorderRight() + cellParams.getPaddingRight();
        int colSpan = getColSpan(cellElement);
        int spanWidth = 0;
        for (int i = columnIndex; i < columnIndex + colSpan; i++) {
          spanWidth += columnList.get(i).maxMeasuredWidth;
          if (i > columnIndex) {
            spanWidth += borderSpacing;
          }
        }
        cell.measure(View.MeasureSpec.EXACTLY | (spanWidth - leftOffset - rightOffset), View.MeasureSpec.UNSPECIFIED);
        cellParams.setMeasuredPosition(currentX + leftOffset, currentY + topOffset);
        columnData.remainingRowSpan = getRowSpan(cellElement);
        columnData.remainingHeight = topOffset + cell.getMeasuredHeight() + bottomOffset;
        columnData.startCell = cell;
        columnData.yOffset = currentY + topOffset + bottomOffset;
        currentX += spanWidth + borderSpacing;
        columnIndex += colSpan;
      }
      for (ColumnData columnData : columnList) {
        if (columnData.remainingRowSpan == 1) {
          rowHeight = Math.max(rowHeight, columnData.remainingHeight);
        }
      }
      for (ColumnData columnData : columnList) {
        if (columnData.remainingRowSpan == 1) {
          columnData.remainingRowSpan = 0;
          columnData.startCell.measure(View.MeasureSpec.EXACTLY | columnData.startCell.getMeasuredWidth(),
              View.MeasureSpec.EXACTLY | (currentY + rowHeight - columnData.yOffset));
        } else if (columnData.remainingRowSpan > 1) {
          columnData.remainingHeight -= rowHeight - borderSpacing;
          columnData.remainingRowSpan--;
        }
      }
      currentY += rowHeight + borderSpacing;
    }
    htmlLayout.setMeasuredSize(totalWidth, heightMode == View.MeasureSpec.EXACTLY ? height : currentY - borderSpacing);
  }

  List<List<View>> collectRows(HtmlViewGroup htmlLayout) {
    ArrayList<List<View>> rows = new ArrayList<>();
    if (!(htmlLayout.getLayoutParams() instanceof HtmlViewGroup.LayoutParams)) {
      return rows;
    }

    Iterator<? extends CssStylableElement> rowIterator =
        ((HtmlViewGroup.LayoutParams) htmlLayout.getLayoutParams()).element.getChildElementIterator();

    while (rowIterator.hasNext()) {
      CssStylableElement potentialRowElement = rowIterator.next();
      if (potentialRowElement instanceof Hv2DomElement) {
        Hv2DomElement row = (Hv2DomElement) potentialRowElement;
        ArrayList<View> cells = new ArrayList<View>();
        rows.add(cells);
        Iterator<? extends CssStylableElement> colIterator = row.getChildElementIterator();
        while (colIterator.hasNext()) {
          Hv2DomElement potentialCell = (Hv2DomElement) colIterator.next();
          if (potentialCell.componentType == Hv2DomContainer.ComponentType.PHYSICAL_CONTAINER) {
            cells.add(potentialCell.getView());
          }
        }
      }
    }
    return rows;
  }



  static class ColumnData {
    int maxMeasuredWidth;
    Map<Integer,Integer> maxWidthForColspan;
    int remainingRowSpan;
    int remainingHeight;
    View startCell;
    int yOffset;
  }

}
