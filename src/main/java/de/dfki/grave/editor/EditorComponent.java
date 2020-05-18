package de.dfki.grave.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Observer;

import javax.swing.JComponent;

import de.dfki.grave.editor.event.ElementSelectedEvent;
import de.dfki.grave.editor.panels.WorkSpace;
import de.dfki.grave.model.flow.ContentHolder;
import de.dfki.grave.model.flow.Position;
import de.dfki.grave.model.project.EditorConfig;
import de.dfki.grave.util.evt.EventDispatcher;

@SuppressWarnings("serial")
public abstract class EditorComponent extends JComponent
  implements MouseListener, Observer, DocumentContainer {
  private final EventDispatcher mDispatcher = EventDispatcher.getInstance();
  
  protected WorkSpace mWorkSpace;
  
  private CodeArea mCodeArea = null;
  protected ObserverDocument mDocument = null;
  
  // edit panel
  protected boolean mSelected = false;
  
  protected EditorConfig getEditorConfig() {
    return mWorkSpace.getEditorConfig();
  }

  protected void initCodeArea(ContentHolder ch, Color col) {
    //if (getDescription() == null) return;
    // TODO: ACTIVATE AFTER FIXING CODEEDITOR.SETEDITEDOBJECT

    if (ch != null) {
      mDocument = new ObserverDocument(ch);
      mCodeArea = new CodeArea(this, 
          new Font("Monospaced", Font.ITALIC,
              getEditorConfig().sWORKSPACEFONTSIZE), col);
      /*
    mTextArea.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "enter");
    mTextArea.getActionMap().put("enter", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setDeselected();
        mDispatcher.convey(new ElementSelectedEvent(null));
        updateFromTextEditor();
      }
    });
    mTextArea.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "escape");
    mTextArea.getActionMap().put("escape", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        setDeselected();
      }
    });
    */
    } else {
      mCodeArea = null;
      mDocument = null;
    }
  }
  
  public CodeArea getCodeArea() { return mCodeArea; }
  
  abstract Point getCodeAreaLocation(Dimension r);
  
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

  public ObserverDocument getDoc() {
    return mDocument;
  }
  
  public void setSelected() {
    mSelected = true;
    if (mCodeArea != null) {
      mCodeArea.setSelected();
    }      
    mDispatcher.convey(new ElementSelectedEvent(this));
    repaint(100);
  }

  public void setDeselected() {
    if (mCodeArea != null && mSelected) {
      mCodeArea.setDeselected();
    }
    mSelected = false;
    repaint(100);
  }
}
