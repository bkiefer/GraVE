package de.dfki.grave.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import de.dfki.grave.editor.project.EditorConfig;
import de.dfki.grave.editor.project.FontConfig;
import de.dfki.grave.util.JaxbUtilities;

public class JaxbMain {

  public static void main(String[] args) 
      throws JAXBException, FileNotFoundException {
    /**/
    Preferences.loadUserPrefs();
    JaxbUtilities.marshal(new File("prefs.xml"), Preferences.getPrefs(),
        Preferences.class, EditorConfig.class, FontConfig.class);
    Object o = JaxbUtilities.unmarshal(new FileInputStream("prefs.xml"), "prefs.xml",
        Preferences.class, EditorConfig.class, FontConfig.class);
    JaxbUtilities.marshal(new File("test.xml"), o,
        Preferences.class, EditorConfig.class, FontConfig.class);
    System.exit(0);
    /**/
    JAXBContext jc = JAXBContext.newInstance( Preferences.class );
    Marshaller m = jc.createMarshaller();
    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    m.marshal(Preferences.getPrefs(), new FileOutputStream("prefs.xml"));

    Unmarshaller u = jc.createUnmarshaller();
    Object flow = u.unmarshal(new FileInputStream("prefs.xml"));

    m.marshal(flow, new FileOutputStream("test.xml"));
    System.out.println("Done");
  }
}
