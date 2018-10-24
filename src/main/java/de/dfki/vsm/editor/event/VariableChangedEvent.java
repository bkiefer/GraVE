package de.dfki.vsm.editor.event;

import de.dfki.vsm.util.Pair;
//~--- non-JDK imports --------------------------------------------------------
import de.dfki.vsm.util.evt.EventObject;

/**
 * @author Gregor Mehlmann
 */
public class VariableChangedEvent extends EventObject {

  private Pair<String, String> mVariableValuePair;

  public VariableChangedEvent(Object source, Pair<String, String> variableValuePair) {
    super(source);
    mVariableValuePair = variableValuePair;
  }

  public Pair<String, String> getVarValue() {
    return mVariableValuePair;
  }

  public String getEventDescription() {
    return "VariableChangedEvent(" + mVariableValuePair.getFirst() + ", " + mVariableValuePair.getSecond() + ")";
  }

  @Override
  public String toString() {
    return getEventDescription();
  }

}
