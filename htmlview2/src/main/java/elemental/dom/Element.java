package elemental.dom;

public interface Element extends Node {
    String getLocalName();
    void setAttribute(String name, String value);
    String getAttribute(String name);
}
