package org.kobjects.htmlview2;

import android.view.View;
import elemental.dom.Node;
import elemental.dom.Text;

/**
 * Synchronizes the phsical view hierarchy to the DOM.
 */
public class TreeSync {

    public static void sync(HtmlViewGroup physicalContainer, Node logicalContainer, boolean clear) {
        if (clear) {
            physicalContainer.removeAllViews();
        }
        HtmlTextView pendingText = null;
        Node child = logicalContainer.getFirstChild();
        while (child != null) {
            if (child instanceof HvViewElement) {
                View view = ((HvViewElement) child).getView();
                physicalContainer.addView(view);
                ((HtmlViewGroup.LayoutParams) view.getLayoutParams()).element = (HvViewElement) child;
                pendingText = null;
                if (view instanceof HtmlViewGroup) {
                    sync((HtmlViewGroup) view, child, true);
                }
            } else if (child instanceof HvTextNode || child instanceof HvTextElement) {
                if (pendingText == null) {
                    pendingText = new HtmlTextView(physicalContainer.htmlView);
                    physicalContainer.addView(pendingText);
                }
                if (child instanceof HvTextNode) {
                    pendingText.appendNormalized(((Text) child).getData());
                } else {
                    ((HvTextElement) child).sync(pendingText);
                }
            } else if (child instanceof HvElement) {
                pendingText = null;
                sync(physicalContainer, child, false);
            }
            child = child.getNextSibling();
        }
    }

}
