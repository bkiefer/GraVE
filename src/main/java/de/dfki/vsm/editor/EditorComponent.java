package de.dfki.vsm.editor;

import java.awt.event.MouseListener;
import java.util.Observer;

import javax.swing.JComponent;

@SuppressWarnings("serial")
public abstract class EditorComponent extends JComponent
  implements MouseListener, Observer, Selectable {

}
