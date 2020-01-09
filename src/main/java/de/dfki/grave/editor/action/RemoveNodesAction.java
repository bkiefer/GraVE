package de.dfki.grave.editor.action;

import java.util.Collection;
//~--- JDK imports ------------------------------------------------------------
import java.util.HashSet;
import java.util.Set;

import de.dfki.grave.editor.panels.WorkSpace;
import de.dfki.grave.model.flow.AbstractEdge;
import de.dfki.grave.model.flow.BasicNode;
import de.dfki.grave.util.Triple;

/**
 * @author Patrick Gebhard
 */
public class RemoveNodesAction extends EditorAction {

  private Set<BasicNode> mNodes = null;
  private boolean isCutOperation;
  private Triple<Collection<AbstractEdge>, Collection<BasicNode>, Collection<AbstractEdge>> mAffected;


  public RemoveNodesAction(WorkSpace workSpace, Collection<BasicNode> mSelectedNodes,
      boolean toClipboard) {
    mWorkSpace = workSpace;
    mNodes = new HashSet<BasicNode>(mSelectedNodes);
    isCutOperation = toClipboard;
  }

  @SuppressWarnings("serial")
  public RemoveNodesAction(WorkSpace workSpace, BasicNode node, boolean toClipboard) {
    this(workSpace, new HashSet<BasicNode>(){{ add(node); }}, toClipboard);
  }

  protected void doIt() {
    mAffected = mWorkSpace.removeNodes(isCutOperation, mNodes);
  }

  protected void undoIt() {
    mWorkSpace.pasteNodesAndEdges(mAffected.getSecond(), mAffected.getThird());
    mWorkSpace.addEdges(mAffected.getFirst());
  }

  @Override
  public String msg() {
    return "Deletion Of Nodes ";
  }
}
