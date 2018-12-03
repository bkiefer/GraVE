package de.dfki.vsm.editor.event;

import de.dfki.vsm.editor.project.WorkSpace;

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
