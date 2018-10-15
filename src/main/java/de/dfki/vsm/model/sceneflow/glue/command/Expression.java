package de.dfki.vsm.model.sceneflow.glue.command;

/**
 * @author Gregor Mehlmann
 */
public abstract class Expression extends Command {

  @Override
  public Expression getCopy()  {
    Expression result = null;
    try {
      result = this.getClass().newInstance();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    result.content = this.content;
    return result;
  };

}
