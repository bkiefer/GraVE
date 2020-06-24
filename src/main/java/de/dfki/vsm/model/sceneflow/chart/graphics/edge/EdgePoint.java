package de.dfki.vsm.model.sceneflow.chart.graphics.edge;

import de.dfki.vsm.model.ModelObject;
import de.dfki.vsm.util.ios.IOSIndentWriter;
import org.w3c.dom.Element;

/**
 * @author Gregor Mehlmann
 */
public final class EdgePoint implements ModelObject {

    private int mXPpos;
    private int mCtrlXPos;
    private int mYPos;
    private int mCtrlYPos;

    public EdgePoint() {
        mXPpos = Integer.MIN_VALUE;
        mYPos = Integer.MIN_VALUE;
        mCtrlXPos = Integer.MIN_VALUE;
        mCtrlYPos = Integer.MIN_VALUE;
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
      // was x-pos, y-pos, control-x-pos, control-y-pos
      // is: xPos, yPos, ctrlXPos, ctrlYPos
      if (! element.getAttribute("xPos").isEmpty()) {
        mXPpos = Integer.valueOf(element.getAttribute("xPos"));
        mYPos = Integer.valueOf(element.getAttribute("yPos"));
        mCtrlXPos = Integer.valueOf(element.getAttribute("ctrlXPos"));
        mCtrlYPos = Integer.valueOf(element.getAttribute("ctrlYPos"));
      } else {
        mXPpos = Integer.valueOf(element.getAttribute("x-pos"));
        mYPos = Integer.valueOf(element.getAttribute("y-pos"));
        mCtrlXPos = Integer.valueOf(element.getAttribute("control-x-pos"));
        mCtrlYPos = Integer.valueOf(element.getAttribute("control-y-pos"));
      }
    }
}
