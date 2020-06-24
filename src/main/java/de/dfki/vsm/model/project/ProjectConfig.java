package de.dfki.vsm.model.project;

import java.io.ByteArrayOutputStream;

import org.w3c.dom.Element;

import de.dfki.vsm.model.ModelObject;
import de.dfki.vsm.util.ios.IOSIndentWriter;
import de.dfki.vsm.util.log.LOGDefaultLogger;
import de.dfki.vsm.util.xml.XMLParseAction;
import de.dfki.vsm.util.xml.XMLParseError;
import de.dfki.vsm.util.xml.XMLWriteError;

/**
 * @author Gregor Mehlmann
 */
public final class ProjectConfig implements ModelObject {
  private final LOGDefaultLogger mLogger = LOGDefaultLogger.getInstance();

    // The name of the project
    private String mProjectName;
    //

    // Construct an empty project
    public ProjectConfig() {
        // Initialize The Project Name
        mProjectName = new String();
        // Initialize The Plugin List
    }


    // Write the project configuration
    @Override
    public final void writeXML(final IOSIndentWriter stream) throws XMLWriteError {
        stream.println("<Project name=\"" + mProjectName + "\">");
        stream.push();
        // Write the plugin configurations
        stream.pop().println("<Agents/>");
        // Write the player configurations
        stream.pop().print("</Project>").flush();
    }


    // Parse the project configuration
    @Override
    public final void parseXML(final Element element) throws XMLParseError {
        // Get The Type Of The Config
        final String tag = element.getTagName();
        // Check The Type Of The Config
        if (tag.equals("Project")) {
            // Get The Project Name
            mProjectName = element.getAttribute("name");
            // Parse The Individual Entries
            XMLParseAction.processChildNodes(element, new XMLParseAction() {
                @Override
                public void run(final Element element) throws XMLParseError {
                    // Get The Tag Name
                    final String tag = element.getTagName();
                    // Check The Tag Name
                }
            });
        }
    }

     // Get string representation
    @Override
    public final String toString() {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final IOSIndentWriter writer = new IOSIndentWriter(buffer);
        try {
            writeXML(writer);
        } catch (final XMLWriteError exc) {
            mLogger.failure(exc.toString());
        }
        writer.flush();
        writer.close();
       try {
            //return buffer.toString("UTF-8");
            return buffer.toString();
        } catch (final Exception exc) {
            exc.printStackTrace();
            //
            return null;
        }
    }

}
