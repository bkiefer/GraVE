package de.dfki.vsm.model.sceneflow.glue.command.expression.literal;

import de.dfki.vsm.model.sceneflow.glue.command.expression.LiteralExpression;
import de.dfki.vsm.util.ios.IOSIndentWriter;
import org.w3c.dom.Element;

/**
 * @author Gregor Mehlmann
 */
public final class NullLiteral extends LiteralExpression {

    public NullLiteral() {
    }


    @Override
    public final String getConcreteSyntax() {
        return "null";
    }

    @Override
    public void writeXML(final IOSIndentWriter out) {
        out.println("<NullLiteral/>");
    }

    @Override
    public void parseXML(final Element element) {
    }
}
