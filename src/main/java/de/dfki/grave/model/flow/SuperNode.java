package de.dfki.grave.model.flow;

import java.awt.geom.Point2D;
import java.util.*;

import javax.xml.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregor Mehlmann
 */
@XmlRootElement(name="SuperNode")
@XmlAccessorType(XmlAccessType.NONE)
public class SuperNode extends BasicNode {

  public static final Logger logger = LoggerFactory.getLogger(SuperNode.class);

  @XmlElement(name="Comment")
  protected ArrayList<CommentBadge> mCommentList = new ArrayList<CommentBadge>();
  @XmlElements({
    @XmlElement(name="Node", type=BasicNode.class, required=false),
    @XmlElement(name="SuperNode", type=SuperNode.class, required=false)
  })
  protected ArrayList<BasicNode> mNodeList = new ArrayList<BasicNode>();

  @XmlAttribute(name="start")
  protected String mStartNodeId = "";
  protected BasicNode mStartNode = null;
  @XmlAttribute(name="hideLocalVar")
  protected boolean mHideLocalVarBadge = false;
  @XmlAttribute(name="hideGlobalVar")
  protected boolean mHideGlobalVarBadge = false;

  public SuperNode() {
  }

  /** Create a SuperNode from an existing BasicNode: Node Type Change */
  public SuperNode(final BasicNode node) {
    IDManager mgr = node.getRoot().getIDManager();
    mNodeId = mgr.getNextFreeID(this);
    this.copyBasicFields(node);
  }

  public boolean isBasic() { return false; }
  
  /** Get a new SuperNode from the GUI, but only if the parent SuperNode already
   *  has a BasicNode as StartNode.
   */
  public BasicNode createNode(IDManager mgr, Position p, SuperNode s) {
    // first node created must be a BasicNode!
    if (s.getNodeSize() == 0)
      return null;
    return new SuperNode().init(mgr.getNextFreeID(this), p, s);
  }

  @XmlTransient
  public BasicNode getStartNode() {
    return mStartNode;
  }

  /** NODE MODIFICATION (?)
   * TODO: In the future, this should only be allowed for BasicNodes. Currently
   * it's still in because old style projects have SuperNodes that are start
   * nodes
   */
  public void setStartNode(BasicNode value) {
    mStartNode = value;
    mStartNodeId = value.getId();
  }
  
  /** No code allowed with SuperNodes, must be associated with the 
   *  SuperNode's Start or End Node(s)
   */
  public String getContent() {
    return null;
  }
  
  public void setContent(String s) {
    throw new UnsupportedOperationException(
        "SuperNode code must moved inside the Node");
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

  /** Only for BasicNode */
  boolean isStartNode(BasicNode value) {
    return mStartNode == value;
  }

  /** Only for BasicNode */
  List<AbstractEdge> computeIncomingEdges(BasicNode node) {
    ArrayList<AbstractEdge> result = new ArrayList<>();
    for (BasicNode n : mNodeList) {
      for (AbstractEdge e : n.getEdgeList()) {
        if (e.getTargetNode() == node) {
          result.add(e);
        }
      }
    }
    return result;
  }
  
  /** Add a node to the list of nodes
   *
   *  NODE MODIFICATION
   */
  public void addNode(BasicNode value) {
    if (mNodeList.isEmpty()) {
      setStartNode(value);
    }
    mNodeList.add(value);
  }

  /** Add a node, with side effects, such as adding it as a start node if it's
   *  the first node added
   *
   * NODE MODIFICATION
   */
  public void removeNode(BasicNode value) {
    mNodeList.remove(value);
  }

  public Iterable<BasicNode> getNodes() {
    return mNodeList;
  }

  public int getNodeSize() {
    return mNodeList.size();
  }
  
  /***********************************************************************/
  /******************** READING THE GRAPH FROM FILE **********************/
  /***********************************************************************/
  
  /** Only for reading the graph from file */
  BasicNode getChildNodeById(String id) {
    for (BasicNode node : getNodes()) {
      if (node.getId().equals(id)) {
        return node;
      }
    }

    return null;
  }

  /** Only for reading the graph from file */
  public void establishParentNodes() {
    for (BasicNode node : mNodeList) {
      node.setParentNode(this);
      if (node instanceof SuperNode) {
        ((SuperNode)node).establishParentNodes();
      }
    }
  }

  /** Only for reading the graph from file */
  @Override
  public void establishTargetNodes() {
    super.establishTargetNodes();

    for (BasicNode node : getNodes()) {
      node.establishTargetNodes();
    }
  }

  /** Only for reading the graph from file */
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

    for (BasicNode node : mNodeList) {
      if (node instanceof SuperNode)
        ((SuperNode)node).establishStartNodes();
    }
  }

  /***********************************************************************/
  /********************** COPY NODES AND SUBGRAPH  ***********************/
  /***********************************************************************/
  
  /** Copy constructor, only used by deepCopy */
  private void copyFieldsFrom(final SuperNode node) {
    super.copyFieldsFrom(node);
    // unfilled: mCommentList, mNodeList, mSuperNodeList, mStartNodeMap
    mHideLocalVarBadge = node.mHideLocalVarBadge;
    mHideGlobalVarBadge = node.mHideGlobalVarBadge;
  }

  /** This copies all nodes and edges *inside* this SuperNode, but not the
   *  edges starting at this node.
   */
  protected BasicNode deepCopy(IDManager mgr, SuperNode newParent) {
    SuperNode superCopy = new SuperNode();
    superCopy.copyFieldsFrom(this);
    superCopy.mNodeId = mgr.getNextFreeID(this);
    superCopy.mParentNode = newParent;
    Map<BasicNode, BasicNode> orig2copy = new IdentityHashMap<>();
    // copy all subnodes in this SuperNode
    for (BasicNode orig : getNodes()) {
      BasicNode copy = orig.deepCopy(mgr, superCopy);
      orig2copy.put(orig, copy);
      superCopy.mNodeList.add(copy);
    }
    // copy all edges between nodes inside this SuperNode
    for (BasicNode n : getNodes()) {
      for (AbstractEdge e: n.getEdgeList()) {
        e.deepCopy(orig2copy);
      }
    }

    return superCopy;
  }

  /** This is a specialised deep copy operation, where only a *subset* of this
   *  SuperNode's nodes and all *internal* edges (i.e., edges start start *and*
   *  end in the set) are copied. All node copies of the set will be deep copies.
   *
   *  The copied node models will be subnodes of this SuperNode.
   *  @return a pair containing the copied nodes and inner edges of the set
   *          (only on this level, not the deeper SuperNodes)
   */
  public Collection<BasicNode> copyNodeSet(Collection<BasicNode> nodes) {
    Map<BasicNode, BasicNode> orig2copy = new IdentityHashMap<>();
    IDManager mgr = getRoot().getIDManager(); 
    for (BasicNode orig : nodes) {
      BasicNode copy = orig.deepCopy(mgr, this);
      orig2copy.put(orig, copy);
    }

    List<AbstractEdge> newEdges = new ArrayList<>();
    // copy all edges between nodes inside this node set
    for (BasicNode n : nodes) {
      for (AbstractEdge e: n.getEdgeList()) {
        if (nodes.contains(e.getTargetNode()))
          newEdges.add(e.deepCopy(orig2copy));
      }
    }
    return orig2copy.values();    
  }
  
  /** Remove the nodes in the given collection.
   *  This is only legal if none of the selected nodes is a start node!
   *
   *  This collects three types of edges: internal, incoming, and outgoing
   *  edges, which is defined relative to the set of nodes given as input.
   *  Incoming edges have to be removed and saved for undo, the rest disappears
   *  anyway when the nodes are removed from the graph
   *
   *  @return a triple emerging, internal, incoming edges
   */
  @SuppressWarnings("unchecked")
  public Collection<AbstractEdge>[] removeNodes(Collection<BasicNode> nodes) {
    // Edges pointin.getDataNodeg from the set to the outside
    List<AbstractEdge> emergingEdges = new ArrayList<>();
    // Edges between nodes in the set
    List<AbstractEdge> internalEdges = new ArrayList<>();
    // Edges pointing from the outside into the set
    List<AbstractEdge> incomingEdges = new ArrayList<>();

    for (BasicNode n : nodes) {
      // remove node with attached edges from this SuperNode, must be UNDOne
      removeNode(n);
      // collect internal and outgoing edges
      for(AbstractEdge e : n.getEdgeList()) {
        if (nodes.contains(e.getTargetNode())) {
          internalEdges.add(e);
        } else {
          emergingEdges.add(e);
        }
      }
    }
    // This results in quadratic complexity, but we currently don't have 
    // and incoming edge list in the model graph
    for (BasicNode n : mNodeList) {
      // for all nodes not in the set
      if (!nodes.contains(n)) {
        for(AbstractEdge e : n.getEdgeList()) {
          if (nodes.contains(e.getTargetNode())) {
            // incoming edge (not in set) --> (in set), must be removed later
            // because of ConcurrentOperationException
            incomingEdges.add(e);
          } 
        }
      }
    }
    // remove incoming edges from model, this destructively changes the source
    // node of the edge, which must be UNDOne
    for (AbstractEdge e : incomingEdges) {
      e.getSourceNode().removeEdge(e);
    }
    Collection<AbstractEdge>[] result =
        new Collection[]{ emergingEdges, internalEdges, incomingEdges };
    return result;
  }
  
  /** Add a set of edges. Prerequisite: the node models and the
   *  edges and their respective models exist, and were not modified in a way
   *  which interferes with, e.g., the docking points, or the positions.
   */
  public void addEdges(Collection<AbstractEdge> edges) {
    for (AbstractEdge e: edges) {
      e.getSourceNode().addEdge(e);
    }
  }
  
  /** Add the given nodes and edges of a disconnected subgraph of nodes as is
   *  to the view and model. This is used if re-inserted after a cut operation,
   *  or as part of an undo. The nodes are just added, without further 
   *  modifications.
   */
  public void addNodes(Collection<BasicNode> nodes) {
    // first add all nodes, so they exist when adding edges
    for (BasicNode n : nodes) {
      addNode(n);
    }
  }
  
  /*************************************************************************/
  /********************* MISC. PUBLIC ACCESS METHODS ***********************/
  /*************************************************************************/
  
  /*
  @Override
  public int hashCode() {
    int hash = 5;
    hash = 53 * hash + mCommentList.hashCode();
    hash = 53 * hash + mNodeList.hashCode();
    hash = 53 * hash + (mStartNodeId == null ? 0 : mStartNodeId.hashCode());
    hash = 53 * hash + Boolean.hashCode(this.mHideLocalVarBadge);
    hash = 53 * hash + Boolean.hashCode(this.mHideGlobalVarBadge);
    return hash;
  }
  */

  /** Dock point for square, returns a fresh Point2D for the given dock, which 
   *  still must be translated by the center point of the node */
  public Point2D getDockPoint(int which, int width) {
    return Geom.getDockPointSquare(which, width);
  }
}
