package de.dfki.vsm.model.flow;

import java.util.ArrayList;

import javax.xml.bind.annotation.*;

import de.dfki.vsm.model.flow.edge.*;
import de.dfki.vsm.model.flow.geom.Position;
import de.dfki.vsm.util.cpy.CopyTool;
import de.dfki.vsm.util.cpy.Copyable;

/**
 * @author Gregor Mehlmann
 */
@XmlType(name="Node")
public class BasicNode implements Copyable {

  @XmlAttribute(name="id")
  protected String mNodeId = new String();
  @XmlAttribute(name="name")
  protected String mNodeName = new String();
  @XmlAttribute(name="comment")
  protected String mComment = new String();
  protected String mCmdList = new String();

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
  @XmlTransient
  protected SuperNode mParentNode = null;
  @XmlAttribute(name="history")
  protected boolean mIsHistoryNode = false;

  @XmlTransient
  public Byte hasNone = new Byte("0");
  @XmlTransient
  public Byte hasOne = new Byte("1");
  @XmlTransient
  public Byte hasMany = new Byte("2");

  @XmlTransient
  public enum FLAVOUR {

    NONE, ENODE, TNODE, CNODE, PNODE, INODE, FNODE
  };

  public BasicNode() {
  }

  //
  public boolean isSubNodeOf(BasicNode node) {
    if (node instanceof SuperNode) {
      SuperNode parentNode = mParentNode;

      while (parentNode != null) {
        if (parentNode.equals(node)) {
          return true;
        } else {
          parentNode = parentNode.getParentNode();
        }
      }
    }
    return false;
  }

  @XmlTransient
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

  @XmlTransient
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

  @XmlTransient
  public void setComment(String value) {
    mComment = value;
  }

  public String getComment() {
    return mComment;
  }

//    public void setExhaustive(boolean value) {
//        mExhaustive = value;
//    }
//
//    public boolean getExhaustive() {
//        return mExhaustive;
//    }
//
//    public void setPreserving(boolean value) {
//        mPreserving = value;
//    }
//
//    public boolean getPreserving() {
//        return mPreserving;
//    }
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

    if (mDEdge != null) {
      return true;
    }

    if (mCEdgeList != null) {
      if (mCEdgeList.size() > 0) {
        return true;
      }
    }

    if (mPEdgeList != null) {
      if (mPEdgeList.size() > 0) {
        return true;
      }
    }

    if (mFEdgeList != null) {
      if (mFEdgeList.size() > 0) {
        return true;
      }
    }

    if (mIEdgeList != null) {
      if (mIEdgeList.size() > 0) {
        return true;
      }
    }
    return false;
  }

  /**
   * Tells if the node has more than 0 Probabilistic edge in case true, it is
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

  @XmlTransient
  public FLAVOUR getFlavour() {
    if (mCEdgeList != null) {
      if (mCEdgeList.size() > 0) {
        return FLAVOUR.CNODE;
      }
    }

    if (mPEdgeList != null) {
      if (mPEdgeList.size() > 0) {
        return FLAVOUR.PNODE;
      }
    }

    if (mFEdgeList != null) {
      if (mFEdgeList.size() > 0) {
        return FLAVOUR.FNODE;
      }
    }

    if (mIEdgeList != null) {
      if (mIEdgeList.size() > 0) {
        return FLAVOUR.INODE;
      }
    }

    if (mDEdge != null) {
      return (mDEdge instanceof TimeoutEdge)
              ? FLAVOUR.TNODE
              : FLAVOUR.ENODE;
    }

    return FLAVOUR.NONE;
  }

  @XmlTransient
  public void setDedge(AbstractEdge value) {
    mDEdge = value;
  }

  public AbstractEdge getDedge() {
    return mDEdge;
  }

  public void removeDEdge() {
    mDEdge = null;
  }

  /** Not to be used! */
  @XmlElement(name="TEdge")
  public void setTEdge(TimeoutEdge value) { mDEdge = value; }
  public TimeoutEdge getTEdge() { return (TimeoutEdge)mDEdge; }

  /** Not to be used! */
  @XmlElement(name="EEdge")
  protected void setEEdge(EpsilonEdge value) { mDEdge = value; }
  protected EpsilonEdge getEEdge() { return (EpsilonEdge)mDEdge; }

  @XmlTransient
  public void setPosition(Position value) {
    mPosition = value;
  }

  public Position getPosition() {
    return mPosition;
  }

  @XmlTransient
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

  public void addFEdge(ForkingEdge value) {
    mFEdgeList.add(value);
  }

  public void removeFEdge(ForkingEdge value) {
    mFEdgeList.remove(value);
  }

  public void removeAllFEdges() {
    mFEdgeList = new ArrayList<ForkingEdge>();
  }

  public ArrayList<ForkingEdge> getFEdgeList() {
    return mFEdgeList;
  }

  public void addPEdge(RandomEdge value) {
    mPEdgeList.add(value);
  }

  public RandomEdge getPEdgeAt(int index) {
    return mPEdgeList.get(index);
  }

  public void removePEdge(RandomEdge value) {
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

  public void addIEdge(InterruptEdge value) {
    mIEdgeList.add(value);
  }

  public InterruptEdge getIEdgeAt(int index) {
    return mIEdgeList.get(index);
  }

  public void removeIEdge(InterruptEdge value) {
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

  public ArrayList<AbstractEdge> getEdgeList() {
    ArrayList<AbstractEdge> edgeList = new ArrayList<AbstractEdge>();

    for (GuardedEdge edge : mCEdgeList) {
      edgeList.add(edge);
    }

    for (InterruptEdge edge : mIEdgeList) {
      edgeList.add(edge);
    }

    for (RandomEdge edge : mPEdgeList) {
      edgeList.add(edge);
    }

    for (ForkingEdge edge : mFEdgeList) {
      edgeList.add(edge);
    }

    if (mDEdge != null) {
      edgeList.add(mDEdge);
    }

    return edgeList;
  }

  protected void establishTargetNodes() {
    for (AbstractEdge edge : getEdgeList()) {
      edge.setTargetNode(mParentNode.getChildNodeById(edge.getTargetUnid()));
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

  public String getAbstractSyntax() {
    return null;
  }

  public String getConcreteSyntax() {
    return this.mNodeName;
  }

  public String getFormattedSyntax() {
    return null;
  }

  public BasicNode getCopy() {
    return (BasicNode) CopyTool.copy(this);
  }

  @XmlElement(name="Commands")
  public String getCmd() {
    return mCmdList;
  }

  public void setCmd(String s) {
    mCmdList = s;
  }

  public int getHashCode() {

    // Add hash of General Attributes
    int hashCode = ((mNodeName == null)
            ? 0
            : mNodeName.hashCode()) + ((mComment == null)
            ? 0
            : mComment.hashCode()) + ((mPosition == null)
            ? 0
            : mPosition.hashCode());

    // Add hash of all commands inside BasicNode
    hashCode += mCmdList.hashCode();

    // Epsilon and Time Edges
    for (int cntEdge = 0; cntEdge < getEdgeList().size(); cntEdge++) {
      hashCode += getEdgeList().get(cntEdge).hashCode() + getEdgeList().get(cntEdge).getArrow().hashCode();

      // TODO: find a way to parse the TEDGE mDEGE to take timeout into accout
    }

    // Add hash of all Conditional Edges
    for (int cntEdge = 0; cntEdge < getSizeOfCEdgeList(); cntEdge++) {
      hashCode += mCEdgeList.get(cntEdge).hashCode()
              + mCEdgeList.get(cntEdge).getArrow().hashCode()
              + mCEdgeList.get(cntEdge).getCondition().hashCode()
              + mCEdgeList.get(cntEdge).getSourceUnid().hashCode()
              + mCEdgeList.get(cntEdge).getTargetUnid().hashCode();
    }

    // Add hash of all Probability Edges
    for (int cntEdge = 0; cntEdge < getSizeOfPEdgeList(); cntEdge++) {

      hashCode += mPEdgeList.get(cntEdge).hashCode()
              + mPEdgeList.get(cntEdge).getArrow().hashCode()
              + mPEdgeList.get(cntEdge).getProbability()
              + mPEdgeList.get(cntEdge).getSourceUnid().hashCode()
              + mPEdgeList.get(cntEdge).getTargetUnid().hashCode();
    }

    // Add hash of all Fork Edges
    for (int cntEdge = 0; cntEdge < mFEdgeList.size(); cntEdge++) {
      hashCode += mFEdgeList.get(cntEdge).hashCode() + mFEdgeList.get(cntEdge).getArrow().hashCode()
              + mFEdgeList.get(cntEdge).getSourceUnid().hashCode() + mFEdgeList.get(cntEdge).getTargetUnid().hashCode();
    }

    return hashCode;
  }
}
