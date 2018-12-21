package de.dfki.vsm.model.flow;

import static de.dfki.vsm.model.flow.geom.Geom.*;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.vsm.model.flow.geom.ControlPoint;
import de.dfki.vsm.model.flow.geom.EdgeArrow;
import de.dfki.vsm.model.flow.geom.Geom;

/**
 * @author Gregor Mehlmann
 */
@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractEdge {
  private final static int MIN_CTRL_LEN = 50;

  private static final Logger logger = LoggerFactory.getLogger(AbstractEdge.class);

  public static class ExpressionAdapter extends XmlAdapter<String, Expression> {
    @Override
    public String marshal(Expression v) throws Exception {
      return v.getContent();
    }

    @Override
    public Expression unmarshal(String v) throws Exception {
      return new Expression(v);
    }
  }

  protected String mTargetUnid = new String();
  protected String mSourceUnid = new String();
  protected BasicNode mTargetNode = null;
  protected BasicNode mSourceNode = null;

  // Replaces EdgeArrow
  /* TODO: TURN ARROW DATA INTO NEW FIELDS */
  private int mTargetDock;
  private int mSourceDock;
  private Point mSourceCtrlPoint; // relative, not absolute
  private Point mTargetCtrlPoint; // relative, not absolute
  /**/

  // DEPRECATED
  protected EdgeArrow mArrow = null;
  @XmlElement(name="Commands")
  protected String mCmdList = null;

  @XmlAttribute(name="target")
  public final String getTargetUnid() {
    return mTargetUnid;
  }

  public final void setTargetUnid(final String value) {
    mTargetUnid = value;
  }

  @XmlAttribute(name="source")
  public final String getSourceUnid() {
    return mSourceUnid;
  }

  public final void setSourceUnid(final String value) {
    mSourceUnid = value;
  }

  @XmlTransient
  public final BasicNode getTargetNode() {
    return mTargetNode;
  }

  public final BasicNode getSourceNode() {
    return mSourceNode;
  }

  public final int getSourceDock() {
    return mSourceDock;
  }

  public final Point getSourceCtrlPoint() {
    return mSourceCtrlPoint;
  }

  public final void setSourceCtrlPoint(Point p) {
    mSourceCtrlPoint = p;
    checkControl(mSourceCtrlPoint, mSourceDock);
  }

  public final int getTargetDock() {
    return mTargetDock;
  }

  public final Point getTargetCtrlPoint() {
    return mTargetCtrlPoint;
  }

  public final void setTargetCtrlPoint(Point p) {
    mTargetCtrlPoint = p;
    checkControl(mTargetCtrlPoint, mTargetDock);
  }

  /** Only for establishTargetNodes. TODO: should go */
  final void setNodes(BasicNode source, BasicNode target) {
    mSourceNode = source;
    mSourceUnid = source.getId();
    mTargetNode = target;
    //mTargetUnid = target.getId(); // already set
    arrowToDock();
  }

  public final void setSource(final BasicNode value, int dock) {
    mSourceUnid = value.getId();
    mSourceNode = value;
    mSourceDock = dock;
  }

  public final void setTarget(final BasicNode value, int dock) {
    mTargetUnid = value.getId();
    mTargetNode = value;
    mTargetDock = dock;
  }

  public Expression getExpression() {
    if (this instanceof TimeoutEdge)
      return ((TimeoutEdge)this).mExpression;
    else if (this instanceof GuardedEdge)
      return ((GuardedEdge)this).mCondition;
    else if (this instanceof InterruptEdge)
      return ((InterruptEdge)this).mCondition;
    return null;
  }

  public final void connect(final BasicNode source, final BasicNode target) {
    mSourceNode = source;
    mSourceUnid = source.getId();
    mTargetNode = target;
    mTargetUnid = target.getId();
  }

  /* TODO: DROP AFTER REVAMP */
  @XmlElement(name="Connection")
  public final EdgeArrow getArrow() {
    return mArrow;
  }

  /* TODO: DROP AFTER REVAMP */
  public final void setArrow(final EdgeArrow value) {
    mArrow = value;
  }

  /* TODO: DROP AFTER REVAMP */
  public final void arrowToDock() {
    EdgeArrow arr = getArrow();
    List<ControlPoint> pl = arr.getPointList();
    // For start and target node:
    // a) find a dock close to the dock point
    // b) turn the absolute control point into a relative control point
    mSourceDock = getSourceNode().getNearestFreeDock(pl.get(0).getPoint());
    mTargetDock = getTargetNode().getNearestFreeDock(pl.get(1).getPoint());
    getSourceNode().occupyDock(mSourceDock);
    getTargetNode().occupyDock(mTargetDock);
    Point cp = pl.get(0).getCtrlPoint();
    cp.translate(-pl.get(0).getXPos(), -pl.get(0).getYPos());
    mSourceCtrlPoint = cp;
    cp = pl.get(1).getCtrlPoint();
    cp.translate(-pl.get(1).getXPos(), -pl.get(1).getYPos());
    mTargetCtrlPoint = cp;
  }

  @XmlTransient
  public final String getCmdList() {
    return mCmdList;
  }

  public final void setCmdList(final String value) {
    mCmdList = value;
  }

  public final String getCopyOfCmdList() {
    final String copy = new String(this.mCmdList);
    return copy;
  }

  /** Set the content of an edge, if applicable */
  public void setContent(String s) { }

  /** Get the content of an edge, as string (if applicable) */
  public String getContent() { return null; }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = mTargetUnid != null? 31 * hash + this.mTargetUnid.hashCode() : hash;
    hash = mSourceUnid != null? 31 * hash + this.mSourceUnid.hashCode() : hash;
    hash = mArrow != null? 31 * hash + this.mArrow.hashCode() : hash;
    hash = mCmdList != null? 31 * hash + this.mCmdList.hashCode() : hash;
    return hash;
  }


  /** Disallow control points too close to the node, or past the orthogonal
   *  vectors to the dock vector
   */
  public static void checkControl(Point ctrlPoint, int dock) {
    // Unit Vector from Center to Dock
    Point2D dockVec = Geom.getDockPointCircle(dock, 4);
    double ctrlLen = norm2(ctrlPoint);
    if (ctrlLen < MIN_CTRL_LEN) { // scale vector to MIN_CTRL_LEN
      double f = MIN_CTRL_LEN / ctrlLen;
      ctrlPoint.x *= f;
      ctrlPoint.y *= f;
      ctrlLen = MIN_CTRL_LEN;
    }
    double dvlen = norm2(dockVec);
    double dot = dotProd(dockVec, ctrlPoint); // for cosine: / (startlen*ctrlLen);
    if (dot < 0) {
      dot /= dvlen;
      // reject: turn it into an orthogonal vector with the same length:
      // cv = dv - ((cv . dv)/ norm(dv)) * dv
      ctrlPoint.translate(-(int)(dot * dockVec.getX()), -(int)(dot * dockVec.getY()));
      double l = ctrlLen / norm2(ctrlPoint);
      ctrlPoint.x *= l;
      ctrlPoint.y *= l;
    }
  }

  /** compute (relative) bezier control points
   *  (using node center point and edge connection points)
   */
  private void initCurve(int nodeWidth) {
    Point start = mSourceNode.getCenter();
    Point target = mTargetNode.getCenter();

    // Unit Vector from Center to Dock
    Point2D startVec = Geom.getDockPointCircle(mSourceDock, 2);
    Point2D targVec = Geom.getDockPointCircle(mTargetDock, 2);

    // TODO: LOOP EDGES NEED MORE THINKING, FOR THE CONTROL POINTS *AND* THE
    // DOCKS

    // scale control point in relation to distance between nodes
    double scale = (mSourceNode == mTargetNode) ? 3
        : Math.max(start.distance(target) / nodeWidth - 0.5, 1.25)
        * nodeWidth/2; // TODO: not my preferred solution.

    mSourceCtrlPoint = new Point((int) (scale * startVec.getX()), (int) (scale * startVec.getY()));
    mTargetCtrlPoint = new Point((int) (scale * targVec.getX()), (int) (scale * targVec.getY()));

    // re-done for relative control points
    checkControl(mSourceCtrlPoint, mSourceDock);
    checkControl(mTargetCtrlPoint, mTargetDock);
  }

  /** Compute the edge closest to the straight connection, as far as it's
   *  allowed concerning the dock points already taken.
   */
  public void straightenEdge(int nodeWidth) {
    mSourceNode.freeDock(mSourceDock);
    mTargetNode.freeDock(mTargetDock);
    int s = mSourceNode.getNearestFreeDock(mTargetNode.getCenter());
    mSourceDock = s;
    int t = mTargetNode.getNearestFreeDock(mSourceNode.getCenter());
    mTargetDock = t;
    mSourceNode.occupyDock(mSourceDock);
    mTargetNode.occupyDock(mTargetDock);
    // mEg.updateDrawingParameters(this); // no: compute new control points
    initCurve(nodeWidth);
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    AbstractEdge other = (AbstractEdge) obj;
    if (mArrow == null) {
      if (other.mArrow != null)
        return false;
    } else if (!mArrow.equals(other.mArrow))
      return false;
    if (mCmdList == null) {
      if (other.mCmdList != null)
        return false;
    } else if (!mCmdList.equals(other.mCmdList))
      return false;
    if (mSourceUnid == null) {
      if (other.mSourceUnid != null)
        return false;
    } else if (!mSourceUnid.equals(other.mSourceUnid))
      return false;
    if (mTargetUnid == null) {
      if (other.mTargetUnid != null)
        return false;
    } else if (!mTargetUnid.equals(other.mTargetUnid))
      return false;
    return true;
  }

  public boolean isGuardedEdge() { return this instanceof GuardedEdge; }
  public boolean isInterruptEdge() { return this instanceof InterruptEdge; }
  public boolean isRandomEdge() { return this instanceof RandomEdge; }
  public boolean isTimeoutEdge() { return this instanceof TimeoutEdge; }

  /** Do a deep copy of AbstractEdge, remapping nodes, and adding new edge to
   *  the copied source node.
   */
  public abstract AbstractEdge deepCopy(Map<BasicNode, BasicNode> orig2copy);

  /** Do a deep copy of AbstractEdge into edgeCopy, remapping nodes.
   *  Adds edge to the copied source node.
   */
  protected <T extends AbstractEdge> T deepCopy(T edgeCopy,
      Map<BasicNode, BasicNode> orig2copy) {
    BasicNode sourceCopy = orig2copy.get(mSourceNode);
    BasicNode targetCopy = orig2copy.get(mTargetNode);
    edgeCopy.connect(sourceCopy, targetCopy);
    sourceCopy.addEdge(edgeCopy);
    edgeCopy.mArrow = mArrow.deepCopy();
    edgeCopy.mCmdList = mCmdList;
    return edgeCopy;
  }

  /** Factory method to create new edges from prototypes */
  public static AbstractEdge getNewEdge(AbstractEdge e) {
    try {
      Constructor<? extends AbstractEdge> cons = e.getClass().getConstructor();
      return cons.newInstance();
    } catch (NoSuchMethodException | SecurityException | InstantiationException
        | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException ex) {
      logger.error("Error constructing edge: {}", ex);
    }
    return null;
  }

  // TODO: DROP AFTER REVAMP
  public void translate(int deltaX, int deltaY) {
    for (ControlPoint cp : mArrow.getPointList()) {
      cp.setCtrlXPos(cp.getCtrlXPos() + deltaX);
      cp.setCtrlYPos(cp.getCtrlYPos() + deltaY);
    }
  }

  public String toString() {
    return "" + this.getClass().getName().charAt(0) + '(' + mSourceNode + ','
    + mTargetNode + ')'
    + '[' + mSourceDock + '|' + mSourceCtrlPoint + ','
    + mTargetDock + '|' + mTargetCtrlPoint + ']';
  }
}
