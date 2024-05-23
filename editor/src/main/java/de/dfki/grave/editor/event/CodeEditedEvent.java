package de.dfki.grave.editor.event;

import de.dfki.grave.editor.CodeArea;

/**
 * @author Gregor Mehlmann
 */
public class CodeEditedEvent  {

  private CodeArea mDoc;
  private boolean mActive;

  public CodeEditedEvent(CodeArea doc, boolean active) {
    mDoc = doc;
    mActive = active;
  }

  public CodeArea getContainer() { return mDoc; }
  
  public boolean isActive() { return mActive; }

  public String getEventDescription() {
    return "NodeSelectedEvent(" + mDoc + "," + mActive + ")";
  }
}
