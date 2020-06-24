package de.dfki.vsm.model.sceneflow.chart.graphics.edge;

import de.dfki.vsm.model.ModelObject;
import de.dfki.vsm.model.sceneflow.glue.command.Command;
import de.dfki.vsm.util.ios.IOSIndentWriter;
import de.dfki.vsm.util.xml.XMLParseAction;
import de.dfki.vsm.util.xml.XMLParseError;
import org.w3c.dom.Element;

// The edge graphics
public final class EdgeGraphics implements ModelObject {

    // The edge connection
    private EdgeArrow mConnection;

    // Create the graphics
    public EdgeGraphics() {
        mConnection = new EdgeArrow();
    }

    @Override
    public final void writeXML(IOSIndentWriter out) {
        if (!Command.convertToVOnDA) out.println("<Graphics>").push();
        mConnection.writeXML(out);
        if (!Command.convertToVOnDA) out.pop().println("</Graphics>");
    }

    @Override
    public final void parseXML(Element element) throws XMLParseError {
      // was Arrow, now: Connection
        XMLParseAction.processChildNodes(element, "Arrow", new XMLParseAction() {
            @Override
            public void run(Element element) throws XMLParseError {
                mConnection.parseXML(element);
            }
        });
        XMLParseAction.processChildNodes(element, "Connection", new XMLParseAction() {
          @Override
          public void run(Element element) throws XMLParseError {
              mConnection.parseXML(element);
          }
      });
    }

}
