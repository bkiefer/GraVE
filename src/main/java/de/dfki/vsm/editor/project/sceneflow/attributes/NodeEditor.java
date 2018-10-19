package de.dfki.vsm.editor.project.sceneflow.attributes;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import de.dfki.vsm.editor.event.NodeSelectedEvent;
import de.dfki.vsm.model.sceneflow.chart.BasicNode;
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
class NodeEditor extends JPanel implements EventListener {

  private final NameEditor mNameEditor;

  public NodeEditor() {

    // Init the child editors
    mNameEditor = new NameEditor();


    // Init components
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    setBackground(Color.white);
    setBorder(BorderFactory.createEmptyBorder());
    add(mNameEditor);

    // Add the element editor to the event multicaster
    EventDispatcher.getInstance().register(this);
  }

  @Override
  public void update(EventObject event) {
    if (event instanceof NodeSelectedEvent) {

      // Get the selected node
      BasicNode node = ((NodeSelectedEvent) event).getNode();

    } else {

      // Do nothing
    }
  }
}