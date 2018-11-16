package de.dfki.vsm.model.flow;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.vsm.model.flow.geom.EdgeArrow;
import de.dfki.vsm.util.Pair;
import de.dfki.vsm.util.cpy.Copyable;

/**
 * @author Gregor Mehlmann
 */
@XmlAccessorType(XmlAccessType.NONE)
public abstract class AbstractEdge implements Copyable {

  private static final Logger logger = LoggerFactory.getLogger(AbstractEdge.class);

  protected String mTargetUnid = new String();
  protected String mSourceUnid = new String();
  protected BasicNode mTargetNode = null;
  protected BasicNode mSourceNode = null;
  protected EdgeArrow mArrow = null;
  @XmlElement(name="Commands")
  protected String mCmdList = null;
  protected HashMap<Pair<String, BasicNode>, Pair<String, BasicNode>> mAltMap =
      new HashMap<>();

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


  /*
    public final ArrayList<BasicNode> getAltList() {
        final ArrayList<BasicNode> altList = new ArrayList();
        for (TPLTuple<String, BasicNode> pair : mAltMap.values()) {
            altList.add(pair.getSecond());
        }
        return altList;
    }
   */
  @XmlTransient
  public final HashMap<
        Pair<String, BasicNode>, Pair<String, BasicNode>> getAltMap() {
    return mAltMap;
  }

  public final void setAltMap(final
      HashMap<Pair<String, BasicNode>, Pair<String, BasicNode>> value) {
    mAltMap = value;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((mAltMap == null) ? 0 : mAltMap.hashCode());
    result = prime * result + ((mArrow == null) ? 0 : mArrow.hashCode());
    result = prime * result + ((mCmdList == null) ? 0 : mCmdList.hashCode());
    result = prime * result
        + ((mSourceUnid == null) ? 0 : mSourceUnid.hashCode());
    result = prime * result
        + ((mTargetUnid == null) ? 0 : mTargetUnid.hashCode());
    return result;
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
    if (mAltMap == null) {
      if (other.mAltMap != null)
        return false;
    } else if (!mAltMap.equals(other.mAltMap))
      return false;
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

  // TODO: This is not yet a deep copy
  public HashMap<Pair<String, BasicNode>, Pair<String, BasicNode>> getCopyOfAltStartNodeMap() {
    HashMap<Pair<String, BasicNode>, Pair<String, BasicNode>> copy =
        new HashMap<Pair<String, BasicNode>, Pair<String, BasicNode>>();
    Iterator<Map.Entry<Pair<String, BasicNode>, Pair<String, BasicNode>>> it =
        mAltMap.entrySet().iterator();

    while (it.hasNext()) {
      Map.Entry<Pair<String, BasicNode>, Pair<String, BasicNode>> pairs = it.next();
      Pair<String, BasicNode> startNodePair = (Pair<String, BasicNode>) pairs.getKey();
      Pair<String, BasicNode> altStartNodePair = (Pair<String, BasicNode>) pairs.getValue();
      Pair<String, BasicNode> startNodePairCopy = new Pair<String, BasicNode>(startNodePair.getFirst(),
              startNodePair.getSecond());
      Pair<String, BasicNode> altStartNodePairCopy = new Pair<String, BasicNode>(altStartNodePair.getFirst(),
              altStartNodePair.getSecond());

      copy.put(startNodePairCopy, altStartNodePairCopy);
    }

    return copy;
  }

  // TODO: do this over the list of strings
  public String getAltStartNodesAsString() {
    String result = "";
    Iterator<Map.Entry<Pair<String, BasicNode>,Pair<String, BasicNode>>> it =
        mAltMap.entrySet().iterator();

    while (it.hasNext()) {
      Map.Entry<Pair<String, BasicNode>,Pair<String, BasicNode>> pairs = it.next();
      Pair<String, BasicNode> start = pairs.getKey();
      Pair<String, BasicNode> alt = pairs.getValue();
      result += start.getFirst() + "/" + alt.getFirst() + ";";
    }

    return result;
  }

  public boolean isGuardedEdge() { return this instanceof GuardedEdge; }
  public boolean isInterruptEdge() { return this instanceof InterruptEdge; }
  public boolean isRandomEdge() { return this instanceof RandomEdge; }
  public boolean isTimeoutEdge() { return this instanceof TimeoutEdge; }

  @Override
  public abstract AbstractEdge getCopy();

  /** Copy helper. TODO: deep copy */
  protected <T extends AbstractEdge> T copyFieldsTo(T e) {
    mTargetUnid = e.mTargetUnid;
    mSourceUnid = e.mSourceUnid;
    mTargetNode = e.mTargetNode;
    mSourceNode = e.mSourceNode;
    mArrow = e.mArrow;
    mCmdList = e.mCmdList;
    mAltMap = e.getCopyOfAltStartNodeMap();
    return e;
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

}
