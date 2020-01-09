package de.dfki.grave.util.extensions.value;

/**
 * Created by alvaro on 4/20/17.
 */
public interface PropertyValue {

  void render();

  boolean validate();

  String getValue();
}
