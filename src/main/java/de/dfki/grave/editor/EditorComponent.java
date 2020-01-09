package de.dfki.grave.editor;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Observer;

import javax.swing.JComponent;

import de.dfki.grave.editor.panels.WorkSpace;
import de.dfki.grave.model.project.EditorConfig;

@SuppressWarnings("serial")
public abstract class EditorComponent extends JComponent
  implements MouseListener, Observer, Selectable {
  protected WorkSpace mWorkSpace;

  protected EditorConfig getEditorConfig() {
    return mWorkSpace.getEditorConfig();
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }
}
