package de.dfki.grave.model.flow;

import java.util.Observable;

import de.dfki.grave.model.flow.Code;
import de.dfki.grave.util.Copyable;

/**
 * @author Gregor Mehlmann
 */
public class Code extends Observable implements Copyable {
  private String content;

  public Code(String c) {
    content = c.trim();
  }

  public Code() {
    content = "";
  }

  public String getContent() { return content; }

  public void setContent(String s) {
    content = s.trim();
    setChanged();
    notifyObservers();
  }

  @Override
  public Code deepCopy() {
    return new Code(content);
  }

  public String toString() {
    return content;
  }

  @Override
  public int hashCode(){
    return content.hashCode();
  }
}
