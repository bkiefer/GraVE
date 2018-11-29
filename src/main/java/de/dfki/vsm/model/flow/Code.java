package de.dfki.vsm.model.flow;

import javax.xml.bind.annotation.*;

import org.eclipse.persistence.oxm.annotations.XmlCDATA;

import de.dfki.vsm.util.cpy.Copyable;

/**
 * @author Gregor Mehlmann
 */
@XmlType(name="Code")
@XmlAccessorType(XmlAccessType.NONE)
public class Code implements Copyable {
  @XmlValue
  @XmlCDATA
  private String content;

  public Code(String c) {
    content = c.trim();
  }

  public Code() {
  }

  @XmlTransient
  public String getContent() { return content; }

  public void setContent(String s) { content = s.trim(); }

  @Override
  public Code deepCopy() {
    Code result = new Code();
    result.content = content;
    return result;
  }

  public String toString() {
    return content;
  }
}
