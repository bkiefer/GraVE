package de.dfki.grave.model.flow;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.grave.model.flow.*;
import de.dfki.grave.util.JaxbUtilities;

/**
 * @author Gregor Mehlmann
 */
@XmlType(name="SceneFlow")
@XmlRootElement(name="SceneFlow")
public final class SceneFlow extends SuperNode {

  // The singleton logger instance
  private static final Logger mLogger = LoggerFactory.getLogger(SceneFlow.class);

  protected String mXMLNameSpace = new String();
  protected String mXMLSchemeInstance = new String();
  protected String mXMLSchemeLocation = new String();

  protected String mPackageName = new String();

  protected String mContextClass = new String();

  protected String mContextCode = new String();
  
  @XmlTransient
  private IDManager mIDMananger;

  protected ArrayList<String> mClassPathList = new ArrayList<String>();
  @XmlAttribute(name="modifDate")
  protected String mModifDate = new String();

  public SceneFlow() {
    mPosition = new Position(0,0); 
  }

  /** To be called when the whole graph has been read */
  public void init() {
    mIDMananger = new IDManager(this);
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

  @XmlTransient
  public IDManager getIDManager() {
    return mIDMananger;
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

  /** Load sceneflow from file, return null upon failure */
  public static SceneFlow load(final File file) {
    InputStream inputStream = null;
    try {
      inputStream = new FileInputStream(file);
      return loadFrom(inputStream, file);
    } catch (FileNotFoundException e) {
      mLogger.error("Cannot find or opensceneflow file '{}'", file);
    }
    return null;
  }
  
  public static SceneFlow loadFrom(InputStream inputStream, File file) {
    SceneFlow mSceneFlow = (SceneFlow) JaxbUtilities.unmarshal(inputStream,
        file.getAbsolutePath(),
        SceneFlow.class, SuperNode.class, BasicNode.class,
        AbstractEdge.class, TimeoutEdge.class, EpsilonEdge.class,
        GuardedEdge.class, InterruptEdge.class, ForkingEdge.class,
        RandomEdge.class, CommentBadge.class);

    // Perform all the postprocessing steps
    mSceneFlow.establishParentNodes();
    mSceneFlow.establishStartNodes();
    mSceneFlow.establishTargetNodes();
    // Print an information message in this case
    mLogger.info("Loaded sceneflow from '{}'", file);
    // Return success if the project was loaded
    return mSceneFlow;
  }

  /** Save this sceneflow to file, return true upon success, false otherwise */
  public boolean save(final File file) {
    // Check if the configuration file does exist
    if (!file.exists()) {
      // Print a warning message in this case
      mLogger.warn("Creating the new sceneflow file '{}'", file);
      // Create a new configuration file now
      try {
        // Try to create a new configuration file
        if (!file.createNewFile()) {
          // Print an error message in this case
          mLogger.warn("There already exists a sceneflow file '{}'", file);
        }
      } catch (final IOException exc) {
        // Print an error message in this case
        mLogger.error("Cannot create the new sceneflow file '{}'", file);
        // Return failure if it does not exist
        return false;
      }
    }
    // Write the sceneflow configuration file
    JaxbUtilities.marshal(file, this,
        SceneFlow.class, SuperNode.class, BasicNode.class,
        AbstractEdge.class, TimeoutEdge.class, EpsilonEdge.class,
        GuardedEdge.class, InterruptEdge.class, ForkingEdge.class,
        RandomEdge.class, CommentBadge.class);
    // Print an information message in this case
    mLogger.info("Saved sceneflow configuration file '{}'", file);
    // Return success if the project was saved
    return true;
  }
}
