package de.dfki.grave.model.flow;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import de.dfki.grave.util.Copyable;

/**
 * @author Gregr Mehlmann
 */
@XmlType(name="Boundary")
public final class Boundary implements Copyable {

  private int mXPos;
  private int mYPos;
  private int mWidth;
  private int mHeight;

  public Boundary() {
    mXPos = Integer.MIN_VALUE;
    mYPos = Integer.MIN_VALUE;
    mWidth = Integer.MIN_VALUE;
    mHeight = Integer.MIN_VALUE;
  }

  public Boundary(
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
  public final Boundary deepCopy() {
    return new Boundary(mXPos, mYPos, mWidth, mHeight);
  }
}
