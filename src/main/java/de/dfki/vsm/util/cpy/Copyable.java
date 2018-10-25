package de.dfki.vsm.util.cpy;

/**
 * An interface for all copyable objects.
 *
 * @author Gregor Mehlmann
 */
public interface Copyable {

  /**
   * Create a deep copy of the copyable object.
   *
   * @return A deep copy of the copyable object.
   */
  public abstract Copyable getCopy();

}
