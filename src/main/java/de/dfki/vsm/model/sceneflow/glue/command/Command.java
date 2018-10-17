package de.dfki.vsm.model.sceneflow.glue.command;

import de.dfki.vsm.model.sceneflow.glue.SyntaxObject;
import de.dfki.vsm.util.ios.IOSIndentWriter;
import de.dfki.vsm.util.xml.XMLParseError;
import de.dfki.vsm.util.xml.XMLWriteError;

import java.util.Collection;

import org.w3c.dom.Element;

/**
 * @author Gregor Mehlmann
 */
public abstract class Command extends SyntaxObject {

    public static boolean convertToVOnDA = false;

    @Override
    public abstract Command getCopy();

    public static Command parse(final Element element) throws XMLParseError {
        // The command to parse
        Command command = null;
        // The name of the XML tag
        final String tag = element.getTagName();
        // Parse the command
        if (tag.equals("Assign") || tag.equals("Assignment")) { // is: Assignment
            command = new Assignment();
            command.parseXML(element);
        } else {
            command = Invocation.parse(element);
        }
        // Parse the expression
        if (command == null) {
            command = Expression.parse(element);
        }
        return command;
    }

    public static void writeListXML(IOSIndentWriter out, Collection<? extends Command> l)
        throws XMLWriteError {
      if (Command.convertToVOnDA) {
        StringBuilder sb = new StringBuilder();
        for (Command c : l) {
          sb.append(c.getConcreteSyntax()).append(";\n");
        }
        if (sb.length() > 0) {
          out.print("<Command><![CDATA[").printPlain(sb.toString()).println("]]></Command>");
        }
      } else {
        for (Command c : l) {
          c.writeXML(out);
        }
      }
    }
}
