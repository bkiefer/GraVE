package de.dfki.grave.editor;

import java.awt.*;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * @author Patrick Gebhard
 * This class holds all graphical data that are needed to draw an edge
 * from a start node to an end node
 *
 * this class is responsible for handling the curve, especially during
 * dragging, such that the edge view object is only affected when the mouse
 * is released.
 */
public final class EdgeArrow {

  // position of selected element
  public final static int S = 0, C1 = 1, C2 = 2, E = 3;

  public CubicCurve2D.Double mCurve = null;
  public CubicCurve2D.Double mLeftCurve = null;

  // Information about the selected element (start, ctrl1, ctrl2, end)
  public short mSelected = 0;

  /** this is necessary to determine the location of the expression text field */
  private void getMidPoint() {
    mLeftCurve = (CubicCurve2D.Double) mCurve.clone();
    CubicCurve2D.subdivide(mCurve, mLeftCurve, null);
  }

  public void computeCurve(Point start, Point ctrlStart,
      Point ctrlEnd, Point end) {
    // setup curve
    mCurve = new CubicCurve2D.Double(
        start.x, start.y, ctrlStart.x, ctrlStart.y,
        ctrlEnd.x, ctrlEnd.y, end.x, end.y);
    getMidPoint();
  }

  /** get bounds of curve */
  public Rectangle computeBounds() {
    Rectangle bounds = mCurve.getBounds();
    final int r = 7;
    // Add small rectangles for end and ctrl points
    for (Point2D p: getCoords()) {
      Rectangle2D rect = new Rectangle2D.Double(
          p.getX() - r, p.getY() - r, r + r, r + r);
      bounds.add(rect);
    }
      
    return bounds;
  }

  /** Clear the selection mask */
  public void deselectMCs() {
    mSelected = -1;
  }


  /** the edge curve control points as array (S, SC, EC, E) */
  private Point2D[] getCoords() {
    return new Point2D[] {
        mCurve.getP1(), mCurve.getCtrlP1(), mCurve.getCtrlP2(), mCurve.getP2()
    };
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

  public void mouseDragged(Edge e, Point p) {
    if (mSelected < 0 || mSelected > 3) return;
    Point2D[] ctrl = getCoords();
    ctrl[mSelected].setLocation(p);
    mCurve.setCurve(ctrl, 0);
    getMidPoint();
  }


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
    /*
    Graphics2D graphics = (Graphics2D) mEdge.getGraphics();
    graphics.setColor(Color.RED.darker());
    graphics.setStroke(new BasicStroke(1.0f));
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    */
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
      int[] mXPoints = {
          (int) (x1 + ox), (int) (x2 + ox), (int) (x2 - ox), (int) (x1 - oy)
      };
      int[] mYPoints = {
          (int) (y1 + oy), (int) (y2 + oy), (int) (y2 - oy), (int) (y1 - oy)
      };

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
    double mArrowDir =
        Math.atan2(mCurve.ctrlx2 - mCurve.x2, mCurve.ctrly2 - mCurve.y2);

    double mArrow1Point;
    double mArrow2Point;
    final int sz = 12;
    mArrow1Point = Math.sin(mArrowDir - .5);
    mArrow2Point = Math.cos(mArrowDir - .5);
    mHead.addPoint((int)(mCurve.x2 + (mArrow1Point * sz)),
        (int)(mCurve.y2 + (mArrow2Point * sz)));
    mArrow1Point = Math.sin(mArrowDir + .5);
    mArrow2Point = Math.cos(mArrowDir + .5);
    mHead.addPoint((int)(mCurve.x2 + (mArrow1Point * sz)),
        (int)(mCurve.y2 + (mArrow2Point * sz)));
    mHead.addPoint((int)mCurve.x2, (int)mCurve.y2);
    return mHead;
  }



  public void paintArrow(java.awt.Graphics2D g, float lineWidth, Color color) {
    g.draw(mCurve);
    Polygon mHead = computeHead();
    // if selected draw interface control points
    if (mSelected >= 0
        // || mSelected < 0 // debugging: show them always
        ) {
      final int r = 6, d = 2*r;
      g.setColor(mSelected == C1 ? color : Color.DARK_GRAY);
      g.drawLine((int) mCurve.x1, (int) mCurve.y1, (int) mCurve.ctrlx1,
          (int) mCurve.ctrly1);
      g.drawOval((int) mCurve.ctrlx1 - r, (int) mCurve.ctrly1 - r, d, d);
      g.fillOval((int) mCurve.ctrlx1 - r, (int) mCurve.ctrly1 - r, d, d);
      g.setColor(Color.DARK_GRAY);

      g.setColor(mSelected == C2 ? color : Color.DARK_GRAY);
      g.drawLine((int) mCurve.x2, (int) mCurve.y2, (int) mCurve.ctrlx2,
              (int) mCurve.ctrly2);
      g.drawOval((int) mCurve.ctrlx2 - r, (int) mCurve.ctrly2 - r, d, d);
      g.fillOval((int) mCurve.ctrlx2 - r, (int) mCurve.ctrly2 - r, d, d);
      g.setColor(Color.DARK_GRAY);

      g.fillRect((int) mCurve.x1 - r, (int) mCurve.y1 - r, d, d);
      g.drawRect((int) mCurve.x1 - r, (int) mCurve.y1 - r, d, d);
      // This draws the arrow head
      g.drawPolygon(mHead);
      g.fillPolygon(mHead);
    } else {
      // This draws the arrow head
      g.fillPolygon(mHead);
      g.setColor(color);
      g.drawPolygon(mHead);
    }
  }

}
