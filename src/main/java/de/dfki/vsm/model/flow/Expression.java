package de.dfki.vsm.model.flow;

import de.dfki.vsm.util.cpy.Copyable;
import java.util.Observable;

/**
 * @author Gregor Mehlmann
 */
public class Expression extends Observable implements Copyable {

  protected String content;

  public Expression(String c) {
    content = c.trim();
  }

  public Expression() {
  }

  public String getContent() { return content; }

  public void setContent(String s) {
    content = s.trim();
    setChanged();
    notifyObservers();
  }

  @Override
  public Expression deepCopy() {
    return new Expression(content);
  }
}
