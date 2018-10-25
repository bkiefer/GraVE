package de.dfki.vsm.model.project;

import java.awt.Dimension;
import java.io.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

//~--- non-JDK imports --------------------------------------------------------
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.vsm.util.JaxbUtilities;

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
  public boolean sVISUALIZATIONTRACE = true;
  public int sVISUALISATIONTIME = 15;    // 25 = 1 second
  public boolean sSHOW_VARIABLE_BADGE_ON_WORKSPACE = true;
  public boolean sSHOW_SMART_PATH_DEBUG = false;
  public boolean sSHOWIDSOFNODES = true;
  public String sSCRIPT_FONT_TYPE = "Monospaced";
  public int sSCRIPT_FONT_SIZE = 16;
  public boolean sSHOWSCENE_ELEMENTS = false;
  public boolean sAUTOHIDE_BOTTOMPANEL = true; // Saves the pricked pin of the bottom panel of the editor
  public String sMAINSUPERNODENAME = "default";

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

    return (EditorConfig)JaxbUtilities.unmarshal(
        inputStream, file.getAbsolutePath(), EditorConfig.class);
  }


  // Get the string representation of the configuration
  public final EditorConfig copy() {

    // Create a new byte array buffer stream
    final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    EditorConfig result = null;
    try {
      JAXBContext jc = JAXBContext.newInstance( EditorConfig.class );
      Marshaller m = jc.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.FALSE);
      m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

      m.marshal(this, buffer);
      Unmarshaller u = jc.createUnmarshaller();

      result = (EditorConfig)u.unmarshal(
          new ByteArrayInputStream(buffer.toByteArray()));
    } catch (JAXBException e) {
      mLogger.error("Error: Cannot convert editor configuration to string: " + e);
    }
    return result;
  }
}
