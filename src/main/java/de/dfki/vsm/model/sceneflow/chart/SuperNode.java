package de.dfki.vsm.model.sceneflow.chart;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Element;

import de.dfki.vsm.model.sceneflow.chart.badge.CommentBadge;
import de.dfki.vsm.model.sceneflow.chart.badge.VariableBadge;
import de.dfki.vsm.model.sceneflow.chart.edge.AbstractEdge;
import de.dfki.vsm.model.sceneflow.glue.command.Command;
import de.dfki.vsm.util.ios.IOSIndentWriter;
import de.dfki.vsm.util.tpl.TPLTuple;
import de.dfki.vsm.util.xml.XMLParseAction;
import de.dfki.vsm.util.xml.XMLParseError;
import de.dfki.vsm.util.xml.XMLWriteError;

/**
 * @author Gregor Mehlmann
 */
public class SuperNode extends BasicNode {

    protected ArrayList<CommentBadge> mCommentList = new ArrayList<CommentBadge>();
    protected ArrayList<BasicNode> mNodeList = new ArrayList<BasicNode>();
    protected ArrayList<SuperNode> mSuperNodeList = new ArrayList<SuperNode>();
    protected HashMap<String, BasicNode> mStartNodeMap = new HashMap<String, BasicNode>();
    protected BasicNode mHistoryNode = null;
    protected boolean mHideLocalVarBadge = false;
    protected boolean mHideGlobalVarBadge = false;
    protected VariableBadge mLocalVariableBadge = new VariableBadge("LocalVariableBadge");
    protected VariableBadge mGlobalVariableBadge = new VariableBadge("GlobalVariableBadge");

    public SuperNode() {
    }

    public SuperNode(final BasicNode node) {
        mNodeId = node.mNodeId;
        mNodeName = node.mNodeName;
        mComment = node.mComment;
        mTypeDefList = node.mTypeDefList;
        mVarDefList = node.mVarDefList;
        mCmdList = node.mCmdList;
        mCEdgeList = node.mCEdgeList;
        mPEdgeList = node.mPEdgeList;
        mIEdgeList = node.mIEdgeList;
        mFEdgeList = node.mFEdgeList;
        mDEdge = node.mDEdge;
        mGraphics = node.mGraphics;
        mParentNode = node.mParentNode;
        mIsHistoryNode = node.mIsHistoryNode;
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

    public void setHistoryNode(BasicNode value) {
        mHistoryNode = value;
    }

    public BasicNode getHistoryNode() {
        return mHistoryNode;
    }

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

    // TODO: this is not a deep copy
    public HashMap<String, BasicNode> getCopyOfStartNodeMap() {
        HashMap<String, BasicNode> copy = new HashMap<String, BasicNode>();
        Iterator it = mStartNodeMap.entrySet().iterator();

        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            String nodeId = (String) pairs.getKey();
            BasicNode nodeData = (BasicNode) pairs.getValue();

            copy.put(nodeId, nodeData);
        }

        return copy;
    }

    public void addSuperNode(SuperNode value) {
        mSuperNodeList.add(value);
    }

    public void removeSuperNode(SuperNode value) {
        mSuperNodeList.remove(value);
    }

    public SuperNode getSuperNodeAt(int index) {
        return mSuperNodeList.get(index);
    }

    public ArrayList<SuperNode> getSuperNodeList() {
        return mSuperNodeList;
    }

    public void addNode(BasicNode value) {
        mNodeList.add(value);
    }

    public void removeNode(BasicNode value) {
        mNodeList.remove(value);
    }

    public BasicNode getNodeAt(int index) {
        return mNodeList.get(index);
    }

    public ArrayList<BasicNode> getNodeList() {
        return mNodeList;
    }


    public ArrayList<BasicNode> getNodeAndSuperNodeList() {
        ArrayList<BasicNode> list = new ArrayList<BasicNode>();

        for (BasicNode n : mNodeList) {
            list.add(n);
        }

        for (SuperNode sn : mSuperNodeList) {
            list.add(sn);
        }

        return list;
    }


    public BasicNode getChildNodeById(String id) {
        for (BasicNode node : getNodeAndSuperNodeList()) {
            if (node.getId().equals(id)) {
                return node;
            }
        }

        return null;
    }

    public VariableBadge getLocalVariableBadge() {
        return mLocalVariableBadge;
    }

    public void setLocalVariableBadge(VariableBadge vb) {
        mLocalVariableBadge = vb;
    }

    public VariableBadge getGlobalVariableBadge() {
        return mGlobalVariableBadge;
    }

    public void setGlobalVariableBadge(VariableBadge vb) {
        mGlobalVariableBadge = vb;
    }

    @Override
    public void establishTargetNodes() {
        super.establishTargetNodes();

        for (BasicNode node : getNodeAndSuperNodeList()) {
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

    // TODO:
    public void establishAltStartNodes() {
        for (BasicNode node : getNodeAndSuperNodeList()) {
            for (AbstractEdge edge : node.getEdgeList()) {
                if (edge.getTargetNode() instanceof SuperNode) {

                    // First establish the start nodes
                    for (TPLTuple<String, BasicNode> startNodePair : edge.getAltMap().keySet()) {
                        if (!startNodePair.getFirst().equals("")) {
                            BasicNode n = ((SuperNode) edge.getTargetNode()).getChildNodeById(startNodePair.getFirst());

                            startNodePair.setSecond(n);
                        }
                    }

                    // Second establish the alternative nodes
                    for (TPLTuple<String, BasicNode> altStartNodePair : edge.getAltMap().values()) {
                        BasicNode n = ((SuperNode) edge.getTargetNode()).getChildNodeById(altStartNodePair.getFirst());

                        altStartNodePair.setSecond(n);
                    }
                }
            }
        }
    }


    protected void writeFieldsXML(IOSIndentWriter out) throws XMLWriteError {
        super.writeFieldsXML(out);
        if (! Command.convertToVOnDA) {
          if (mLocalVariableBadge != null) {
            mLocalVariableBadge.writeXML(out);
          }

          if (mGlobalVariableBadge != null) {
            mGlobalVariableBadge.writeXML(out);
          }
        }

        int i = 0;

        for (i = 0; i < mCommentList.size(); i++) {
            mCommentList.get(i).writeXML(out);
        }

        for (i = 0; i < mNodeList.size(); i++) {
            mNodeList.get(i).writeXML(out);
        }

        for (i = 0; i < mSuperNodeList.size(); i++) {
            mSuperNodeList.get(i).writeXML(out);
        }
    }

    @Override
    public void writeXML(IOSIndentWriter out) throws XMLWriteError {
        String start = "";

        for (String id : mStartNodeMap.keySet()) {
            start += id + ";";
        }

        out.println("<SuperNode id=\"" + mNodeId + "\" name=\"" + mNodeName + "\" comment=\"" + mComment + "\" hideLocalVar=\"" + mHideLocalVarBadge
                + "\" hideGlobalVar=\"" + mHideGlobalVarBadge + "\" start=\"" + start + "\">").push();

        writeFieldsXML(out);

        out.pop().println("</SuperNode>");
    }

    @Override
    public void parseXML(Element element) throws XMLParseError {
        super.parseXML(element);
        mComment = element.getAttribute("comment");
        mHideLocalVarBadge = Boolean.valueOf(element.getAttribute("hideLocalVar"));
        mHideGlobalVarBadge = Boolean.valueOf(element.getAttribute("hideGlobalVar"));

        /**
         * Construct start node list from the start string
         */
        String[] arr = element.getAttribute("start").split(";");

        for (String str : arr) {
            if (!str.isEmpty() && !str.equals("null")) {
                mStartNodeMap.put(str, null);
            }
        }

        final SuperNode superNode = this;

        XMLParseAction.processChildNodes(element, new XMLParseAction() {
            public void run(Element element) throws XMLParseError {
                java.lang.String tag = element.getTagName();

                if (tag.equals("LocalVariableBadge")) {
                    VariableBadge varBadge = new VariableBadge("LocalVariableBadge");

                    varBadge.parseXML(element);
                    mLocalVariableBadge = varBadge;
                } else if (tag.equals("GlobalVariableBadge")) {
                    VariableBadge varBadge = new VariableBadge("GlobalVariableBadge");

                    varBadge.parseXML(element);
                    mGlobalVariableBadge = varBadge;
                } else if (tag.equals("VariableBadge")) {

                    // do nothing (left for old project's compatibility)
                } else if (tag.equals("Comment")) {
                    CommentBadge comment = new CommentBadge();

                    comment.parseXML(element);
                    comment.setParentNode(superNode);
                    mCommentList.add(comment);
                } else if (tag.equals("Node")) {
                    BasicNode node = new BasicNode();

                    node.parseXML(element);
                    node.setParentNode(superNode);
                    mNodeList.add(node);

                    if (node.isHistoryNode()) {
                        mHistoryNode = node;
                    }
                } else if (tag.equals("SuperNode")) {
                    SuperNode node = new SuperNode();

                    node.parseXML(element);
                    node.setParentNode(superNode);
                    mSuperNodeList.add(node);
                } else if (this.getClass().equals(SuperNode.class)) {
                    throw new XMLParseError(null,
                            "Cannot parse the element with the tag \"" + tag
                            + "\" into a supernode child!");
                }
            }
        });
    }

}
