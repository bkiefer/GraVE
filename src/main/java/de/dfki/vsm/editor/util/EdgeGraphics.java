package de.dfki.vsm.editor.util;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import de.dfki.vsm.editor.Edge;

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
 *
 * THIS CLASS SHOULD BE RESPONSIBLE FOR HANDLING THE CURVE, ESPECIALLY DURING
 * DRAGGING, SUCH THAT THE EDGE VIEW OBJECT IS ONLY AFFECTED WHEN THE MOUSE
 * IS RELEASED.
 */
public final class EdgeGraphics {
  private final static int mCCtrmin = 15; //MIN POSITION OF THE CONTROLPOINTS OF THE EDGE

  // position of selected element
  public final static int S = 0, C1 = 1, C2 = 2, E = 3;

  public CubicCurve2D.Double mCurve = null;
  public CubicCurve2D.Double mLeftCurve = null;

  // I need these indepenent of the Edge for dragging!
  // NO: I use the points from the curve!
  //private Point[] mCtrl;

  // Information about the selected element (start, ctrl1, ctrl2, end)
  public short mSelected = 0;

  public EdgeGraphics(Edge e) {
    updateDrawingParameters(e);
  }

  // TODO: SANITIZE
  public void updateDrawingParameters(Edge e) {
    Point[] mCtrl = new Point[]{
        e.getStart(), e.getStartCtrl(), e.getEndCtrl(), e.getEnd()
    };

    /*
    AbstractEdge mDataEdge = e.getDataEdge();
    // check if edge has already graphic information in data model
    if (mDataEdge.getArrow() != null) {
      ArrayList<ControlPoint> curvePoints = mDataEdge.getArrow().getPointList();

      // if curve's data model isn't consistent on graphical data, init edge!
      if (curvePoints.size() != 2) {
        initEdgeGraphics(sourceDockpoint, targetDockpoint);
      } else {
        updateFromEdge();
      }
    } else {
      initEdgeGraphics(sourceDockpoint, targetDockpoint);
    }
    */

    computeCurve(mCtrl);
    //updateDataModel();
    computeBounds(e);
  }

  /** Clear the selection mask */
  public void deselectMCs() {
    mSelected = -1;
  }

  private Point2D[] getCoords() {
    Point2D[] mCoords = {    // the edge curve control points
        mCurve.getP1(), mCurve.getCtrlP1(), mCurve.getCtrlP2(), mCurve.getP2()
    };
    return mCoords;
  }


  /** On mouse click or press, set the selection mask for the control points */
  public void edgeSelected(Point p) {
    Point2D[] controlPoints = getCoords();
    short i = 0;
    for(Point2D cp : controlPoints) {
      if (cp.distance(p) < 10) {
        mSelected = i;
        return;
      }
      ++i;
    }
    mSelected = 4; // selected, but none of the control points
  }

  public void mouseDragged(Point p) {
    if (mSelected < 0 || mSelected > 3) return;
    Point2D[] ctrl = getCoords();
    ctrl[mSelected].setLocation(p);
    mCurve.setCurve(ctrl, 0);
  }

  private void computeBounds(Edge mEdge) {

    // set bounds of edge
    Rectangle bounds = mCurve.getBounds();
    /* ?? should already be included
    for (Point p : mCtrl) {
      bounds.add(p);
    }
    */

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

  /* NOT HERE
  public void initEdgeGraphics(Point sourceDockPoint, Point targetDockPoint) {
    if ((sourceDockPoint != null) && (targetDockPoint != null)) {
      //mCtrlPoints[S] = sourceDockPoint;
      //mCtrlPoints[E] = targetDockPoint;
    } else {
      getShortestDistance();
    }

    //mCtrlPoints[S] = mEdge.getSourceNode().connectAsSource(mEdge, mCtrlPoints[S]);
    //mCtrlPoints[E] = mEdge.getTargetNode().connectAsTarget(mEdge, mCtrlPoints[E]);

    initCurve();
  }
  */



  /** compute bezier control points
   *  (using node center point and edge connection points)
   */
  public static void initCurve(Point[] mCtrl, boolean isLoop,
      Point sNC,Point tNC) {
    /*
    Node source = mEdge.getSourceNode();
    Node target = mEdge.getTargetNode();

    Point sNC = source.getCenterPoint();
    Point tNC = target.getCenterPoint();
    */

    // Vector from Center to Dock
    Point cES = new Point(mCtrl[S].x - sNC.x, mCtrl[S].y - sNC.y);
    Point cET = new Point(mCtrl[E].x - tNC.x, mCtrl[E].y - tNC.y);

    // scale control point in relation to distance between nodes
    double scale = (isLoop)
            ? 3
            : Math.max(sNC.distance(tNC) / sNC.distance(mCtrl[S]) - 0.5, 1.25);

    mCtrl[C1] = new Point(
        (int) (sNC.x + scale * cES.x),
        (int) (sNC.y + scale * cES.y));
    mCtrl[C2] = new Point((int) (tNC.x + scale * cET.x),
        (int) (tNC.y + scale * cET.y));
    mCtrl[C1].x = mCtrl[C1].x < mCCtrmin ? mCCtrmin : mCtrl[C1].x;
    mCtrl[C1].y = mCtrl[C1].y < mCCtrmin ? mCCtrmin : mCtrl[C1].y;
    mCtrl[C2].x = mCtrl[C2].x < mCCtrmin ? mCCtrmin : mCtrl[C2].x;
    mCtrl[C2].y = mCtrl[C2].y < mCCtrmin ? mCCtrmin : mCtrl[C2].y;
  }

  /*
  private void sanitizeControlPoint() {
    mCtrl[C1].x = mCtrl[C1].x < mCCtrmin ? mCCtrmin : mCtrl[C1].x;
    mCtrl[C1].y = mCtrl[C1].y < mCCtrmin ? mCCtrmin : mCtrl[C1].y;
    mCtrl[C2].x = mCtrl[C2].x < mCCtrmin ? mCCtrmin : mCtrl[C2].x;
    mCtrl[C2].y = mCtrl[C2].y < mCCtrmin ? mCCtrmin : mCtrl[C2].y;
  }
  */

  private void computeCurve(Point[] mCtrl) {
    // make sure that edge is still in the limits of the workspace
    if (mCtrl[1].y < 0) {
      mCtrl[1].y = mCtrl[2].y;
    }
    // setup curve
    mCurve = new CubicCurve2D.Double(
        mCtrl[S].x, mCtrl[S].y,
        mCtrl[C1].x, mCtrl[C1].y,
        mCtrl[C2].x, mCtrl[C2].y,
        mCtrl[E].x, mCtrl[E].y);
    mLeftCurve = (CubicCurve2D.Double) mCurve.clone();
    CubicCurve2D.subdivide(mCurve, mLeftCurve, null);
  }

  // TODO: CAN BE DONE SIMPLER, IS WRONG HERE, ANYWAY
  /*
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
            mCtrl[S] = startPos;
            endPos.setLocation(q.x, q.y);
            mCtrl[E] = endPos;
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
              mCtrl[S] = startPos;
              endPos.setLocation(q.x, q.y);
              mCtrl[E] = endPos;
              done = true;
            }
          }
        }
      }
    }
  }

  /* Must be re-done anyway
  public void updateRelativeEdgeControlPointPos(Node n, int xOffset, int yOffset) {
    if (n.equals(mEdge.getSourceNode())) {
      mCtrl[C1].x = mCtrl[C1].x + xOffset;
      mCtrl[C1].y = mCtrl[C1].y + yOffset;
    }

    if (n.equals(mEdge.getTargetNode())) {
      mCtrl[C2].x = mCtrl[C2].x + xOffset;
      mCtrl[C2].y = mCtrl[C2].y + yOffset;
    }
//        mCtrlPoints[C1].y = (mCtrlPoints[C1].y < mCCtrmin) ? mCCtrmin : mCtrlPoints[C1].y;
//        mCtrlPoints[C2].y = (mCtrlPoints[C2].y < mCCtrmin) ? mCCtrmin : mCtrlPoints[C2].y;
    sanitizeControlPoint();
    updateDataModel();
  }
  */

  private boolean controlPointHandlerContainsPoint(Point point, int threshold) {
    return mCurve.getCtrlP1().distance(point) < threshold
        || mCurve.getCtrlP2().distance(point) < threshold;
  }

  public boolean curveContainsPoint(Point point) {
    Point2D[] ctrl = getCoords();

    // check if point is inside the control point handlers
    if (controlPointHandlerContainsPoint(point, 10)
        || mCurve.getP1().distance(point) < 10
        || mCurve.getP2().distance(point) < 10) {
      return true;
    }

    double x1 = ctrl[0].getX();
    double y1 = ctrl[0].getY();
    double x2, y2;

    // Debug - draw what is computed
//      Graphics2D graphics = (Graphics2D) mEdge.getGraphics();
//      graphics.setColor(Color.RED.darker());
//      graphics.setStroke(new BasicStroke(1.0f));
//      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    // use Bernstein polynomials for curve approximation
    double t;         // step interval
    double k = .1;    // .025;   // setp increment

    for (t = k; t <= 1; t += k) {
      x2 = (ctrl[0].getX()
          + t * (-ctrl[0].getX() * 3
              + t * (3 * ctrl[0].getX() - ctrl[0].getX() * t)))
          + t * (3 * ctrl[1].getX()
              + t * (-6 * ctrl[1].getX() + ctrl[1].getX() * 3 * t))
          + t * t * (ctrl[2].getX() * 3 - ctrl[2].getX() * 3 * t)
          + ctrl[3].getX() * t * t * t;
      y2 = (ctrl[0].getY()
          + t * (-ctrl[0].getY() * 3
              + t * (3 * ctrl[0].getY() - ctrl[0].getY() * t)))
          + t * (3 * ctrl[1].getY()
              + t * (-6 * ctrl[1].getY() + ctrl[1].getY() * 3 * t))
          + t * t * (ctrl[2].getY() * 3 - ctrl[2].getY() * 3 * t)
          + ctrl[3].getY() * t * t * t;

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

  /* NOT HERE!
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
  }*/

  /** How is that different from the one below? */
  public boolean isIntersectByRectangle(double x1, double x2, double y1, double y2) {
    return mCurve.intersects(new Rectangle2D.Double(x1, y1, x2, y2));
  }

  /*
  public boolean isIntersectByRectangle(double x1, double x2, double y1, double y2) {
    CubicBezierCurve2D edgeBezier = new CubicBezierCurve2D(
        mCtrl[S].getX(), mCtrl[S].getY(), mCtrl[C1].getX(), mCtrl[C1].getY(),
        mCtrl[C2].getX(), mCtrl[C2].getY(), mCtrl[E].getX(), mCtrl[E].getY());


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
  */

  public Polygon computeHead() {
    Polygon mHead = new Polygon();
    // build arrow head
    //mHead.reset();
    double mArrowDir =
        Math.atan2(mCurve.ctrlx2 - mCurve.x2, mCurve.ctrly2 - mCurve.y2);

    // TODO corrected to the arrow heads direction
    ////System.out.println("arrow dir angle " + Math.toDegrees(mArrowDir) + "(" + mArrowDir + ")");
    // double angletoEndPoints = Math.atan2(mCtrlPoints[S].x - mCtrlPoints[E].x, mCtrlPoints[S].y - mCtrlPoints[E].y);
    ////System.out.println("angle between end points " + Math.toDegrees(angletoEndPoints) + "(" + angletoEndPoints + ")");
    // mArrowDir = (mArrowDir * 9 + angletoEndPoints) / 10;
    double mArrow1Point;
    double mArrow2Point;
    mArrow1Point = Math.sin(mArrowDir - .5);
    mArrow2Point = Math.cos(mArrowDir - .5);
    mHead.addPoint((int)(mCurve.x2 + (mArrow1Point * 12)),
        (int)(mCurve.y2 + (mArrow2Point * 12)));
    mArrow1Point = Math.sin(mArrowDir + .5);
    mArrow2Point = Math.cos(mArrowDir + .5);
    mHead.addPoint((int)(mCurve.x2 + (mArrow1Point * 12)),
        (int)(mCurve.y2 + (mArrow2Point * 12)));
    mHead.addPoint((int)mCurve.x2, (int)mCurve.y2);
    return mHead;
  }



  public void paintArrow(java.awt.Graphics2D g, float lineWidth, Color color) {
    //updateDrawingParameters();
    g.setColor(color);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);
    g.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_BUTT,
            BasicStroke.JOIN_MITER));
    g.draw(mCurve);

    Polygon mHead = computeHead();
    // if selected draw interface control points
    g.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_MITER));
    if (mSelected != 0) {
      //g.setColor(Color.DARK_GRAY);
      //g.setStroke(new BasicStroke(0.5f));

      // TODO: CAN'T WE USE THE REAL POINTS INSTEAD OF THE CURVE POINTS?
      g.setColor(mSelected == C1 ? color : Color.DARK_GRAY);
      g.drawLine((int) mCurve.x1, (int) mCurve.y1, (int) mCurve.ctrlx1,
          (int) mCurve.ctrly1);
      g.drawOval((int) mCurve.ctrlx1 - 7, (int) mCurve.ctrly1 - 7, 14, 14);
      g.fillOval((int) mCurve.ctrlx1 - 7, (int) mCurve.ctrly1 - 7, 14, 14);
      g.setColor(Color.DARK_GRAY);

      g.setColor(mSelected == C2 ? color : Color.DARK_GRAY);
      g.drawLine((int) mCurve.x2, (int) mCurve.y2, (int) mCurve.ctrlx2,
              (int) mCurve.ctrly2);
      g.drawOval((int) mCurve.ctrlx2 - 7, (int) mCurve.ctrly2 - 7, 14, 14);
      g.fillOval((int) mCurve.ctrlx2 - 7, (int) mCurve.ctrly2 - 7, 14, 14);
      g.setColor(Color.DARK_GRAY);

      g.fillRect((int) mCurve.x1 - 7, (int) mCurve.y1 - 7, 14, 14);
      g.drawRect((int) mCurve.x1 - 7, (int) mCurve.y1 - 7, 14, 14);
      // This draws the arrow head
      g.drawPolygon(mHead);
      g.fillPolygon(mHead);
    } else {
      //g.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_BUTT,
      //        BasicStroke.JOIN_MITER));
      // This draws the arrow head
      g.fillPolygon(mHead);
      g.setColor(color);
      g.drawPolygon(mHead);
    }
  }

}
