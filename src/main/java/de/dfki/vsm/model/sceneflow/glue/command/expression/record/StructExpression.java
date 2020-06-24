package de.dfki.vsm.model.sceneflow.glue.command.expression.record;

import de.dfki.vsm.model.sceneflow.glue.command.Assignment;
import de.dfki.vsm.model.sceneflow.glue.command.Expression;
import de.dfki.vsm.util.ios.IOSIndentWriter;
import de.dfki.vsm.util.xml.XMLParseAction;
import de.dfki.vsm.util.xml.XMLParseError;
import de.dfki.vsm.util.xml.XMLWriteError;
import java.util.ArrayList;
import org.w3c.dom.Element;

/**
 * @author Gregor Mehlmann
 */
public final class StructExpression extends Expression {

    final private ArrayList<Assignment> mExpList;

    public StructExpression() {
        mExpList = new ArrayList();
    }

    public StructExpression(final ArrayList list) {
        mExpList = list;
    }

    public final ArrayList<Assignment> getExpList() {
        return mExpList;
    }

    @Override
    public final String getConcreteSyntax() {
        String desc = "{ ";
        for (int i = 0; i < mExpList.size(); i++) {
            desc += mExpList.get(i).getConcreteSyntax();

            if (i != mExpList.size() - 1) {
                desc += " , ";
            }
        }
        return desc + " }";
    }


    @Override
    public final void writeXML(final IOSIndentWriter out) throws XMLWriteError {
        out.println("<StructExpression>").push();
        for (int i = 0; i < mExpList.size(); i++) {
            mExpList.get(i).writeXML(out);
        }
        out.pop().println("</StructExpression>");
    }

    @Override
    public final void parseXML(final Element element) throws XMLParseError {
        XMLParseAction.processChildNodes(element, new XMLParseAction() {
            @Override
            public final void run(final Element element) throws XMLParseError {
                final Assignment exp = new Assignment();
                exp.parseXML(element);
                mExpList.add(exp);
            }
        });
    }
}
