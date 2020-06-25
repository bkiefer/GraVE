package de.dfki.grave;

import java.net.URL;
import java.text.SimpleDateFormat;

public class Constants {

  public static final String PROJECT_CONFIG_NAME = "project.xml";
  public static final String SCENEFLOW_NAME = "sceneflow.xml";
  
  //////////////////////////////////////////////////////////////////////////////
  // DATE FORMAT
  //////////////////////////////////////////////////////////////////////////////
  public static final SimpleDateFormat DATE_FORMAT =
      new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");

  //////////////////////////////////////////////////////////////////////////////
  // FILE RESSOURCES
  //////////////////////////////////////////////////////////////////////////////
  public static final URL ABOUT_FILE = Constants.class.getResource("doc/about.html");
  public static final URL HELP_FILE = Constants.class.getResource("doc/index.html");

}
