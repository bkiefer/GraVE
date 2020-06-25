package de.dfki.grave.model.flow;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import de.dfki.grave.model.flow.Boundary;
import de.dfki.grave.model.flow.CommentBadge;
import de.dfki.grave.model.flow.ContentHolder;
import de.dfki.grave.model.flow.SuperNode;
import de.dfki.grave.util.Copyable;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
@XmlType(name="Comment")
@XmlAccessorType(XmlAccessType.NONE)
public class CommentBadge implements ContentHolder, Copyable {

  protected SuperNode mParentNode = null;
  
  @XmlElement(name="Text")
  private String text = "";
  @XmlElement(name="Boundary")
  private Boundary mBoundary;

  public static CommentBadge createComment(SuperNode parent, Boundary b) {
    CommentBadge c = new CommentBadge();
    c.mBoundary = b;
    c.mParentNode = parent;
    c.mParentNode.addComment(c);
    return c;
  }
  
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

  public SuperNode getParentNode() {
    return mParentNode;
  }
  
  @Override
  public CommentBadge deepCopy() {
    CommentBadge result = new CommentBadge();
    result.mBoundary = mBoundary;
    result.text = text;
    return result;
  }
}
