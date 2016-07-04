package org.kobjects.htmllayout;

/**
 * Interface for the di
 */
public interface LayoutManager {
  /**
   * The implementation is expected to call htmlLayout.setMeasuredSize() (which just forwards the
   * call to the protected method setMeasuredDimension().
   */
  void onMeasure(HtmlLayout htmlLayout, int widthSpec, int heightSpec);
}
