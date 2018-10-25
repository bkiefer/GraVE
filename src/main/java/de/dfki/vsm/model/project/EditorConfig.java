package de.dfki.vsm.model.project;

//~--- JDK imports ------------------------------------------------------------
import java.awt.Dimension;
import java.io.*;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.bind.annotation.*;

//~--- non-JDK imports --------------------------------------------------------
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.vsm.util.xml.XMLUtilities;

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
  private final Logger mLogger = LoggerFactory.getLogger(EditorConfig.class);;

  ////////////////////////////////////////////////////////////////////////////
  // VARIABLE FIELDS
  ////////////////////////////////////////////////////////////////////////////
  public int sNODEWIDTH = 100;
  public int sNODEHEIGHT = 100;
  public int sSUPERNODEWIDTH = 100;
  public int sSUPERNODEHEIGHT = 100;
  @XmlTransient
  public Dimension sNODESIZE = new Dimension(sNODEWIDTH, sNODEHEIGHT);
  @XmlTransient
  public Dimension sSUPERNODESIZE = new Dimension(sSUPERNODEWIDTH, sSUPERNODEHEIGHT);
  public int sGRID_NODEWIDTH = 100;
  public int sGRID_NODEHEIGHT = 100;
  public int sGRID_XSCALE = 1;
  public int sGRID_YSCALE = 1;
  @XmlTransient
  public int sGRID_XSPACE = sNODEWIDTH * sGRID_XSCALE;
  @XmlTransient
  public int sGRID_YSPACE = sNODEHEIGHT * sGRID_YSCALE;
  @XmlTransient
  public int sXOFFSET = sGRID_NODEWIDTH / 3;
  @XmlTransient
  public int sYOFFSET = sGRID_NODEHEIGHT / 3;
  public int sWORKSPACEFONTSIZE = 16;
  public float sEDITORFONTSIZE = 11;
  public boolean sLAUNCHPLAYER = false;
  public boolean sSHOWGRID = true;
  public boolean sVISUALISATION = true;
  public boolean sACTIVITYTRACE = true;
  public int sVISUALISATIONTIME = 15;    // 25 = 1 second
  public boolean sSHOW_VARIABLE_BADGE_ON_WORKSPACE = true;
  public boolean sSHOW_SMART_PATH_DEBUG = false;
  public boolean sSHOWIDSOFNODES = true;
  public String sSCRIPT_FONT_TYPE = "Monospaced";
  public int sSCRIPT_FONT_SIZE = 16;
  public boolean sSHOWSCENE_ELEMENTS = false;
  public boolean sAUTOHIDE_BOTTOMPANEL = true; // Saves the pricked pin of the bottom panel of the editor
  public String sMAINSUPERNODENAME = "default";

  public boolean sSHOWELEMENTS = true;
  public boolean sSHOWELEMENT_PROPERTIES = true;
  public int sPROPERTIES_DIVIDER_LOCATION = 230;
  public boolean sSHOWSCENEFLOW_EDITOR = true;
  public boolean sSHOWSCENE_EDITOR = true;
  public double sSCENEFLOW_SCENEEDITOR_RATIO = 0.75;
  public boolean SHOWGESTURES = true;
  public int NUM_MAGNETS = 8;

  public EditorConfig() {
  }

  private synchronized void init() {
  }

  public boolean save(final File base) {

    // Create the project configuration file
    final File file = new File(base, "editorconfig.xml");

    // Check if the configuration does exist
    if (!file.exists()) {

      // Print a warning message if this case
      mLogger.warn("Warning: Creating the new project editor configuration file '" + file + "'");

      // Create a new configuration file now
      try {

        // Try to create a new configuration file
        if (!file.createNewFile()) {

          // Print an error message if this case
          mLogger.warn("Warning: There already exists a project editor configuration file '" + file + "'");
        }
      } catch (final IOException exc) {

        // Print an error message if this case
        mLogger.error("Failure: Cannot create the new project editor configuration file '" + file + "'");

        // Return failure if it does not exist
        return false;
      }
    }

    // Write the project configuration file
    if (!XMLUtilities.writeToXMLFile(sPROPERTIES, file)) {

      // Print an error message if this case
      mLogger.error("Error: Cannot write project editor configuration file '" + file + "'");

      // Return failure if it does not exist
      return false;
    }

    init();

    // Print an information message if this case
    //mLogger.message("Saved project configuration file '" + file + "':\n" + sPROPERTIES);
    // Return success if the project was saved
    return true;

  }

  public synchronized boolean load(final String path) {
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
      inputStream = ClassLoader.getSystemResourceAsStream(path + System.getProperty("file.separator") + "editorconfig.xml");
      if (inputStream == null) {
        // Print an error message in this case
        mLogger.error("Error: Cannot find project configuration file  " + file);
        // Return failure if it does not exist
        return false;
      }
    }

    // TODO: unmarshal?

    if (!XMLUtilities.parseFromXMLStream(sPROPERTIES, inputStream)) {
      mLogger.error("Error: Cannot parse project configuration file  in path" + path);
      return false;
    }

    //
    init();

    // Print an information message if this case
    mLogger.info("Loaded project editor configuration file in path'" + path + "':\n");
    ByteArrayOutputStream stream = new ByteArrayOutputStream();
    if (XMLUtilities.writeToXMLStream(sPROPERTIES, stream)) {
      try {
        // mLogger.message(stream.toString("UTF-8"));
        mLogger.info("Configuration File Loaded: " + path);
      } catch (Exception exc) {
        mLogger.error(exc.getMessage());
      }
    }

    // Return success if the project was loaded
    return true;
  }

  // Get the string representation of the configuration
  @Override
  public final String toString() {

    // Create a new byte array buffer stream
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    // Try to write the project into the stream
    XMLUtilities.writeToXMLStream(sPROPERTIES, buffer);

    // Return the stream string representation
    try {
      //return buffer.toString("UTF-8");
      return buffer.toString();
    } catch (final Exception exc) {
      exc.printStackTrace();
      //
      return null;
    }
  }
}
