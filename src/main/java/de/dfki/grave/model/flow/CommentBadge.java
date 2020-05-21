package de.dfki.grave.model.flow;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import de.dfki.grave.util.Copyable;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
@XmlType(name="Comment")
@XmlAccessorType(XmlAccessType.NONE)
public class CommentBadge implements ContentHolder, Copyable {

  @XmlElement(name="Text")
  private String text = "";
  @XmlElement(name="Boundary")
  private Boundary mBoundary;

  public String getContent() {
    return text;
  }

  public void setContent(String s) {
    text = s;
  }

  public Boundary getBoundary() {
    return mBoundary;
  }

  public void setBoundary(Boundary value) {
    mBoundary = value;
  }

  @Override
  public CommentBadge deepCopy() {
    CommentBadge result = new CommentBadge();
    result.mBoundary = mBoundary;
    result.text = text;
    return result;
  }
}
