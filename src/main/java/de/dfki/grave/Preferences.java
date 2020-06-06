package de.dfki.grave;

import static de.dfki.grave.Icons.*;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.grave.model.project.EditorConfig;
import de.dfki.grave.model.project.FontConfig;
import de.dfki.grave.util.JaxbUtilities;

import java.io.File;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 *
 * User preferences and app-wide "constants"
 */
@XmlRootElement(name="Preferences")
@XmlType(name="Preferences")
@XmlAccessorType(XmlAccessType.FIELD)
public final class Preferences {
  private static final Logger mLogger = LoggerFactory.getLogger(MainGrave.class);
  
  // The global preferences and settings file
  private static final String CONFIG_FILE = System.getProperty("user.home") 
      + System.getProperty("file.separator") + ".grave";
  
  public static boolean DEBUG_COMPONENT_BOUNDARIES = false;
  public static boolean DEBUG_MOUSE_LOCATIONS = false;

  //////////////////////////////////////////////////////////////////////////////
  // NODE COLORS
  //////////////////////////////////////////////////////////////////////////////
  public static final Color sBASIC_NODE_COLOR = new Color(125, 125, 125);
  public static final Color sSUPER_NODE_COLOR = new Color(125, 125, 125);
  public static final Color sSELECTED_NODE_COLOR = new Color(211, 211, 211);

  //////////////////////////////////////////////////////////////////////////////
  // EDGE COLORS
  //////////////////////////////////////////////////////////////////////////////
  public static final Color sFEDGE_COLOR = new Color(35, 77, 103);  //BLUE
  public static final Color sEEDGE_COLOR = new Color(130, 125, 120);  //GRAY
  public static final Color sTEDGE_COLOR = new Color(84, 63, 29);  //BROWN
  public static final Color sCEDGE_COLOR = new Color(152, 142, 52);   //YELLOW
  public static final Color sPEDGE_COLOR = new Color(42, 103, 35);   //GREEN
  public static final Color sIEDGE_COLOR = new Color(152, 52, 52);    //RED

  //////////////////////////////////////////////////////////////////////////////
  // VISUALIZATION COLORS
  //////////////////////////////////////////////////////////////////////////////
  public static final Color sHIGHLIGHT_COLOR = new Color(211, 211, 211);
  public static final Color sTRANSLUCENT_HIGHLIGHT_COLOR = new Color(111, 251, 211, 100);    // Do not change opacity!
  public static final Color sTRANSLUCENT_RED_COLOR = new Color(246, 0, 0, 100);        // Do not change opacity!
  public static final Color sTRANSLUCENT_BLUE_COLOR = new Color(0, 0, 246, 100);        // Do not change opacity!
  public static final Color sTRANSLUCENT_GREEN_COLOR = new Color(0, 246, 0, 100);        // Do not change opacity!
  public static final Color sTRANSLUCENT_YELLOW_COLOR = new Color(0, 246, 246, 100);      // Do not change opacity!
  public static final Color sCOMMENT_BADGE_COLOR = new Color(211, 215, 207, 128);
  public static final Color sSTART_SIGN_COLOR = new Color(181, 45, 13);
  public static final Color sMESSAGE_COLOR = new Color(181, 45, 13);
  public static final Color sHIGHLIGHT_SCENE_COLOR = Color.YELLOW;

  //////////////////////////////////////////////////////////////////////////////
  // APPEARANCE CONFIGURATION
  //////////////////////////////////////////////////////////////////////////////
  // The Screen Size
  @XmlTransient
  public final Dimension SCREEN_SIZE = Toolkit.getDefaultToolkit().getScreenSize();
  @XmlTransient
  public final int SCREEN_HORIZONTAL = SCREEN_SIZE.width;
  @XmlTransient
  public final int SCREEN_VERTICAL = SCREEN_SIZE.height;
  //Components dimensions
  @XmlTransient
  public Dimension SF_PALETTEITEM_SIZE = new Dimension(61, 65);

  //////////////////////////////////////////////////////////////////////////////
  // RECENT PROJECTS
  //////////////////////////////////////////////////////////////////////////////
  private final int sMAX_RECENT_PROJECTS = 8;

  public String FRAME_TITLE = "Graphical VOnDA Editor";
  public String FRAME_NAME = "GraphEditor";
  private int FRAME_POS_X = 0;
  private int FRAME_POS_Y = 0;
  private int FRAME_WIDTH = 800;
  private int FRAME_HEIGHT = 600;
  //public String XMLNS = "xml.sceneflow.dfki.de";
  //public String XMLNS_XSI = "http://www.w3.org/2001/XMLSchema-instance";
  //public String XSI_SCHEMELOCATION = "res/xsd/sceneflow.xsd";

  private ArrayList<RecentProject> recentProjects = new ArrayList<>();

  public EditorConfig editorConfig;

  @XmlTransient
  private static Preferences instance = null;

  private Preferences() {}

  public static Preferences getPrefs() {
    if (instance == null) 
        instance = new Preferences();
    return instance;
  }


  public static synchronized void savePrefs() {
    // write the Preferences file
    JaxbUtilities.marshal(new File(CONFIG_FILE), getPrefs(),
            Preferences.class, EditorConfig.class, FontConfig.class);
  }

  private static synchronized void load() {
    try {
      instance = (Preferences)JaxbUtilities.unmarshal(
          new FileInputStream(CONFIG_FILE), CONFIG_FILE,
          Preferences.class, EditorConfig.class, FontConfig.class);
    } catch (IOException e) {
      mLogger.warn("Cannot read global preference file {}, creating default",
          CONFIG_FILE);
      instance = new Preferences();
      instance.editorConfig = EditorConfig.loadBundleDefault();
      savePrefs();
    }
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public static synchronized void configure() {
    try {
      load();
      
      // Use system look and feel
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

      UIManager.put("TabbedPane.contentBorderInsets", new Insets(0, 0, 0, 0));
      UIManager.put("TabbedPane.background", new Color(100, 100, 100));
      UIManager.put("TabbedPane.contentAreaColor", new Color(100, 100, 100));
      UIManager.put("TabbedPane.tabAreaBackground", new Color(100, 100, 100));

      // paint a nice doc icon when os is mac
      if (isMac()) {
        // Mac/Apple Settings
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name",
            instance.FRAME_TITLE);

        final Class appClass = Class.forName("com.apple.eawt.Application");
        // Get the application and the method to set the dock icon
        final Object app = appClass.getMethod("getApplication", new Class[]{}).invoke(null, new Object[]{});
        final Method setDockIconImage = appClass.getMethod("setDockIconImage", new Class[]{Image.class});
        // Set the dock icon to the logo of Visual Scene Maker 3
        setDockIconImage.invoke(app, new Object[]{ICON_DOC.getImage()});
      }
    } catch (final ClassNotFoundException | InstantiationException | 
        IllegalAccessException | UnsupportedLookAndFeelException | 
        NoSuchMethodException | SecurityException | IllegalArgumentException | 
        InvocationTargetException exc) {
      mLogger.error("Error: " + exc.getMessage());
    }
  }
  
  /* **********************************************************************
   * Frame Position and Size
   * ********************************************************************** */
  
  public Point getFramePosition() {
    return new Point(FRAME_POS_X, FRAME_POS_Y);
  }
  
  public Dimension getFrameDimension() {
    return new Dimension(FRAME_WIDTH, FRAME_HEIGHT);
  }
  
  public void updateBounds(int x, int y, int width, int height) {
    FRAME_POS_X = x;
    FRAME_POS_Y = y;
    FRAME_WIDTH = width;
    FRAME_HEIGHT = height;
    savePrefs();
  }

  /* **********************************************************************
   * Recent Projects
   * ********************************************************************** */
  
  public Iterable<RecentProject> getRecentProjects() {
    return recentProjects;
  }
  
  public void clearRecentProjects(){
    recentProjects.clear();
    savePrefs();
  }
  
  private int findRecentProject(String path) {
    int i = 0;
    for (RecentProject rp : recentProjects) {
      if (isWindows()) {
        if (rp.path.equalsIgnoreCase(path))
          return i;
      } else {
        if (rp.path.equals(path))
          return i;
      }
      ++i;
    }
    return -1;
  }
  
  public void updateRecentProjects(String name, String path, String date) {
    int index = findRecentProject(path);
    if (index >= 0) {
      RecentProject rp = recentProjects.get(index);
      recentProjects.remove(index);
      recentProjects.add(0, rp);
      rp.name = name;
      rp.date = date;
    } else {
      recentProjects.add(0, new RecentProject(name, path, date));
    }
    
    int si = recentProjects.size();
    while (si > sMAX_RECENT_PROJECTS) {
      recentProjects.remove(si);
      --si;
    }
    savePrefs();
  }

  // Check if we are on a WINDOWS system
  public static synchronized boolean isWindows() {
    final String os = System.getProperty("os.name").toLowerCase();
    return (os.contains("win"));
  }

  // Check if we are on a MAC system
  public static synchronized boolean isMac() {
    final String os = System.getProperty("os.name").toLowerCase();
    return (os.contains("mac"));
  }

  // Check if we are on a UNIX system
  public static synchronized boolean isUnix() {
    final String os = System.getProperty("os.name").toLowerCase();
    return ((os.contains("nix")) || (os.contains("nux")));
  }
}
