package de.dfki.vsm.model.flow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.vsm.model.flow.geom.Position;
import de.dfki.vsm.util.cpy.CopyTool;
import de.dfki.vsm.util.cpy.Copyable;

/**
 * @author Gregor Mehlmann
 */
@XmlType(name="Node")
@XmlAccessorType(XmlAccessType.NONE)
public class BasicNode implements Copyable {

  private static final Logger logger = LoggerFactory.getLogger(BasicNode.class);

  @XmlAttribute(name="id")
  protected String mNodeId = new String();
  @XmlAttribute(name="name")
  protected String mNodeName = new String();
  @XmlAttribute(name="comment")
  protected String mComment = new String();
  @XmlElement(name="Commands")
  protected Code mCmdList = new Code();


  // TODO: MERGE INTO ONE LIST!
  @XmlElement(name="CEdge")
  protected ArrayList<GuardedEdge> mCEdgeList = new ArrayList<>();
  @XmlElement(name="PEdge")
  protected ArrayList<RandomEdge> mPEdgeList = new ArrayList<>();
  @XmlElement(name="IEdge")
  protected ArrayList<InterruptEdge> mIEdgeList = new ArrayList<>();
  @XmlElement(name="FEdge")
  protected ArrayList<ForkingEdge> mFEdgeList = new ArrayList<>();

  protected AbstractEdge mDEdge = null;
  @XmlElement(name="Position")
  protected Position mPosition = null;

  protected SuperNode mParentNode = null;
  @XmlAttribute(name="history")
  protected boolean mIsHistoryNode = false;

  public Byte hasNone = new Byte("0");
  public Byte hasOne = new Byte("1");
  public Byte hasMany = new Byte("2");

  public enum FLAVOUR {
    NONE, ENODE, TNODE, CNODE, PNODE, INODE, FNODE
  };

  public BasicNode() {
  }

  public void setId(String value) {
    mNodeId = value;
  }

  public String getId() {
    return mNodeId;
  }

  public boolean isHistoryNode() {
    return mIsHistoryNode;
  }

  public void setHistoryNodeFlag(boolean value) {
    mIsHistoryNode = value;
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
    if (mComment == null) {
      return false;
    }

    if (mComment.length() == 0) {
      return false;
    }

    return true;
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

  public void addEdge(AbstractEdge e) {
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
  }

  public void removeEdge(AbstractEdge e) {
    if ((e instanceof EpsilonEdge) || e instanceof TimeoutEdge)
      mDEdge = null;
    else if (e instanceof ForkingEdge)
      removeFEdge((ForkingEdge) e);
    else if (e instanceof GuardedEdge)
      removeCEdge((GuardedEdge) e);
    else if (e instanceof RandomEdge)
      removePEdge((RandomEdge) e);
    else if (e instanceof InterruptEdge)
      removeIEdge((InterruptEdge) e);
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

  public ArrayList<GuardedEdge> getCopyOfCEdgeList() {
    ArrayList<GuardedEdge> copy = new ArrayList<GuardedEdge>();

    for (GuardedEdge edge : mCEdgeList) {
      copy.add(edge.getCopy());
    }

    return copy;
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

  public ArrayList<RandomEdge> getCopyOfPEdgeList() {
    ArrayList<RandomEdge> copy = new ArrayList<RandomEdge>();

    for (RandomEdge edge : mPEdgeList) {
      copy.add(edge.getCopy());
    }

    return copy;
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

  public ArrayList<InterruptEdge> getCopyOfIEdgeList() {
    ArrayList<InterruptEdge> copy = new ArrayList<InterruptEdge>();

    for (InterruptEdge edge : mIEdgeList) {
      copy.add(edge.getCopy());
    }

    return copy;
  }

  private class EdgeIterable implements Iterable<AbstractEdge> {

    private LinkedList<Iterator<? extends AbstractEdge>> iterators;
    private AbstractEdge dEdge = null;

    private class EdgeIterator implements Iterator<AbstractEdge> {
      @Override
      public boolean hasNext() {
        while (! iterators.isEmpty() && ! iterators.getFirst().hasNext()) {
          iterators.removeFirst();
        }
        return ! iterators.isEmpty() || dEdge != null;
      }

      @Override
      public AbstractEdge next() {
        if (! hasNext()) throw new IllegalStateException("No next Element");
        if (iterators.isEmpty()) {
          AbstractEdge e = dEdge;
          dEdge = null;
          return e;
        }
        return iterators.getFirst().next();
      }
    }

    @Override
    public Iterator<AbstractEdge> iterator() {
      iterators = new LinkedList<>();
      iterators.add(mCEdgeList.iterator());
      iterators.add(mIEdgeList.iterator());
      iterators.add(mPEdgeList.iterator());
      iterators.add(mFEdgeList.iterator());
      dEdge = mDEdge;
      return new EdgeIterator();
    }

  }

  public Iterable<AbstractEdge> getEdgeList() {
    return new EdgeIterable();
  }

  protected void establishTargetNodes() {
    for (AbstractEdge edge : getEdgeList()) {
      BasicNode n = mParentNode.getChildNodeById(edge.getTargetUnid());
      if (n == null) {
        logger.error("There is no node with ID {} in SuperNode {}",
            edge.getTargetUnid(), this.getId());
      } else {
        edge.setTargetNode(n);
      }
      edge.setSourceNode(this);
      edge.setSourceUnid(getId());
    }
  }

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
  }

  public BasicNode getCopy() {
    return (BasicNode) CopyTool.copy(this);
  }

  public String getCmd() {
    return mCmdList.getContent();
  }

  public void setCmd(String s) {
    mCmdList.setContent(s);
  }

  public int hashCode() {
    // the id is unique, so it's absolutely OK to just use the id's hashcode
    return mNodeId.hashCode() + 91;
  }
}
