package de.dfki.vsm.model.flow.geom;

import javax.xml.bind.annotation.*;

import org.w3c.dom.Element;

import de.dfki.vsm.model.ModelObject;
import de.dfki.vsm.util.ios.IOSIndentWriter;

/**
 * @author Gregor Mehlmann
 */
@XmlType(name="ControlPoint")
public final class ControlPoint implements ModelObject {

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

  public ControlPoint(int xPos, int ctrlXPos, int yPos, int ctrlYPos) {
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

  @Override
  public final ControlPoint getCopy() {
    return new ControlPoint(mXPpos, mCtrlXPos, mYPos, mCtrlYPos);
  }

  @Override
  public final void writeXML(final IOSIndentWriter out) {
    out.println("<ControlPoint "
            + "xPos=\"" + mXPpos + "\" "
            + "yPos=\"" + mYPos + "\" "
            + "ctrlXPos=\"" + mCtrlXPos + "\" "
            + "ctrlYPos=\"" + mCtrlYPos + "\"/>");
  }

  @Override
  public final void parseXML(final Element element) {
    mXPpos = Integer.valueOf(element.getAttribute("xPos"));
    mYPos = Integer.valueOf(element.getAttribute("yPos"));
    mCtrlXPos = Integer.valueOf(element.getAttribute("ctrlXPos"));
    mCtrlYPos = Integer.valueOf(element.getAttribute("ctrlYPos"));
  }
}