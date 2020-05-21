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
public class OKButton extends JLabel {

  private final Dimension buttonSize = new Dimension(135, 30);

  public OKButton() {
    setText("OK");
    setHorizontalAlignment(SwingConstants.RIGHT);
    setOpaque(true);
    setBackground(Color.white);
    setIcon(Preferences.ICON_OK_STANDARD);
    setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    setToolTipText("OK");
    setIconTextGap(20);
    setFont(Preferences.getPrefs().editorConfig.sBUTTON_FONT.getFont());
    setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
    setPreferredSize(buttonSize);
    setMinimumSize(buttonSize);
    addMouseListener(new java.awt.event.MouseAdapter() {

//          public void mouseClicked(java.awt.event.MouseEvent evt) {
//              //savePreferences(true);
//          }
      @Override
      public void mouseEntered(MouseEvent me) {
        if (isEnabled()) {
          setIcon(Preferences.ICON_OK_ROLLOVER);
          setBackground(new Color(82, 127, 255));
        }
      }

      @Override
      public void mouseExited(MouseEvent me) {
        setIcon(Preferences.ICON_OK_STANDARD);
        setBackground(new Color(255, 255, 255));
      }
    });
  }
}
