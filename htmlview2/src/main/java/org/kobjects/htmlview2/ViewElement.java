package org.kobjects.htmlview2;

import android.view.View;
import org.kobjects.css.CssStyle;

public class ViewElement extends VirtualElement {
    View view;

    public ViewElement(String name, View view) {
        super(name);
        this.view = view;
    }

    @Override
    public void setComputedStyle(CssStyle style) {
        super.setComputedStyle(style);

        if (view instanceof HtmlLayout) {
            HtmlLayout htmlLayout = (HtmlLayout) view;
            for (int i = 0; i < htmlLayout.getChildCount(); i++) {
                if (htmlLayout.getChildAt(i) instanceof HtmlTextView) {
                    ((HtmlTextView) htmlLayout.getChildAt(i)).setComputedStyle(style);
                }
            }
        }
    }
}
