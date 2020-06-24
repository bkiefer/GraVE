package de.dfki.vsm.model.sceneflow.glue.command.invocation;

import de.dfki.vsm.model.sceneflow.glue.command.Invocation;
import de.dfki.vsm.util.ios.IOSIndentWriter;
import org.w3c.dom.Element;

/**
 * @author Gregor Mehlmann
 */
public final class HistoryClearFlat extends Invocation {

    private String mState;

    public HistoryClearFlat() {
    }

    public HistoryClearFlat(final String state) {
        mState = state;
    }

    public final String getState() {
        return mState;
    }

    @Override
    public final String getConcreteSyntax() {
        return "HistoryClearFlat(" + mState + ")";
    }

    @Override
    public final void writeXML(final IOSIndentWriter out) {
        out.println("<HistoryClearFlat state=\"" + mState + "\"/>");
    }

    @Override
    public final void parseXML(final Element element) {
        mState = element.getAttribute("state");
    }
}
