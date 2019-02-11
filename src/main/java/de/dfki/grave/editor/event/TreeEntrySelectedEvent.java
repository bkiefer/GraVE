package de.dfki.grave.editor.event;

import de.dfki.grave.editor.TreeEntry;

/**
 *     @author Martin Fallas
 *     This event is used to know when elements of the left panel are selected
 */
public class TreeEntrySelectedEvent {

  private TreeEntry mEntry;

  public TreeEntrySelectedEvent(Object source, TreeEntry entry) {
    mEntry = entry;
  }

  public TreeEntry getmEntry() {
    return mEntry;
  }
}
