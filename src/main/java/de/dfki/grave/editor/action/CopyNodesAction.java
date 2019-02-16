package de.dfki.grave.editor.action;

import java.util.Collection;
//~--- JDK imports ------------------------------------------------------------
import java.util.HashSet;
import java.util.Set;

import de.dfki.grave.editor.panels.WorkSpace;
import de.dfki.grave.model.flow.BasicNode;

/**
 * @author Patrick Gebhard
 *
 * TODO: BK: DOESN'T MAKE A LOT OF SENSE TO ME TO UNDO COPY
 */
public class CopyNodesAction extends EditorAction {

  private Set<BasicNode> mNodes = null;

  @SuppressWarnings("serial")
  public CopyNodesAction(WorkSpace workSpace, BasicNode node) {
    mWorkSpace = workSpace;
    mNodes = new HashSet<BasicNode>(){{ add(node); }};
  }

  public CopyNodesAction(WorkSpace workSpace, Collection<BasicNode> mSelectedNodes) {
    mWorkSpace = workSpace;
    mNodes = new HashSet<>(mSelectedNodes);
  }

  protected void doIt() {
    mWorkSpace.copyNodes(mNodes);
  }

  protected void undoIt() {
    mWorkSpace.clearClipBoard();
  }

  protected String msg() { return "Copying Of Nodes "; }
}
