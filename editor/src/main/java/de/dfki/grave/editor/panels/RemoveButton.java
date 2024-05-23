
/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package de.dfki.grave.editor.panels;

//~--- JDK imports ------------------------------------------------------------
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import de.dfki.grave.app.Preferences;
import de.dfki.grave.util.ResourceLoader;

/**
 *
 * @author mfallas
 */
@SuppressWarnings("serial")
public class RemoveButton extends JLabel {

  private final Dimension buttonSize = new Dimension(20, 20);
  //Icons
  private final ImageIcon ICON_REMOVE_STANDARD = ResourceLoader.loadImageIcon("img/toolbar_icons/remove.png");
  private final ImageIcon ICON_REMOVE_ROLLOVER = ResourceLoader.loadImageIcon("img/toolbar_icons/remove_blue.png");

  public RemoveButton() {
    setHorizontalAlignment(SwingConstants.RIGHT);
    setOpaque(false);
    setBackground(Color.white);
    setIcon(ICON_REMOVE_STANDARD);
    setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    setToolTipText("Remove");
    setIconTextGap(10);
    setFont(Preferences.getPrefs().editorConfig.sBUTTON_FONT.getFont());
    setFocusable(false);
    setPreferredSize(buttonSize);
    setMinimumSize(buttonSize);
    addMouseListener(new java.awt.event.MouseAdapter() {

//          public void mouseClicked(java.awt.event.MouseEvent evt) {
//              //savePreferences(true);
//          }
      public void mouseEntered(MouseEvent me) {
        setIcon(ICON_REMOVE_ROLLOVER);
      }

      public void mouseExited(MouseEvent me) {
        setIcon(ICON_REMOVE_STANDARD);
      }
    });
  }
}
