package de.dfki.vsm.model.flow;

import java.util.*;

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
public class SuperNode extends BasicNode {

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

  /** Create a new SuperNode from the GUI */
  public SuperNode(IDManager mgr, Position pos, SuperNode s) {
    mNodeId = mNodeName = mgr.getNextFreeID(this);
    mPosition = pos;
    mParentNode = s;
    mParentNode.mSuperNodeList.add(this);
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
    return mStartNodeMap.containsKey(value.getId());
  }

  /** Add a node to the list of nodes */
  public void addNode(BasicNode value) {
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
    for (String id : mStartNodeMap.keySet()) {
      mStartNodeMap.put(id, getChildNodeById(id));
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

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 53 * hash + this.mCommentList.hashCode();
    hash = 53 * hash + this.mNodeList.hashCode();
    hash = 53 * hash + this.mSuperNodeList.hashCode();
    hash = 53 * hash + this.mStartNodeMap.hashCode();
    hash = 53 * hash + Boolean.hashCode(this.mHideLocalVarBadge);
    hash = 53 * hash + Boolean.hashCode(this.mHideGlobalVarBadge);
    return hash;
  }

}
