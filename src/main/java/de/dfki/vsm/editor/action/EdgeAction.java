package de.dfki.vsm.editor.action;

//~--- JDK imports ------------------------------------------------------------
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Set;

import javax.swing.undo.UndoManager;

import de.dfki.vsm.editor.Edge;
//~--- non-JDK imports --------------------------------------------------------
import de.dfki.vsm.editor.EditorInstance;
import de.dfki.vsm.editor.Node;
import de.dfki.vsm.editor.project.sceneflow.SceneFlowEditor;
import de.dfki.vsm.editor.project.sceneflow.WorkSpacePanel;
import de.dfki.vsm.editor.util.grid.*;
import de.dfki.vsm.editor.util.grid.pathfinding.Path;
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
  protected Node mLastTargetGUINode = null;
  protected Edge mGUIEdge = null;
  protected AbstractEdge mDataEdge = null;
  protected Point mSourceGUINodeDockPoint = null;
  protected Point mTargetGUINodeDockPoint = null;
  protected Point mLastTargetGUINodeDockPoint = null;
  protected GridRectangle gridSource = null;
  protected GridRectangle gridDestination = null;

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
      if (mSourceGUINode.equals(mTargetGUINode)) {

        // same nodes
        mSourceGUINodeDockPoint = mSourceGUINode.connectEdgeAtSourceNode(mGUIEdge, mSourceGUINodeDockPoint);
        mTargetGUINodeDockPoint = mTargetGUINode.connectSelfPointingEdge(mGUIEdge, mTargetGUINodeDockPoint);
      } else {

        // different nodes
        mSourceGUINodeDockPoint = mSourceGUINode.connectEdgeAtSourceNode(mGUIEdge, mSourceGUINodeDockPoint);
        mTargetGUINodeDockPoint = mTargetGUINode.connectEdgetAtTargetNode(mGUIEdge, new Point(50, 50));
      }
    }

    // mSourceGUINode.update();
    EditorInstance.getInstance().refresh();
    mWorkSpace.add(mGUIEdge);
    mWorkSpace.revalidate();
    mWorkSpace.repaint(100);

//      recalculateWeight();
//      setEdgePath();
  }

  public void recalculateWeight() {
    mWorkSpace.getGridManager().resetAllGridWeight();

    Set<Edge> edgeSet = mWorkSpace.getEdges();

    for (Edge edge : edgeSet) {
      if (!edge.getName().equals(mGUIEdge.getName())) {
        mWorkSpace.getGridManager().setEdgeWeight(edge);
        mWorkSpace.getGridManager().setNodeWeight(edge.getSourceNode());
        mWorkSpace.getGridManager().setNodeWeight(edge.getTargetNode());
      }
    }
  }

  public void setEdgePath() {

    // if weight of grid intersection is larger than max weight threshold, rerouting needed.
    if (isReroutingNeeded()) {
      AStarEdgeFinder aStarPath = new AStarEdgeFinder(mWorkSpace.mGridManager.getmTransitionArea());
      Path alternatePath = aStarPath.getPath(gridSource.getColumnIndex(), gridSource.getRowIndex(),
              gridDestination.getColumnIndex(), gridDestination.getRowIndex());

//          aStarPath.printPath(gridSource.getColumnIndex(), gridSource.getRowIndex(),
//                  gridDestination.getColumnIndex(), gridDestination.getRowIndex());
      // Calculate the control point of the bezier curve that should be made
      ArrayList<BezierPoint> pathPoints = new ArrayList<BezierPoint>();
      int deviationSourceX = 0;
      int deviationSourceY = 0;
      int deviationTargetX = 0;
      int deviationTargetY = 0;

      for (int i = 0; i < alternatePath.getLength(); i++) {
        BezierPoint point
                = new BezierPoint(mWorkSpace.mGridManager
                        .getmTransitionArea()[alternatePath.getY(i)][alternatePath.getX(i)].getCenterX(), mWorkSpace.mGridManager.getmTransitionArea()[alternatePath.getY(i)][alternatePath.getX(i)].getCenterY());

        pathPoints.add(point);

        if (i < alternatePath.getLength() / 2 + 2) {
          deviationSourceX += (alternatePath.getX(i + 1) - alternatePath.getX(i));
          deviationSourceY += (alternatePath.getY(i + 1) - alternatePath.getY(i));
        } else if ((i >= alternatePath.getLength() / 2 - 2) && (i < alternatePath.getLength() - 1)) {
          deviationTargetX += (alternatePath.getX(i + 1) - alternatePath.getX(i));
          deviationTargetY += (alternatePath.getY(i + 1) - alternatePath.getY(i));
        }
      }

      int thresholdSourceX = 0;
      int thresholdSourceY = 0;
      int thresholdTargetX = 0;
      int thresholdTargetY = 0;

      // Indicate vertical movement tendency for source node
      if (Math.abs(deviationSourceX) >= Math.abs(deviationSourceY)) {

        // System.out.println("Vertical movement source." + deviationSourceX + "," + deviationSourceY);
        if (deviationSourceY > 0) {
          thresholdSourceX = 100;
        } else if (deviationSourceY < 0) {
          thresholdSourceX = -100;
        }
      } // Indicate horizontal movement tendency for source node
      else {

        // System.out.println("Horizontal movement source." + deviationSourceX + "," + deviationSourceY);
        if (deviationSourceX > 0) {
          thresholdSourceY = 100;
        } else if (deviationSourceX < 0) {
          thresholdSourceY = -100;
        }
      }

      // Indicate vertical movement tendency for target node
      if (Math.abs(deviationTargetX) >= Math.abs(deviationTargetY)) {

        // System.out.println("Vertical movement target." + deviationTargetX + "," + deviationTargetY);
        if (deviationTargetY > 0) {
          thresholdTargetX = -100;
        } else if (deviationTargetY < 0) {
          thresholdTargetX = 100;
        }
      } // Indicate horizontal movement tendency for target node
      else {

        // System.out.println("Horizontal movement target." + deviationTargetX + "," + deviationTargetY);
        if (deviationTargetX > 0) {
          thresholdTargetY = -100;
        } else if (deviationTargetX < 0) {
          thresholdTargetY = 100;
        }
      }

      BezierFit bezierFit = new BezierFit();
      BezierPoint[] controlPoint = bezierFit.bestFit(pathPoints);
      DockingPoint sourceDockingPoint = new DockingPoint(mSourceGUINode,
              new Point2D.Double(controlPoint[1].getX(), controlPoint[1].getY()));
      DockingPoint targetDockingPoint = new DockingPoint(mTargetGUINode,
              new Point2D.Double(controlPoint[2].getX(), controlPoint[2].getY()));

      if ((sourceDockingPoint.getIntersectionX() > -1) && (sourceDockingPoint.getIntersectionY() > -1)) {
        mSourceGUINodeDockPoint = mSourceGUINode.connectEdgeAtSourceNode(mGUIEdge,
                new Point(sourceDockingPoint.getIntersectionX(), sourceDockingPoint.getIntersectionY()));
      }

      if ((targetDockingPoint.getIntersectionX() > -1) && (targetDockingPoint.getIntersectionY() > -1)) {
        mTargetGUINodeDockPoint = mTargetGUINode.connectEdgetAtTargetNode(mGUIEdge,
                new Point(targetDockingPoint.getIntersectionX(), targetDockingPoint.getIntersectionY()));
      }

      // Manipulate the control point based on the BezierFit calculation
      mGUIEdge.mEg.mCCrtl1.x = (int) Math.round(controlPoint[1].getX()) + thresholdSourceX;
      mGUIEdge.mEg.mCCrtl1.y = (int) Math.round(controlPoint[1].getY()) + thresholdSourceY;
      mGUIEdge.mEg.mCCrtl2.x = (int) Math.round(controlPoint[2].getX()) + thresholdTargetX;
      mGUIEdge.mEg.mCCrtl2.y = (int) Math.round(controlPoint[2].getY()) + thresholdTargetY;

      // getEdgeTotalWeight();
      // setGridWeight();
      mWorkSpace.add(mGUIEdge);
      mWorkSpace.revalidate();
      mWorkSpace.repaint(100);
    } else {

      // setGridWeight();
      mWorkSpace.add(mGUIEdge);
      mWorkSpace.revalidate();
      mWorkSpace.repaint(100);
    }
  }

//  public void setGridWeight() {
//      mWorkSpace.getGridManager().setEdgeWeight(mGUIEdge);
//      mWorkSpace.getGridManager().setNodeWeight(mSourceGUINode);
//      mWorkSpace.getGridManager().setNodeWeight(mTargetGUINode);
//  }
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

      //
      mLastTargetGUINodeDockPoint = mGUIEdge.mLastTargetNodeDockPoint;
    } else {
      mSourceGUINodeDockPoint = mSourceGUINode.disconnectEdge(mGUIEdge);
      mLastTargetGUINodeDockPoint = mGUIEdge.mLastTargetNodeDockPoint;
    }
    cleanUpData();
  }

  public void delete() {

    // Disconnect the GUI edge from the GUI nodes
    if (mSourceGUINode.equals(mTargetGUINode)) {
      mSourceGUINodeDockPoint = mSourceGUINode.disconnectEdge(mGUIEdge);
      mTargetGUINodeDockPoint = mTargetGUINode.disconnectSelfPointingEdge(mGUIEdge);
    } else {
      mSourceGUINodeDockPoint = mSourceGUINode.disconnectEdge(mGUIEdge);
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
