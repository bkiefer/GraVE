package de.dfki.grave.model.flow.geom;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import de.dfki.grave.model.flow.Position;
import de.dfki.grave.util.Copyable;

/**
 * @author Gregor Mehlmann
 */
@XmlType(name="ControlPoint")
@Deprecated
public final class ControlPoint implements Copyable {

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + mCtrlXPos;
    result = prime * result + mCtrlYPos;
    result = prime * result + mXPpos;
    result = prime * result + mYPos;
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ControlPoint other = (ControlPoint) obj;
    if (mCtrlXPos != other.mCtrlXPos)
      return false;
    if (mCtrlYPos != other.mCtrlYPos)
      return false;
    if (mXPpos != other.mXPpos)
      return false;
    if (mYPos != other.mYPos)
      return false;
    return true;
  }

  private int mXPpos;
  private int mCtrlXPos;
  private int mYPos;
  private int mCtrlYPos;

  public ControlPoint() {
    mXPpos = Integer.MIN_VALUE;
    mYPos = Integer.MIN_VALUE;
    mCtrlXPos = Integer.MIN_VALUE;
    mCtrlYPos = Integer.MIN_VALUE;
  }

  private ControlPoint(int xPos, int ctrlXPos, int yPos, int ctrlYPos) {
    mXPpos = xPos;
    mYPos = yPos;
    mCtrlXPos = ctrlXPos;
    mCtrlYPos = ctrlYPos;
  }

  @XmlAttribute
  public final int getXPos() {
    return mXPpos;
  }

  public final void setXPos(final int value) {
    mXPpos = value;
  }

  @XmlAttribute
  public final int getCtrlXPos() {
    return mCtrlXPos;
  }

  public final void setCtrlXPos(final int value) {
    mCtrlXPos = value;
  }

  @XmlAttribute
  public final int getYPos() {
    return mYPos;
  }

  public final void setYPos(final int value) {
    mYPos = value;
  }

  @XmlAttribute
  public final int getCtrlYPos() {
    return mCtrlYPos;
  }

  public final void setCtrlYPos(final int value) {
    mCtrlYPos = value;
  }

  public Position getPoint() { return new Position(mXPpos, mYPos); }
  public Position getCtrlPoint() { return new Position(mCtrlXPos, mCtrlYPos); }

  @Override
  public final ControlPoint deepCopy() {
    return new ControlPoint(mXPpos, mCtrlXPos, mYPos, mCtrlYPos);
  }
}
