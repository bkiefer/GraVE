package de.dfki.vsm.editor.action;

import static de.dfki.vsm.editor.Node.Type.*;

//~--- JDK imports ------------------------------------------------------------
import java.awt.Point;
import java.util.HashMap;

import javax.swing.undo.UndoManager;

//~--- non-JDK imports --------------------------------------------------------
import de.dfki.vsm.editor.CmdBadge;
import de.dfki.vsm.editor.Node.Type;
import de.dfki.vsm.editor.project.SceneFlowEditor;
import de.dfki.vsm.editor.project.WorkSpacePanel;
import de.dfki.vsm.editor.util.IDManager;
import de.dfki.vsm.model.flow.BasicNode;
import de.dfki.vsm.model.flow.SuperNode;
import de.dfki.vsm.model.project.EditorConfig;

/**
 * @author Gregor Mehlmann
 */
public abstract class NodeAction extends EditorAction {

  // Common data
  protected UndoManager mUndoManager = null;
  protected SceneFlowEditor mSceneFlowPane = null;
  protected WorkSpacePanel mWorkSpace = null;
  protected Point mCoordinate = null;
  protected Type mGUINodeType = null;
  protected IDManager mIDManager = null;
  protected String mDataNodeId = null;
  protected BasicNode mDataNode = null;
  protected SuperNode mParentDataNode = null;

  //
  protected de.dfki.vsm.editor.Node mGUINode;
  protected CmdBadge mCmdBadge;

  NodeAction() {
  }

  public void delete() {
    // System.out.println("Delete");
    // Remove the data node from the sceneflow, which means that we
    // remove the node from the from the parent nodes list of child
    // nodes and set the parent node of the node to null. Additionally
    // we have to update the start node status of the node.
    if (mGUINodeType == BasicNode) {
      mParentDataNode.removeNode(mDataNode);
    } else if (mGUINodeType == SuperNode) {
      mParentDataNode.removeSuperNode((SuperNode) mDataNode);
//             mParentDataNode.removeNode(mDataNode);
    }

    /*
    // Check the start node status of the removed node
    HashMap<String, de.dfki.vsm.model.flow.BasicNode> startNodeMap = mParentDataNode.getStartNodeMap();

    if (startNodeMap.containsKey(mDataNode.getId())) {
      startNodeMap.remove(mDataNode.getId());

      // System.out.println("Removing node " + mDataNode.getId() + " from start node list");
      // System.out.println("Startnode Map is " + startNodeMap);
      if (startNodeMap.isEmpty()) {
        if (mParentDataNode.getNodeList().isEmpty()) {
          if (mParentDataNode.getSuperNodeList().isEmpty()) {

            // No nodes which could be choosen as start node
            // System.out.println("No new start node can be found");
          } else {

            // Get a supernode as start node
            BasicNode newStartNode = mParentDataNode.getSuperNodeList().get(0);

            mParentDataNode.getStartNodeMap().put(newStartNode.getId(), newStartNode);
          }
        } else {
          for (de.dfki.vsm.model.flow.BasicNode n : mParentDataNode.getNodeList()) {
            if (n.isHistoryNode()) {
              continue;
            } else {
              mParentDataNode.getStartNodeMap().put(n.getId(), n);
            }
          }

          // Get a node as start node
          // de.dfki.vsm.xml.sceneflow.BasicNode newStartNode =
          // mParentDataNode.getNodeAt(0);
          // mParentDataNode.getStartNodeMap().put(newStartNode.getId(), newStartNode);
        }
      }
    }
      */

    ////////PROBLEM:
    // mDataNode.setParentNode(null);
    ////////////////////////////////
    // Remove the GUI node from the workspace, which means that we
    // free the grid position of the node, if the node was a startnode,
    // then we remove the start sign from workspace, if commands have been
    // show, then we remove the command badge.
    // mWorkSpace.removeNode(mGUINode);
    mWorkSpace.getGridManager().freeGridPosition(mCoordinate);
    mGUINode.removeStartSign();

    //
    // System.err.println("Removing node and command badge");
    mWorkSpace.remove(mGUINode);
    //mWorkSpace.removeCmdBadge(mGUINode);

    //////////// After paint, as in paint we access the parent node
    // Doesnt work
    // mDataNode.setParentNode(null);
    mWorkSpace.revalidate();
    mWorkSpace.repaint(100);
  }

  public void create() {

    // System.out.println("BasicNode action create!");
    // Set the parent node
    // System.err.println("Setting parent node to " + mParentDataNode.getId());
    mDataNode.setParentNode(mParentDataNode);

    /*
    // Set the start node status
    if (mParentDataNode.getStartNodeMap().isEmpty()) {
      mParentDataNode.getStartNodeMap().put(mDataNode.getId(), mDataNode);
      mGUINode.addStartSign();
    }
    */

    // Add the node as a child to the parent node
    if (mGUINodeType == BasicNode) {
      mParentDataNode.addNode(mDataNode);
    } else if (mGUINodeType == SuperNode) {
      if (!mParentDataNode.getSuperNodeList().contains((SuperNode) mDataNode)) {
        mParentDataNode.addSuperNode((SuperNode) mDataNode);
      }
    }

    // TODO: Take the grid position!!!!!!!!!!!
    // Add the GUI-BasicNode
    mWorkSpace.addNode(mGUINode);
    //mWorkSpace.addCmdBadge(mGUINode, mCmdBadge);

    //
    mWorkSpace.revalidate();
    mWorkSpace.repaint(100);
  }
}
