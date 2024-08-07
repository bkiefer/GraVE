package de.dfki.grave.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
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
      mLogger.error("Error: Cannot parse file {}: {}", path, e);
    }
    return result;
  }

  public static boolean marshal(File file, Object o, Class ... classes) {
    File temp = new File(file.getPath() + "~");
    try {
      file.renameTo(temp);
      marshal(new FileOutputStream(file), o, classes);
    } catch (JAXBException|FileNotFoundException e) {
      mLogger.error("Error: Cannot write file {}: {}", file, e);
      temp.renameTo(file);
      temp.delete();
      return false;
    }
    temp.delete();
    return true;
  }

  public static void marshal(OutputStream out, Object o, Class ... classes)
      throws JAXBException {
    JAXBContext jc = JAXBContext.newInstance( classes );
    Marshaller m = jc.createMarshaller();
    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
    m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
    m.marshal(o, out);
  }

}
