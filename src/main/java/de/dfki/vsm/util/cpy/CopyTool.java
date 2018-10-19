package de.dfki.vsm.util.cpy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregor Mehlmann
 */
public abstract class CopyTool {

  // The Logger Instance
  private final static Logger sLogger = LoggerFactory.getLogger(CopyTool.class);;

  // Return A Deep Copy
  public final static Copyable copy(final Copyable obj) {
    try {
      // Write Out The Object
      final ByteArrayOutputStream bos = new ByteArrayOutputStream();
      final ObjectOutputStream oos = new ObjectOutputStream(bos);
      // Write Out The Object
      oos.writeObject(obj);
      // Read In The Object
      final ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
      final ObjectInputStream ois = new ObjectInputStream(bis);
      // Read In The Object
      final Copyable copy = (Copyable) ois.readObject();
      // Return The Copy Now
      return copy;
    } catch (Exception exc) {
      // Print Some Information
      sLogger.error(exc.toString());
      // Return The Null Object
      return null;
    }
  }
}
