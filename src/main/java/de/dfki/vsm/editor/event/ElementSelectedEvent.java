package de.dfki.vsm.editor.event;

/**
 * @author Gregor Mehlmann
 */
public class ElementSelectedEvent  {

  private Object mElement;

  public ElementSelectedEvent(Object elt) {
    mElement = elt;
  }

  public Object getElement() { return mElement; }

  public String getEventDescription() {
    return "NodeSelectedEvent(" + mElement + ")";
  }
}
