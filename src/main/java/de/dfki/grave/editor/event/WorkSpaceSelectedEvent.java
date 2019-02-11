package de.dfki.grave.editor.event;

import de.dfki.grave.editor.panels.WorkSpace;

//~--- non-JDK imports --------------------------------------------------------

/**
 *     @author Martin Fallas
 */
public class WorkSpaceSelectedEvent {
  private WorkSpace mSource;

  public WorkSpaceSelectedEvent(WorkSpace workSpace) {
    mSource = workSpace;
  }

  public WorkSpace getSource() { return mSource; }
}
