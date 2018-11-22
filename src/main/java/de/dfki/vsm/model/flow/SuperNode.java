package de.dfki.vsm.model.flow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import de.dfki.vsm.editor.util.IDManager;
import de.dfki.vsm.model.flow.geom.Position;

/**
 * @author Gregor Mehlmann
 */
@XmlType(name="SuperNode")
@XmlAccessorType(XmlAccessType.NONE)
public class SuperNode extends BasicNode implements Iterable<BasicNode> {

  public static class StartNodeAdapter extends XmlAdapter<String, Map<String, BasicNode>> {
    @Override
    public String marshal(Map<String, BasicNode> v) throws Exception {
      StringBuilder sb = new StringBuilder();
      for (String s : v.keySet()) {
        sb.append(s).append(';');
      }
      return sb.toString();
    }

    @Override
    public Map<String, BasicNode> unmarshal(String v) throws Exception {
      Map<String, BasicNode> result = new HashMap<>();
      for (String str : v.split(";")) {
        if (!str.isEmpty() && !str.equals("null")) {
          result.put(str, null);
        }
      }
      return result;
    }
  }

  @XmlElement(name="Comment")
  protected ArrayList<CommentBadge> mCommentList = new ArrayList<CommentBadge>();
  @XmlElement(name="Node")
  protected ArrayList<BasicNode> mNodeList = new ArrayList<BasicNode>();
  @XmlElement(name="SuperNode")
  protected ArrayList<SuperNode> mSuperNodeList = new ArrayList<SuperNode>();
  @XmlAttribute(name="start")
  @XmlJavaTypeAdapter(StartNodeAdapter.class)
  protected HashMap<String, BasicNode> mStartNodeMap = new HashMap<String, BasicNode>();
  protected BasicNode mHistoryNode = null;
  @XmlAttribute(name="hideLocalVar")
  protected boolean mHideLocalVarBadge = false;
  @XmlAttribute(name="hideGlobalVar")
  protected boolean mHideGlobalVarBadge = false;

  public SuperNode() {
  }

  /** Create a SuperNode from an existing BasicNode: Node Type Change */
  public SuperNode(IDManager mgr, final BasicNode node) {
    mNodeId = mgr.getNextFreeID(this);
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
    mIsHistoryNode = node.mIsHistoryNode;
    mHistoryNode = new BasicNode(this);
    mNodeList.add(mHistoryNode);
  }

  /** Create a new SuperNode from the GUI */
  public SuperNode(IDManager mgr, Position pos, SuperNode s) {
    mNodeId = mNodeName = mgr.getNextFreeID(this);
    mPosition = pos;
    mParentNode = s;
    mParentNode.addSuperNode(this);
    mHistoryNode = new BasicNode(this);
    mNodeList.add(mHistoryNode);
  }

  @XmlTransient
  public HashMap<String, BasicNode> getStartNodeMap() {
    return mStartNodeMap;
  }

  public void setStartNodeMap(HashMap<String, BasicNode> value) {
    mStartNodeMap = value;
  }

  public void addStartNode(BasicNode node) {
    mStartNodeMap.put(node.getId(), node);
  }

  public void removeStartNode(BasicNode node) {
    mStartNodeMap.remove(node.getId());
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

  public void removeComment(CommentBadge value) {
    mCommentList.remove(value);
  }

  public ArrayList<CommentBadge> getCommentList() {
    return mCommentList;
  }

  @XmlTransient
  public void setHistoryNode(BasicNode value) {
    mHistoryNode = value;
  }

  public BasicNode getHistoryNode() {
    return mHistoryNode;
  }

  public boolean isStartNode(BasicNode value) {
    return mStartNodeMap.containsKey(value.getId());
  }

  public void addSuperNode(SuperNode value) {
    mSuperNodeList.add(value);
  }

  public void removeSuperNode(SuperNode value) {
    mSuperNodeList.remove(value);
  }

  public ArrayList<SuperNode> getSuperNodeList() {
    return mSuperNodeList;
  }

  ArrayList<SuperNode> getCopyOfSuperNodeList() {
    ArrayList<SuperNode> copy = new ArrayList<SuperNode>();

    for (SuperNode node : mSuperNodeList) {
      copy.add(node.getCopy());
    }

    return copy;
  }

  /** Add a node, with side effects, such as adding it as a start node if it's
   *  the first node added
   */
  public void addNode(BasicNode value) {
    mNodeList.add(value);
  }

  /** Remove a node */
  public void removeNode(BasicNode value) {
    mNodeList.remove(value);
  }

  public Iterable<BasicNode> getNodeList() {
    return mNodeList;
  }

  ArrayList<BasicNode> getCopyOfNodeList() {
    ArrayList<BasicNode> copy = new ArrayList<BasicNode>();

    for (BasicNode node : mNodeList) {
      copy.add(node.getCopy());
    }

    return copy;
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

  public Iterator<BasicNode> iterator() {
    return new NodeIterator();
  }

  public int getNodeSize() {
    return mNodeList.size() + mSuperNodeList.size();
  }

  public ArrayList<BasicNode> getCopyOfNodeAndSuperNodeList() {
    ArrayList<BasicNode> copy = new ArrayList<BasicNode>();

    for (BasicNode n : this) {
      copy.add(n.getCopy());
    }

    return copy;
  }

  public BasicNode getChildNodeById(String id) {
    for (BasicNode node : this) {
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

    for (BasicNode node : this) {
      node.establishTargetNodes();
    }
  }

  /*
  public void establishStartNodes() {
    for (SuperNode n : mSuperNodeList) {
      n.establishStartNodes();
      if (n.mIsStartNode) {
        mStartNode = n;
      }
    }
    if (mStartNode != null)
      return;
    for (BasicNode n : mNodeList) {
      if (n.mIsStartNode) {
        mStartNode = n;
        return;
      }
    }
  }
  */

  public void establishStartNodes() {
    for (String id : mStartNodeMap.keySet()) {
      mStartNodeMap.put(id, getChildNodeById(id));
    }

    for (SuperNode node : mSuperNodeList) {
      node.establishStartNodes();
    }
  }

  public SuperNode getCopy() {
    return (SuperNode) null;
  }

}
