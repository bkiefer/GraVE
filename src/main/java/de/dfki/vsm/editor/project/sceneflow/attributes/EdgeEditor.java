package de.dfki.vsm.editor.project.sceneflow.attributes;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import de.dfki.vsm.editor.event.EdgeSelectedEvent;
import de.dfki.vsm.model.flow.edge.*;
import de.dfki.vsm.util.evt.EventDispatcher;
import de.dfki.vsm.util.evt.EventListener;
import de.dfki.vsm.util.evt.EventObject;

/**
 *
 *
 * @author Gregor Mehlmann
 *
 *
 */
@SuppressWarnings("serial")
class EdgeEditor extends JPanel implements EventListener {

  private final TimeOutEditor mTimeOutEditor;
  private final ConditionEditor mConditionEditor;
  private final ProbabilityEditor mProbabilityEditor;
  private final InterruptEditor mInterruptEditor;

  public EdgeEditor() {

    // Init the child editors
    mTimeOutEditor = new TimeOutEditor();
    mConditionEditor = new ConditionEditor();
    mProbabilityEditor = new ProbabilityEditor();
    mInterruptEditor = new InterruptEditor();

    // Init components
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setBackground(Color.white);
    setBorder(BorderFactory.createEmptyBorder());
    add(mTimeOutEditor);
    add(mConditionEditor);
    add(mProbabilityEditor);
    add(mInterruptEditor);
    EventDispatcher.getInstance().register(this);
  }

  @Override
  public void update(EventObject event) {
    if (event instanceof EdgeSelectedEvent) {

      // Get the selected node
      AbstractEdge edge = ((EdgeSelectedEvent) event).getEdge();

      if (edge instanceof TimeoutEdge) {
        mTimeOutEditor.setVisible(true);
      } else {
        mTimeOutEditor.setVisible(false);
      }

      if (edge instanceof GuardedEdge) {
        mConditionEditor.setVisible(true);
      } else {
        mConditionEditor.setVisible(false);
      }

      if (edge instanceof InterruptEdge) {
        mInterruptEditor.setVisible(true);
      } else {
        mInterruptEditor.setVisible(false);
      }

      if (edge instanceof RandomEdge) {
        mProbabilityEditor.setVisible(true);
      } else {
        mProbabilityEditor.setVisible(false);
      }
    } else {

      // Do nothing
    }
  }
}