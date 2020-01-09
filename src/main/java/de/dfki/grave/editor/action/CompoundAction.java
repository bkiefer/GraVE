
/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package de.dfki.grave.editor.action;

import java.util.List;

import de.dfki.grave.editor.panels.WorkSpace;

/**
 * @author Bernd Kiefer
 *
 * For all actions that modify the shape of an edge only with some function,
 * not source or target node
 */
public class CompoundAction extends EditorAction {

  List<EditorAction> actions;
  String message;

  public CompoundAction(WorkSpace workSpace, List<EditorAction> acts,
      String msg) {
    mWorkSpace = workSpace;
    actions = acts;
    message = msg;
  }

  @Override
  protected void undoIt() {
    for (EditorAction action : actions) {
      action.undoIt();
    }
  }

  @Override
  protected void doIt() {
    for (EditorAction action : actions) {
      action.doIt();
    }
  }

  @Override
  protected String msg() {
    return message;
  }
}
