package de.dfki.grave.model.flow;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.BitSet;

public class Geom {

  private static double[] num2angle;
  private static double[] angles; // sorted array of available angles
  private static int[] angle2num; // maps angle in angles back to numerical index
  private static int max_bits;

  /** primitive, but only used once */
  private static void sortAngles() {
    for (int i = 0; i < angles.length - 1; ++i) {
      for (int k = i + 1; k < angles.length; ++k) {
        if (angles[i] > angles[k]) {
          double val = angles[k];
          angles[k] = angles[i];
          angles[i] = val;
          int j = angle2num[k];
          angle2num[k] = angle2num[i];
          angle2num[i] = j;
        }
      }
    }
  }

  /** Initialize the geometry utilities with max no. of docking points */
  public static void initialize(int maxPoints) {
    num2angle = new double[maxPoints];
    angles = new double[maxPoints];
    angle2num = new int[maxPoints];
    for (int i = 0; i < maxPoints; ++i) {
      int k = i;
      double add = Math.PI;
      double res = 0;
      while (k > 0) {
        if ((k & 1) != 0) res += add;
        k >>= 1;
        add /= 2;
      }
      num2angle[i] = res;
      angles[i] = res;
      angle2num[i] = i;
    }
    sortAngles();
    max_bits = maxPoints;
  }

  public static Point add(Point a, Point b) {
    Point result = new Point(a);
    result.translate(b.x, b.y);
    return result;
  }

  public static double dotProd(Point a, Point b) {
    return a.x * b.x + a.y * b.y;
  }

  public static double dotProd(Point2D a, Point2D b) {
    return a.getX() * b.getX() + a.getY() * b.getY();
  }

  public static double norm2(Point a) {
    return Math.sqrt(dotProd(a, a));
  }

  public static double norm2(Point2D a) {
    return Math.sqrt(dotProd(a, a));
  }

  public static Point2D.Double smul(double f, Point p) {
    return new Point2D.Double(p.x * f, p.y * f);
  }

  public static int findClosestDock(BitSet taken, double angle, boolean target) {
    if (taken.cardinality() >= max_bits) return -1; // all docks taken
    // compute the closest angle
    int k = Arrays.binarySearch(angles, angle);
    if (k < 0) k = -k - 2;
    int kprime = k == angles.length - 1 ? 0 : k + 1;
    if (Math.abs(angle - angles[k]) > Math.abs(angle - angles[kprime])) {
      k = kprime;
    }
    int d = target ? 1 : 2;
    do { // alternate back and forth,
      // TODO: START WITH THE ONE THAT IS CLOSEST: I.E. IF k < angle < k-1
      // start with k-1, otherwise k+1
      if (! taken.get(angle2num[k])) break;
      k = ((d & 1) != 0) ? k + d/2 + 1 : k - d/2;
      if (k < 0) k += angle2num.length;
      ++d;
    } while (d < 2 * angle2num.length);
    return angle2num[k];
  }

  /** Returns a value between 0 .. 2*pi */
  public static double dockToAngle(int dock) {
    return num2angle[dock];
  }


  public static Point2D.Double getDockPointCircle(int which, int width) {
    width = width / 2;
    double angle = Geom.dockToAngle(which);
    return new Point2D.Double(Math.sin(angle) * width,
        Math.cos(angle) * width);
  }

  public static Point2D.Double getDockPointSquare(int which, int width) {
    width = width / 2;
    double angle = Geom.dockToAngle(which);
    if (angle >= .25*Math.PI && angle < 0.75*Math.PI) { // east
      return new Point2D.Double(width, -Math.tan(angle - Math.PI / 2) * width);
    } else if (angle >= 1.25*Math.PI && angle <= 1.75*Math.PI) { // west
      return new Point2D.Double(-width, Math.tan(angle - 1.5 * Math.PI) * width);
    } else if (angle >= .75*Math.PI && angle < 1.25*Math.PI) { // north
      return new Point2D.Double(-Math.tan(angle - Math.PI) * width, -width);
    }
    return new Point2D.Double(Math.tan(angle - 2*Math.PI) * width, width);
  }

  public static Point2D sub(Point2D p1, Point2D p2) {
    return new Point2D.Double(p1.getX() - p2.getX(), p1.getY() - p2.getY());
  }

}
