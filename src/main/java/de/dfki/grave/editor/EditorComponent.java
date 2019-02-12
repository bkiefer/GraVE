package de.dfki.grave.editor;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Observer;

import javax.swing.JComponent;

import de.dfki.grave.editor.panels.WorkSpace;
import de.dfki.grave.model.flow.geom.Position;
import de.dfki.grave.model.project.EditorConfig;

@SuppressWarnings("serial")
public abstract class EditorComponent extends JComponent
  implements MouseListener, Observer, Selectable {
  protected WorkSpace mWorkSpace;

  protected EditorConfig getEditorConfig() {
    return mWorkSpace.getEditorConfig();
  }

  protected int zoom(int val) {
    return (int)(val * mWorkSpace.mZoomFactor);
  }

  protected int unzoom(int val) {
    return (int)(val / mWorkSpace.mZoomFactor);
  }

  protected Point zoom(Point val) {
    return new Point((int)(val.x * mWorkSpace.mZoomFactor),
        (int)(val.y * mWorkSpace.mZoomFactor));
  }

  protected Point unzoom(Point val) {
    return new Point((int)(val.x / mWorkSpace.mZoomFactor),
        (int)(val.y / mWorkSpace.mZoomFactor));
  }

  protected Position zoom(Position val) {
    return new Position((int)(val.getXPos() * mWorkSpace.mZoomFactor),
        (int)(val.getYPos() * mWorkSpace.mZoomFactor));
  }

  /*
  protected Position unzoom(Position val) {
    return new Position((int)(val.getXPos() / mWorkSpace.mZoomFactor),
        (int)(val.getYPos() / mWorkSpace.mZoomFactor));
  }
  */

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }
}
