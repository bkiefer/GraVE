/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.dfki.vsm.editor.event;

import de.dfki.vsm.editor.project.WorkSpace;

/**
 *
 * @author Anna Welker
 */
public class ClearCodeEditorEvent {

  public WorkSpace panel;

  public ClearCodeEditorEvent(WorkSpace workSpace) {
    panel = workSpace;
  }

}
