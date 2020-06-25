package de.dfki.vsm.model.sceneflow.chart.graphics.comment;

import de.dfki.vsm.model.ModelObject;
import de.dfki.vsm.model.sceneflow.glue.command.Command;
import de.dfki.vsm.util.ios.IOSIndentWriter;
import de.dfki.vsm.util.xml.XMLParseAction;
import de.dfki.vsm.util.xml.XMLParseError;
import org.w3c.dom.Element;

/**
 * @author Gregor Mehlmann
 */
public final class CommentGraphics implements ModelObject {

    private CommentBoundary mRectangle;

    public CommentGraphics() {
        mRectangle = new CommentBoundary();
    }


    @Override
    public final void writeXML(final IOSIndentWriter out) {
        if (! Command.convertToVOnDA) out.println("<Graphics>").push();
        mRectangle.writeXML(out);
        if (! Command.convertToVOnDA) out.pop().println("</Graphics>");
    }

    @Override
    public final void parseXML(final Element element) throws XMLParseError {
        XMLParseAction.processChildNodes(element, "Boundary", new XMLParseAction() {
            @Override
            public void run(final Element element) throws XMLParseError {
                mRectangle.parseXML(element);
            }
        });
        XMLParseAction.processChildNodes(element, "Rect", new XMLParseAction() {
          @Override
          public void run(final Element element) throws XMLParseError {
              mRectangle.parseXML(element);
          }
        });
    }
}