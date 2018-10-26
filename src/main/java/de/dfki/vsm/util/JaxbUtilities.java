package de.dfki.vsm.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JaxbUtilities {
  // The singleton logger instance
  private static final Logger mLogger =
      LoggerFactory.getLogger(JaxbUtilities.class);

  public static Object unmarshal(InputStream inputStream, String path, Class ... classes){
    Object result = null;
    try {
      JAXBContext jc = JAXBContext.newInstance( classes );
      Unmarshaller u = jc.createUnmarshaller();
      result = u.unmarshal(inputStream);
    } catch (JAXBException e) {
      mLogger.error("Error: Cannot parse sceneflow file " + path + " : " + e);
    }
    return result;
  }

  public static boolean marshal(File file, Object o, Class ... classes) {
    try {
      JAXBContext jc = JAXBContext.newInstance( classes );
      Marshaller m = jc.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
      m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
      m.marshal(o, new FileOutputStream(file));
    } catch (JAXBException|FileNotFoundException e) {
      mLogger.error("Error: Cannot write file " + file + " : " + e);
      return false;
    }
    return true;
  }

}