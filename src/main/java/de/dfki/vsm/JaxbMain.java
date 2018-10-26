package de.dfki.vsm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

public class JaxbMain {

  public static void main(String[] args) throws JAXBException, FileNotFoundException {
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
