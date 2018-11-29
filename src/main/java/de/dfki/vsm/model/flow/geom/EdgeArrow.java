package de.dfki.vsm.model.flow.geom;

import java.util.ArrayList;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import de.dfki.vsm.util.cpy.Copyable;

/**
 * @author Gregor Mehlmann
 */
@XmlType(name="Connection")
public class EdgeArrow implements Copyable {

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((mPointList == null) ? 0 : mPointList.hashCode());
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
    EdgeArrow other = (EdgeArrow) obj;
    if (mPointList == null) {
      if (other.mPointList != null)
        return false;
    } else if (!mPointList.equals(other.mPointList))
      return false;
    return true;
  }

  // The control point list
  @XmlElement(name="ControlPoint")
  private ArrayList<ControlPoint> mPointList;

  // Create the connection
  public EdgeArrow() {
    mPointList = new ArrayList<>();
  }

  // Create the connection
  public EdgeArrow(final ArrayList<ControlPoint> pointList) {
    mPointList = pointList;
  }

  // Set the point list
  @XmlTransient
  public void setPointList(final ArrayList<ControlPoint> value) {
    mPointList = value;
  }

  // Get the point list
  public final ArrayList<ControlPoint> getPointList() {
    return mPointList;
  }

  // Copy the point list
  public final ArrayList<ControlPoint> getCopyOfPointList() {
    final ArrayList<ControlPoint> copy = new ArrayList<>();
    for (final ControlPoint point : mPointList) {
      copy.add(point.deepCopy());
    }
    return copy;
  }

  // Copy the connection
  @Override
  public final EdgeArrow deepCopy() {
    return new EdgeArrow(getCopyOfPointList());
  }
}
