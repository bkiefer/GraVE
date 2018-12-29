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
 * this class is responsible for handling the curve, especially during
 * dragging, such that the edge view object is only affected when the mouse
 * is released.
 */
public final class EdgeGraphics {

  // position of selected element
  public final static int S = 0, C1 = 1, C2 = 2, E = 3;

  public CubicCurve2D.Double mCurve = null;
  public CubicCurve2D.Double mLeftCurve = null;

  // Information about the selected element (start, ctrl1, ctrl2, end)
  public short mSelected = 0;

  public EdgeGraphics(Edge e) {
    updateDrawingParameters(e);
  }

  public void updateDrawingParameters(Edge e) {
    deselectMCs();
    Point[] mCtrl = new Point[]{
        e.getStart(), e.getStartCtrl(), e.getEndCtrl(), e.getEnd()
    };

    computeCurve(mCtrl);
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

  public void mouseDragged(Edge e, Point p) {
    if (mSelected < 0 || mSelected > 3) return;
    Point2D[] ctrl = getCoords();
    ctrl[mSelected].setLocation(p);
    mCurve.setCurve(ctrl, 0);
    computeBounds(e);
  }

  private void computeBounds(Edge mEdge) {
    // set bounds of edge
    Rectangle bounds = mCurve.getBounds();

    /* add some boundaries */
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

    // TODO: This sucks big time. Overwriting a component's paintComponent
    // means we're painting in *relative* not absolute positions, which is
    // why this is added: this way, all edge positions are relative to (0,0)
    // add (0,0) for flickerfree edge display
    bounds.add(0, 0);

    // set the components bounds
    mEdge.setBounds(bounds);
  }

  private void computeCurve(Point[] mCtrl) {
    // make sure that edge is still in the limits of the workspace
    /*
    if (mCtrl[1].y < 0) {
      mCtrl[1].y = mCtrl[2].y;
    }
    */
    // setup curve
    mCurve = new CubicCurve2D.Double(
        mCtrl[S].x, mCtrl[S].y,
        mCtrl[C1].x, mCtrl[C1].y,
        mCtrl[C2].x, mCtrl[C2].y,
        mCtrl[E].x, mCtrl[E].y);
    mLeftCurve = (CubicCurve2D.Double) mCurve.clone();
    CubicCurve2D.subdivide(mCurve, mLeftCurve, null);
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
    g.draw(mCurve);
    Polygon mHead = computeHead();
    // if selected draw interface control points
    //g.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_BUTT,
    //    BasicStroke.JOIN_MITER));
    if (mSelected >= 0// || mSelected < 0 // debugging: show them always
        ) {
      //g.setColor(Color.DARK_GRAY);
      //g.setStroke(new BasicStroke(0.5f));

      // TODO: relative points would be nicer (see computeBounds)
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
