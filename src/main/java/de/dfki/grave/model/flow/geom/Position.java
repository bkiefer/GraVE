package de.dfki.grave.model.flow.geom;

import java.awt.Point;
import java.awt.geom.Point2D;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import de.dfki.grave.util.Copyable;

/**
 * @author Gregor Mehlmann
 */
@XmlType(name="Position")
public final class Position implements Copyable {

  // The Y coordinate
  private int x;
  // The Y coordinate
  private int y;

  // Create a node position
  public Position() {
    x = Integer.MIN_VALUE;
    y = Integer.MIN_VALUE;
  }

  // Create a node position
  public Position(final int xPos, final int yPos) {
    x = xPos;
    y = yPos;
  }

  public Position(Point p) {
    this(p.x, p.y);
  }

  // Set the X coordinate
  @XmlAttribute
  public final void setXPos(final int value) {
    x = value;
  }

  // Get the X coordinate
  public final int getXPos() {
    return x;
  }

  // Set the Y coordinate
  @XmlAttribute
  public final void setYPos(final int value) {
    y = value;
  }

  // Get the Y coordinate
  public final int getYPos() {
    return y;
  }

  @Override
  public final Position deepCopy() {
    return new Position(x, y);
  }

  public void setTo(Point p) {
    x = p.x;
    y = p.y;
  }

  public Point toPoint() {
    return new Point(x, y);
  }
  
  public int hashCode() {
    return x + 17 * y;
  }
  
  public boolean equals(Object o) {
    if (! (o instanceof Position)) return false;
    Position p = (Position)o;
    return p.x == x && p.y == y;
  }
  
  public String toString() {
    return "(" + x + "," + y + ")";
  }

  public void translate(int xx, int yy) {
    x += xx; y += yy;
  }

  public void stretch(double d) {
    x *= d; y *= d;
  }
  
  public double norm2() {
    return Math.sqrt(dotProd(this));
  }

  public double dotProd(Position b) {
    return x * b.x + y * b.y;
  }
  
  public double dotProd(Point2D b) {
    return x * b.getX() + y * b.getY();
  }
}
