package de.dfki.grave.util.extensions.renderers;

import de.dfki.grave.util.extensions.value.ProjectValueProperty;
import de.dfki.grave.util.extensions.value.ValueRenderable;
import javafx.scene.Node;

/**
 * Created by alvaro on 4/23/17.
 */
public abstract class ValueRender implements ValueRenderable {

  protected ProjectValueProperty valueProperty;
  protected Node control;

  public ValueRender(ProjectValueProperty valueProperty) {
    this.valueProperty = valueProperty;
  }

  public ValueRender() {
  }

  public void setValueProperty(ProjectValueProperty valueProperty) {
    this.valueProperty = valueProperty;
  }

  @Override
  public abstract void render();

  @Override
  public Node getRenderer() {
    return control;
  }

  public abstract String getValue();

  @Override
  public void setStyle() {

  }
}