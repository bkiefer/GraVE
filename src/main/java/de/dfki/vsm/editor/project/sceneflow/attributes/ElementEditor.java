package de.dfki.vsm.editor.project.sceneflow.attributes;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

//~--- non-JDK imports --------------------------------------------------------
import com.sun.java.swing.plaf.windows.WindowsScrollBarUI;

import de.dfki.vsm.editor.event.EdgeSelectedEvent;
import de.dfki.vsm.editor.event.NodeSelectedEvent;
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
public class ElementEditor extends JScrollPane implements EventListener {

  private final NodeEditor mNodeEditor;
  private final EdgeEditor mEdgeEditor;

  public ElementEditor() {

    // Init node editor and edge editor
    mNodeEditor = new NodeEditor();
    mEdgeEditor = new EdgeEditor();

    //
    // Init the scrollpane attributes
    setPreferredSize(new Dimension(260, 500));
    setMinimumSize(new Dimension(260, 500));
    setBorder(BorderFactory.createEtchedBorder());
    setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    getVerticalScrollBar().setUI(new WindowsScrollBarUI());
    getViewport().setOpaque(false);
    setOpaque(false);

    // Set the initial viewport to null
    setViewportView(null);

    // Add the element editor to the event multicaster
    EventDispatcher.getInstance().register(this);
  }

  public final void refresh() {

    // Print some information
    //mLogger.message("Refreshing '" + this + "'");
  }

  @Override
  public void update(EventObject event) {
    if (event instanceof NodeSelectedEvent) {

      // Update the node of the node editor
      // mNodeEditor.update(((NodeSelectedEvent) event).getNode());
      setViewportView(mNodeEditor);
    } else if (event instanceof EdgeSelectedEvent) {

      // Update the edge of the edge editor
      // mEdgeEditor.update(((EdgeSelectedEvent) event).getEdge());
      setViewportView(mEdgeEditor);
    } else {

      // Do nothing
    }
  }
}
