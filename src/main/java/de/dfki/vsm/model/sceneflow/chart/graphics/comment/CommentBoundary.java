package de.dfki.vsm.model.sceneflow.chart.graphics.comment;

import de.dfki.vsm.model.ModelObject;
import de.dfki.vsm.util.ios.IOSIndentWriter;
import de.dfki.vsm.util.xml.XMLParseError;
import org.w3c.dom.Element;

/**
 * @author Gregr Mehlmann
 */
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
      if (! element.getAttribute("xPos").isEmpty()) {
        mXPos = Integer.valueOf(element.getAttribute("xPos"));
        mYPos = Integer.valueOf(element.getAttribute("yPos"));
      } else {
        mXPos = Integer.valueOf(element.getAttribute("x-pos"));
        mYPos = Integer.valueOf(element.getAttribute("y-pos"));
      }
      mWidth = Integer.valueOf(element.getAttribute("width"));
      mHeight = Integer.valueOf(element.getAttribute("height"));
    }
}