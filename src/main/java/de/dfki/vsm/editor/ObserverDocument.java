/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.vsm.editor;

import de.dfki.vsm.model.flow.Code;
import de.dfki.vsm.model.flow.Expression;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

/**
 *
 * @author Anna Welker
 */
public class ObserverDocument extends RSyntaxDocument implements Observer {

  public ObserverDocument(String syntaxStyle) {
    super(syntaxStyle);
  }

  public ObserverDocument() {
    super(SyntaxConstants.SYNTAX_STYLE_JAVA);
  }

  @Override
  public void update(Observable o, Object o1) {
    try {
      if (o instanceof Code) {
          insertString(0, ((Code)o).getContent(), null);
      } else if (o instanceof Expression) {
        insertString(0, ((Expression)o).getContent(), null);
      }
    } catch (BadLocationException ex) {
      Logger.getLogger(ObserverDocument.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

}
