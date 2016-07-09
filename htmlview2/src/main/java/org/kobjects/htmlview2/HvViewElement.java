package org.kobjects.htmlview2;

import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import org.kobjects.css.CssStyle;

class HvViewElement extends HvElement {
    private View view;

    HvViewElement(HvDocument ownerDocument, String name, View view) {
        super(ownerDocument, name);
        this.view = view;
    }

    @Override
    public void setComputedStyle(CssStyle style) {
        super.setComputedStyle(style);

        if (view instanceof HtmlViewGroup) {
            HtmlViewGroup htmlLayout = (HtmlViewGroup) view;
            for (int i = 0; i < htmlLayout.getChildCount(); i++) {
                if (htmlLayout.getChildAt(i) instanceof HtmlTextView) {
                    ((HtmlTextView) htmlLayout.getChildAt(i)).setComputedStyle(style);
                }
            }
        }
    }

    public View getView() {
        if (view == null) {
            String type = getAttribute("type");
            String value = getAttribute("value");
            if ("button".equals(type) || "submit".equals(type) || "reset".equals(type)) {
                view = new Button(ownerDocument.htmlContext.getContext());
            } else if ("checkbox".equals(type)) {
                view = new CheckBox(ownerDocument.htmlContext.getContext());
            } else {
                view = new EditText(ownerDocument.htmlContext.getContext());
            }
            if (value != null) {
                ((TextView) view).setText(value);
            }
        }
        return view;
    }
}
