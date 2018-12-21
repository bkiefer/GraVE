package de.dfki.vsm.editor.util;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.CubicCurve2D;
import java.util.ArrayList;
import java.util.Collection;

import de.dfki.vsm.editor.Edge;
import de.dfki.vsm.editor.Node;
import de.dfki.vsm.model.flow.AbstractEdge;
import de.dfki.vsm.model.flow.geom.ControlPoint;
import de.dfki.vsm.model.flow.geom.EdgeArrow;
import math.geom2d.Point2D;
import math.geom2d.line.Line2D;
import math.geom2d.spline.CubicBezierCurve2D;

/**
 * @author Patrick Gebhard
 * This class holds all graphical data that are needed to draw an edge
 * from a start node to an end node
 *
 * TODO: start and end point are duplicated in the docking points on start and
 * end node. Is that necessary? NO.
 *
 * TODO: the control points should be vectors *relative to the start resp end
 * point*, and not absolute locations. That would make zooming a piece of cake.
 *
 * TODO: Like all classes in this project, i suspect that a lot of the fields
 * in this class should rather be local variables of some functions.
 */
public final class EdgeGraphics {
  private final static int mCCtrmin = 15; //MIN POSITION OF THE CONTROLPOINTS OF THE EDGE

  private Edge mEdge = null;

  public CubicCurve2D.Double mCurve = null;
  public CubicCurve2D.Double mLeftCurve = null;
  public Point mAbsoluteStartPos = new Point();
  public Point mAbsoluteEndPos = new Point();
  public Point mCCrtl1 = new Point();
  public Point mCCrtl2 = new Point();

  // variables for head
  public Polygon mHead = new Polygon();

  // general flags
  private boolean mPointingToSameNode = false;
  public double mArrowDir;

  public EdgeGraphics(Edge e, Point sourceDockpoint, Point targetDockpoint) {
    mEdge = e;
    AbstractEdge mDataEdge = e.getDataEdge();

    // check if edge has already graphic information in data model
    if (mDataEdge.getArrow() != null) {
      ArrayList<ControlPoint> curvePoints = mDataEdge.getArrow().getPointList();

      // if curve's data model isn't consistent on graphical data, init edge!
      if (curvePoints.size() != 2) {
        initEdgeGraphics(sourceDockpoint, targetDockpoint);
      } else {

        mAbsoluteStartPos.setLocation(curvePoints.get(0).getXPos(), curvePoints.get(0).getYPos());
        mAbsoluteEndPos.setLocation(curvePoints.get(1).getXPos(), curvePoints.get(1).getYPos());
        mCCrtl1.setLocation(curvePoints.get(0).getCtrlXPos(), curvePoints.get(0).getCtrlYPos());
        mCCrtl2.setLocation(curvePoints.get(1).getCtrlXPos(), curvePoints.get(1).getCtrlYPos());

        mAbsoluteStartPos = mEdge.getSourceNode().connectAsSource(mEdge, mAbsoluteStartPos);
        mAbsoluteEndPos = mEdge.getTargetNode().connectAsTarget(mEdge, mAbsoluteEndPos);
      }
    } else {
      initEdgeGraphics(sourceDockpoint, targetDockpoint);
    }

    computeCurve();
    updateDataModel();
    computeBounds();
  }

  private void computeBounds() {

    // set bounds of edge
    Rectangle bounds = mCurve.getBounds();

    Point[] mCurveControlPoints = {
        mAbsoluteStartPos, mCCrtl1, mCCrtl2, mAbsoluteEndPos
    };
    for (Point p : mCurveControlPoints) {
      bounds.add(p);
    }

    // add safty boundaries
    bounds.add(new Point(bounds.x - 10, bounds.y - 10));
    bounds.width = bounds.width + 10;
    bounds.height = bounds.height + 10;

    // check if a badge is there and add its bounds
    String desc = mEdge.getDescription();
    if (mEdge.getGraphics() != null && desc != null) {
      FontRenderContext renderContext = ((Graphics2D) mEdge.getGraphics()).getFontRenderContext();
      GlyphVector glyphVector = mEdge.getFont().createGlyphVector(renderContext, desc);
      Rectangle visualBounds = glyphVector.getVisualBounds().getBounds();

      bounds.add(this.mLeftCurve.x2 - visualBounds.width / 2 - 5,
              this.mLeftCurve.y2 - visualBounds.height / 2 - 2);
      bounds.add(this.mLeftCurve.x2 + visualBounds.width / 2 + 5,
              this.mLeftCurve.y2 + visualBounds.height / 2 + 2);
    }

    // add (0,0) for flickerfree edge display
    bounds.add(0, 0);

    // set the components bounds
    mEdge.setBounds(bounds);
    mEdge.setSize(bounds.width, bounds.height);
  }

  public void initEdgeGraphics(Point sourceDockPoint, Point targetDockPoint) {
    if ((sourceDockPoint != null) && (targetDockPoint != null)) {
      mAbsoluteStartPos = sourceDockPoint;
      mAbsoluteEndPos = targetDockPoint;
    } else {
      getShortestDistance();
    }

    mAbsoluteStartPos = mEdge.getSourceNode().connectAsSource(mEdge, mAbsoluteStartPos);
    mAbsoluteEndPos = mEdge.getTargetNode().connectAsTarget(mEdge, mAbsoluteEndPos);

    initCurve();
  }

  public void updateDrawingParameters() {
    Node source = mEdge.getSourceNode();
    Node target = mEdge.getTargetNode();
    if (!mEdge.mCEPSelected) {
      mAbsoluteStartPos = source.getEdgeDockPoint(mEdge);
      if (source != target) {
        mAbsoluteEndPos = target.getEdgeDockPoint(mEdge);
      } else {
        mAbsoluteEndPos = target.getSelfPointingEdgeDockPoint(mEdge);
      }
    }

    computeCurve();
    updateDataModel();
    computeBounds();
  }

  public void initCurve() {
    Node source = mEdge.getSourceNode();
    Node target = mEdge.getTargetNode();

    // compute bezier control points (using node center point and edge connection points)
    Point sNC = source.getCenterPoint();
    Point tNC = target.getCenterPoint();
    Point cES = new Point(mAbsoluteStartPos.x - sNC.x, mAbsoluteStartPos.y - sNC.y);
    Point cET = new Point(mAbsoluteEndPos.x - tNC.x, mAbsoluteEndPos.y - tNC.y);
    // scale control point in relation to distance between nodes
    double distance = Point.distance(sNC.x, sNC.y, tNC.x, tNC.y);
    double scalingFactor = (mPointingToSameNode)
            ? 3
            : ((distance / source.getHeight()) - 0.5d);

    scalingFactor = (scalingFactor < 1.0d)
            ? 1.25d
            : scalingFactor;
    mCCrtl1 = new Point((int) (sNC.x + scalingFactor * cES.x), (int) (sNC.y + scalingFactor * cES.y));
    mCCrtl2 = new Point((int) (tNC.x + scalingFactor * cET.x), (int) (tNC.y + scalingFactor * cET.y));
    sanitizeControlPoint();
  }

  private void sanitizeControlPoint() {
    mCCrtl1.x = mCCrtl1.x < mCCtrmin ? mCCtrmin : mCCrtl1.x;
    mCCrtl1.y = mCCrtl1.y < mCCtrmin ? mCCtrmin : mCCrtl1.y;
    mCCrtl2.x = mCCrtl2.x < mCCtrmin ? mCCtrmin : mCCrtl2.x;
    mCCrtl2.y = mCCrtl2.y < mCCtrmin ? mCCtrmin : mCCrtl2.y;
  }

  private void computeCurve() {

    // set bezier start end and control points
    Point[] mCurveControlPoints = {
        mAbsoluteStartPos, mCCrtl1, mCCrtl2, mAbsoluteEndPos
    };

    // make sure that edge is still in the limits of the workspace
    if (mCurveControlPoints[1].y < 0) {
      mCurveControlPoints[1].y = mCurveControlPoints[2].y;
    }
    // setup curve
    mCurve = new CubicCurve2D.Double(mCurveControlPoints[0].x, mCurveControlPoints[0].y,
            mCurveControlPoints[1].x, mCurveControlPoints[1].y,
            mCurveControlPoints[2].x, mCurveControlPoints[2].y,
            mCurveControlPoints[3].x, mCurveControlPoints[3].y);
    mLeftCurve = (CubicCurve2D.Double) mCurve.clone();
    CubicCurve2D.subdivide(mCurve, mLeftCurve, null);
  }

  public void computeHead() {
    // build arrow head
    mHead.reset();
    mArrowDir = Math.atan2(mCurve.ctrlx2 - mAbsoluteEndPos.x, mCurve.ctrly2 - mAbsoluteEndPos.y);

    // TODO corrected to the arrow heads direction
    ////System.out.println("arrow dir angle " + Math.toDegrees(mArrowDir) + "(" + mArrowDir + ")");
    // double angletoEndPoints = Math.atan2(mAbsoluteStartPos.x - mAbsoluteEndPos.x, mAbsoluteStartPos.y - mAbsoluteEndPos.y);
    ////System.out.println("angle between end points " + Math.toDegrees(angletoEndPoints) + "(" + angletoEndPoints + ")");
    // mArrowDir = (mArrowDir * 9 + angletoEndPoints) / 10;
    double mArrow1Point;
    double mArrow2Point;
    mArrow1Point = Math.sin(mArrowDir - .5);
    mArrow2Point = Math.cos(mArrowDir - .5);
    mHead.addPoint(mAbsoluteEndPos.x + (int) (mArrow1Point * 12), mAbsoluteEndPos.y + (int) (mArrow2Point * 12));
    mArrow1Point = Math.sin(mArrowDir + .5);
    mArrow2Point = Math.cos(mArrowDir + .5);
    mHead.addPoint(mAbsoluteEndPos.x + (int) (mArrow1Point * 12), mAbsoluteEndPos.y + (int) (mArrow2Point * 12));
    mHead.addPoint(mAbsoluteEndPos.x, mAbsoluteEndPos.y);
  }

  private void getShortestDistance() {
    Node source = mEdge.getSourceNode();
    Node target = mEdge.getTargetNode();

    ArrayList<Point> freeSourceNodeDockPoints = source.getEdgeStartPoints();
    ArrayList<Point> freeTargetNodeDockPoints = target.getEdgeStartPoints();
    Point startPos = new Point();
    Point endPos = new Point();

    // 1. case - start node and target node are different
    // 2. case - start node and target node are the same
    if (!source.equals(target)) {

      // figure the shortest distance
      double dist = -1.0d;

      for (Point p : freeSourceNodeDockPoints) {

        // DEBUG //System.out.println("Source Dock point absolute location " + (p.x) + ", " + (p.y));
        for (Point q : freeTargetNodeDockPoints) {
          double actualDist = Point.distance(p.x, p.y, q.x, q.y);

          // add distance from dockPoint to nodes' center point
          actualDist += Point.distance(source.getCenterPoint().x, source.getCenterPoint().y, p.x,
                  p.y);
          actualDist += Point.distance(target.getCenterPoint().x, target.getCenterPoint().y, q.x,
                  q.y);
          dist = (dist == -1.0d)
                  ? actualDist
                  : dist;

          if (actualDist < dist) {
            dist = actualDist;
            startPos.setLocation(p.x, p.y);
            mAbsoluteStartPos = startPos;
            endPos.setLocation(q.x, q.y);
            mAbsoluteEndPos = endPos;
          }

          // DEBUG //System.out.println("Target Dock point absolute location " + (q.x) + ", " + (q.y) + " distance: " + actualDist);
        }

        // DEBUG //System.out.println("Shortest distance: " + dist);
      }
    } else {
      mPointingToSameNode = true;

      double dist = -1;
      boolean done = false;

      // let the start and end point bet placed at least one third of the mean
      // of width and height od nodes away from each other
      double minDist = (source.getHeight() + source.getWidth()) / 2 / 3;

      for (Point p : freeSourceNodeDockPoints) {
        for (Point q : freeSourceNodeDockPoints) {
          if (!q.equals(p)) {
            dist = Point.distance(p.x, p.y, q.x, q.y);

            if ((dist > minDist) && !done) {
              startPos.setLocation(p.x, p.y);
              mAbsoluteStartPos = startPos;
              endPos.setLocation(q.x, q.y);
              mAbsoluteEndPos = endPos;
              done = true;
            }
          }
        }
      }
    }
  }

  public void updateRelativeEdgeControlPointPos(Node n, int xOffset, int yOffset) {
    if (n.equals(mEdge.getSourceNode())) {
      mCCrtl1.x = mCCrtl1.x + xOffset;
      mCCrtl1.y = mCCrtl1.y + yOffset;
    }

    if (n.equals(mEdge.getTargetNode())) {
      mCCrtl2.x = mCCrtl2.x + xOffset;
      mCCrtl2.y = mCCrtl2.y + yOffset;
    }
//        mCCrtl1.y = (mCCrtl1.y < mCCtrmin) ? mCCtrmin : mCCrtl1.y;
//        mCCrtl2.y = (mCCrtl2.y < mCCtrmin) ? mCCtrmin : mCCrtl2.y;
    sanitizeControlPoint();
    updateDataModel();
  }

  private boolean controlPointHandlerContainsPoint(Point point, int threshold) {
    if (controlPoint1HandlerContainsPoint(point, threshold)) {
      return true;
    }

    if (controlPoint2HandlerContainsPoint(point, threshold)) {
      return true;
    }

    return false;
  }

  public boolean controlPoint1HandlerContainsPoint(Point point, int threshold) {

    // Debug //System.out.println("is point " + point + " in (e 5) " + mEg.mCCrtl1.x + ", " + mEg.mCCrtl1.y);
    if ((((int) mCCrtl1.x - threshold) < point.x) && (((int) mCCrtl1.x + threshold) > point.x)
            && (((int) mCCrtl1.y - threshold) < point.y) && (((int) mCCrtl1.y + threshold) > point.y)) {

      // Debug //System.out.println("\tyes!");
      return true;
    }

    return false;
  }

  public boolean controlPoint2HandlerContainsPoint(Point point, int threshold) {

    // Debug //System.out.println("is point " + point + " in (e 5) " + mEg.mCCrtl2.x + ", " + mEg.mCCrtl2.y);
    if ((((int) mCCrtl2.x - threshold) < point.x) && (((int) mCCrtl2.x + threshold) > point.x)
            && (((int) mCCrtl2.y - threshold) < point.y) && (((int) mCCrtl2.y + threshold) > point.y)) {

      // Debug //System.out.println("\tyes!");
      return true;
    }

    return false;
  }

  public boolean curveStartPointContainsPoint(Point point, int threshold) {

    // Debug //System.out.println("cep check is point " + point + " in (e 5) " + mEg.mAbsoluteStartPos.x + ", " + mEg.mAbsoluteStartPos.y);
    if ((((int) mAbsoluteStartPos.x - threshold) < point.x) && (((int) mAbsoluteStartPos.x + threshold) > point.x)
            && (((int) mAbsoluteStartPos.y - threshold) < point.y)
            && (((int) mAbsoluteStartPos.y + threshold) > point.y)) {

      // Debug //System.out.println("\tyes!");
      return true;
    }

    return false;
  }

  public boolean curveEndPointContainsPoint(Point point, int threshold) {

    // Debug //System.out.println("csp check is point " + point + " in (e 5) " + mEg.mAbsoluteEndPos.x + ", " + mEg.mAbsoluteEndPos.y);
    if ((((int) mAbsoluteEndPos.x - threshold) < point.x) && (((int) mAbsoluteEndPos.x + threshold) > point.x)
            && (((int) mAbsoluteEndPos.y - threshold) < point.y)
            && (((int) mAbsoluteEndPos.y + threshold) > point.y)) {

      // Debug //System.out.println("\tyes!");
      return true;
    }

    return false;
  }

  public boolean curveContainsPoint(Point point) {

    // check if point is inside the control point handlers
    if (controlPointHandlerContainsPoint(point, 10)) {
      return true;
    }

    if (curveEndPointContainsPoint(point, 10)) {
      return true;
    }

    if (curveStartPointContainsPoint(point, 10)) {
      return true;
    }

    double x1, x2, y1, y2;
    Point[] mCoordList = new Point[4];    // the edge curve control points
    mCoordList[0] = new Point((int) mCurve.x1, (int) mCurve.y1);
    mCoordList[1] = new Point((int) mCurve.ctrlx1, (int) mCurve.ctrly1);
    mCoordList[2] = new Point((int) mCurve.ctrlx2, (int) mCurve.ctrly2);
    mCoordList[3] = new Point((int) mCurve.x2, (int) mCurve.y2);
    x1 = mCoordList[0].x;
    y1 = mCoordList[0].y;

    // Debug - draw what is computed
//      Graphics2D graphics = (Graphics2D) mEdge.getGraphics();
//      graphics.setColor(Color.RED.darker());
//      graphics.setStroke(new BasicStroke(1.0f));
//      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    // use Berstein polynomials for curve approximation
    double t;         // step interval
    double k = .1;    // .025;   // setp increment

    for (t = k; t <= 1; t += k) {
      x2 = (mCoordList[0].x + t * (-mCoordList[0].x * 3 + t * (3 * mCoordList[0].x - mCoordList[0].x * t)))
              + t * (3 * mCoordList[1].x + t * (-6 * mCoordList[1].x + mCoordList[1].x * 3 * t))
              + t * t * (mCoordList[2].x * 3 - mCoordList[2].x * 3 * t) + mCoordList[3].x * t * t * t;
      y2 = (mCoordList[0].y + t * (-mCoordList[0].y * 3 + t * (3 * mCoordList[0].y - mCoordList[0].y * t)))
              + t * (3 * mCoordList[1].y + t * (-6 * mCoordList[1].y + mCoordList[1].y * 3 * t))
              + t * t * (mCoordList[2].y * 3 - mCoordList[2].y * 3 * t) + mCoordList[3].y * t * t * t;

      // normalize lineVector
      double nx2 = x2 - x1;
      double ny2 = y2 - y1;

      // compute normal vector
      double ox = -ny2;
      double oy = nx2;

      // resize it
      double len = Math.sqrt((ox * ox) + (oy * oy));

      ox = ox / len * 5;
      oy = oy / len * 5;

      // build rectangular polygon around curve vector:
      //
      // 0----------1
      // |          |
      // x---------->
      // |          |
      // 3----------2
      int[] mXPoints = new int[4];
      int[] mYPoints = new int[4];

      mXPoints[0] = (int) (x1 + ox);
      mYPoints[0] = (int) (y1 + oy);
      mXPoints[1] = (int) (x2 + ox);
      mYPoints[1] = (int) (y2 + oy);
      mXPoints[2] = (int) (x2 - ox);
      mYPoints[2] = (int) (y2 - oy);
      mXPoints[3] = (int) (x1 - ox);
      mYPoints[3] = (int) (y1 - oy);

      Polygon lineHull = new Polygon(mXPoints, mYPoints, 4);

      // Debug
      // graphics.drawPolygon(lineHull);
      // is clicked point inside polygon
      if (lineHull.contains(point)) {
        return true;
      }

      x1 = x2;
      y1 = y2;
    }

    return false;
  }

  private void updateDataModel() {

    // add the graphic information to the sceneflow!
    EdgeArrow arrow = new EdgeArrow();
    ArrayList<ControlPoint> xmlEdgePoints = new ArrayList<>();
    ControlPoint startPoint = new ControlPoint();

    startPoint.setXPos((int) mCurve.x1);
    startPoint.setYPos((int) mCurve.y1);
    startPoint.setCtrlXPos((int) mCurve.ctrlx1);
    startPoint.setCtrlYPos((int) mCurve.ctrly1);

    ControlPoint endPoint = new ControlPoint();

    endPoint.setXPos((int) mCurve.x2);
    endPoint.setYPos((int) mCurve.y2);
    endPoint.setCtrlXPos((int) mCurve.ctrlx2);
    endPoint.setCtrlYPos((int) mCurve.ctrly2);
    xmlEdgePoints.add(startPoint);
    xmlEdgePoints.add(endPoint);
    arrow.setPointList(xmlEdgePoints);
    mEdge.getDataEdge().setArrow(arrow);
    mEdge.getDataEdge().setTargetUnid(mEdge.getTargetNode().getDataNode().getId());

    // TODO: straigthen edge, if source/targets node location has changed
  }

  public boolean isIntersectByRectangle(double x1, double x2, double y1, double y2) {
    CubicBezierCurve2D edgeBezier = new CubicBezierCurve2D(mAbsoluteStartPos.getX(),
            mAbsoluteStartPos.getY(), mCCrtl1.getX(), mCCrtl1.getY(),
            mCCrtl2.getX(), mCCrtl2.getY(), mAbsoluteEndPos.getX(),
            mAbsoluteEndPos.getY());

    // Get point of intersections on upper rectangle
    Line2D upperRect = new Line2D(x1, y1, x2, y1);
    Collection<Point2D> upperIntersection = edgeBezier.intersections(upperRect);

    if (!upperIntersection.isEmpty()) {

      // System.out.println("Found intersection on: " + getRowIndex() + "," + getColumnIndex());
      return true;
    }

    // Get point of intersections on left rectangle
    Line2D leftRect = new Line2D(x1, y1, x1, y2);
    Collection<Point2D> leftIntersection = edgeBezier.intersections(leftRect);

    if (!leftIntersection.isEmpty()) {

      // System.out.println("Found intersection on: " + getRowIndex() + "," + getColumnIndex());
      return true;
    }

    // Get point of intersections on right rectangle
    Line2D rightRect = new Line2D(x2, y1, x2, y2);
    Collection<Point2D> rightIntersection = edgeBezier.intersections(rightRect);

    if (!rightIntersection.isEmpty()) {

      // System.out.println("Found intersection on: " + getRowIndex() + "," + getColumnIndex());
      return true;
    }

    // Get point of intersections on right rectangle
    Line2D bottomRect = new Line2D(x1, y2, x2, y2);
    Collection<Point2D> bottomIntersection = edgeBezier.intersections(bottomRect);

    if (!bottomIntersection.isEmpty()) {

      // System.out.println("Found intersection on: " + getRowIndex() + "," + getColumnIndex());
      return true;
    }

    return false;
  }
}
