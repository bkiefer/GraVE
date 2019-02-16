package de.dfki.grave.editor.action;

import java.awt.Point;
import java.util.Collection;

import de.dfki.grave.editor.panels.WorkSpace;
import de.dfki.grave.editor.panels.WorkSpacePanel;
import de.dfki.grave.model.flow.BasicNode;

/**
 * @author Patrick Gebhard
 */
public class PasteNodesAction extends EditorAction {

  Point mMousePosition;
  Collection<BasicNode> mAffected;

  public PasteNodesAction(WorkSpace workSpace, Point p) {
    mWorkSpace = workSpace;
    mMousePosition = p;
  }

  protected void doIt() {
    mAffected = mWorkSpace.pasteNodesFromClipboard(mMousePosition);
    // dangerous, but (currently) working
    ((WorkSpacePanel)mWorkSpace).selectNodes(mAffected);
  }

  protected void undoIt() {
    mWorkSpace.removeNodes(false, mAffected);
  }

  @Override
  public String msg() {
    return "Pasting Of Nodes ";
  }

}
