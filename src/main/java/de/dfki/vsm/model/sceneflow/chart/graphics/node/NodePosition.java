package de.dfki.vsm.model.sceneflow.chart.graphics.node;

import de.dfki.vsm.model.ModelObject;
import de.dfki.vsm.util.ios.IOSIndentWriter;
import org.w3c.dom.Element;

/**
 * @author Gregor Mehlmann
 */
public final class NodePosition implements ModelObject {

    // The Y coordinate
    private int mXPos;
    // The Y coordinate
    private int mYPos;

    // Create a node position
    public NodePosition() {
        mXPos = Integer.MIN_VALUE;
        mYPos = Integer.MIN_VALUE;
    }

    // Create a node position
    public NodePosition(final int xPos, final int yPos) {
        mXPos = xPos;
        mYPos = yPos;
    }

    @Override
    public final void writeXML(IOSIndentWriter out) {
        out.println("<Position xPos=\"" + mXPos + "\" yPos=\"" + mYPos + "\"/>");
    }

    @Override
    public final void parseXML(Element element) {
      // was: x-pos, y-pos now: xPos, yPos
      if (! element.getAttribute("xPos").isEmpty()) {
        mXPos = Integer.valueOf(element.getAttribute("xPos"));
        mYPos = Integer.valueOf(element.getAttribute("yPos"));
      } else {
        mXPos = Integer.valueOf(element.getAttribute("x-pos"));
        mYPos = Integer.valueOf(element.getAttribute("y-pos"));
      }
    }
}
