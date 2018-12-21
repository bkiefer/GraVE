package de.dfki.vsm.editor.util;

//~--- JDK imports ------------------------------------------------------------
import java.awt.Point;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- non-JDK imports --------------------------------------------------------
import de.dfki.vsm.editor.Edge;
import de.dfki.vsm.editor.Node;
import de.dfki.vsm.editor.project.WorkSpace;

/**
 * EdgeNodeDockingManager manages incoming and outgoing edges of a
 * node/supernode Initially nodes have 24 free dock positions where edges con be
 * connected
 *
 * @author Patrick Gebhard
 */
public class DockingManager {

  private static final Logger logger = LoggerFactory.getLogger(WorkSpace.class);

  private Node mGUINode = null;
  private Node.Type mNodeType = null;    // The type defines the location of the dock points
  private ArrayList<DockPoint> mDockPoints = new ArrayList<DockPoint>();
  private Hashtable<Edge, DockPoint> mEdgeDockPoints = new Hashtable<Edge, DockPoint>();
  private Hashtable<Edge, DockPoint> mEdgeSecondDockPoints = new Hashtable<Edge, DockPoint>();

  public DockingManager(Node node) {
    mGUINode = node;
    mNodeType = node.getType();
    update();
  }

  public void update() {
    switch (mNodeType) {
      case BasicNode:
        initNodeDockPoints();
        break;

      case SuperNode:
        initSuperNodeDockPoints();
        break;
    }
  }

  public ArrayList<Point> getFreeDockPoints() {
    ArrayList<Point> points = new ArrayList<Point>();

    for (DockPoint dp : mDockPoints) {
      if (!dp.mOccupied) {
        points.add(dp.mPos);
      }
    }

    return points;
  }

  public ArrayList<Point> getOccupiedDockPoints() {
    ArrayList<Point> points = new ArrayList<Point>();

    for (DockPoint dp : mDockPoints) {
      if (dp.mOccupied) {
        points.add(dp.mPos);
      }
    }

    return points;
  }

  public synchronized Point occupyDockPointForStartSign() {
    for (DockPoint dp : mDockPoints) {
      if (dp.mType.equalsIgnoreCase(DockPoint.sSTARTSIGN_TYPE)) {

        // TODO remap, if occupied
        dp.use();

        return dp.mPos;
      }
    }

    return null;
  }

  public void releaseDockPointForStartSign() {
    for (DockPoint dp : mDockPoints) {
      if (dp.mType.equals(DockPoint.sSTARTSIGN_TYPE)) {
        dp.release();
      }
    }
  }

  public DockPoint getNearestDock(Edge e, Point p) {
    DockPoint rp = null;
    int lastXDist = -1;
    int lastYDist = -1;
    int actualXDist = -1;
    int actualYDist = -1;

    for (DockPoint dp : mDockPoints) {
      if (!dp.mOccupied) {
        actualXDist = Math.abs(p.x - dp.mPos.x);
        actualYDist = Math.abs(p.y - dp.mPos.y);

        // Store first free Dockpoint
        if ((lastXDist == -1) && (lastYDist == -1)) {
          lastXDist = actualXDist;
          lastYDist = actualYDist;
          rp = dp;
        }

        // Store nearest free DockPoint
        if ((actualXDist + actualYDist) < (lastXDist + lastYDist)) {
          lastXDist = actualXDist;
          lastYDist = actualYDist;
          rp = dp;
        }
      }
    }
    if (rp != null) {
      rp.use(); // mark DockPoint as used
    } else {
      logger.error("No more docking points available! Edge will not connected!");
    }
    return rp;
  }

  public Point getNearestDockPoint(Edge e, Point p) {
    DockPoint rp = getNearestDock(e, p);
    // mark DockPoint for edge
    if (rp != null) {
      mEdgeDockPoints.put(e, rp);
      // return a new Point instance that can be altered
      return (Point) rp.mPos.clone();
    }
    return null;
  }

  /*
     * This method finds a dockpoint for edges that point to the same node
   */
  public Point getNearestSecondDockPoint(Edge e, Point p) {
    DockPoint rp = getNearestDock(e, p);
    // mark DockPoint for edge
    if (rp != null) {
      mEdgeSecondDockPoints.put(e, rp);
      // return a new Point instance that can be altered
      return (Point) rp.mPos.clone();
    }
    return null;

  }

  /**
   * Returns the dock point to which the edge is connected
   */
  public Point getDockPoint(Edge e) {
    if (mEdgeDockPoints.containsKey(e)) {
      // return a new Point intance that can be altered
      return (Point) mEdgeDockPoints.get(e).mPos.clone();
    }
    return null;
  }

  /**
   * Return the second dock point to which the edge is connected (pointing to
   * the same node
   */
  public Point getSecondDockPoint(Edge e) {
    if (mEdgeSecondDockPoints.containsKey(e)) {
      // return a new Point intance that can be altered
      return (Point) mEdgeSecondDockPoints.get(e).mPos.clone();
    }
    return null;
  }

  /**
   * Releases the dock point an edge has used on a node
   *
   * @param Edge that should be released
   */
  public Point freeDockPoint(Edge e) {
    if (mEdgeDockPoints.containsKey(e)) {
      DockPoint dp = mEdgeDockPoints.get(e);

      dp.release();
      mEdgeDockPoints.remove(e);

      // DEBUG
      // System.out.println("Edge " + e.getName() + " dock point disconnected");
      return dp.mPos;
    }
    // DEBUG System.out.println("Edge should be disconnected but never was connected");
    return null;
  }

  /**
   * Releases the seoncd dock point an edge has used on a node
   *
   * @param Edge that should be released
   */
  public Point freeSecondDockPoint(Edge e) {
    if (mEdgeSecondDockPoints.containsKey(e)) {
      DockPoint dp = mEdgeSecondDockPoints.get(e);

      dp.release();
      mEdgeSecondDockPoints.remove(e);

      // DEBUG
      // System.out.println("Edge "+ e.getName() + " second dock point disconnected");
      return dp.mPos;
    }
    // DEBUG System.out.println("Edge should be disconnected but never was connected");
    return null;
  }

  /**
   * Return a Set of all connected edges
   */
  public Set<Edge> getConnectedEdges() {
    return mEdgeDockPoints.keySet();
  }

  /**
   *  Removes a connected egde for a dock point and frees the dock point for
   * other edges
   *
   * @param Point from edge which should be released
   */
  private boolean releaseDockPoint(Point p) {
    DockPoint rp = null;
    int lastXDist = -1;
    int lastYDist = -1;
    int actualXDist = -1;
    int actualYDist = -1;

    for (DockPoint dp : mDockPoints) {
      if (dp.mOccupied) {
        actualXDist = Math.abs(p.x - dp.mPos.x);
        actualYDist = Math.abs(p.y - dp.mPos.y);

        // search
        if ((lastXDist == -1) && (lastYDist == -1)) {
          lastXDist = actualXDist;
          lastYDist = actualYDist;
          rp = dp;
        }

        // Store nearest free DockPoint
        if ((actualXDist + actualYDist) < (lastXDist + lastYDist)) {
          lastXDist = actualXDist;
          lastYDist = actualYDist;
          rp = dp;
        }
      }
    }

    // mark DockPoint as free
    if (rp != null) {
      rp.release();
    }

    return (rp != null);
  }

  private boolean hasDockpoint(String name) {
    for (DockPoint dp : mDockPoints) {
      if (dp.mName.equalsIgnoreCase(name)) {
        return true;
      }
    }

    return false;
  }

  private DockPoint getDockpointByName(String name) {
    for (DockPoint dp : mDockPoints) {
      if (dp.mName.equalsIgnoreCase(name)) {
        return dp;
      }
    }

    return null;
  }

  private void initNodeDockPoints() {
    String dpName = null;
    double a = 0.0d;
    double dockXPos = 0.0d;
    double dockYPos = 0.0d;

    for (int cnt = 23; cnt >= 0; cnt--) {
      a = cnt * Math.PI / 12.0d + (Math.PI);
      dpName = "dp" + cnt;
      dockXPos = Math.round((Math.sin(a) * 0.5d + 0.5d) * mGUINode.getWidth());
      dockYPos = Math.round((Math.cos(a) * 0.5d + 0.5d) * mGUINode.getHeight());

      if ((dockXPos == 0) && (dockYPos == mGUINode.getHeight() / 2)) {

        // use most left dockpoint as startsign dockpoint
        if (hasDockpoint(dpName)) {

          // update dockpoint
          DockPoint dp = getDockpointByName(dpName);

          dp.mPos.x = (int) dockXPos;
          dp.mPos.y = (int) dockYPos;
        } else {

          // create new dockpoint
          mDockPoints.add(new DockPoint(dpName, (int) dockXPos, (int) dockYPos, DockPoint.sSTARTSIGN_TYPE));
        }
      } else {
        if (hasDockpoint(dpName)) {

          // update dockpoint
          DockPoint dp = getDockpointByName(dpName);

          dp.mPos.x = (int) dockXPos;
          dp.mPos.y = (int) dockYPos;
        } else {

          // create new dockpoint
          mDockPoints.add(new DockPoint(dpName, (int) dockXPos, (int) dockYPos));
        }
      }
    }
  }

  private void initSuperNodeDockPoints() {
    String dpName = null;
    double a = 0.0d;
    double xa = 0.0d;
    double ya = 0.0d;
    double fy = 0.0d;
    double fx = 0.0d;
    double rh = mGUINode.getHeight() / 2.0d;
    double rw = mGUINode.getWidth() / 2.0d;
    double dockXPos = 0.0d;
    double dockYPos = 0.0d;

    for (int cnt = 24; cnt >= 1; cnt--) {
      dpName = "dp" + cnt;
      a = cnt * Math.PI / 12 + (Math.PI);
      ya = Math.cos(a);                  // y-achsenabschnitt
      xa = Math.sin(a);                  // x-achsenabschnitt

      if (Math.abs(xa) <= Math.abs(ya)) {
        fy = 1.0d / Math.abs(ya);    // scaling factor for y
        dockYPos = rh * Math.signum(ya) + rh;
        dockXPos = Math.round(xa * fy * rw) + rw;
      } else {
        fx = 1.0d / Math.abs(xa);    // scaling factor for x
        dockYPos = Math.round(ya * fx * rh) + rh;
        dockXPos = rw * Math.signum(xa) + rw;
      }

      // Debug System.out.println("(x,y)= " + dockXPos + "," + dockYPos);
      if ((dockXPos == 0) && (dockYPos == mGUINode.getHeight() / 2)) {

        // use most left dockpoint as startsign dockpoint
        if (hasDockpoint(dpName)) {

          // update dockpoint
          DockPoint dp = getDockpointByName(dpName);

          dp.mPos.x = (int) dockXPos;
          dp.mPos.y = (int) dockYPos;
        } else {

          // create new dockpoint
          mDockPoints.add(new DockPoint(dpName, (int) dockXPos, (int) dockYPos, DockPoint.sSTARTSIGN_TYPE));
        }
      } else {
        if (hasDockpoint(dpName)) {

          // update dockpoint
          DockPoint dp = getDockpointByName(dpName);

          dp.mPos.x = (int) dockXPos;
          dp.mPos.y = (int) dockYPos;
        } else {

          // create new dockpoint
          mDockPoints.add(new DockPoint(dpName, (int) dockXPos, (int) dockYPos));
        }
      }
    }
  }

  private static class DockPoint {

    private static final String sSTARTSIGN_TYPE = "start pos dock point";
    private String mName = null;
    private Point mPos = null;
    private boolean mOccupied = false;
    private String mType = "dock point";

    private DockPoint(String name, int x, int y) {
      mName = name;
      mPos = new Point(x, y);
    }

    private DockPoint(String name, int x, int y, String id) {
      mName = name;
      mPos = new Point(x, y);
      mType = id;
    }

    private void use() { mOccupied = true; }
    private void release() { mOccupied = false; }
  }
}
