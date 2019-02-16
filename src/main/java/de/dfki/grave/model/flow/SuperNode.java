package de.dfki.grave.model.flow;

import java.awt.geom.Point2D;
import java.util.*;

import javax.xml.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.grave.editor.panels.IDManager;
import de.dfki.grave.model.flow.geom.Geom;
import de.dfki.grave.model.flow.geom.Position;
import de.dfki.grave.util.Pair;

/**
 * @author Gregor Mehlmann
 */
@XmlType(name="SuperNode")
@XmlAccessorType(XmlAccessType.NONE)
public class SuperNode extends BasicNode {

  public static final Logger logger = LoggerFactory.getLogger(SuperNode.class);

  @XmlElement(name="Comment")
  protected ArrayList<CommentBadge> mCommentList = new ArrayList<CommentBadge>();
  @XmlElement(name="Node")
  protected ArrayList<BasicNode> mNodeList = new ArrayList<BasicNode>();
  @XmlElement(name="SuperNode")
  protected ArrayList<SuperNode> mSuperNodeList = new ArrayList<SuperNode>();
  @XmlAttribute(name="start")
  protected String mStartNodeId = null;
  protected BasicNode mStartNode = null;
  @XmlAttribute(name="hideLocalVar")
  protected boolean mHideLocalVarBadge = false;
  @XmlAttribute(name="hideGlobalVar")
  protected boolean mHideGlobalVarBadge = false;

  public SuperNode() {
  }

  /** Create a SuperNode from an existing BasicNode: Node Type Change */
  public SuperNode(IDManager mgr, final BasicNode node) {
    mNodeId = mgr.getNextFreeID(this);
    copyBasicFields(node);
  }

  /** Get a new SuperNode from the GUI */
  public BasicNode createNode(IDManager mgr, Position p, SuperNode s) {
    return new SuperNode().init(mgr.getNextFreeID(this), p, s);
  }

  @XmlTransient
  public BasicNode getStartNode() {
    return mStartNode;
  }

  public void setStartNode(BasicNode value) {
    mStartNode = value;
    mStartNodeId = value.getId();
  }

  public void setComment(String value) {
    mComment = value;
  }

  public String getComment() {
    return mComment;
  }

  public void addComment(CommentBadge value) {
    mCommentList.add(value);
  }


  public void removeComment(CommentBadge value) {
    mCommentList.remove(value);
  }

  public ArrayList<CommentBadge> getCommentList() {
    return mCommentList;
  }

  /*
  public void hideGlobalVarBadge(Boolean value) {
    mHideGlobalVarBadge = value;
  }

  public Boolean isGlobalVarBadgeHidden() {
    return mHideGlobalVarBadge;
  }

  public void hideLocalVarBadge(Boolean value) {
    mHideLocalVarBadge = value;
  }

  public Boolean isLocalVarBadgeHidden() {
    return mHideLocalVarBadge;
  }
  */

  public boolean isStartNode(BasicNode value) {
    return mStartNode == value;
  }

  /** Add a node to the list of nodes */
  public void addNode(BasicNode value) {
    if (mSuperNodeList.isEmpty() && mNodeList.isEmpty()) {
      setStartNode(value);
    }
    if (value instanceof SuperNode) {
      mSuperNodeList.add((SuperNode)value);
    } else {
      mNodeList.add(value);
    }
  }

  /** Add a node, with side effects, such as adding it as a start node if it's
   *  the first node added
   */
  public void removeNode(BasicNode value) {
    if (value instanceof SuperNode) {
      mSuperNodeList.remove((SuperNode)value);
    } else {
      mNodeList.remove(value);
    }
  }


  public Iterable<SuperNode> getSuperNodeList() {
    return mSuperNodeList;
  }

  public Iterable<BasicNode> getNodeList() {
    return mNodeList;
  }


  private class NodeIterator implements Iterator<BasicNode> {
    Iterator<? extends BasicNode> impl = mNodeList.iterator();
    boolean basic = true;

    @Override
    public boolean hasNext() {
      boolean result = impl.hasNext();
      if (!result && basic) {
        basic = false;
        impl = mSuperNodeList.iterator();
        result = impl.hasNext();
      }
      return result;
    }

    @Override
    public BasicNode next() { return impl.next(); }
  }

  public Iterable<BasicNode> getNodes() {
    return new Iterable<BasicNode>() {
      @Override
      public Iterator<BasicNode> iterator() { return new NodeIterator(); }
    };
  }

  public int getNodeSize() {
    return mNodeList.size() + mSuperNodeList.size();
  }

  public BasicNode getChildNodeById(String id) {
    for (BasicNode node : getNodes()) {
      if (node.getId().equals(id)) {
        return node;
      }
    }

    return null;
  }

  public void establishParentNodes() {
    for (BasicNode node : mNodeList) {
      node.setParentNode(this);
    }
    for (SuperNode node : mSuperNodeList) {
      node.setParentNode(this);
      node.establishParentNodes();
    }
    for (CommentBadge c : mCommentList) {
      c.setParentNode(this);
    }
  }

  @Override
  public void establishTargetNodes() {
    super.establishTargetNodes();

    for (BasicNode node : getNodes()) {
      node.establishTargetNodes();
    }
  }

  public void establishStartNodes() {
    if (! mStartNodeId.isEmpty()) {
      if (mStartNodeId.endsWith(";")) {
        mStartNodeId = mStartNodeId.substring(0, mStartNodeId.length()-1);
      }
      mStartNode = getChildNodeById(mStartNodeId);
      if (mStartNode == null) {
        logger.error("Start node not found: {}", mStartNodeId);
      }
    }

    for (SuperNode node : mSuperNodeList) {
      node.establishStartNodes();
    }
  }

  /** Copy constructor */
  protected void copyFieldsFrom(final SuperNode node) {
    super.copyFieldsFrom(node);
    // unfilled: mCommentList, mNodeList, mSuperNodeList, mStartNodeMap
    mHideLocalVarBadge = node.mHideLocalVarBadge;
    mHideGlobalVarBadge = node.mHideGlobalVarBadge;
  }

  /** This copies all nodes and edges *inside* this SuperNode, but not the
   *  edges starting at this node.
   */
  public BasicNode deepCopy(IDManager mgr, SuperNode newParent) {
    SuperNode copy = new SuperNode();
    copy.copyFieldsFrom(this);
    copy.mNodeId = mgr.getNextFreeID(this);
    copy.mParentNode = newParent;
    Map<BasicNode, BasicNode> orig2copy = new IdentityHashMap<>();
    // copy all subnodes in this SuperNode
    for (BasicNode n : getNodes()) {
      BasicNode nCopy = n.deepCopy(mgr, copy);
      orig2copy.put(n, nCopy);
      copy.mNodeList.add(nCopy);
    }
    // copy all edges between nodes inside this SuperNode
    for (BasicNode n : getNodes()) {
      for (AbstractEdge e: n.getEdgeList()) {
        e.deepCopy(orig2copy);
      }
    }

    return copy;
  }

  /** This copies some subset of node and edge models. One basic assumption is
   *  that there are no "dangling" edges which either start or end at a node
   *  outside the given node set.
   *
   *  The copied node models will be subnodes of this SuperNode.
   */
  public Pair<Collection<BasicNode>, List<AbstractEdge>> copyGraphModel(
      IDManager mgr, List<BasicNode> nodeViews, List<AbstractEdge> edgeViews) {
    Map<BasicNode, BasicNode> orig2copy = new IdentityHashMap<>();
    for (BasicNode n : nodeViews) {
      BasicNode cpy = n.deepCopy(mgr, this);
      orig2copy.put(n, cpy);
    }

    List<AbstractEdge> newEdges = new ArrayList<>();
    for (AbstractEdge edge: edgeViews) {
      AbstractEdge e = edge.deepCopy(orig2copy);
      newEdges.add(e);
    }
    return new Pair<Collection<BasicNode>, List<AbstractEdge>>(
        orig2copy.values(), newEdges);
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 53 * hash + mCommentList.hashCode();
    hash = 53 * hash + mNodeList.hashCode();
    hash = 53 * hash + mSuperNodeList.hashCode();
    hash = 53 * hash + (mStartNodeId == null ? 0 : mStartNodeId.hashCode());
    hash = 53 * hash + Boolean.hashCode(this.mHideLocalVarBadge);
    hash = 53 * hash + Boolean.hashCode(this.mHideGlobalVarBadge);
    return hash;
  }

  /** Dock point for square */
  public Point2D getDockPoint(int which, int width) {
    return Geom.getDockPointSquare(which, width);
  }
}
