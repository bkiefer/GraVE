package de.dfki.grave.editor;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Observer;

import javax.swing.JComponent;

import de.dfki.grave.editor.panels.WorkSpace;
import de.dfki.grave.model.flow.Position;
import de.dfki.grave.model.project.EditorConfig;

@SuppressWarnings("serial")
public abstract class EditorComponent extends JComponent
  implements MouseListener, Observer, Selectable {
  protected WorkSpace mWorkSpace;
  
  protected CodeArea mCodeArea = null;
  protected ObserverDocument mDocument = null;

  protected EditorConfig getEditorConfig() {
    return mWorkSpace.getEditorConfig();
  }

  public CodeArea getCodeArea() { return mCodeArea; }
  
  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }   

  // Set initial view position and size, given *model* coordinates
  public void setViewBounds(int x, int y, int width, int height) {
    // transfer to view coordinates
    int xx = mWorkSpace.toViewXPos(x), yy = mWorkSpace.toViewYPos(y);
    int w = mWorkSpace.toViewXPos(x + width) - xx;  
    int h = mWorkSpace.toViewYPos(y + height) - yy;  
    setBounds(xx, yy, w, h);
  }
  
  public void setViewLocation(int x, int y) {
    super.setLocation(mWorkSpace.toViewXPos(x), mWorkSpace.toViewYPos(y));
  }
  
  /** Convert from view to model coordinates */
  public Position toModelPos(Point val) {
    return mWorkSpace.toModelPos(val);
  }

  /** Convert from model to view coordinates */
  public Point toViewPoint(Position val) {
    return mWorkSpace.toViewPoint(val);
  }

}
