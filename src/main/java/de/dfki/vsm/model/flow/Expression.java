package de.dfki.vsm.model.flow;

import javax.xml.bind.annotation.*;

import org.eclipse.persistence.oxm.annotations.XmlCDATA;

import de.dfki.vsm.util.cpy.Copyable;

/**
 * @author Gregor Mehlmann
 */
@XmlType(name="Condition")
@XmlAccessorType(XmlAccessType.NONE)
public class Expression implements Copyable {
  @XmlValue
  @XmlCDATA
  protected String content;

  public Expression(String c) {
    content = c.trim();
  }

  public Expression() {
  }

  @XmlTransient
  public String getContent() { return content; }

  public void setContent(String s) { content = s.trim(); }

  @Override
  public Expression deepCopy() {
    Expression result = new Expression();
    result.content = content;
    return result;
  }
}
