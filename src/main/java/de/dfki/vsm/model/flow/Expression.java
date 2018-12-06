package de.dfki.vsm.model.flow;

import de.dfki.vsm.util.cpy.Copyable;

/**
 * @author Gregor Mehlmann
 */
public class Expression implements Copyable {

  protected String content;

  public Expression(String c) {
    content = c.trim();
  }

  public Expression() {
  }

  public String getContent() { return content; }

  public void setContent(String s) {
    content = s.trim();
  }

  @Override
  public Expression deepCopy() {
    return new Expression(content);
  }
}
