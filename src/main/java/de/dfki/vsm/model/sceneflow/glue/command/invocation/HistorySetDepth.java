package de.dfki.vsm.model.sceneflow.glue.command.invocation;

import de.dfki.vsm.model.sceneflow.glue.command.Invocation;
import de.dfki.vsm.util.ios.IOSIndentWriter;
import org.w3c.dom.Element;

/**
 * @author Gregor Mehlmann
 */
public final class HistorySetDepth extends Invocation {

    private String mState;
    private int mDepth;

    public HistorySetDepth() {
        mState = null;
        mDepth = -1;
    }

    public HistorySetDepth(final String state, final int depth) {
        mState = state;
        mDepth = depth;
    }

    public final int getDepth() {
        return mDepth;
    }

    public final String getState() {
        return mState;
    }

    @Override
    public final String getConcreteSyntax() {
        return "HistorySetDepth(" + mState + ", " + mDepth + ")";
    }

    @Override
    public final void writeXML(final IOSIndentWriter out) {
        out.println("<HistorySetDepth state=\"" + mState + "\" depth=\"" + mDepth + "\"/>");
    }

    @Override
    public final void parseXML(final Element element) {
        mState = element.getAttribute("state");
        mDepth = Integer.valueOf(element.getAttribute("depth"));
    }
}
