package de.dfki.vsm.model.flow.geom;

import java.awt.Point;
import java.util.Arrays;
import java.util.BitSet;

public class Geom {

  private static double[] num2angle;
  private static double[] angles; // sorted
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
    max_bits = (int)(Math.log(maxPoints) / Math.log(2)) + 1;
  }

  public static Point add(Point a, Point b) {
    Point result = new Point(a);
    result.translate(b.x, b.y);
    return result;
  }

  // returns an angle in the range 0 .. 2*pi
  public static double angle(Point center, Point p) {
    double dx = p.x - center.x;
    if (dx == 0) return 0;
    double res = Math.atan2(p.x - center.x, p.y - center.y );
    /*if (dx > 0) {
      res = res < 0 ? Math.PI * 2 - res : res;
    } else {
      res = Math.PI + res;
    }*/
    if (res < 0) res = 2 * Math.PI - res;
    return res;
  }

  private static int closestAngle(double angle) {
    int k = Arrays.binarySearch(angles, angle);
    if (k < 0) k = -k ;
    if (Math.abs(angle - angles[k]) > Math.abs(angle - angles[k + 1])) {
      k += 1;
    }
    return k;
  }

  public static int findClosestDock(BitSet taken, double angle) {
    if (taken.length() >= max_bits) return -1; // all docks taken
    int k = closestAngle(angle);
    int d = 1;
    do { // alternate back and forth
      if (k > 0 && k < angle2num.length && ! taken.get(angle2num[k])) break;
      k = ((d & 1) != 0) ? k + d : k - d;
      ++d;
    } while (d < angle2num.length);
    return angle2num[k];
  }

  public static double dockToAngle(int dock) {
    return num2angle[dock];
  }
}
