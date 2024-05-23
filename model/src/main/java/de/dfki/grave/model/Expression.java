package de.dfki.grave.model;

import java.util.Observable;

import de.dfki.grave.util.Copyable;

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

  @Override
  public int hashCode() {
    return content != null? this.content.hashCode() : 31;
  }
}
