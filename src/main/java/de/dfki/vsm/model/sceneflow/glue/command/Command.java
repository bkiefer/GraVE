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
      if (! l.isEmpty() && Command.convertToVOnDA) out.print("<Command><![CDATA[");
      for (Command c : l) {
        if (Command.convertToVOnDA) {
          out.print(c.getConcreteSyntax());
          out.println(";");
        } else {
          c.writeXML(out);
        }
      }
      if (! l.isEmpty() && Command.convertToVOnDA) out.print("]]></Command>");
    }
}
