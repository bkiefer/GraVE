package de.dfki.vsm.model.sceneflow.chart;

import java.util.ArrayList;
import java.util.Date;

import org.w3c.dom.Element;

import de.dfki.vsm.Preferences;
import de.dfki.vsm.model.sceneflow.chart.badge.CommentBadge;
import de.dfki.vsm.model.sceneflow.glue.command.Command;
import de.dfki.vsm.util.cpy.CopyTool;
import de.dfki.vsm.util.ios.IOSIndentWriter;
import de.dfki.vsm.util.xml.XMLParseAction;
import de.dfki.vsm.util.xml.XMLParseError;
import de.dfki.vsm.util.xml.XMLWriteError;

/**
 * @author Gregor Mehlmann
 */
public final class SceneFlow extends SuperNode {

  protected String mXMLNameSpace = new String();
  protected String mXMLSchemeInstance = new String();
  protected String mXMLSchemeLocation = new String();
  protected String mPackageName = new String();
  protected String mContextClass = new String();
  protected String mContextCode = new String();
  protected ArrayList<String> mClassPathList = new ArrayList<String>();
  protected String mModifDate = new String();

  public SceneFlow() {
  }

  public String getContextCode() {
    return mContextCode;
  }

  public void setContextCode(String initContext) {
    mContextCode = initContext;
  }

  public String getContextClass() {
    return mContextClass;
  }

  public void setContextClass(String value) {
    mContextClass = value;
  }

  public String getPackageName() {
    return mPackageName;
  }

  public void setPackageName(String value) {
    mPackageName = value;
  }

  public ArrayList<String> getClassPathList() {
    return mClassPathList;
  }

  public void setClassPathList(ArrayList<String> classPath) {
    mClassPathList = classPath;
  }

  public ArrayList<String> getCopyOfClassPathList() {
    ArrayList<String> copy = new ArrayList<String>();

    for (String str : mClassPathList) {
      copy.add(str);
    }

    return copy;
  }

  @Override
  public SceneFlow getCopy() {
    return (SceneFlow) CopyTool.copy(this);
  }

  @Override
  public void writeXML(IOSIndentWriter out) throws XMLWriteError {
    String start = "";

    for (String id : mStartNodeMap.keySet()) {
      start += id + ";";
    }

    //out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    out.println("<SceneFlow "
            + "id=\"" + mNodeId + "\" "
            + "name=\"" + mNodeName + "\" "
            + "comment=\"" + mComment + "\" hideLocalVar=\"" + mHideLocalVarBadge + "\" hideGlobalVar=\"" + mHideGlobalVarBadge + "\" "
            + "modifDate=\"" + Preferences.sDATE_FORMAT.format(new Date()) + "\" " + "start=\""
            + start + "\" "
            // + "context=\""+(context.equals("") ? "java.lang.Object" : context)+"\" "
            + "context=\"" + mContextClass + "\" " + "package=\"" + mPackageName + "\" "
            // + "scenefile=\"" + mSceneFileName + "\" "
            // + "sceneinfo=\"" + mSceneInfoFileName + "\" "
            + "xmlns=\"" + Preferences.getProperty("xmlns") + "\" " + "xmlns:xsi=\"" + Preferences.getProperty("xmlns_xsi")
            + "\" " + "xsi:schemaLocation=\"" + Preferences.getProperty("xmlns") + " "
            + Preferences.getProperty("xsi_schemeLocation") + "\">").push();

    int i = 0;

    out.println("<Commands>").push();

    mCmdList.writeXML(out);

    out.pop().println("</Commands>");

    for (i = 0; i < mCEdgeList.size(); i++) {
      mCEdgeList.get(i).writeXML(out);
    }

    for (i = 0; i < mPEdgeList.size(); i++) {
      mPEdgeList.get(i).writeXML(out);
    }

    for (i = 0; i < mIEdgeList.size(); i++) {
      mIEdgeList.get(i).writeXML(out);
    }

    if (mDEdge != null) {
      mDEdge.writeXML(out);
    }

    for (i = 0; i < mCommentList.size(); i++) {
      mCommentList.get(i).writeXML(out);
    }

    for (i = 0; i < mNodeList.size(); i++) {
      mNodeList.get(i).writeXML(out);
    }

    for (i = 0; i < mSuperNodeList.size(); i++) {
      mSuperNodeList.get(i).writeXML(out);
    }

    out.println("<ClassPath>").push();

    for (i = 0; i < mClassPathList.size(); i++) {
      out.println("<ClassPathElement>").push();
      out.println(mClassPathList.get(i));
      out.pop().println("</ClassPathElement>");
    }

    out.pop().println("</ClassPath>");
    out.print("<InitContext>");
    out.print(mContextCode);
    out.println("</InitContext>");
    out.pop().print("</SceneFlow>");
  }

  @Override
  public void parseXML(Element element) throws XMLParseError {
    mNodeId = element.getAttribute("id");
    mNodeName = element.getAttribute("name");
    mComment = element.getAttribute("comment");

    String start = element.getAttribute("start");

    mContextClass = element.getAttribute("context");
    mPackageName = element.getAttribute("package");
    mXMLSchemeLocation = element.getAttribute("xsi:schemaLocation");
    mXMLNameSpace = element.getAttribute("xmlns");
    mXMLSchemeInstance = element.getAttribute("xmlns:xsi");
    mHideLocalVarBadge = Boolean.valueOf(element.getAttribute("hideLocalVar"));
    mHideGlobalVarBadge = Boolean.valueOf(element.getAttribute("hideGlobalVar"));
    mModifDate = element.getAttribute("modifDate");

    /**
     * Construct start node list from the start string
     */
    String[] arr = start.split(";");

    for (String str : arr) {
      if (!str.isEmpty() && !str.equals("null")) {
        mStartNodeMap.put(str, null);
      }
    }

    final SceneFlow sceneFlow = this;

    XMLParseAction.processChildNodes(element, new XMLParseAction() {
      public void run(Element element) throws XMLParseError {
        String tag = element.getTagName();

        if (tag.equals("LocalVariableBadge")) {

        } else if (tag.equals("GlobalVariableBadge")) {

        } else if (tag.equals("VariableBadge")) {

          // do nothing (left for old project's compatibility)
        } else if (tag.equals("Comment")) {
          CommentBadge comment = new CommentBadge();

          comment.parseXML(element);
          comment.setParentNode(sceneFlow);
          mCommentList.add(comment);
        } else if (tag.equals("Node")) {
          BasicNode node = new BasicNode();

          node.parseXML(element);
          node.setParentNode(sceneFlow);
          mNodeList.add(node);
        } else if (tag.equals("SuperNode")) {
          SuperNode node = new SuperNode();

          node.parseXML(element);
          node.setParentNode(sceneFlow);
          mSuperNodeList.add(node);
        } else if (tag.equals("Commands")) {
          XMLParseAction.processChildNodes(element, new XMLParseAction() {
            public void run(Element element) throws XMLParseError {
              mCmdList = (Command) Command.parse(element);
            }
          });
        } else if (tag.equals("ClassPath")) {
          XMLParseAction.processChildNodes(element, "ClassPathElement", new XMLParseAction() {
            public void run(Element element) throws XMLParseError {
              mClassPathList.add(element.getTextContent().trim());
            }
          });
        } else if (tag.equals("InitContext")) {
          mContextCode = element.getTextContent().trim();
        } else {
          throw new XMLParseError(null,
                  "Cannot parse the element with the tag \"" + tag
                  + "\" into a sceneflow child!");
        }
      }
    });
  }

  @Override
  public int getHashCode() {

    // Add hash of General Attributes
    int hashCode = ((mNodeName == null) ? 0 : mNodeName.hashCode())
        + ((mComment == null) ? 0 : mComment.hashCode())
        + ((mGraphics == null) ? 0 : mGraphics.hashCode())
        + ((mParentNode == null) ? 0 : mParentNode.hashCode())
        + ((mHistoryNode == null) ? 0 : mHistoryNode.hashCode())
        + ((mStartNodeMap == null) ? 0 : mStartNodeMap.hashCode())
        + ((mIsHistoryNode == true) ? 1 : 0)
        + ((mHideLocalVarBadge == true) ? 1 : 0)
        + ((mHideGlobalVarBadge == true) ? 1 : 0);

    // Add hash of all nodes on workspace
    for (int cntNode = 0; cntNode < mNodeList.size(); cntNode++) {
      hashCode += getNodeAt(cntNode).getHashCode();
    }

    // Add hash of all superNodes on workspace
    for (int cntSNode = 0; cntSNode < mSuperNodeList.size(); cntSNode++) {
      hashCode += getSuperNodeAt(cntSNode).getHashCode();
    }

    // Add hash of all commands on workspace
    hashCode += mCmdList.hashCode();

    // Add hash of all comments on workspace
    for (int cntComment = 0; cntComment < getCommentList().size(); cntComment++) {
      hashCode += mCommentList.get(cntComment).getGraphics().getRectangle().hashCode();
      hashCode += mCommentList.get(cntComment).getHTMLText().hashCode();
    }

    return hashCode;
  }
}
