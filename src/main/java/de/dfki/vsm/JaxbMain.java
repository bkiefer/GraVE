package de.dfki.vsm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import de.dfki.vsm.model.project.EditorConfig;

public class JaxbMain {

  public static void main(String[] args) throws JAXBException, FileNotFoundException {
    JAXBContext jc = JAXBContext.newInstance( EditorConfig.class );
    Marshaller m = jc.createMarshaller();
    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    m.marshal(new EditorConfig(), new FileOutputStream("editorconfig.xml"));

    Unmarshaller u = jc.createUnmarshaller();
    EditorConfig flow = (EditorConfig)
        u.unmarshal(new FileInputStream("editorconfig.xml"));

    m.marshal(flow, new FileOutputStream("test.xml"));
  }
}
