package de.dfki.vsm.editor.event;

import de.dfki.vsm.editor.project.WorkSpacePanel;

//~--- non-JDK imports --------------------------------------------------------

/**
 *     @author Martin Fallas
 */
public class WorkSpaceSelectedEvent {
  private WorkSpacePanel mSource;

  public WorkSpaceSelectedEvent(WorkSpacePanel source) {
    mSource = source;
  }

  public WorkSpacePanel getSource() { return mSource; }
}
