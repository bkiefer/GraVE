package de.dfki.grave.editor.event;

//~--- non-JDK imports --------------------------------------------------------

/**
 * @author Martin Fallas
 */
public class ProjectChangedEvent {

  private Object mSource;

  public ProjectChangedEvent(Object source) {
    mSource = source;
  }

  public Object getSource() { return mSource; }
}
