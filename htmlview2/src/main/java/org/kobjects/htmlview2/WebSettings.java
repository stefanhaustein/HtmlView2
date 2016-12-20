package org.kobjects.htmlview2;

/**
 * Settings for HtmlView.
 */
public class WebSettings {
  private int defaultFontSize = 16;

  WebSettings() {
  }

  public void setDefaultFontSize(int defaultFontSize) {
    this.defaultFontSize = defaultFontSize;
  }

  public int getDefaultFontSize() {
    return defaultFontSize;
  }

}
