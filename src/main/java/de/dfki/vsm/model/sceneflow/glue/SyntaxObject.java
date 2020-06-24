package de.dfki.vsm.model.sceneflow.glue;

import de.dfki.vsm.model.ModelObject;

/**
 * @author Gregor Mehlmann
 */
public abstract class SyntaxObject implements ModelObject {

    public abstract String getConcreteSyntax();

    @Override
    public final String toString() {
        return getConcreteSyntax();
    }
}