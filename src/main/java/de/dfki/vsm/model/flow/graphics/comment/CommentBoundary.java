package de.dfki.vsm.model.flow.graphics.comment;

import org.w3c.dom.Element;

import javax.xml.bind.annotation.*;

import de.dfki.vsm.model.ModelObject;
import de.dfki.vsm.util.ios.IOSIndentWriter;
import de.dfki.vsm.util.xml.XMLParseError;

/**
 * @author Gregr Mehlmann
 */
@XmlType(name="Boundary")
public final class CommentBoundary implements ModelObject {

  private int mXPos;
  private int mYPos;
  private int mWidth;
  private int mHeight;

  public CommentBoundary() {
    mXPos = Integer.MIN_VALUE;
    mYPos = Integer.MIN_VALUE;
    mWidth = Integer.MIN_VALUE;
    mHeight = Integer.MIN_VALUE;
  }

  public CommentBoundary(
          final int xPos, final int yPos,
          final int width, final int height) {
    mXPos = xPos;
    mYPos = yPos;
    mWidth = width;
    mHeight = height;
  }

  @XmlAttribute
  public final void setXPos(final int value) {
    mXPos = value;
  }

  public final int getXPos() {
    return mXPos;
  }

  @XmlAttribute
  public final void setYPos(final int value) {
    mYPos = value;
  }

  public final int getYPos() {
    return mYPos;
  }

  @XmlAttribute
  public final void setWidth(final int value) {
    mWidth = value;
  }

  public final int getWidth() {
    return mWidth;
  }

  @XmlAttribute
  public final void setHeight(final int value) {
    mHeight = value;
  }

  public final int getHeight() {
    return mHeight;
  }

  @Override
  public final CommentBoundary getCopy() {
    return new CommentBoundary(mXPos, mYPos, mWidth, mHeight);
  }

  @Override
  public final void writeXML(final IOSIndentWriter out) {
    out.println("<Boundary "
            + "xPos=\"" + mXPos + "\" "
            + "yPos=\"" + mYPos + "\" "
            + "width=\"" + mWidth + "\" "
            + "height=\"" + mHeight + "\"/>");
  }

  @Override
  public final void parseXML(final Element element) throws XMLParseError {
    mXPos = Integer.valueOf(element.getAttribute("xPos"));
    mYPos = Integer.valueOf(element.getAttribute("yPos"));
    mWidth = Integer.valueOf(element.getAttribute("width"));
    mHeight = Integer.valueOf(element.getAttribute("height"));
  }
}
