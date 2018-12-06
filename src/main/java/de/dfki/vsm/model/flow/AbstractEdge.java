package de.dfki.vsm.model.flow;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import javax.xml.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.vsm.model.flow.geom.ControlPoint;
import de.dfki.vsm.model.flow.geom.EdgeArrow;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Gregor Mehlmann
 */
@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractEdge {

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

  public final void setTargetNode(final BasicNode value) {
    mTargetNode = value;
  }

  public final BasicNode getSourceNode() {
    return mSourceNode;
  }

  public final void setSourceNode(final BasicNode value) {
    mSourceNode = value;
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

  @XmlElement(name="Connection")
  public final EdgeArrow getArrow() {
    return mArrow;
  }

  public final void setArrow(final EdgeArrow value) {
    mArrow = value;
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

  public void translate(int deltaX, int deltaY) {
    for (ControlPoint cp : mArrow.getPointList()) {
      cp.setCtrlXPos(cp.getCtrlXPos() + deltaX);
      cp.setCtrlYPos(cp.getCtrlYPos() + deltaY);
    }
  }

}
