package de.dfki.grave.editor.panels;

import static de.dfki.grave.Icons.*;

import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

import de.dfki.grave.Preferences;

/**
 *
 * @author mfallas
 */
@SuppressWarnings("serial")
public class AddButton extends JLabel {

  private final Dimension buttonSize = new Dimension(20, 20);
  private int TabPos; //ONLY NECESSARY TO WORK WITH THE PLUS BUTTONS ON TABBEDPANES

  public int getTabPos() {
    return TabPos;
  }

  public void setTabPos(int TabPos) {
    this.TabPos = TabPos;
  }

  public AddButton() {
    setHorizontalAlignment(SwingConstants.RIGHT);
    setIcon(ICON_PLUS_STANDARD);
    setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    setToolTipText("Add");
    setIconTextGap(10);
    setFont(Preferences.getPrefs().editorConfig.sBUTTON_FONT.getFont());
    setFocusable(false);
    setPreferredSize(buttonSize);
    setMinimumSize(buttonSize);
    addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseEntered(MouseEvent me) {
        setIcon(ICON_PLUS_ROLLOVER);
      }

      public void mouseExited(MouseEvent me) {
        setIcon(ICON_PLUS_STANDARD);
      }
    });
  }
}
