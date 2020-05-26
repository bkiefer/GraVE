package de.dfki.grave.model.project;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

//~--- non-JDK imports --------------------------------------------------------
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.grave.util.JaxbUtilities;

/**
 * @author Patrick Gebhard
 * @author Sergio Soto
 *
 * This class saves project related configurations.
 */
@XmlRootElement
@XmlType
public class EditorConfig {

  // The Logger Instance
  private static final Logger mLogger = LoggerFactory.getLogger(EditorConfig.class);;

  ////////////////////////////////////////////////////////////////////////////
  // VARIABLE FIELDS
  ////////////////////////////////////////////////////////////////////////////
  public int sNODEWIDTH = 100;
  public int sNODEHEIGHT = 100;
  
  public float sZOOM_FACTOR = 1;
  public float sZOOM_INCREMENT = 1.2f;
  
  public float sGRID_SCALE = 2;
  public boolean sSHOWGRID = true;
  public boolean sSNAPTOGRID = true;
  
  public boolean sSHOWIDSOFNODES = true;

  public boolean sSHOW_VARIABLE_BADGE_ON_WORKSPACE = true;
  public boolean sSHOW_SMART_PATH_DEBUG = false;
  public boolean sAUTOHIDE_BOTTOMPANEL = true; // Saves the pricked pin of the bottom panel of the editor
  public String sMAINSUPERNODENAME = "default";

  public boolean sSHOW_ELEMENTS = true;
  public int sELEMENTS_DIVIDER_LOCATION = 230;
  public boolean sSHOW_SCENEFLOWEDITOR = true;
  public boolean sSHOW_CODEEDITOR = true;
  public int sCODE_DIVIDER_LOCATION = 450;
  public double sSCENEFLOW_SCENE_EDITOR_RATIO = 0.15;
  
  public FontConfig sNODE_FONT;  // Node name font
  // Font f = new Font(Font.SANS_SERIF, DEMIBOLD, 16);
  public FontConfig sCODE_FONT;  // Code Font for code attached to Nodes/Edges
  //new Font("Monospaced", Font.ITALIC, 11)
  // new Font(Font.SANS_SERIF, Font.PLAIN, 16);
  public FontConfig sCODEAREA_FONT; // Code Font for code in Code Editor Areas
  // new Font(Font.SANS_SERIF, Font.PLAIN, 16);
  public FontConfig sCOMMENT_FONT; // Font in Comment Badges
  // new Font(Font.SANS_SERIF, Font.PLAIN, 16);
  public FontConfig sBUTTON_FONT; // Button Font
  //new Font("Helvetica", Font.PLAIN, 20)
  public FontConfig sDIALOG_FONT; // Dialog Font
  //new Font("SansSerif", Font.PLAIN, 11)
  public FontConfig sTREE_FONT; // Tree View Font
  // new Font("Helvetica", Font.PLAIN, 10);
  public FontConfig sUI_FONT; // Editor Menu and Control Elements font
  // new Font("Helvetica", Font.PLAIN, 14);

  public EditorConfig() {
  }

  public boolean save(final File base) {

    // Create the project configuration file
    final File file = new File(base, "editorconfig.xml");

    // Check if the configuration does exist
    if (!file.exists()) {
      // Print a warning message if this case
      mLogger.warn("Warning: Creating the new project editor configuration "
              + "file '" + file + "'");

      // Create a new configuration file now
      try {
        // Try to create a new configuration file
        if (!file.createNewFile()) {
          // Print an error message if this case
          mLogger.warn("Warning: There already exists a project editor"
                  + " configuration file '" + file + "'");
        }
      } catch (final IOException exc) {
        // Print an error message if this case
        mLogger.error("Failure: Cannot create the new project editor"
                + " configuration file '" + file + "'");
        // Return failure if it does not exist
        return false;
      }
    }
    return JaxbUtilities.marshal(file, this, EditorConfig.class);
  }

  public static synchronized EditorConfig load(final String path) {
    InputStream inputStream = null;
    final File file = new File(path, "editorconfig.xml");
    // Check if the configuration file does exist
    if (file.exists()) {
      try {
        inputStream = new FileInputStream(file);
      } catch (FileNotFoundException e) {
        mLogger.error("Error: Cannot find sproject configuration file '" + file + "'");
      }
    } else {
      inputStream = ClassLoader.getSystemResourceAsStream(
          path + System.getProperty("file.separator") + "editorconfig.xml");
      if (inputStream == null) {
        // Print an error message in this case
        mLogger.error("Error: Cannot find project configuration file  " + file);
        // Return failure if it does not exist
        return null;
      }
    }

    return (EditorConfig)JaxbUtilities.unmarshal(inputStream,
        file.getAbsolutePath(), EditorConfig.class, FontConfig.class);
  }

  public static EditorConfig loadBundleDefault() {
    InputStream in = EditorConfig.class.getResourceAsStream("editorconfig.xml");
    return (EditorConfig)JaxbUtilities.unmarshal(
        in, "default", EditorConfig.class, FontConfig.class);
  }

  // Get the string representation of the configuration
  public final EditorConfig copy() {

    // Create a new byte array buffer stream
    final ByteArrayOutputStream out = new ByteArrayOutputStream();
    EditorConfig result = null;
    try {
      JaxbUtilities.marshal(out, this, EditorConfig.class, FontConfig.class);
      ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
      result = (EditorConfig) JaxbUtilities.unmarshal(in, "EdConf",
          EditorConfig.class, FontConfig.class);
    } catch (JAXBException e) {
      mLogger.error("Error: Cannot convert editor configuration to string: " + e);
    }
    return result;
  }
  
}
