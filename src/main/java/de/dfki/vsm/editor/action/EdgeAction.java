package de.dfki.vsm.editor.action;

//~--- JDK imports ------------------------------------------------------------
import java.awt.Point;

import javax.swing.undo.UndoManager;

import de.dfki.vsm.editor.Edge;
//~--- non-JDK imports --------------------------------------------------------
import de.dfki.vsm.editor.EditorInstance;
import de.dfki.vsm.editor.Node;
import de.dfki.vsm.editor.project.sceneflow.SceneFlowEditor;
import de.dfki.vsm.editor.project.sceneflow.WorkSpacePanel;
import de.dfki.vsm.editor.util.grid.*;
import de.dfki.vsm.model.flow.*;

/**
 * @author Gregor Mehlmann
 */
public abstract class EdgeAction extends EditorAction {

  protected UndoManager mUndoManager = null;
  protected SceneFlowEditor mSceneFlowPane = null;
  protected WorkSpacePanel mWorkSpace = null;
  protected Node mSourceGUINode = null;
  protected Node mTargetGUINode = null;

  protected Edge mGUIEdge = null;
  protected AbstractEdge mDataEdge = null;
  protected Point mSourceGUINodeDockPoint = null;
  protected Point mTargetGUINodeDockPoint = null;

  private GridRectangle gridSource = null;
  private GridRectangle gridDestination = null;

  public void create() {
    mDataEdge.setTargetNode(mTargetGUINode.getDataNode());
    mDataEdge.setSourceNode(mSourceGUINode.getDataNode());
    mDataEdge.setTargetUnid(mDataEdge.getTargetNode().getId());

    mSourceGUINode.getDataNode().addEdge(mDataEdge);

    // Connect GUI AbstractEdge to Source GUI node
    // Connect GUI AbstractEdge to Target GUI node
    // TODO: Recompute the appearance of the source GUI node
    if (mGUIEdge == null) {
      mGUIEdge = new Edge(mWorkSpace, mDataEdge, mSourceGUINode, mTargetGUINode);
    } else {
      mSourceGUINodeDockPoint = mSourceGUINode.connectEdgeAtSourceNode(mGUIEdge, mSourceGUINodeDockPoint);
      if (mSourceGUINode.equals(mTargetGUINode)) {
        // same nodes
        mTargetGUINodeDockPoint = mTargetGUINode.connectSelfPointingEdge(mGUIEdge, mTargetGUINodeDockPoint);
      } else {
        // different nodes
        mTargetGUINodeDockPoint = mTargetGUINode.connectEdgetAtTargetNode(mGUIEdge, new Point(50, 50));
      }
    }

    EditorInstance.getInstance().refresh();
    mWorkSpace.add(mGUIEdge);
    mWorkSpace.revalidate();
    mWorkSpace.repaint(100);
  }

  public boolean isReroutingNeeded() {
    return getEdgeTotalWeight() >= GridConstants.MAX_WEIGHT_THRESHOLD;
  }

  public int getEdgeTotalWeight() {
    int sumWeight = 0;

    // Determining the positioning of edge's anchor. False means source has
    // smaller coordinate than destination
    boolean anchorMode = false;

    if ((mSourceGUINode.getX() >= mTargetGUINode.getX()) || (mSourceGUINode.getY() >= mTargetGUINode.getY())) {
      anchorMode = true;
    }

    gridSource = null;
    gridDestination = null;

    for (GridRectangle[] gridParent : mWorkSpace.mGridManager.getmTransitionArea()) {
      for (GridRectangle gridRectangle : gridParent) {
        if (gridRectangle.isIntersectedbyNode(mSourceGUINode)) {
          gridRectangle.setIntersectionType(GridRectangle.NODE_INTERSECTION);
          sumWeight += gridRectangle.getWeight();

          if (anchorMode) {
            if (gridSource == null) {
              gridSource = gridRectangle;
            }
          } else {
            gridSource = gridRectangle;
          }
        }

        if (gridRectangle.isIntersectedbyNode(mTargetGUINode)) {
          gridRectangle.setIntersectionType(GridRectangle.NODE_INTERSECTION);
          sumWeight += gridRectangle.getWeight();

          if (anchorMode) {
            gridDestination = gridRectangle;
          } else {
            if (gridDestination == null) {
              gridDestination = gridRectangle;
            }
          }
        }

        if (gridRectangle.isIntersectByRectangle(mGUIEdge.mEg)) {
          gridRectangle.setIntersectionType(GridRectangle.EDGE_INTERSECTION);
          sumWeight += gridRectangle.getWeight();
        }
      }
    }

    // System.out.println("Sum Weight is :" + sumWeight);
    return sumWeight;
  }

  public void deleteDeflected() {
    if (mSourceGUINode.equals(mTargetGUINode)) {
      mSourceGUINodeDockPoint = mSourceGUINode.disconnectEdge(mGUIEdge);
    } else {
      mSourceGUINodeDockPoint = mSourceGUINode.disconnectEdge(mGUIEdge);
    }
    cleanUpData();
  }

  public void delete() {

    // Disconnect the GUI edge from the GUI nodes
    mSourceGUINodeDockPoint = mSourceGUINode.disconnectEdge(mGUIEdge);
    if (mSourceGUINode.equals(mTargetGUINode)) {
      mTargetGUINodeDockPoint = mTargetGUINode.disconnectSelfPointingEdge(mGUIEdge);
    } else {
      mTargetGUINodeDockPoint = mTargetGUINode.disconnectEdge(mGUIEdge);
    }
    cleanUpData();
    /* TODO: we'll most probably turn these into something else anyway
    if (mGUIEdgeType.equals(PEDGE) && mSourceGUINode.getDataNode().hasPEdges() == mSourceGUINode.getDataNode().hasMany) //TODO VALUES OF hasMany SHOULD BE GLOBAL
    {
      ModifyPEdgeDialog mPEdgeDialog = new ModifyPEdgeDialog(mSourceGUINode.getDataNode().getFirstPEdge()); //OPEN EDITION DIALOG TO ASSING NEW PROBABILITIES
      mPEdgeDialog.run();
    }
    */
    if (mSourceGUINode.getDataNode().hasPEdges() == mSourceGUINode.getDataNode().hasOne) //HAS ONLY ONE EDGE LEFT
    {
      mSourceGUINode.getDataNode().getFirstPEdge().setProbability(100);// ASSIGN 100% PROBABILITY AUTOMATICALLY
    }
  }

  private void cleanUpData() {
    // Disconnect the data edge from the source data node
    mSourceGUINode.getDataNode().removeEdge(mDataEdge);

    // Remove the GUI-AbstractEdge from the workspace and
    // update the source node appearance
    EditorInstance.getInstance().refresh();
    mWorkSpace.remove(mGUIEdge);
    mWorkSpace.revalidate();
    mWorkSpace.repaint(100);
  }
}
