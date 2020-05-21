package de.dfki.grave.editor.dialog;

//~--- JDK imports ------------------------------------------------------------
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import de.dfki.grave.Preferences;

/**
 *
 * @author mfallas
 */
@SuppressWarnings("serial")
public class CancelButton extends JLabel {

  private final Dimension buttonSize = new Dimension(125, 30);

  public CancelButton() {
    setText("Cancel");
    setHorizontalAlignment(SwingConstants.RIGHT);
    setOpaque(true);
    setBackground(Color.white);
    setIcon(Preferences.ICON_CANCEL_STANDARD);
    setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    setToolTipText("Cancel");
    setIconTextGap(10);
    setFont(Preferences.getPrefs().editorConfig.sBUTTON_FONT.getFont());
    setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
    setPreferredSize(buttonSize);
    setMinimumSize(buttonSize);
    addMouseListener(new java.awt.event.MouseAdapter() {

//          public void mouseClicked(java.awt.event.MouseEvent evt) {
//              //savePreferences(true);
//          }
      public void mouseEntered(MouseEvent me) {
        if (isEnabled()) {
          setIcon(Preferences.ICON_CANCEL_ROLLOVER);
          setBackground(new Color(82, 127, 255));
        }
      }

      public void mouseExited(MouseEvent me) {
        setIcon(Preferences.ICON_CANCEL_STANDARD);
        setBackground(new Color(255, 255, 255));
      }
    });
  }
}
