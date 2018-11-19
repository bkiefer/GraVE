package de.dfki.vsm.editor.action;

import de.dfki.vsm.editor.CmdBadge;
import de.dfki.vsm.editor.event.NodeSelectedEvent;
import de.dfki.vsm.editor.project.sceneflow.WorkSpacePanel;
import de.dfki.vsm.util.evt.EventDispatcher;

/**
 * Sergio Soto
 */
public class EditCommandAction extends EditorAction {
  //

  private final EventDispatcher mDispatcher
          = EventDispatcher.getInstance();

  private final CmdBadge mCmdBadge;

  public EditCommandAction(WorkSpacePanel workSpace, CmdBadge c) {
    mCmdBadge = c;
  }

  @Override
  public void run() {
    mDispatcher.convey(new NodeSelectedEvent(this, mCmdBadge.getNode().getDataNode()));
    //mCmdBadge.setEditMode();
    mCmdBadge.revalidate();
    mCmdBadge.repaint(100);
  }
}
