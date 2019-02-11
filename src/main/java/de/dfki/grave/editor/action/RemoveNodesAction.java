package de.dfki.grave.editor.action;

import java.util.Collection;
//~--- JDK imports ------------------------------------------------------------
import java.util.HashSet;
import java.util.Set;

import de.dfki.grave.editor.Edge;
import de.dfki.grave.editor.Node;
import de.dfki.grave.editor.panels.WorkSpace;
import de.dfki.grave.util.Triple;

/**
 * @author Patrick Gebhard
 */
public class RemoveNodesAction extends EditorAction {

  private Set<Node> mNodes = null;
  private boolean isCutOperation;
  private Triple<Collection<Edge>, Collection<Node>, Collection<Edge>> mAffected;


  public RemoveNodesAction(WorkSpace workSpace, Set<Node> mSelectedNodes,
      boolean toClipboard) {
    mWorkSpace = workSpace;
    mNodes = new HashSet<Node>(mSelectedNodes);
    isCutOperation = toClipboard;
  }

  @SuppressWarnings("serial")
  public RemoveNodesAction(WorkSpace workSpace, Node node, boolean toClipboard) {
    this(workSpace, new HashSet<Node>(){{ add(node); }}, toClipboard);
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
