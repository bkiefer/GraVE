package de.dfki.vsm.editor.action;

//~--- JDK imports ------------------------------------------------------------
import java.util.HashSet;
import java.util.Set;

//~--- non-JDK imports --------------------------------------------------------
import de.dfki.vsm.editor.Node;
import de.dfki.vsm.editor.project.WorkSpace;

/**
 * @author Patrick Gebhard
 */
public class CopyNodesAction extends EditorAction {

  private Set<Node> mNodes = null;

  @SuppressWarnings("serial")
  public CopyNodesAction(WorkSpace workSpace, Node node) {
    mWorkSpace = workSpace;
    mNodes = new HashSet<Node>(){{ add(node); }};
  }

  public CopyNodesAction(WorkSpace workSpace, Set<Node> mSelectedNodes) {
    mWorkSpace = workSpace;
    mNodes = new HashSet<Node>(mSelectedNodes);
  }

  protected void doIt() {
    mWorkSpace.copyNodes(mNodes);
  }

  protected void undoIt() {
    mWorkSpace.clearClipBoard();
  }

  protected String msg() { return "Copying Of Nodes "; }
}
