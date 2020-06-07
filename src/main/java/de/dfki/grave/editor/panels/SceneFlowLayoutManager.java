
package de.dfki.grave.editor.panels;

//~--- JDK imports ------------------------------------------------------------
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;

/**
 *
 * @author Patrick Gebhard
 */
/**
 * Use the LayoutManager facility to compute sizes
 */
public class SceneFlowLayoutManager implements LayoutManager {
  @Override
  public void addLayoutComponent(String name, Component comp) {}
  @Override
  public void removeLayoutComponent(Component comp) {}
  @Override
  public void layoutContainer(Container parent) { }

  public Dimension preferredLayoutSize(Container parent) {
    Dimension size = new Dimension(0, 0);
    for (Component c : parent.getComponents()) {
      if (c.getLocation().x > size.getWidth() - c.getWidth())
        size.setSize(c.getLocation().x + c.getWidth(), size.getHeight());
      if (c.getLocation().y > size.getHeight() - c.getHeight())
        size.setSize(size.getWidth(), c.getLocation().y + c.getHeight());
    }
    return size;
  }

  public Dimension minimumLayoutSize(Container parent) {
    return preferredLayoutSize(parent);
  }
}
