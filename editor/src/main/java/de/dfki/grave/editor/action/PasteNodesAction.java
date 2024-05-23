package de.dfki.grave.editor.action;

import java.util.Collection;

import de.dfki.grave.editor.panels.ClipBoard;
import de.dfki.grave.editor.panels.ProjectEditor;
import de.dfki.grave.model.BasicNode;
import de.dfki.grave.model.Position;

/**
 * @author Patrick Gebhard
 */
public class PasteNodesAction extends EditorAction {

  Position mMousePosition;
  Collection<BasicNode> mAffected;

  public PasteNodesAction(ProjectEditor ed, Position p) {
    super(ed);
    mMousePosition = p;
  }
  
  protected void doIt() {
    ClipBoard cb = mEditor.mClipboard;
    Collection<BasicNode> nodes = cb.getNodes();
    if (cb.needsCopy(mEditor)) {
      Collection<BasicNode> toAdd = mSuperNode.copyNodeSet(nodes);
      if (mMousePosition != null) {
        // snap to grid: currently not.
        //mousePosition = mGridManager.getClosestGridPoint(mousePosition);
        BasicNode.translateNodes(toAdd, mMousePosition);
      }
      nodes = toAdd;
    } else {
      // now the clipboard must be set to copy: the nodes are used.
      cb.forceCopy();
    }
    // just add nodes and edges to the model as is: same positions, etc.
    mSuperNode.addNodes(nodes);
    mAffected = nodes;
    if (onActiveWorkSpace()) {
      getWorkSpace().addNodes(nodes);
      // dangerous, but (currently) working
      getWorkSpace().selectNodes(mAffected);
    }
  }

  protected void undoIt() {
    Object[] edgeLists = mSuperNode.removeNodes(mAffected);
    if (onActiveWorkSpace())
      getWorkSpace().removeNodes(mAffected, edgeLists);
  }

  @Override
  public String msg() {
    return "Pasting Of Nodes ";
  }

}
