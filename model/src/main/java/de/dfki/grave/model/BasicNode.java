package de.dfki.grave.model;

import java.awt.geom.Point2D;
import java.util.*;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.grave.util.ChainedIterator;

/**
 * @author Gregor Mehlmann
 */
@XmlRootElement(name="Node")
@XmlAccessorType(XmlAccessType.NONE)
public class BasicNode implements ContentHolder {

  private static final Logger logger = LoggerFactory.getLogger(BasicNode.class);

  public static class CodeAdapter extends XmlAdapter<String, Code> {
    @Override
    public String marshal(Code v) throws Exception {
      return v.getContent();
    }

    @Override
    public Code unmarshal(String v) throws Exception {
      return new Code(v);
    }
  }

  @XmlAttribute(name="id")
  protected String mNodeId = new String();
  @XmlAttribute(name="name")
  protected String mNodeName = new String();
  @XmlAttribute(name="comment")
  protected String mComment = new String();
  @XmlElement(name="Commands")
  @XmlJavaTypeAdapter(CodeAdapter.class)
  protected Code mCmdList = new Code();


  /** Edges leaving this node. Only outgoing, no storing of incoming edges
   * TODO: MERGE INTO ONE LIST!?
   */
  @XmlElement(name="CEdge")
  protected ArrayList<GuardedEdge> mCEdgeList = new ArrayList<>();
  @XmlElement(name="PEdge")
  protected ArrayList<RandomEdge> mPEdgeList = new ArrayList<>();
  @XmlElement(name="IEdge")
  protected ArrayList<InterruptEdge> mIEdgeList = new ArrayList<>();
  @XmlElement(name="FEdge")
  protected ArrayList<ForkingEdge> mFEdgeList = new ArrayList<>();
  // XML handling using access functions (see below)
  protected AbstractEdge mDEdge = null;

  /** Center point of the node */
  @XmlElement(name="Position")
  protected Position mPosition = null;

  protected SuperNode mParentNode = null;

  protected boolean mIsEndNode = true;

  protected BitSet mDocksTaken = new BitSet();

  public enum FLAVOUR {
    NONE, ENODE, TNODE, CNODE, PNODE, INODE, FNODE
  };

  public boolean isBasic() { return true; }

  /** For a given set of nodes that are subnodes of the same SuperNode, compute
   *  all edge views that emerge from a node inside the set and end in a node
   *  inside the set
   *
   *  Only returns edges, no change in model or view
   */
  public static List<AbstractEdge> computeInnerEdges(Collection<BasicNode> nodes) {
    List<AbstractEdge> result = new ArrayList<>();
    for (BasicNode n : nodes)
      for (AbstractEdge e : n.getEdgeList())
        if (nodes.contains(e.getTargetNode())) result.add(e);
    return result;
  }

  /** Adjust the positions of node and edge models such that the center of the
   *  covered area is at the given position.
   *
   *  Exploits the fact that the node and edge views can be perfectly
   *  reconstructed from the models, and only models are handled internally
   */
  public static void translateNodes(Collection<BasicNode> nodes, Position p) {
    int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE,
        maxX = 0, maxY = 0;
    // compute the covered area
    for (BasicNode n : nodes) {
      int x = n.getPosition().getXPos();
      int y = n.getPosition().getYPos();
      minX = Math.min(minX, x); minY = Math.min(minY, y);
      maxX = Math.max(maxX, x); maxY = Math.max(maxY, y);
    }
    // translate such that the center of the area is on p
    int translateX = Math.max(p.getXPos() - (maxX + minX) / 2, -minX);
    int translateY = Math.max(p.getYPos() - (maxY + minY) / 2, -minY);
    // will move the edges, too
    for (BasicNode n : nodes) {
      n.translate(translateX, translateY);
    }
  }

  public BasicNode() {}

  public void addToSupernode() {
    if (mParentNode.mNodeList.isEmpty()) {
      mParentNode.setStartNode(this);
    }
    mParentNode.mNodeList.add(this);
  }

  /** helper method to get new BasicNode, with no other side effects in the
   *  graph beyond the node being added to the super node s, and it being the
   *  start node in case it's the first node added to s.
   */
  protected BasicNode init(String newId, Position p, SuperNode s) {
    mNodeId = mNodeName = newId;
    mPosition = p;
    mParentNode = s;
    return this;
  }

  /** factory method to get new BasicNode, with no other side effects in the
   *  graph beyond the node being added to the super node s, and it being the
   *  start node in case it's the first node added to s.
   */
  public BasicNode createNode(Position p, SuperNode s) {
    IDManager mgr = s.getRoot().getIDManager();
    return new BasicNode().init(mgr.getNextFreeID(this), p, s);
  }

  protected void copyBasicFields(BasicNode node) {
    mNodeName = node.mNodeName;
    mComment = node.mComment;
    mCmdList = node.mCmdList;
    mCEdgeList = node.mCEdgeList;
    mPEdgeList = node.mPEdgeList;
    mIEdgeList = node.mIEdgeList;
    mFEdgeList = node.mFEdgeList;
    mDEdge = node.mDEdge;
    mPosition = node.mPosition;
    mParentNode = node.mParentNode;
  }

  /** Create a BasicNode from an existing SuperNode: Node Type Change, only
   *  valid if the subnode list of the supernode is empty
   */
  BasicNode(final SuperNode node) {
    IDManager mgr = node.getRoot().getIDManager();
    assert(node.mNodeList.isEmpty());
    mNodeId = mgr.getNextFreeID(this);
    this.copyBasicFields(node);
  }

  public Code getCode() {
    return mCmdList;
  }

  public String getId() {
    return mNodeId;
  }

  /** NODE MODIFICATION, ONLY THROUGH ACTION! */
  public void setName(String value) {
    mNodeName = value;
  }

  public String getName() {
    return mNodeName;
  }

  /** NODE MODIFICATION, NOT USED */
  public void setComment(String value) {
    mComment = value;
  }

  public String getComment() {
    return mComment;
  }

  public boolean hasComment() {
    return ! (mComment == null || mComment.isEmpty());
  }

  /** NODE MODIFICATION, ONLY THROUGH ACTION!
   * @param newNode if null, a new node of the opposite type will be created
   *        otherwise, this is used in an undo operation and the edges from
   *        and to the old node will be now go to/from the new one
   * @return a new node with the other type
   * @throws Exception when the change is not legal
   */
  public BasicNode changeType(BasicNode newNode) throws Exception {
    // adapt node lists of parent SuperNode
    SuperNode s = getParentNode();
    Collection<AbstractEdge> incoming = s.computeIncomingEdges(this);
    if (newNode == null) {
      if (this instanceof SuperNode) {
        SuperNode n = (SuperNode)this;
        if (n.getNodeSize() > 0) {
          // complain: this operation can not be done, SuperNode has subnodes
          throw new Exception("SuperNode contains Nodes: Type change not possible");
        }
        newNode = new BasicNode(n);
      } else {
        if (getContent() != null && ! getContent().trim().isEmpty())  {
          throw new Exception("BasicNode with Code: Type change not possible");
        }
        newNode = new SuperNode(this);
        // adapt node lists of parent SuperNode
      }
    }
    for (AbstractEdge e : incoming) {
      e.connect(e.getSourceNode(), newNode);
    }
    s.removeNode(this);
    s.addNode(newNode);
    return newNode;
  }

  /** Add an outgoing edge from this node, with all consequences.
   *
   *  For use in GUI modifications: add new edge, copy, paste, etc.
   *
   *  NODE MODIFICATION, ONLY THROUGH ACTION (not finally checked)
   */
  public void addEdge(AbstractEdge e) {
    mDocksTaken.set(e.getSourceDock());
    e.getTargetNode().mDocksTaken.set(e.getTargetDock());
    if ((e instanceof EpsilonEdge) || e instanceof TimeoutEdge)
      mDEdge = e;
    else if (e instanceof ForkingEdge)
      mFEdgeList.add((ForkingEdge) e);
    else if (e instanceof GuardedEdge)
      mCEdgeList.add((GuardedEdge) e);
    else if (e instanceof RandomEdge)
      mPEdgeList.add((RandomEdge) e);
    else if (e instanceof InterruptEdge)
      mIEdgeList.add((InterruptEdge) e);
    // this is an end node if either it has no outgoing edges at all, or
    // only outgoing conditional edges
  }

  /** Remove an outgoing edge from this node, with all consequences.
   *
   *  For use in GUI modifications: delete edge, cut, etc.
   *
   *  NODE MODIFICATION, ONLY THROUGH ACTION (not finally checked)
   */
  public void removeEdge(AbstractEdge e) {
    mDocksTaken.clear(e.getSourceDock());
    e.getTargetNode().mDocksTaken.clear(e.getTargetDock());
    if ((e instanceof EpsilonEdge) || e instanceof TimeoutEdge) {
      mDEdge = null; return;
    } else if (e instanceof ForkingEdge) {
      mFEdgeList.remove(e); return;
    } else if (e instanceof GuardedEdge) {
      mCEdgeList.remove(e); return;
    } else if (e instanceof RandomEdge) {
      mPEdgeList.remove(e); return;
    } else if (e instanceof InterruptEdge)
      mIEdgeList.remove(e);
  }

  /** NODE MODIFICATION */
  public void setPosition(Position value) {
    mPosition = value;
  }

  /** Return center point (position) of the node: for the model, nodes are
   *  points.
   */
  public Position getPosition() {
    return mPosition;
  }

  /** Translate this node's position by the given values
   *
   * NODE MODIFICATION
   */
  public void translate(int x, int y) {
    mPosition.translate(x, y);
  }

  public void setParentNode(SuperNode value) {
    mParentNode = value;
  }

  public SuperNode getParentNode() {
    return mParentNode;
  }

  public SceneFlow getRoot() {
    BasicNode curr = this;
    while (! (curr instanceof SceneFlow)) {
      curr = curr.getParentNode();
    }
    return (SceneFlow) curr;
  }

  /* not used
  public boolean isDockTaken(int which) {
    return mDocksTaken.get(which);
  }
  */

  /** NODE MODIFICATION */
  public void occupyDock(int which) {
    mDocksTaken.set(which);
  }

  /** NODE MODIFICATION */
  public void freeDock(int which) {
    if (which < 0) return;
    mDocksTaken.clear(which);
  }

  /*
  public ArrayList<BasicNode> getReachableNodeList() {
    ArrayList<BasicNode> reachableNodeList = new ArrayList<BasicNode>();

    reachableNodeList.add(this);
    fillReachableNodeList(reachableNodeList);

    return reachableNodeList;
  }

  private void fillReachableNodeList(ArrayList<BasicNode> fromSourceReachableNodeList) {
    for (AbstractEdge edge : getEdgeList()) {
      BasicNode targetNode = edge.getTargetNode();

      if (!fromSourceReachableNodeList.contains(targetNode)) {
        fromSourceReachableNodeList.add(targetNode);
        targetNode.fillReachableNodeList(fromSourceReachableNodeList);
      }
    }
  }*/

  @Override
  public String getContent() {
    return mCmdList.getContent();
  }

  /** NODE MODIFICATION */
  @Override
  public void setContent(String s) {
    mCmdList.setContent(s);
  }


  /***********************************************************************/
  /******************** READING THE GRAPH FROM FILE **********************/
  /***********************************************************************/

  /** NODE MODIFICATION
   *  (theoretically, though this is never used -> remove? */
  @XmlElement(name="TEdge")
  protected void setTEdge(TimeoutEdge value) { mDEdge = value; }
  protected TimeoutEdge getTEdge() {
    return mDEdge instanceof TimeoutEdge ? (TimeoutEdge)mDEdge : null;
  }

  /** NODE MODIFICATION
   *  (theoretically, though this is never used */
  @XmlElement(name="EEdge")
  protected void setEEdge(EpsilonEdge value) { mDEdge = value; }
  protected EpsilonEdge getEEdge() {
    return mDEdge instanceof EpsilonEdge ? (EpsilonEdge)mDEdge : null;
  }

  /** Only for reading the graph from file */
  protected void establishTargetNodes() {
    for (AbstractEdge edge : getEdgeList()) {
      BasicNode n = mParentNode.getChildNodeById(edge.getTargetUnid());
      if (n == null) {
        logger.error("There is no node with ID {} in SuperNode {}",
            edge.getTargetUnid(), this.getId());
      } else {
        edge.setNodes(this, n);
      }
    }
  }

  /***********************************************************************/
  /********************** COPY NODES AND SUBGRAPH  ***********************/
  /***********************************************************************/

  /** Copy fields for deep copy */
  protected void copyFieldsFrom(BasicNode b) {
    mNodeName = b.mNodeName;
    mComment = b.mComment;
    mCmdList = b.mCmdList.deepCopy();
    // unfilled: mCEdgeList, mPEdgeList, mIEdgeList, mFEdgeList, mDEdge;
    mPosition = b.mPosition.deepCopy();
    // unfilled: mParentNode
    mIsEndNode = b.mIsEndNode;
  }

  /** Deep copy, without edges, only used by SuperNode.copySubgraph and
   *  SuperNode.deepCopy
   */
  protected BasicNode deepCopy(IDManager mgr, SuperNode parentCopy) {
    BasicNode copy = new BasicNode();
    copy.copyFieldsFrom(this);
    copy.mNodeId = mgr.getNextFreeID(this);
    copy.mParentNode = parentCopy;
    return copy;
  }

  /*************************************************************************/
  /********************** MISC. PUBLIC ACCESS METHODS **********************/
  /*************************************************************************/

  public boolean isStartNode() {
    return mParentNode.isStartNode(this);
  }

  public boolean isEndNode() {
    FLAVOUR f = getFlavour();
    return f == FLAVOUR.NONE || (f == FLAVOUR.CNODE && mDEdge == null);
  }

  /* not used
  public boolean hasEdge() {
    return getFlavour() == FLAVOUR.NONE;
  }

  public boolean hasDEdge() {
    return (mDEdge != null);
  }
  */

  public FLAVOUR getFlavour() {
    if (mCEdgeList != null && mCEdgeList.size() > 0) {
      return FLAVOUR.CNODE;
    }

    if (mPEdgeList != null && mPEdgeList.size() > 0) {
      return FLAVOUR.PNODE;
    }

    if (mFEdgeList != null && mFEdgeList.size() > 0) {
      return FLAVOUR.FNODE;
    }

    if (mIEdgeList != null && mIEdgeList.size() > 0) {
      return FLAVOUR.INODE;
    }

    if (mDEdge == null) return FLAVOUR.NONE;
    return (mDEdge instanceof TimeoutEdge)
        ? FLAVOUR.TNODE : FLAVOUR.ENODE;
  }

  /** Can this node be the start node for e? Depends on edge and node type */
  public boolean canAddEdge(AbstractEdge e) {
    FLAVOUR flavour = getFlavour();
    switch (flavour) {
    case NONE:    // if node working type is unclear, allow all (except iedge for basic nodes)
      return !isBasic() || ! (e instanceof InterruptEdge);

    case ENODE:    // only one eegde is allowed
    case TNODE:    // only one tegde is allowed
      return (e instanceof GuardedEdge) || (e instanceof InterruptEdge);

    case CNODE:    // only cedges are allowed - TODO allow dedge/tedge
      return (e instanceof GuardedEdge)
          || ((mDEdge == null)
              && ((e instanceof TimeoutEdge)
                  || (e instanceof EpsilonEdge)));

    case PNODE:    // only pedges are allowed - TODO allow dedge/tedge
      return e instanceof RandomEdge;

    case FNODE:    // only fedges are allowed
      return e instanceof ForkingEdge;

    case INODE:    // allow TEdges and IEdges
      return (e instanceof InterruptEdge)
          || ((mDEdge == null)
              && ((e instanceof TimeoutEdge)
                  || (e instanceof EpsilonEdge)));
    }
    return false;
  }

  public int getNearestFreeDock(Position p, boolean source) {
    // start with the closest angle with a reasonable representation
    double angle = getPosition().angle(p);
    return Geom.findClosestDock(mDocksTaken, angle, source);
  }

  private class EdgeIterator implements Iterator<AbstractEdge> {
    private Iterator<? extends AbstractEdge> it;
    private AbstractEdge dEdge = null;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public EdgeIterator() {
      it = new ChainedIterator(mCEdgeList.iterator(), mIEdgeList.iterator(),
          mPEdgeList.iterator(), mFEdgeList.iterator());
      dEdge = mDEdge;
    }

    @Override
    public boolean hasNext() {
      return it.hasNext() || dEdge != null;
    }

    @Override
    public AbstractEdge next() {
      if (! hasNext()) throw new IllegalStateException("No next Element");
      if (! it.hasNext()) {
        AbstractEdge e = dEdge;
        dEdge = null;
        return e;
      }
      return it.next();
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
      //if (lastEdge != null) removeEdge(lastEdge);
    }
  }

  /** Return an Iterable over all edges */
  public Iterable<AbstractEdge> getEdgeList() {
    return new Iterable<AbstractEdge>() {
      @Override
      public Iterator<AbstractEdge> iterator() {
        return new EdgeIterator();
      }
    };
  }

  @Override
  public int hashCode() {
    int result = 0;
    result = 17 * result + Boolean.hashCode(mIsEndNode);

    result = 31 * result + mNodeId.hashCode();
    result = 31 * result + mNodeName.hashCode();
    result = 31 * result + mComment.hashCode();
    result = 31 * result + mCmdList.hashCode();

    for (GuardedEdge e : mCEdgeList) {
      result = 31 * result + e.hashCode();
    }
    for (RandomEdge e : mPEdgeList) {
      result = 31 * result + e.hashCode();
    }
    for (InterruptEdge e : mIEdgeList) {
      result = 31 * result + e.hashCode();
    }
    for (ForkingEdge e : mFEdgeList) {
      result = 31 * result + e.hashCode();
    }
    if (mPosition != null) {
      result = 31 * result + mPosition.hashCode();
    }
    // mDEdge and mParentNode should be covered by super node (?)...
    return result;
  }

  @Override
  public String toString() {
    return mNodeId + "[" + mNodeName + "]" + mPosition;
  }

  /** Returns a fresh Point2D for the given dock, which still must be
   *  translated by the center point of the node
   */
  public Point2D getDockPoint(int which, int width) {
    return Geom.getDockPointCircle(which, width);
  }

  /*************************************************************************/
  /********************* VONDA COMPILER ACCESS METHODS *********************/
  /*************************************************************************/

  /**
   * @return A boolean indicating if a process can die at this {@BasicNode} (if
   *         it has only conditional/interruptive outgoing edges, or none at all)
   */
  public boolean processCanDieHere() {
    return mDEdge == null && mFEdgeList.isEmpty() && mPEdgeList.isEmpty();
  }

}
