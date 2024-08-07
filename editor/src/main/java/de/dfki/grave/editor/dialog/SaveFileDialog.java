/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.grave.editor.dialog;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.image.RenderedImage;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import de.dfki.grave.app.AppFrame;
import de.dfki.grave.editor.panels.ProjectEditorToolBar;

/**
 *
 * @author mfallas
 */
@SuppressWarnings("serial")
public class SaveFileDialog extends JFileChooser {

  public SaveFileDialog() {
    setCurrentDirectory(new File(System.getProperty("user.home")));
    setFileSelectionMode(JFileChooser.FILES_ONLY);
    //addChoosableFileFilter(new FileNameExtensionFilter("Images", "jpg", "png", "gif", "bmp"));
  }

  public boolean save() {
    int userSelection = showSaveDialog(AppFrame.getInstance());
    if (userSelection == JFileChooser.APPROVE_OPTION) {
      try {
        File fileToSave = getSelectedFile();
        String path = fileToSave.getAbsolutePath();
        if (!path.matches(".*\\.(jpg|jpeg|png|bmp|gif)")) {
          // If the extension isn't there. We need to replace it
          path = path.concat(".jpg");
          fileToSave = new File(path);
          System.out.println(path);
        }
        Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
        RenderedImage image = (RenderedImage) t.getTransferData(DataFlavor.imageFlavor);
        boolean isSuccess = ImageIO.write(image, "png", fileToSave);
        //isSuccess ? return true : return false;
      } catch (Exception ex) {
        Logger.getLogger(ProjectEditorToolBar.class.getName()).log(Level.SEVERE, null, ex);
      }
      return true;
    }
    return false;
  }

}
