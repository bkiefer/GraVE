package de.dfki.grave.editor.action;

import java.util.Collection;
import java.util.Collections;
//~--- JDK imports ------------------------------------------------------------
import java.util.HashSet;
import java.util.Set;

import de.dfki.grave.editor.panels.ProjectEditor;
import de.dfki.grave.model.flow.AbstractEdge;
import de.dfki.grave.model.flow.BasicNode;

/**
 * @author Patrick Gebhard
 */
public class RemoveNodesAction extends EditorAction {

  private Set<BasicNode> mNodes = null;
  private boolean isCutOperation;
  private Collection<AbstractEdge>[] mAffected;

  public RemoveNodesAction(ProjectEditor editor, Collection<BasicNode> mSelectedNodes,
      boolean toClipboard) {
    super(editor);
    mNodes = new HashSet<BasicNode>(mSelectedNodes);
    isCutOperation = toClipboard;
  }

  @SuppressWarnings("serial")
  public RemoveNodesAction(ProjectEditor editor, BasicNode node, boolean toClipboard) {
    this(editor, new HashSet<BasicNode>(){{ add(node); }}, toClipboard);
  }

  protected void doIt() {
    // affected edges: emerging, internal, incoming to the set of mNodes
    mAffected = mSuperNode.removeNodes(mNodes);
    if (isCutOperation) {
      mEditor.mClipboard.set(mEditor, mNodes,
          Collections.emptyList(), Collections.emptyList());
    }   
    if (onActiveWorkSpace())
      getWorkSpace().removeNodes(mNodes, mAffected);
  }

  protected void undoIt() {
    // add nodes (will also add internal and outgoing edges) and incoming edges
    mSuperNode.addNodes(mNodes);
    mSuperNode.addEdges(mAffected[2]);
    if (onActiveWorkSpace()) {
      getWorkSpace().addNodes(mNodes);
      getWorkSpace().addEdges(mAffected[2]);
    }
  }

  @Override
  public String msg() {
    return "Deletion Of Nodes ";
  }
}
