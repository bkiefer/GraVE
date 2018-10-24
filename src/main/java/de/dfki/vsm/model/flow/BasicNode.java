package de.dfki.vsm.model.flow;

import java.util.ArrayList;

import org.w3c.dom.Element;

import de.dfki.vsm.model.ModelObject;
import de.dfki.vsm.model.flow.edge.*;
import de.dfki.vsm.model.flow.graphics.node.NodeGraphics;
import de.dfki.vsm.model.sceneflow.glue.command.Command;
import de.dfki.vsm.util.cpy.CopyTool;
import de.dfki.vsm.util.ios.IOSIndentWriter;
import de.dfki.vsm.util.xml.XMLParseAction;
import de.dfki.vsm.util.xml.XMLParseError;
import de.dfki.vsm.util.xml.XMLWriteError;

/**
 * @author Gregor Mehlmann
 */
public class BasicNode implements ModelObject {

  protected String mNodeId = new String();
  protected String mNodeName = new String();
  protected String mComment = new String();
  //
  protected Command mCmdList = new Command("");
  protected ArrayList<GuardedEdge> mCEdgeList = new ArrayList();
  protected ArrayList<RandomEdge> mPEdgeList = new ArrayList();
  protected ArrayList<InterruptEdge> mIEdgeList = new ArrayList();
  protected ArrayList<ForkingEdge> mFEdgeList = new ArrayList();
  //
  protected AbstractEdge mDEdge = null;
  protected NodeGraphics mGraphics = null;
  protected SuperNode mParentNode = null;
  protected boolean mIsHistoryNode = false;

  public Byte hasNone = new Byte("0");
  public Byte hasOne = new Byte("1");
  public Byte hasMany = new Byte("2");

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

  public void setDedge(AbstractEdge value) {
    mDEdge = value;
  }

  public AbstractEdge getDedge() {
    return mDEdge;
  }

  public void removeDEdge() {
    mDEdge = null;
  }

  public void setGraphics(NodeGraphics value) {
    mGraphics = value;
  }

  public NodeGraphics getGraphics() {
    return mGraphics;
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

  public GuardedEdge getCEdgeAt(int index) {
    return mCEdgeList.get(index);
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

  public Command getCmd() {
    return mCmdList;
  }

  public void writeXML(IOSIndentWriter out) throws XMLWriteError {
    out.println("<Node id=\"" + mNodeId + "\" name=\"" + mNodeName + "\" history=\"" + mIsHistoryNode + "\">").push();

    int i = 0;

    out.println("<Commands>").push();

    mCmdList.writeXML(out);

    out.pop().println("</Commands>");

    for (i = 0; i < mCEdgeList.size(); i++) {
      mCEdgeList.get(i).writeXML(out);
    }

    if (mDEdge != null) {
      mDEdge.writeXML(out);
    }

    for (i = 0; i < mPEdgeList.size(); i++) {
      mPEdgeList.get(i).writeXML(out);
    }

    for (i = 0; i < mFEdgeList.size(); i++) {
      mFEdgeList.get(i).writeXML(out);
    }

    for (i = 0; i < mIEdgeList.size(); i++) {
      mIEdgeList.get(i).writeXML(out);
    }

    if (mGraphics != null) {
      mGraphics.writeXML(out);
    }

    out.pop().println("</Node>");
  }

  public void parseXML(Element element) throws XMLParseError {
    mNodeId = element.getAttribute("id");
    mNodeName = element.getAttribute("name");
    mIsHistoryNode = Boolean.valueOf(element.getAttribute("history"));

    final BasicNode node = this;
    StringBuilder sb = new StringBuilder();
    XMLParseAction.processChildNodes(element, new XMLParseAction() {
      public void run(Element element) throws XMLParseError {
        String tag = element.getTagName();

        if (tag.equals("Commands")) {
          XMLParseAction.processChildNodes(element, new XMLParseAction() {
            @Override
            public void run(Element element) throws XMLParseError {
              sb.append(Command.parse(element).getContent());
            }
          });
        } else if (tag.equals("Graphics")) {
          mGraphics = new NodeGraphics();
          mGraphics.parseXML(element);
        } else if (tag.equals("CEdge")) {
          GuardedEdge edge = new GuardedEdge();

          edge.parseXML(element);
          edge.setSourceNode(node);
          edge.setSourceUnid(node.getId());
          mCEdgeList.add(edge);
        } else if (tag.equals("PEdge")) {
          RandomEdge edge = new RandomEdge();

          edge.parseXML(element);
          edge.setSourceNode(node);
          edge.setSourceUnid(node.getId());
          mPEdgeList.add(edge);
        } else if (tag.equals("FEdge")) {
          ForkingEdge edge = new ForkingEdge();

          edge.parseXML(element);
          edge.setSourceNode(node);
          edge.setSourceUnid(node.getId());
          mFEdgeList.add(edge);
        } else if (tag.equals("IEdge")) {
          InterruptEdge edge = new InterruptEdge();

          edge.parseXML(element);
          edge.setSourceNode(node);
          edge.setSourceUnid(node.getId());
          mIEdgeList.add(edge);
        } else if (tag.equals("EEdge")) {
          EpsilonEdge edge = new EpsilonEdge();

          edge.parseXML(element);
          edge.setSourceNode(node);
          edge.setSourceUnid(node.getId());
          mDEdge = edge;
        } else if (tag.equals("TEdge")) {
          TimeoutEdge edge = new TimeoutEdge();

          edge.parseXML(element);
          edge.setSourceNode(node);
          edge.setSourceUnid(node.getId());
          mDEdge = edge;
        } else {
          throw new XMLParseError(null,
                  "Cannot parse the element with the tag \"" + tag + "\" into a node child!");
        }
      }
    });
    mCmdList.setContent(sb.toString());
  }

  public int getHashCode() {

    // Add hash of General Attributes
    int hashCode = ((mNodeName == null)
            ? 0
            : mNodeName.hashCode()) + ((mComment == null)
            ? 0
            : mComment.hashCode()) + ((mGraphics == null)
            ? 0
            : mGraphics.getPosition().hashCode());

    // Add hash of all commands inside BasicNode
    hashCode += mCmdList.hashCode();

    // Epsilon and Time Edges
    for (int cntEdge = 0; cntEdge < getEdgeList().size(); cntEdge++) {
      hashCode += getEdgeList().get(cntEdge).hashCode() + getEdgeList().get(cntEdge).getGraphics().getHashCode();

      // TODO: find a way to parse the TEDGE mDEGE to take timeout into accout
    }

    // Add hash of all Conditional Edges
    for (int cntEdge = 0; cntEdge < getSizeOfCEdgeList(); cntEdge++) {
      hashCode += mCEdgeList.get(cntEdge).hashCode()
              + mCEdgeList.get(cntEdge).getGraphics().getHashCode()
              + mCEdgeList.get(cntEdge).getCondition().hashCode()
              + mCEdgeList.get(cntEdge).getSourceUnid().hashCode()
              + mCEdgeList.get(cntEdge).getTargetUnid().hashCode();
    }

    // Add hash of all Probability Edges
    for (int cntEdge = 0; cntEdge < getSizeOfPEdgeList(); cntEdge++) {

      hashCode += mPEdgeList.get(cntEdge).hashCode()
              + mPEdgeList.get(cntEdge).getGraphics().getHashCode()
              + mPEdgeList.get(cntEdge).getProbability()
              + mPEdgeList.get(cntEdge).getSourceUnid().hashCode()
              + mPEdgeList.get(cntEdge).getTargetUnid().hashCode();
    }

    // Add hash of all Fork Edges
    for (int cntEdge = 0; cntEdge < mFEdgeList.size(); cntEdge++) {
      hashCode += mFEdgeList.get(cntEdge).hashCode() + mFEdgeList.get(cntEdge).getGraphics().getHashCode()
              + mFEdgeList.get(cntEdge).getSourceUnid().hashCode() + mFEdgeList.get(cntEdge).getTargetUnid().hashCode();
    }

    return hashCode;
  }
}
