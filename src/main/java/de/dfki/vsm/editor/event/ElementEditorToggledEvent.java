package de.dfki.vsm.editor.event;

import de.dfki.vsm.util.evt.EventObject;

/**
 * @author Martin Fallas
 */
public class ElementEditorToggledEvent extends EventObject {

  public ElementEditorToggledEvent(Object source) {
    super(source);
  }

  public String getEventDescription() {
    return "Element Editor toggled";
  }
}
