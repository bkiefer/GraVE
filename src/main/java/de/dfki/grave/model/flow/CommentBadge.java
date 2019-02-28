package de.dfki.grave.model.flow;

//~--- JDK imports ------------------------------------------------------------
import de.dfki.grave.editor.ObserverDocument;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import de.dfki.grave.model.flow.geom.Boundary;
import de.dfki.grave.util.Copyable;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
@XmlType(name="Comment")
public class CommentBadge extends ObserverDocument implements Copyable {

  private SuperNode mParentNode = null;
  private String text = "";
  private Boundary mBoundary;
  private int mFontSize;

  public void setParentNode(SuperNode value) {
    mParentNode = value;
  }

  @XmlElement(name="Boundary")
  public Boundary getBoundary() {
    return mBoundary;
  }

  public void setBoundary(Boundary value) {
    mBoundary = value;
  }

  public void setFontSize(int value) {
    mFontSize = value;
  }

  @Override
  public CommentBadge deepCopy() {
    CommentBadge result = new CommentBadge();
    result.mBoundary = mBoundary;
    result.mFontSize = mFontSize;
    result.text = text;
    return result;
  }
}
