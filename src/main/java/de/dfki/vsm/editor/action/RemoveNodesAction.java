package de.dfki.vsm.editor.action;

import java.util.Collection;
//~--- JDK imports ------------------------------------------------------------
import java.util.HashSet;
import java.util.Set;

import de.dfki.vsm.editor.Edge;
import de.dfki.vsm.editor.Node;
import de.dfki.vsm.editor.project.WorkSpace;
import de.dfki.vsm.util.Triple;

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
