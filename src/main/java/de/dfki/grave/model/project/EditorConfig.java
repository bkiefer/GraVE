package de.dfki.grave.model.project;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

//~--- non-JDK imports --------------------------------------------------------
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.grave.util.JaxbUtilities;

/**
 * @author Bernd Kiefer
 *
 * This class contains editor related configurations.
 */
@XmlRootElement
@XmlType
public class EditorConfig {

  // The Logger Instance
  private static final Logger mLogger = LoggerFactory.getLogger(EditorConfig.class);;

  private static final String EDITOR_CONFIG_NAME = "editorconfig.xml";
  
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

  public boolean sSHOW_ELEMENTS = true;
  public int sELEMENTS_DIVIDER_LOCATION = 230;
  public boolean sSHOW_CODEEDITOR = true;
  public int sCODE_DIVIDER_LOCATION = 450;
  public double sSCENEFLOW_SCENE_EDITOR_RATIO = 0.15;

  // TODO: PUT REASONABLE DEFAULTS INTO SYSTEM DEFAULT EDITORCONFIG
  public FontConfig sNODE_FONT;  // Node name font
  public FontConfig sCODE_FONT;  // Code Font for code attached to Nodes/Edges
  public FontConfig sCODEAREA_FONT; // Code Font for code in Code Editor Areas
  public FontConfig sCOMMENT_FONT; // Font in Comment Badges
  public FontConfig sBUTTON_FONT; // Button Font
  public FontConfig sDIALOG_FONT; // Dialog Font
  public FontConfig sTREE_FONT; // Tree View Font
  public FontConfig sUI_FONT; // Editor Menu and Control Elements font

  /** Only for XML unmarshalling */
  private EditorConfig() {}

  public static EditorConfig loadBundleDefault() {
    InputStream in = EditorConfig.class.getResourceAsStream(EDITOR_CONFIG_NAME);
    return (EditorConfig)JaxbUtilities.unmarshal(
        in, "system default", EditorConfig.class, FontConfig.class);
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
      // should never happen
      mLogger.error("Cannot convert editor configuration to string: {}", e);
    }
    return result;
  }
  
}
