package org.kobjects.htmlview2;

import android.view.View;
import elemental.dom.Node;
import elemental.dom.Text;

/**
 * Synchronizes the phsical view hierarchy to the DOM.
 */
public class TreeSync {

    public static void sync(HtmlViewGroup physicalContainer, Node logicalContainer) {
        HtmlTextView pendingText = null;
        Node child = logicalContainer.getFirstChild();
        while (child != null) {
            if (child instanceof DomViewElement) {
                View view = ((DomViewElement) child).getView();
                physicalContainer.addView(view);
                ((HtmlViewGroup.LayoutParams) view.getLayoutParams()).element = (DomViewElement) child;
                pendingText = null;
                if (view instanceof HtmlViewGroup) {
                    sync((HtmlViewGroup) view, child);
                }
            } else if (child instanceof DomTextNode || child instanceof DomTextElement) {
                if (pendingText == null) {
                    pendingText = new HtmlTextView(physicalContainer.htmlView);
                    physicalContainer.addView(pendingText);
                }
                if (child instanceof DomTextNode) {
                    pendingText.appendNormalized(((Text) child).getData());
                } else {
                    ((DomTextElement) child).sync(pendingText);
                }
            } else if (child instanceof DomElement) {
                pendingText = null;
                sync(physicalContainer, child);
            }
            child = child.getNextSibling();
        }
    }

}
