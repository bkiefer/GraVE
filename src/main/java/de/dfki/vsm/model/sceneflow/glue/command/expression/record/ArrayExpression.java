package de.dfki.vsm.model.sceneflow.glue.command.expression.record;

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
public final class ArrayExpression extends Expression {

    final private ArrayList<Expression> mExpList;

    public ArrayExpression() {
        mExpList = new ArrayList<Expression>();
    }

    public ArrayExpression(final ArrayList expList) {
        mExpList = expList;
    }

    public final ArrayList<Expression> getExpList() {
        return mExpList;
    }


    @Override
    public final String getConcreteSyntax() {
        String desc = /* mType + */ "[ ";
        for (int i = 0; i < mExpList.size(); i++) {
            desc += mExpList.get(i).getConcreteSyntax();

            if (i != mExpList.size() - 1) {
                desc += " , ";
            }
        }
        return desc + " ]";
    }

    @Override
    public final void writeXML(final IOSIndentWriter out) throws XMLWriteError {
        out.println("<ArrayExpression>").push();
        for (int i = 0; i < mExpList.size(); i++) {
            mExpList.get(i).writeXML(out);
        }
        out.pop().println("</ArrayExpression>");
    }

    @Override
    public final void parseXML(final Element element) throws XMLParseError {

        XMLParseAction.processChildNodes(element, new XMLParseAction() {
            @Override
            public final void run(final Element element) throws XMLParseError {
                final Expression exp = Expression.parse(element);
                mExpList.add(exp);
            }
        });
    }
}