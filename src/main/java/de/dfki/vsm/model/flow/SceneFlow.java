package de.dfki.vsm.model.flow;

import java.util.ArrayList;

import javax.xml.bind.annotation.*;

import de.dfki.vsm.util.cpy.CopyTool;

/**
 * @author Gregor Mehlmann
 */
@XmlType(name="SceneFlow")
@XmlRootElement(name="SceneFlow")
public final class SceneFlow extends SuperNode {

  protected String mXMLNameSpace = new String();
  protected String mXMLSchemeInstance = new String();
  protected String mXMLSchemeLocation = new String();

  protected String mPackageName = new String();

  protected String mContextClass = new String();

  protected String mContextCode = new String();

  protected ArrayList<String> mClassPathList = new ArrayList<String>();
  @XmlAttribute(name="modifDate")
  protected String mModifDate = new String();

  public SceneFlow() {
  }

  @XmlTransient
  public String getContextCode() {
    return mContextCode;
  }

  public void setContextCode(String initContext) {
    mContextCode = initContext;
  }

  @XmlTransient
  public String getContextClass() {
    return mContextClass;
  }

  public void setContextClass(String value) {
    mContextClass = value;
  }

  @XmlAttribute(name="package")
  public String getPackageName() {
    return mPackageName;
  }

  public void setPackageName(String value) {
    mPackageName = value;
  }

  // TODO: CORRECT? We'll not use it, so maybe not relevant
  @XmlElementWrapper(name="ClassPath")
  @XmlElement(name="ClassPathElement")
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
  public int getHashCode() {

    // Add hash of General Attributes
    int hashCode = ((mNodeName == null) ? 0 : mNodeName.hashCode())
        + ((mComment == null) ? 0 : mComment.hashCode())
        + ((mPosition == null) ? 0 : mPosition.hashCode())
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
      hashCode += mCommentList.get(cntComment).getBoundary().hashCode();
      hashCode += mCommentList.get(cntComment).getHTMLText().hashCode();
    }

    return hashCode;
  }
}
