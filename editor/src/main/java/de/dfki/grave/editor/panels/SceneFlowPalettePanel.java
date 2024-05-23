package de.dfki.grave.editor.panels;

import static de.dfki.grave.Icons.*;
import static de.dfki.grave.Preferences.getPrefs;

//~--- JDK imports ------------------------------------------------------------
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.awt.event.MouseAdapter;
import java.io.IOException;

import javax.swing.*;

import de.dfki.grave.editor.Comment;
import de.dfki.grave.model.flow.*;

/**
 * @author Sergio Soto
 * @author Gregor Mehlmann
 *
 * Provides graphical elements to create new objects in the workspace
 */
@SuppressWarnings("serial")
public class SceneFlowPalettePanel extends JPanel {
  public final class PaletteItem extends JLabel implements Transferable {

    private final Object mDragableData;

    // Drag & drop support
    private final DragSource mDragSource;

    // Create a sceneflow element item
    public PaletteItem(final String text, final String info,
        final ImageIcon stdIcon, final ImageIcon rollIcon,
        final ImageIcon dragIcon, final Object data) {
      //ICONS
      mDragableData = data;
      //setContentAreaFilled(false);
      setFocusable(false);
      setOpaque(false);

      // to be transferred in the Drag
      TransferHandler mTransferHandler = new TransferHandler(text);
      setTransferHandler(mTransferHandler);
      setHorizontalAlignment(JLabel.CENTER);
      setToolTipText(text + ": " + info);
      setPreferredSize(getPrefs().SF_PALETTEITEM_SIZE);
      setMinimumSize(getPrefs().SF_PALETTEITEM_SIZE);
      setMaximumSize(getPrefs().SF_PALETTEITEM_SIZE);
      setIcon(stdIcon);
      addMouseListener(new MouseAdapter() {

        @Override
        public void mouseEntered(java.awt.event.MouseEvent evt) {
          setIcon(rollIcon);
        }

        @Override
        public void mouseExited(java.awt.event.MouseEvent evt) {
          setIcon(stdIcon);
        }
      });

      // TODO: CURRENTLY, WE DON'T OVERRIDE ANYTHING HERE
      final DragSourceListener mDragSourceListener = new DragSourceAdapter() {
      };

      final DragGestureListener mDragGestureListener = new DragGestureListener() {
        @Override
        public void dragGestureRecognized(DragGestureEvent event) {
          Image cursorIcon = dragIcon.getImage();
          Cursor cur = Toolkit.getDefaultToolkit()
              .createCustomCursor(cursorIcon, new Point(10, 10), text);
          mDragSource.startDrag(event, cur,
              (PaletteItem) event.getComponent(), mDragSourceListener);
        }
      };
      // The Drag will copy the DnDButton rather than moving it
      mDragSource = new DragSource();
      mDragSource.createDefaultDragGestureRecognizer(this,
          DnDConstants.ACTION_COPY, mDragGestureListener);
    }

    // Get the data for a drag & drop operation
    @Override
    public final Object getTransferData(final DataFlavor flavor)
        throws UnsupportedFlavorException, IOException {
      return mDragableData;
    }

    // Generally support all d&d data flavours
    @Override
    public final boolean isDataFlavorSupported(final DataFlavor flavor) {
      return true;
    }

    //
    @Override
    public final DataFlavor[] getTransferDataFlavors() {
      DataFlavor[] df = {};
      return df;
    }

  }

  private final PaletteItem[] items = {
      new PaletteItem("Super Node", "Holds Sub-Scences flow",
          ICON_SUPERNODE_STANDARD,
          ICON_SUPERNODE_ROLLOVER,
          ICON_SUPERNODE_DRAGGING,  new SuperNode()),

      new PaletteItem("Basic Node", "Holds Scenes Actions",
          ICON_BASICNODE_STANDARD,
          ICON_BASICNODE_ROLLOVER,
          ICON_BASICNODE_DRAGGING,  new BasicNode()),

      new PaletteItem("Comment", "Adds a Comment",
          ICON_COMMENT_ENTRY_STANDARD,
          ICON_COMMENT_ENTRY_ROLLOVER,
          ICON_COMMENT_ENTRY_DRAGGING, new Comment()),

      new PaletteItem("Epsilon Edge", "Creates Epsilon Transition",
          ICON_EEDGE_ENTRY_STANDARD,
          ICON_EEDGE_ENTRY_ROLLOVER,
          ICON_EEDGE_ENTRY_DRAGGING, new EpsilonEdge()),

      new PaletteItem("Probability Edge", "Creates Probability Transition",
          ICON_PEDGE_ENTRY_STANDARD,
          ICON_PEDGE_ENTRY_ROLLOVER,
          ICON_PEDGE_ENTRY_DRAGGING, new RandomEdge()),

      new PaletteItem("Fork Edge", "Creates Forked Transition",
          ICON_FEDGE_ENTRY_STANDARD,
          ICON_FEDGE_ENTRY_ROLLOVER,
          ICON_FEDGE_ENTRY_DRAGGING, new ForkingEdge()),

      new PaletteItem("Conditional Edge", "Creates Conditional Transition",
          ICON_CEDGE_ENTRY_STANDARD,
          ICON_CEDGE_ENTRY_ROLLOVER,
          ICON_CEDGE_ENTRY_DRAGGING, new GuardedEdge()),

      new PaletteItem("Timeout Edge", "Creates Timeout Transition",
          ICON_TEDGE_ENTRY_STANDARD,
          ICON_TEDGE_ENTRY_ROLLOVER,
          ICON_TEDGE_ENTRY_DRAGGING, new TimeoutEdge()),

      new PaletteItem("Interruptive Edge", "Creates Interruptive Transition",
          ICON_IEDGE_ENTRY_STANDARD,
          ICON_IEDGE_ENTRY_ROLLOVER,
          ICON_IEDGE_ENTRY_DRAGGING, new InterruptEdge())
  };

  //
  private final int paletteDimension = 230;

  // Construct the tool panel
  public SceneFlowPalettePanel() {
    setLayout(new GridLayout(0, 3));
    setBackground(Color.WHITE);
    setPreferredSize(new Dimension(paletteDimension, paletteDimension));
    setMinimumSize(new Dimension(paletteDimension, paletteDimension));
    setMaximumSize(new Dimension(paletteDimension, paletteDimension));
    //setBorder(BorderFactory.createEtchedBorder());
    for (PaletteItem item : items) {
      add(item);
    }
    ToolTipManager.sharedInstance().registerComponent(this);
  }
}
