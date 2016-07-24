# HtmlLayout2

Cleaner, simplified and "Androidified" version of HtmlView under development (should be ready for beta trials).

- Text styling replaced with Android spans.
- Layouts are now orthogonal to the view hierarchy (opposed to subclassing in HtmlView).
- Native views don't require wrapping.


## Plans

- Improve API to simplify WebView compatibility
- Support box layout
- Better support for rendering and synchronizing to a DOM created programmatically
  (it should be possible to super-source the DOM classes in GWT)


## Restrictions

- No floats
- CSS can't override whether an element is a text or block element.

