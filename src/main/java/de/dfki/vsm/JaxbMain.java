package de.dfki.vsm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import de.dfki.vsm.model.sceneflow.chart.SceneFlow;

public class JaxbMain {

  public static void main(String[] args) throws JAXBException, FileNotFoundException {
    JAXBContext jc = JAXBContext.newInstance( "de.dfki.vsm.model.sceneflow.chart" );
    Unmarshaller u = jc.createUnmarshaller();
    SceneFlow flow = (SceneFlow)
        u.unmarshal(new FileInputStream("src/test/resources/demo/sceneflow.xml"));

    Marshaller m = jc.createMarshaller();
    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    m.marshal(flow, new FileOutputStream("test.xml"));
  }
}
