package de.dfki.vsm.model.flow;

import java.util.ArrayList;

import javax.xml.bind.annotation.*;

import de.dfki.vsm.model.flow.geom.Position;

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
    mPosition = new Position(0,0);
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
  public int hashCode() {
    int hash = super.hashCode();
    hash = 59 * hash + this.mXMLNameSpace.hashCode();
    hash = 59 * hash + this.mXMLSchemeInstance.hashCode();
    hash = 59 * hash + this.mXMLSchemeLocation.hashCode();
    hash = 59 * hash + this.mPackageName.hashCode();
    hash = 59 * hash + this.mContextClass.hashCode();
    hash = 59 * hash + this.mContextCode.hashCode();
    hash = 59 * hash + this.mClassPathList.hashCode();
    hash = 59 * hash + this.mModifDate.hashCode();
    return hash;
  }
}
