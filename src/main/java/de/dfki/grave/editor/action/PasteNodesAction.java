package de.dfki.grave.editor.action;

import java.awt.Point;
import java.util.Collection;

import de.dfki.grave.editor.Node;
import de.dfki.grave.editor.panels.WorkSpace;

/**
 * @author Patrick Gebhard
 */
public class PasteNodesAction extends EditorAction {

  Point mMousePosition;
  Collection<Node> mAffected;

  public PasteNodesAction(WorkSpace workSpace, Point p) {
    mWorkSpace = workSpace;
    mMousePosition = p;
  }

  protected void doIt() {
    mAffected = mWorkSpace.pasteNodesFromClipboard(mMousePosition);
  }

  protected void undoIt() {
    mWorkSpace.removeNodes(false, mAffected);
  }

  @Override
  public String msg() {
    return "Pasting Of Nodes ";
  }

}
