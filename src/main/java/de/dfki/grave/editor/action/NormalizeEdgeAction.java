
/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package de.dfki.grave.editor.action;

import de.dfki.grave.editor.panels.WorkSpace;
import de.dfki.grave.model.flow.AbstractEdge;

/**
 *
 * @author Souza Putra
 */
public class NormalizeEdgeAction extends ReshapeEdgeAction {

  /*
  protected GridRectangle gridSource = null;
  protected GridRectangle gridDestination = null;
  protected de.dfki.grave.editor.Node mSourceGUINode = null;
  protected de.dfki.grave.editor.Node mTargetGUINode = null;
  protected Point mSourceGUINodeDockPoint = null;
  protected Point mTargetGUINodeDockPoint = null;
  */

  public NormalizeEdgeAction(WorkSpace workSpace, AbstractEdge edge) {
    super(workSpace, edge);
  }

  protected void reshape() {
    mWorkSpace.normalizeEdge(mEdge);
  }

  /*
  public void recalculateWeight() {
    mWorkSpace.getGridManager().resetAllGridWeight();
    for (de.dfki.grave.editor.Edge edge : mWorkSpace.getEdges()) {
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

      // System.out.println("Smart Path initiated!");
      GridRectangle[][] transArea = mWorkSpace.getTransitionArea();
      AStarEdgeFinder aStarPath = new AStarEdgeFinder(transArea);

      aStarPath.setDiagonalPathCost(GridConstants.DIAGONAL_PATH_COST);

      Path alternatePath = aStarPath.getPath(gridSource.getColumnIndex(), gridSource.getRowIndex(),
              gridDestination.getColumnIndex(), gridDestination.getRowIndex());

//          aStarPath.printPath(gridSource.getColumnIndex(), gridSource.getRowIndex(),
//                  gridDestination.getColumnIndex(), gridDestination.getRowIndex());
      // Calculate the control point of the bezier curve that should be made
      ArrayList<Point2D.Double> pathPoints = new ArrayList<Point2D.Double>();
      int deviationSourceX = 0;
      int deviationSourceY = 0;
      int deviationTargetX = 0;
      int deviationTargetY = 0;

      for (int i = 0; i < alternatePath.getLength(); i++) {
        Point2D.Double point = new Point2D.Double(
            transArea[alternatePath.getY(i)][alternatePath.getX(i)].getCenterX(),
            transArea[alternatePath.getY(i)][alternatePath.getX(i)].getCenterY());

        transArea[alternatePath.getY(i)][alternatePath.getX(i)].setaStarPath(true);
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
      Point2D.Double[] controlPoint = bezierFit.bestFit(pathPoints);

      /* TODO: MAYBE REACTIVATE
      // Manipulate the control point based on the BezierFit calculation
      if (((int) Math.round(controlPoint[1].getX()) + thresholdSourceX) > 0) {
        mGUIEdge.mEg.mCCrtl1.x = (int) Math.round(controlPoint[1].getX()) + thresholdSourceX;
      } else {
        mGUIEdge.mEg.mCCrtl1.x = 50;
      }

      if (((int) Math.round(controlPoint[1].getY()) + thresholdSourceY) > 0) {
        mGUIEdge.mEg.mCCrtl1.y = (int) Math.round(controlPoint[1].getY()) + thresholdSourceY;
      } else {
        mGUIEdge.mEg.mCCrtl1.y = 50;
      }

      if (((int) Math.round(controlPoint[2].getX()) + thresholdTargetX) > 0) {
        mGUIEdge.mEg.mCCrtl2.x = (int) Math.round(controlPoint[2].getX()) + thresholdTargetX;
      } else {
        mGUIEdge.mEg.mCCrtl2.x = 50;
      }

      if (((int) Math.round(controlPoint[2].getY()) + thresholdTargetY) > 0) {
        mGUIEdge.mEg.mCCrtl2.y = (int) Math.round(controlPoint[2].getY()) + thresholdTargetY;
      } else {
        mGUIEdge.mEg.mCCrtl2.y = 50;
      }

      // System.out.println("Control Point 1: " + mGUIEdge.mEg.mCCrtl1.x + "," + mGUIEdge.mEg.mCCrtl1.y);
      // System.out.println("Control Point 2: " + mGUIEdge.mEg.mCCrtl2.x + "," + mGUIEdge.mEg.mCCrtl2.y);
      // getEdgeTotalWeight();
      // setGridWeight();
      DockingPoint sourceDockingPoint = new DockingPoint(mSourceGUINode,
              new Point2D.Double(mGUIEdge.mEg.mCCrtl1.x, mGUIEdge.mEg.mCCrtl1.y));
      DockingPoint targetDockingPoint = new DockingPoint(mTargetGUINode,
              new Point2D.Double(mGUIEdge.mEg.mCCrtl2.x, mGUIEdge.mEg.mCCrtl2.y));

      if ((sourceDockingPoint.getIntersectionX() > -1) && (sourceDockingPoint.getIntersectionY() > -1)) {
        mSourceGUINode.disconnectEdge(mGUIEdge);
        mSourceGUINodeDockPoint = mSourceGUINode.connectEdgeAtSourceNode(mGUIEdge,
                new Point(sourceDockingPoint.getIntersectionX(), sourceDockingPoint.getIntersectionY()));
      }

      if ((targetDockingPoint.getIntersectionX() > -1) && (targetDockingPoint.getIntersectionY() > -1)) {
        mTargetGUINode.disconnectEdge(mGUIEdge);
        mTargetGUINodeDockPoint = mTargetGUINode.connectEdgeAtTargetNode(mGUIEdge,
                new Point(targetDockingPoint.getIntersectionX(), targetDockingPoint.getIntersectionY()));
      }
       * /
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

    for (GridRectangle[] gridParent : mWorkSpace.getTransitionArea()) {
      for (GridRectangle gridRectangle : gridParent) {
        gridRectangle.setaStarPath(false);

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

        if (gridRectangle.isIntersectByRectangle(mGUIEdge)) {
          gridRectangle.setIntersectionType(GridRectangle.EDGE_INTERSECTION);
          sumWeight += gridRectangle.getWeight();
        }
      }
    }

    return sumWeight;
  }*/


  @Override
  protected String msg() { return "Normalize Edge"; }
}
