package de.dfki.vsm.model.flow.geom;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import de.dfki.vsm.util.cpy.Copyable;

/**
 * @author Gregor Mehlmann
 */
@XmlType(name="Position")
public final class Position implements Copyable {

  // The Y coordinate
  private int mXPos;
  // The Y coordinate
  private int mYPos;

  // Create a node position
  public Position() {
    mXPos = Integer.MIN_VALUE;
    mYPos = Integer.MIN_VALUE;
  }

  // Create a node position
  public Position(final int xPos, final int yPos) {
    mXPos = xPos;
    mYPos = yPos;
  }

  // Set the X coordinate
  @XmlAttribute
  public final void setXPos(final int value) {
    mXPos = value;
  }

  // Get the X coordinate
  public final int getXPos() {
    return mXPos;
  }

  // Set the Y coordinate
  @XmlAttribute
  public final void setYPos(final int value) {
    mYPos = value;
  }

  // Get the Y coordinate
  public final int getYPos() {
    return mYPos;
  }

  @Override
  public final Position getCopy() {
    return new Position(mXPos, mYPos);
  }
}
