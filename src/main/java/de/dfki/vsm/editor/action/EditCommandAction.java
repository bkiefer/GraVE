package de.dfki.vsm.editor.action;

import de.dfki.vsm.editor.CmdBadge;
import de.dfki.vsm.editor.event.ElementSelectedEvent;
import de.dfki.vsm.editor.project.WorkSpace;
import de.dfki.vsm.util.evt.EventDispatcher;

/**
 * Sergio Soto
 */
public class EditCommandAction extends EditorAction {

  private final EventDispatcher mDispatcher = EventDispatcher.getInstance();

  private final CmdBadge mCmdBadge;

  public EditCommandAction(WorkSpace workSpace, CmdBadge c) {
    mCmdBadge = c;
  }

  @Override
  public void doIt() {
    mDispatcher.convey(new ElementSelectedEvent(mCmdBadge));
    //mCmdBadge.setEditMode();
    mCmdBadge.revalidate();
    mCmdBadge.repaint(100);
  }

  @Override
  protected void undoIt() {

  }

  @Override
  protected String msg() {
    return "Edit Command";
  }
}
