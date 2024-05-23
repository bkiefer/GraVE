package de.dfki.grave.app;

import java.awt.Image;

import javax.swing.ImageIcon;

import de.dfki.grave.util.ResourceLoader;

public abstract class Icons {
  //////////////////////////////////////////////////////////////////////////////
  // IMAGE RESSOURCES
  //////////////////////////////////////////////////////////////////////////////
  
  // THE APP ICON
  public static final ImageIcon ICON_FILE = ResourceLoader.loadImageIcon("img/icon.png");
  
  public static final ImageIcon ICON_LOGO = ResourceLoader.loadImageIcon("img/smlogo.png");
  public static final ImageIcon ICON_DOC = ResourceLoader.loadImageIcon("img/docicon.png");
  public static final ImageIcon ICON_SHOW_GRID = ResourceLoader.loadImageIcon("img/grid.png");
  public static final ImageIcon ICON_VISUALISATION = ResourceLoader.loadImageIcon("img/visualisation.png");
  //SUPERNODE
  public static final ImageIcon ICON_SUPERNODE_STANDARD = ResourceLoader.loadImageIcon("img/workspace_toolbar/SUPERNODE_ENTRY.png");
  public static final ImageIcon ICON_SUPERNODE_ROLLOVER = ResourceLoader.loadImageIcon("img/workspace_toolbar/SUPERNODE_ENTRY_BLUE.png");
  public static final ImageIcon ICON_SUPERNODE_DRAGGING = ResourceLoader.loadImageIcon("img/workspace_toolbar/SUPERNODE_ENTRY_SMALL.png");
  //BASIC NODE
  public static final ImageIcon ICON_BASICNODE_STANDARD = ResourceLoader.loadImageIcon("img/workspace_toolbar/BASICNODE_ENTRY.png");
  public static final ImageIcon ICON_BASICNODE_ROLLOVER = ResourceLoader.loadImageIcon("img/workspace_toolbar/BASICNODE_ENTRY_BLUE.png");
  public static final ImageIcon ICON_BASICNODE_DRAGGING = ResourceLoader.loadImageIcon("img/workspace_toolbar/BASICNODE_ENTRY_SMALL.png");
  //COMMENT
  public static final ImageIcon ICON_COMMENT_ENTRY_STANDARD = ResourceLoader.loadImageIcon("img/workspace_toolbar/COMMENT_ENTRY.png");
  public static final ImageIcon ICON_COMMENT_ENTRY_ROLLOVER = ResourceLoader.loadImageIcon("img/workspace_toolbar/COMMENT_ENTRY_BLUE.png");
  public static final ImageIcon ICON_COMMENT_ENTRY_DRAGGING = ResourceLoader.loadImageIcon("img/workspace_toolbar/COMMENT_ENTRY_SMALL.png");
  //EPSILON EDGE
  public static final ImageIcon ICON_EEDGE_ENTRY_STANDARD = ResourceLoader.loadImageIcon("img/workspace_toolbar/EEDGE_ENTRY.png");
  public static final ImageIcon ICON_EEDGE_ENTRY_ROLLOVER = ResourceLoader.loadImageIcon("img/workspace_toolbar/EEDGE_ENTRY_BLUE.png");
  public static final ImageIcon ICON_EEDGE_ENTRY_DRAGGING = ResourceLoader.loadImageIcon("img/workspace_toolbar/EEDGE_ENTRY_SMALL.png");
  //TIMEOUT EDGE
  public static final ImageIcon ICON_TEDGE_ENTRY_STANDARD = ResourceLoader.loadImageIcon("img/workspace_toolbar/TEDGE_ENTRY.png");
  public static final ImageIcon ICON_TEDGE_ENTRY_ROLLOVER = ResourceLoader.loadImageIcon("img/workspace_toolbar/TEDGE_ENTRY_BLUE.png");
  public static final ImageIcon ICON_TEDGE_ENTRY_DRAGGING = ResourceLoader.loadImageIcon("img/workspace_toolbar/TEDGE_ENTRY_SMALL.png");
  //PROBABILISTIC EDGE
  public static final ImageIcon ICON_PEDGE_ENTRY_STANDARD = ResourceLoader.loadImageIcon("img/workspace_toolbar/PEDGE_ENTRY.png");
  public static final ImageIcon ICON_PEDGE_ENTRY_ROLLOVER = ResourceLoader.loadImageIcon("img/workspace_toolbar/PEDGE_ENTRY_BLUE.png");
  public static final ImageIcon ICON_PEDGE_ENTRY_DRAGGING = ResourceLoader.loadImageIcon("img/workspace_toolbar/PEDGE_ENTRY_SMALL.png");
  //CONDITIONAL EDGE
  public static final ImageIcon ICON_CEDGE_ENTRY_STANDARD = ResourceLoader.loadImageIcon("img/workspace_toolbar/CEDGE_ENTRY.png");
  public static final ImageIcon ICON_CEDGE_ENTRY_ROLLOVER = ResourceLoader.loadImageIcon("img/workspace_toolbar/CEDGE_ENTRY_BLUE.png");
  public static final ImageIcon ICON_CEDGE_ENTRY_DRAGGING = ResourceLoader.loadImageIcon("img/workspace_toolbar/CEDGE_ENTRY_SMALL.png");
  //INTERRUPTIVE EDGE
  public static final ImageIcon ICON_IEDGE_ENTRY_STANDARD = ResourceLoader.loadImageIcon("img/workspace_toolbar/IEDGE_ENTRY.png");
  public static final ImageIcon ICON_IEDGE_ENTRY_ROLLOVER = ResourceLoader.loadImageIcon("img/workspace_toolbar/IEDGE_ENTRY_BLUE.png");
  public static final ImageIcon ICON_IEDGE_ENTRY_DRAGGING = ResourceLoader.loadImageIcon("img/workspace_toolbar/IEDGE_ENTRY_SMALL.png");
  //FORK EDGE
  public static final ImageIcon ICON_FEDGE_ENTRY_STANDARD = ResourceLoader.loadImageIcon("img/workspace_toolbar/FEDGE_ENTRY.png");
  public static final ImageIcon ICON_FEDGE_ENTRY_ROLLOVER = ResourceLoader.loadImageIcon("img/workspace_toolbar/FEDGE_ENTRY_BLUE.png");
  public static final ImageIcon ICON_FEDGE_ENTRY_DRAGGING = ResourceLoader.loadImageIcon("img/workspace_toolbar/FEDGE_ENTRY_SMALL.png");
  //
  public static final ImageIcon ICON_ROOT_FOLDER = ResourceLoader.loadImageIcon("img/elementtree/ROOT_FOLDER.png");
  public static final ImageIcon ICON_SCENE_FOLDER = ResourceLoader.loadImageIcon("img/elementtree/SCENE_FOLDER.png");
  public static final ImageIcon ICON_BASIC_FOLDER = ResourceLoader.loadImageIcon("img/elementtree/BASIC_FOLDER.png");
  public static final ImageIcon ICON_RADIOBUTTON_UNSELECTED = ResourceLoader.loadImageIcon("img/elementtree/RADIOBUTTON_UNSELECTED.png");
  public static final ImageIcon ICON_RADIOBUTTON_SELECTED = ResourceLoader.loadImageIcon("img/elementtree/RADIOBUTTON_SELECTED.png");
  public static final ImageIcon ICON_FUNCTION_ENTRY = ResourceLoader.loadImageIcon("img/elementtree/FUNCTION_ENTRY.png");
  public static final ImageIcon ICON_FUNCTION_ERROR_ENTRY = ResourceLoader.loadImageIcon("img/elementtree/FUNCTION_ERROR_ENTRY.png");
  //MORE ICON
  public static final ImageIcon ICON_MORE_STANDARD = ResourceLoader.loadImageIcon("img/toolbar_icons/more.png");
  public static final ImageIcon ICON_MORE_ROLLOVER = ResourceLoader.loadImageIcon("img/toolbar_icons/more_blue.png");
  //LESS ICON
  public static final ImageIcon ICON_LESS_STANDARD = ResourceLoader.loadImageIcon("img/toolbar_icons/less.png");
  public static final ImageIcon ICON_LESS_ROLLOVER = ResourceLoader.loadImageIcon("img/toolbar_icons/less_blue.png");
  //PLUS ICON
  public static final ImageIcon ICON_PLUS_STANDARD = ResourceLoader.loadImageIcon("img/toolbar_icons/add.png");
  public static final ImageIcon ICON_PLUS_ROLLOVER = ResourceLoader.loadImageIcon("img/toolbar_icons/add_blue.png");
  public static final ImageIcon ICON_PLUS_DISABLED = ResourceLoader.loadImageIcon("img/toolbar_icons/add_disabled.png");
  //MINUS ICON
  public static final ImageIcon ICON_MINUS_STANDARD = ResourceLoader.loadImageIcon("img/toolbar_icons/remove.png");
  public static final ImageIcon ICON_MINUS_ROLLOVER = ResourceLoader.loadImageIcon("img/toolbar_icons/remove_blue.png");
  public static final ImageIcon ICON_MINUS_DISABLED = ResourceLoader.loadImageIcon("img/toolbar_icons/remove_disabled.png");
  //EDIT ICON
  public static final ImageIcon ICON_EDIT_STANDARD = ResourceLoader.loadImageIcon("img/toolbar_icons/edit.png");
  public static final ImageIcon ICON_EDIT_ROLLOVER = ResourceLoader.loadImageIcon("img/toolbar_icons/edit_blue.png");
  //UP ICON
  public static final ImageIcon ICON_UP_STANDARD = ResourceLoader.loadImageIcon("img/toolbar_icons/up_20.png");
  public static final ImageIcon ICON_UP_ROLLOVER = ResourceLoader.loadImageIcon("img/toolbar_icons/up_20_blue.png");
  public static final ImageIcon ICON_UP_DISABLED = ResourceLoader.loadImageIcon("img/toolbar_icons/up_20_disabled.png");
  //DOWN ICON
  public static final ImageIcon ICON_DOWN_STANDARD = ResourceLoader.loadImageIcon("img/toolbar_icons/down.png");
  public static final ImageIcon ICON_DOWN_ROLLOVER = ResourceLoader.loadImageIcon("img/toolbar_icons/down_blue.png");
  public static final ImageIcon ICON_DOWN_DISABLED = ResourceLoader.loadImageIcon("img/toolbar_icons/down_disabled.png");
  //CANCEL ICONS
  public static final ImageIcon ICON_CANCEL_STANDARD = ResourceLoader.loadImageIcon("img/cancel_icon_gray.png");
  public static final ImageIcon ICON_CANCEL_ROLLOVER = ResourceLoader.loadImageIcon("img/cancel_icon_blue.png");
  public static final ImageIcon ICON_CANCEL_STANDARD_TINY = ResourceLoader.loadImageIcon("img/cancel_icon_gray_tiny.png");
  public static final ImageIcon ICON_CANCEL_ROLLOVER_TINY = ResourceLoader.loadImageIcon("img/cancel_icon_blue_tiny.png");
  //OK ICONS
  public static final ImageIcon ICON_OK_STANDARD = ResourceLoader.loadImageIcon("img/ok_icon_gray.png");
  public static final ImageIcon ICON_OK_ROLLOVER = ResourceLoader.loadImageIcon("img/ok_icon_blue.png");
  //BACK ICONS
  public static final ImageIcon ICON_PREVIOUS_STANDARD = ResourceLoader.loadImageIcon("img/back_icon_gray.png");
  public static final ImageIcon ICON_PREVIOUS_ROLLOVER = ResourceLoader.loadImageIcon("img/back_icon_blue.png");
  //NEXT ICONS
  public static final ImageIcon ICON_NEXT_STANDARD = ResourceLoader.loadImageIcon("img/next_icon_gray.png");
  public static final ImageIcon ICON_NEXT_ROLLOVER = ResourceLoader.loadImageIcon("img/next_icon_blue.png");
  //BACKGROUND WELCOME
  public static final Image BACKGROUND_IMAGE = ResourceLoader.loadImageIcon("img/icon_big.png").getImage();   // Background for the welcome screen
}
