package de.dfki.grave.editor.action;

import de.dfki.grave.editor.CodeArea;
import de.dfki.grave.editor.event.ElementSelectedEvent;
import de.dfki.grave.editor.panels.WorkSpace;
import de.dfki.grave.util.evt.EventDispatcher;

/**
 * Sergio Soto
 */
public class EditCommandAction extends EditorAction {

  private final EventDispatcher mDispatcher = EventDispatcher.getInstance();

  private final CodeArea mCmdBadge;

  public EditCommandAction(WorkSpace workSpace, CodeArea c) {
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
