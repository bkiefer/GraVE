package de.dfki.grave.util;

import de.dfki.grave.util.Copyable;

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
  public abstract Copyable deepCopy();

}
