package de.dfki.vsm.model.sceneflow.chart.graphics.node;

//~--- non-JDK imports --------------------------------------------------------
import de.dfki.vsm.model.ModelObject;
import de.dfki.vsm.model.sceneflow.glue.command.Command;
import de.dfki.vsm.util.ios.IOSIndentWriter;
import de.dfki.vsm.util.xml.XMLParseAction;
import de.dfki.vsm.util.xml.XMLParseError;

import org.w3c.dom.Element;

/**
 * @author Gregor Mehlmann
 */
public final class NodeGraphics implements ModelObject {

    // The node position
    private NodePosition mPosition;

    // Create the position
    public NodeGraphics() {
        mPosition = new NodePosition();
    }

    @Override
    public final void writeXML(IOSIndentWriter out) {
        if (!Command.convertToVOnDA) out.println("<Graphics>").push();
        mPosition.writeXML(out);
        if (!Command.convertToVOnDA) out.pop().println("</Graphics>");
    }

    @Override
    public final void parseXML(final Element element) throws XMLParseError {
        XMLParseAction.processChildNodes(element, "Position", new XMLParseAction() {
            @Override
            public void run(final Element element) {
                mPosition.parseXML(element);
            }
        });
    }
}
