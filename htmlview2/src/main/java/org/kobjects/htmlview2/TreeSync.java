package org.kobjects.htmlview2;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import elemental.dom.Node;
import elemental.dom.Text;

import java.net.URISyntaxException;

/**
 * Synchronizes the physical view hierarchy to the DOM.
 */
class TreeSync {

  static void syncContainer(HtmlViewGroup physicalContainer, HvDomContainer logicalContainer, boolean clear) {
    if (logicalContainer.syncState == HvDomContainer.SyncState.SYNCED) {
      return;
    }
    logicalContainer.syncState = HvDomContainer.SyncState.SYNCED;
    if (clear) {
      physicalContainer.removeAllViews();
    }
    HtmlTextView pendingText = null;
    Node child = logicalContainer.getFirstChild();
    while (child != null) {
      pendingText = syncChild(physicalContainer, child, pendingText);
      child = child.getNextSibling();
    }
  }

  static HtmlTextView syncChild(HtmlViewGroup physicalContainer, Node childNode, HtmlTextView pendingText) {
    if (childNode instanceof Text) {
      HvDomText text = (HvDomText) childNode;
      if (pendingText == null) {
        pendingText = new HtmlTextView(physicalContainer.htmlView);
        physicalContainer.addView(pendingText);
      }
      pendingText.append(text.getData());
    } else {
      HvDomElement element = (HvDomElement) childNode;
      switch (((HvDomContainer) childNode).componentType) {
        case IMAGE:  // TODO: We may want to treat images as leaf component here (opposed to inside syncTextElement)
        case TEXT: {
          if (pendingText == null) {
            pendingText = new HtmlTextView(physicalContainer.htmlView);
            physicalContainer.addView(pendingText);
          }
          pendingText = syncTextElement(((HvDomElement) childNode), physicalContainer, pendingText);
          break;
        }
        case LOGICAL_CONTAINER:
          syncContainer(physicalContainer, element, false);
          break;
        case PHYSICAL_CONTAINER: {
          HtmlViewGroup childGroup = (HtmlViewGroup) element.getView();
          physicalContainer.addView(childGroup);
          ((HtmlViewGroup.LayoutParams) childGroup.getLayoutParams()).element = element;
          syncContainer(childGroup, element, true);
          break;
        }
        case LEAF_COMPONENT: {
          View childView = element.getView();
          physicalContainer.addView(childView);
          ((HtmlViewGroup.LayoutParams) childView.getLayoutParams()).element = element;

          if (childView instanceof Spinner) {
            syncSelect((Spinner) childView, element);
          }

          break;
        }
        default:
          // No action needed.
      }
    }
    return pendingText;
  }


  static void syncSelect(Spinner spinner, HvDomElement element) {
    ArrayAdapter<String> options = new ArrayAdapter<String>(spinner.getContext(),
        android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(options);

    HvDomElement child = element.getFirstElementChild();
    while (child != null) {
      if (child.getLocalName().equals("option")) {
        boolean selected = child.getAttribute("selected") != null;
        String content = child.getTextContent();
        options.add(content);
        if (child.getAttribute("selected") != null) {
          spinner.setSelection(options.getCount() - 1);
        }
        child = child.getNextElementSibling();
      }
    }
  }


  static HtmlTextView syncTextElement(HvDomElement element, HtmlViewGroup physicalContainer, HtmlTextView htmlTextView) {
    element.sections = new SpanCollection(element, htmlTextView);

    if (element.getLocalName().equals("img")) {
      htmlTextView.append("\u2327");
      String src = element.getAttribute("src");
      if (src != null) {
        try {
          htmlTextView.htmlView.requestImage(element.sections, htmlTextView.htmlView.createUri(src));
        } catch (URISyntaxException e) {
          Log.e(HtmlTextView.TAG, "Error constructing image URL from '" + src + "'", e);
        }
      }
    } else if (element.getLocalName().equals("br")) {
      htmlTextView.hasLineBreaks = true;
      htmlTextView.append("\u200b");
      htmlTextView.pendingBreakPosition = htmlTextView.content.length() - 1;
    } else {
      HvDomNode child = element.getFirstChild();
      while (child != null) {
        if (child instanceof HvDomText) {
          htmlTextView.append(((HvDomText) child).text);
        } else {
            HvDomElement childElement = (HvDomElement) child;
            if (childElement.componentType == HvDomContainer.ComponentType.TEXT ||
                childElement.componentType == HvDomContainer.ComponentType.IMAGE ||
                childElement.componentType == HvDomContainer.ComponentType.LOGICAL_CONTAINER) {
              syncTextElement(((HvDomElement) child), physicalContainer, htmlTextView);
            } else {
              // Physical container or leaf component.
              syncChild(physicalContainer, child, null);
              htmlTextView = new HtmlTextView(physicalContainer.htmlView);
              reopenText(element, htmlTextView);
            }
        }
        child = child.getNextSibling();
      }
    }
    element.sections.end = htmlTextView.content.length();
    return htmlTextView;
  }

  static void reopenText(HvDomElement element, HtmlTextView htmlTextView) {
    element.sections.end = element.sections.htmlTextView.content.length();
    SpanCollection next = new SpanCollection(element, htmlTextView);
    next.previous = element.sections;
    element.sections = next;
    if (element.parentNode.componentType == HvDomContainer.ComponentType.TEXT) {
      reopenText(((HvDomElement) element.parentNode), htmlTextView);
    }
  }

}
