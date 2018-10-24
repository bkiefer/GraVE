package de.dfki.vsm.model.flow.graphics.edge;

import java.util.ArrayList;

import javax.xml.bind.annotation.*;

import org.w3c.dom.Element;

import de.dfki.vsm.model.ModelObject;
import de.dfki.vsm.model.flow.geom.ControlPoint;
import de.dfki.vsm.util.ios.IOSIndentWriter;
import de.dfki.vsm.util.xml.XMLParseAction;
import de.dfki.vsm.util.xml.XMLParseError;

/**
 * @author Gregor Mehlmann
 */
@XmlType(name="Connection")
public class EdgeArrow implements ModelObject {

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
      copy.add(point.getCopy());
    }
    return copy;
  }

  // Copy the connection
  @Override
  public final EdgeArrow getCopy() {
    return new EdgeArrow(getCopyOfPointList());
  }

  // Write the connection
  @Override
  public final void writeXML(final IOSIndentWriter out) {
    out.println("<Connection>").push();
    for (int i = 0; i < mPointList.size(); i++) {
      mPointList.get(i).writeXML(out);
    }
    out.pop().println("</Connection>");
  }

  @Override
  public final void parseXML(final Element element) throws XMLParseError {
    XMLParseAction.processChildNodes(element, "ControlPoint", new XMLParseAction() {
      @Override
      public void run(final Element element) {
        final ControlPoint point = new ControlPoint();
        point.parseXML(element);
        mPointList.add(point);
      }
    });
  }
}
