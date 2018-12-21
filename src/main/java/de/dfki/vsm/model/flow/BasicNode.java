package de.dfki.vsm.model.flow;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.vsm.editor.util.IDManager;
import de.dfki.vsm.model.flow.geom.Geom;
import de.dfki.vsm.model.flow.geom.Position;
import de.dfki.vsm.util.ChainedIterator;

/**
 * @author Gregor Mehlmann
 */
@XmlType(name="Node")
@XmlAccessorType(XmlAccessType.NONE)
public class BasicNode  {

  private static final Logger logger = LoggerFactory.getLogger(BasicNode.class);

  private static final int WIDTH = 100; // default width for model

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

  /** Top-left corner of the node */
  @XmlElement(name="Position")
  protected Position mPosition = null;

  protected SuperNode mParentNode = null;

  protected boolean mIsEndNode = true;

  protected BitSet mDocksTaken = new BitSet();

  public Byte hasNone = new Byte("0");
  public Byte hasOne = new Byte("1");
  public Byte hasMany = new Byte("2");

  public enum FLAVOUR {
    NONE, ENODE, TNODE, CNODE, PNODE, INODE, FNODE
  };

  public BasicNode() {
  }

  /** plain creation of new BasicNode, with no other side effects in the graph */
  public BasicNode(IDManager mgr, Position p, SuperNode s) {
    mNodeId = mNodeName = mgr.getNextFreeID(this);
    mPosition = p;
    mParentNode = s;
    mParentNode.mNodeList.add(this);
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
  public BasicNode(IDManager mgr, final SuperNode node) {
    assert(node.mNodeList.isEmpty() && node.mSuperNodeList.isEmpty());
    mNodeId = mgr.getNextFreeID(this);
    copyBasicFields(node);
  }

  public Code getCode() {
    return mCmdList;
  }

  public String getId() {
    return mNodeId;
  }

  public void setName(String value) {
    mNodeName = value;
  }

  public String getName() {
    return mNodeName;
  }

  public void setNameAndId(String value) {
    mNodeId = value;
    mNodeName = value;
  }

  public void setComment(String value) {
    mComment = value;
  }

  public String getComment() {
    return mComment;
  }

  public boolean hasComment() {
    return ! (mComment == null || mComment.isEmpty());
  }

  public boolean hasEdge() {
    return getFlavour() == FLAVOUR.NONE;
  }

  public boolean canAddEdge(AbstractEdge e) {
    FLAVOUR flavour = getFlavour();
    switch (flavour) {
    case NONE:    // if node working type is unclear, allow all (except iedge for nodes)
      return true;

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

  /** Add an outgoing edge from this node, with all consequences.
   *
   *  For use in GUI modifications: add new edge, copy, paste, etc.
   */
  public void addEdge(AbstractEdge e) {
    mDocksTaken.set(e.getSourceDock());
    e.getTargetNode().mDocksTaken.set(e.getTargetDock());
    if ((e instanceof EpsilonEdge) || e instanceof TimeoutEdge)
      mDEdge = e;
    else if (e instanceof ForkingEdge)
      addFEdge((ForkingEdge) e);
    else if (e instanceof GuardedEdge)
      addCEdge((GuardedEdge) e);
    else if (e instanceof RandomEdge)
      addPEdge((RandomEdge) e);
    else if (e instanceof InterruptEdge)
      addIEdge((InterruptEdge) e);
    // this is an end node if either it has no outgoing edges at all, or
    // only outgoing conditional edges
  }

  /** Remove an outgoing edge from this node, with all consequences.
   *
   *  For use in GUI modifications: delete edge, cut, etc.
   */
  public void removeEdge(AbstractEdge e) {
    mDocksTaken.clear(e.getSourceDock());
    e.getTargetNode().mDocksTaken.clear(e.getTargetDock());
    if ((e instanceof EpsilonEdge) || e instanceof TimeoutEdge) {
      mDEdge = null; return;
    } else if (e instanceof ForkingEdge) {
      removeFEdge((ForkingEdge) e); return;
    } else if (e instanceof GuardedEdge) {
      removeCEdge((GuardedEdge) e); return;
    } else if (e instanceof RandomEdge) {
      removePEdge((RandomEdge) e); return;
    } else if (e instanceof InterruptEdge)
      removeIEdge((InterruptEdge) e);
  }

  public boolean isStartNode() {
    return mParentNode.mStartNodeMap.containsKey(mNodeId);
  }

  public boolean isEndNode() {
    FLAVOUR f = getFlavour();
    return f == FLAVOUR.NONE || (f == FLAVOUR.CNODE && mDEdge == null);
  }

  /**
   * tells if the node has more than 0 Probabilistic edge in case true, it is
   * necessary to reorganise the values of probabilities Used when deleting an
   * edge
   *
   * @return
   */
  public byte hasPEdges() {
    if (mPEdgeList.size() == 1) {
      return hasOne;
    }
    if (mPEdgeList.size() > 1) {
      return hasMany;
    }
    return hasNone;
  }

  public RandomEdge getFirstPEdge() {
    return mPEdgeList.get(0);
  }

  public boolean hasDEdge() {
    return (mDEdge != null);
  }

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

  public void setDedge(AbstractEdge value) {
    mDEdge = value;
  }

  public AbstractEdge getDedge() {
    return mDEdge;
  }

  public void removeDEdge() {
    mDEdge = null;
  }

  @XmlElement(name="TEdge")
  public void setTEdge(TimeoutEdge value) { mDEdge = value; }
  public TimeoutEdge getTEdge() {
    return mDEdge instanceof TimeoutEdge ? (TimeoutEdge)mDEdge : null;
  }

  @XmlElement(name="EEdge")
  protected void setEEdge(EpsilonEdge value) { mDEdge = value; }
  protected EpsilonEdge getEEdge() {
    return mDEdge instanceof EpsilonEdge ? (EpsilonEdge)mDEdge : null;
  }

  public void setPosition(Position value) {
    mPosition = value;
  }

  public Position getPosition() {
    return mPosition;
  }

  /** Translate this node's position by the given values */
  public void translate(int x, int y) {
    mPosition.setXPos(mPosition.getXPos() + x);
    mPosition.setYPos(mPosition.getYPos() + y);
  }

  public void setParentNode(SuperNode value) {
    mParentNode = value;
  }

  public SuperNode getParentNode() {
    return mParentNode;
  }

  public void addCEdge(GuardedEdge value) {
    mCEdgeList.add(value);
  }

  public void removeCEdge(GuardedEdge value) {
    mCEdgeList.remove(value);
  }

  public void removeAllCEdges() {
    mCEdgeList = new ArrayList<GuardedEdge>();
  }

  public int getSizeOfCEdgeList() {
    return mCEdgeList.size();
  }

  public ArrayList<GuardedEdge> getCEdgeList() {
    return mCEdgeList;
  }

  private void addFEdge(ForkingEdge value) {
    mFEdgeList.add(value);
  }

  private void removeFEdge(ForkingEdge value) {
    mFEdgeList.remove(value);
  }

  public void removeAllFEdges() {
    mFEdgeList = new ArrayList<ForkingEdge>();
  }

  public ArrayList<ForkingEdge> getFEdgeList() {
    return mFEdgeList;
  }

  private void addPEdge(RandomEdge value) {
    mPEdgeList.add(value);
  }

  private void removePEdge(RandomEdge value) {
    mPEdgeList.remove(value);
  }

  public void removeAllPEdges() {
    mPEdgeList = new ArrayList<RandomEdge>();
  }

  public int getSizeOfPEdgeList() {
    return mPEdgeList.size();
  }

  public ArrayList<RandomEdge> getPEdgeList() {
    return mPEdgeList;
  }

  private void addIEdge(InterruptEdge value) {
    mIEdgeList.add(value);
  }

  private void removeIEdge(InterruptEdge value) {
    mIEdgeList.remove(value);
  }

  public void removeAllIEdges() {
    mIEdgeList = new ArrayList<InterruptEdge>();
  }

  public int getSizeOfIEdgeList() {
    return mIEdgeList.size();
  }

  public ArrayList<InterruptEdge> getIEdgeList() {
    return mIEdgeList;
  }

  public boolean isDockTaken(int which) {
    return mDocksTaken.get(which);
  }

  public void occupyDock(int which) {
    mDocksTaken.set(which);
  }

  public void freeDock(int which) {
    mDocksTaken.clear(which);
  }

  public Point getCenter() {
    return new Point(mPosition.getXPos() + WIDTH/2, mPosition.getYPos() + WIDTH/2);
  }

  public int getNearestFreeDock(Point p) {
    // start with the closest angle with a reasonable representation
    double angle = Geom.angle(getCenter(), p);
    return Geom.findClosestDock(mDocksTaken, angle);
  }

  /** Dock point for circle */
  protected Point2D getMyDockPoint(int which, int width) {
    return Geom.getDockPointCircle(which, width);
  }

  /** Returns a fresh Point for the given dock */
  public Point getDockPoint(int which, int width) {
    Point p = getCenter();
    Point2D dp = getMyDockPoint(which, width);
    p.translate((int)dp.getX(), (int)dp.getY());
    return p;
  }

  private class EdgeIterator implements Iterator<AbstractEdge> {
    private Iterator<? extends AbstractEdge> it;
    private AbstractEdge lastEdge = null;
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
        lastEdge = dEdge;
        AbstractEdge e = dEdge;
        dEdge = null;
        return e;
      }
      return lastEdge = it.next();
    }

    @Override
    public void remove() {
      if (lastEdge != null)
        removeEdge(lastEdge);
    }
  }

  public Iterable<AbstractEdge> getEdgeList() {
    return new Iterable<AbstractEdge>() {
      @Override
      public Iterator<AbstractEdge> iterator() {
        return new EdgeIterator();
      }
    };
  }


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

  public String getCmd() {
    return mCmdList.getContent();
  }

  public void setCmd(String s) {
    mCmdList.setContent(s);
  }

  @Override
  public int hashCode() {
    int result = 0;
    result = 17 * result + Boolean.hashCode(mIsEndNode);
    result = 17 * result + Byte.hashCode(hasNone);
    result = 17 * result + Byte.hashCode(hasOne);
    result = 17 * result + Byte.hashCode(hasMany);

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

  /** Deep copy, without edges */
  public BasicNode deepCopy(IDManager mgr, SuperNode parentCopy) {
    BasicNode copy = new BasicNode();
    copy.copyFieldsFrom(this);
    copy.mNodeId = mgr.getNextFreeID(this);
    copy.mParentNode = parentCopy;
    return copy;
  }

  public String toString() {
    return mNodeId + "[" + mNodeName + "]";
  }
}
