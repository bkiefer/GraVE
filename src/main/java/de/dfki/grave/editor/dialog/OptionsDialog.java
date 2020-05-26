package de.dfki.grave.editor.dialog;

//~--- JDK imports ------------------------------------------------------------
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;

import javax.swing.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.grave.AppFrame;
import de.dfki.grave.editor.event.ProjectChangedEvent;
import de.dfki.grave.model.project.EditorConfig;
import de.dfki.grave.model.project.FontConfig;
import de.dfki.grave.util.evt.EventDispatcher;
import say.swing.JFontChooser;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
@SuppressWarnings("serial")
public class OptionsDialog extends JDialog {

  private final Logger mLogger = LoggerFactory.getLogger(OptionsDialog.class);;
  private final AppFrame mEditor = AppFrame.getInstance();

  private EditorConfig mCurrentConfig;
  private EditorConfig mEditorConfig;

  public OptionsDialog() {
    super(AppFrame.getInstance(), "Preferences", false);
    AppFrame.getInstance();
    AppFrame.addEscapeListener(this);
    mEditorConfig = 
        mEditor.getSelectedProjectEditor().getEditorProject().getEditorConfig();
    // to enable cancel 
    mCurrentConfig = mEditorConfig.copy();
    initComponents();
  }

  private void initComponents() {
    setBackground(Color.white);

    // For the preferences proper
    JPanel prefs = new JPanel();
    prefs.setLayout(new BoxLayout(prefs, BoxLayout.Y_AXIS));
    prefs.add(initGraphicsPanel());
    prefs.add(initFontPanel());

    // for the OK and Cancel buttons
    JPanel buttons = new JPanel();
    buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
    buttons.setOpaque(false);
    OKButton mOkButton = new OKButton();
    mOkButton.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        saveEditorConfig(true);
      }
    });
    CancelButton mCancelButton = new CancelButton();
    mCancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        mEditor.getSelectedProjectEditor().getEditorProject()
          .setEditorConfig(mCurrentConfig);
        dispose();
      }
    });
    buttons.add(Box.createHorizontalGlue());
    buttons.add(mCancelButton);
    buttons.add(Box.createRigidArea(new Dimension(40, 60)));
    buttons.add(mOkButton);
    buttons.add(Box.createRigidArea(new Dimension(5, 0)));
    
    JPanel main = new JPanel();
    main.setBackground(Color.white);
    main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
    main.add(prefs);
    main.add(buttons);

    KeyboardFocusManager.getCurrentKeyboardFocusManager()
      .addKeyEventDispatcher(new KeyEventDispatcher() {
      
      @Override
      public boolean dispatchKeyEvent(KeyEvent ke) {
        //boolean keyHandled = false;
        if (ke.getID() == KeyEvent.KEY_PRESSED && 
            ke.getKeyCode() == KeyEvent.VK_ENTER) {
          saveEditorConfig(true);
        }
        return false;
      }
    });
    add(main);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setResizable(false);
    this.setModal(true);
    pack();
    setLocation(getParent().getLocation().x + (getParent().getWidth() - getWidth()) / 2,
            getParent().getLocation().y + (getParent().getHeight() - getHeight()) / 2);
  }
  
  private JFormattedTextField getTextField(JSpinner spinner) {
    JComponent editor = spinner.getEditor();
    if (editor instanceof JSpinner.DefaultEditor) {
      return ((JSpinner.DefaultEditor)editor).getTextField();
    } else {
      mLogger.error("Unexpected editor type: "
          + spinner.getEditor().getClass() + " not a descendant of DefaultEditor");
      return null;
    }
  }
  
  private JSpinner createSpinner(JComponent parent, GridBagConstraints c, 
      String label, float min, float max, float incr) {
    c.gridx = 1;
    c.anchor = GridBagConstraints.LINE_START;
    parent.add(new JLabel(label), c);
    JSpinner result = new JSpinner(new SpinnerNumberModel(min, min, max, incr));
    JTextField tf = getTextField(result); 
    tf.setEditable(false);
    tf.setColumns(4);
    c.gridx = 2;
    c.anchor = GridBagConstraints.LINE_END;
    parent.add(result, c);
    return result;
  } 
  
  private JPanel initGraphicsPanel() {
    JPanel grpan = new JPanel();
    grpan.setBackground(Color.white);
    grpan.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.LINE_START;
    grpan.setBorder(BorderFactory.createTitledBorder(" Visual Appearance "));

    c.gridx = 0; // col zero
    JCheckBox dgcb = new JCheckBox("Draw Grid", mEditorConfig.sSHOWGRID);
    dgcb.setPreferredSize(new Dimension(200, 24));
    dgcb.setOpaque(false);
    dgcb.addItemListener((e) -> {
      mEditorConfig.sSHOWGRID = (e.getStateChange() == ItemEvent.SELECTED);
    });
    c.gridy = 0;
    grpan.add(dgcb, c);

    JCheckBox dicb = new JCheckBox("Draw Node ID", mEditorConfig.sSHOWIDSOFNODES);
    dicb.setOpaque(false);
    dicb.addItemListener((e) -> {
      mEditorConfig.sSHOWIDSOFNODES = (e.getStateChange() == ItemEvent.SELECTED);
      mEditor.refresh();
    });
    c.gridy += 1;
    grpan.add(dicb, c);
    
    JCheckBox sgcb = new JCheckBox("Snap To Grid", mEditorConfig.sSNAPTOGRID);
    sgcb.setOpaque(false);
    sgcb.addItemListener((e) -> {
      mEditorConfig.sSNAPTOGRID = (e.getStateChange() == ItemEvent.SELECTED);
      mEditor.refresh();
    });
    c.gridy += 1;
    grpan.add(sgcb, c);
    
    /* TODO: maybe reactivate
    mShowVariablesCheckBox = new JCheckBox("Show Variables", true);
    mShowVariablesCheckBox.setOpaque(false);
    mShowVariablesCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        saveEditorConfig(false);
        mEditor.refresh();
      }
    });
    mShowSmartPathDebugCheckBox = new JCheckBox("Show Smart Path Calculation", false);
    mShowSmartPathDebugCheckBox.setOpaque(false);
    mShowSmartPathDebugCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        saveEditorConfig(false);
        mEditor.refresh();
      }
    });
    */

    c.gridy = 0;
    JSpinner nsspin = createSpinner(grpan, c, "Node Size:", 20, 200, 2);
    ((SpinnerNumberModel) nsspin.getModel()).setValue(
        mEditorConfig.sNODEWIDTH);
    nsspin.addChangeListener((e) -> {
      mEditorConfig.sNODEWIDTH = ((SpinnerNumberModel) nsspin.getModel())
          .getNumber().intValue();
      mEditorConfig.sNODEHEIGHT = ((SpinnerNumberModel) nsspin.getModel())
          .getNumber().intValue();
      mEditor.refresh();
    });
    
    c.gridy += 1;
    JSpinner gsspin = createSpinner(grpan, c, "Grid Scale:", 1, 4, .25f);
    ((SpinnerNumberModel) gsspin.getModel()).setValue(
        mEditorConfig.sGRID_SCALE);
    gsspin.addChangeListener((e) -> {
          mEditorConfig.sGRID_SCALE = 
              ((SpinnerNumberModel) gsspin.getModel())
              .getNumber().floatValue();
          mEditor.refresh();
        });

    c.gridy += 1;
    JSpinner zispin = createSpinner(grpan, c, "Zoom Increment (%):", 10, 100, 10);
    ((SpinnerNumberModel) zispin.getModel()).setValue(
        (int)(100 * (mEditorConfig.sZOOM_INCREMENT - 1)));
    zispin.addChangeListener((e) -> {
      mEditorConfig.sZOOM_INCREMENT = 1.0f + 
          ((SpinnerNumberModel) zispin.getModel())
          .getNumber().intValue() / 100.0f;
      mEditor.refresh();
    });
    
    return grpan;
  }

  private class FontButton extends JButton {
    private FontConfig fontConfig;
    
    private void openFontChooser(Container parent) {
      JFontChooser.setDialogFont(mEditorConfig.sDIALOG_FONT.getFont());
      JFontChooser fontChooser = new JFontChooser();
      fontChooser.setSelectedFont(fontConfig.getFont());
      int result = fontChooser.showModalDialog(parent);
      if (result == JFontChooser.OK_OPTION) {
        Font font = fontChooser.getSelectedFont(); 
        fontConfig.set(font);
        this.setText(fontConfig.toString());
        this.setFont(font);
      }
    }
    
    public FontButton(FontConfig fc) {
      super(fc.toString());
      fontConfig = fc;
      setFont(fontConfig.getFont());
      addActionListener((ev) -> openFontChooser(getParent()));
    }
  }

  private void addFontButton(JComponent parent, String label, FontConfig fc,
      GridBagConstraints c, int row) {
    c.gridy = row;
    c.gridx = 0;
    c.insets = new Insets(0,0,0,10);
    c.anchor = GridBagConstraints.LINE_START;
    JLabel l = new JLabel(label + ":");
    parent.add(l, c);
    c.gridx = 1;
    c.insets = new Insets(1,10,1,0);
    c.anchor = GridBagConstraints.LINE_END;
    FontButton fb = new FontButton(fc);
    fb.setMinimumSize(new Dimension(250, 28));
    fb.setPreferredSize(new Dimension(250, 28));
    parent.add(fb, c);
  }
  
  private JPanel initFontPanel() {
    JPanel mFontPanel = new JPanel();
    mFontPanel.setBackground(Color.white);
    mFontPanel.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.LINE_START;
    mFontPanel.setBorder(BorderFactory.createTitledBorder(" Font Options "));
    addFontButton(mFontPanel, "Node Font", mEditorConfig.sNODE_FONT, c, 0);
    addFontButton(mFontPanel, "Code Font (Area)", mEditorConfig.sCODE_FONT, c, 1);
    addFontButton(mFontPanel, "Code Font (Label)", mEditorConfig.sCODEAREA_FONT, c, 2);
    addFontButton(mFontPanel, "Comment Font", mEditorConfig.sCOMMENT_FONT, c, 3);
    addFontButton(mFontPanel, "Button Font", mEditorConfig.sBUTTON_FONT, c, 4);
    addFontButton(mFontPanel, "Dialog Font", mEditorConfig.sDIALOG_FONT, c, 5);
    addFontButton(mFontPanel, "Tree Font", mEditorConfig.sTREE_FONT, c, 6);
    return mFontPanel;
  }

  private void saveEditorConfig(boolean dispose) {
   
    AppFrame.getInstance().refresh();
    EventDispatcher.getInstance().convey(new ProjectChangedEvent(this));
    if (dispose) {
      dispose();
    }
  }

  
  
  /*
  private void initGeneralPanel() {
    mXMLNSLabel = new JLabel("Namespace:");
    mXMLNSLabel.setMinimumSize(mLabelDimension);
    mXMLNSLabel.setPreferredSize(mLabelDimension);
    mXMLNSTextField = new JTextField();
    mXMLNSTextField.setMinimumSize(textfieldSize);
    mXMLNSTextField.setPreferredSize(textfieldSize);
    mXMLInstanceLabel = new JLabel("Instance:");
    mXMLInstanceLabel.setMinimumSize(mLabelDimension);
    mXMLInstanceLabel.setPreferredSize(mLabelDimension);
    mXMLInstanceTextField = new JTextField();
    mXMLInstanceTextField.setMinimumSize(textfieldSize);
    mXMLInstanceTextField.setPreferredSize(textfieldSize);
    mXSDFileLabel = new JLabel("Location:");
    mXSDFileLabel.setMinimumSize(mLabelDimension);
    mXSDFileLabel.setPreferredSize(mLabelDimension);
    mXSDFileTextField = new JTextField();
    mXSDFileTextField.setMinimumSize(textfieldSize);
    mXSDFileTextField.setPreferredSize(textfieldSize);
    mXSDFileTextField.setEditable(false);
    mXSDFileButton = new JButton(ResourceLoader.loadImageIcon("img/search_icon.png"));
    mXSDFileButton.setRolloverIcon(ResourceLoader.loadImageIcon("img/search_icon_blue.png"));
    mXSDFileButton.setOpaque(false);
    mXSDFileButton.setContentAreaFilled(false);
    mXSDFileButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        JFileChooser file = new JFileChooser(sUSER_DIR);

        file.setFileSelectionMode(JFileChooser.FILES_ONLY);
        file.showDialog(AppFrame.getInstance(), "Select Sceneflow XSD");

        if (file.getSelectedFile() != null) {
          mXSDFileTextField.setText(file.getSelectedFile().getPath());
        }
      }
    });

    JPanel xmlNameSpace = new JPanel();

    xmlNameSpace.setOpaque(false);
    xmlNameSpace.setLayout(new BoxLayout(xmlNameSpace, BoxLayout.X_AXIS));
    xmlNameSpace.add(Box.createRigidArea(new Dimension(5, 0)));
    xmlNameSpace.add(mXMLNSLabel);
    xmlNameSpace.add(Box.createHorizontalGlue());
    xmlNameSpace.add(mXMLNSTextField);
    xmlNameSpace.add(Box.createRigidArea(new Dimension(5, 0)));

    JPanel xmlInstance = new JPanel();

    xmlInstance.setOpaque(false);
    xmlInstance.setLayout(new BoxLayout(xmlInstance, BoxLayout.X_AXIS));
    xmlInstance.add(Box.createRigidArea(new Dimension(5, 0)));
    xmlInstance.add(mXMLInstanceLabel);
    xmlInstance.add(Box.createHorizontalGlue());
    xmlInstance.add(mXMLInstanceTextField);
    xmlInstance.add(Box.createRigidArea(new Dimension(5, 0)));

    JPanel xsdFile = new JPanel();

    xsdFile.setOpaque(false);
    xsdFile.setLayout(new BoxLayout(xsdFile, BoxLayout.X_AXIS));
    xsdFile.add(Box.createRigidArea(new Dimension(5, 0)));
    xsdFile.add(mXSDFileLabel);
    xsdFile.add(Box.createHorizontalGlue());
    xsdFile.add(mXSDFileTextField);
    xsdFile.add(Box.createRigidArea(new Dimension(5, 0)));
    xsdFile.add(mXSDFileButton);
    xsdFile.add(Box.createRigidArea(new Dimension(5, 0)));
    mGeneralPanel = new JPanel();
    mGeneralPanel.setLayout(new BoxLayout(mGeneralPanel, BoxLayout.Y_AXIS));
    mGeneralPanel.setBackground(Color.white);
    mGeneralPanel.setBorder(BorderFactory.createTitledBorder(" Sceneflow Syntax "));
    mGeneralPanel.add(Box.createRigidArea(new Dimension(1, 20)));
    mGeneralPanel.add(xmlNameSpace);
    mGeneralPanel.add(Box.createRigidArea(new Dimension(1, 10)));
    mGeneralPanel.add(xmlInstance);
    mGeneralPanel.add(Box.createRigidArea(new Dimension(1, 10)));
    mGeneralPanel.add(xsdFile);
    mGeneralPanel.add(Box.createRigidArea(new Dimension(1, 20)));
  }

  private void initFileListPanel() {
    mRecentFileList = new JList(new DefaultListModel());
    mRecentFileList.setOpaque(false);
    mRecentFileScrollPane = new JScrollPane(mRecentFileList);
    mRecentFileScrollPane.setOpaque(false);
    mRecentFileScrollPane.setBounds(140, 95, 230, 100);
    mDeleteRecentFileButton = new JButton("Remove Item");
    mDeleteRecentFileButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        int index = mRecentFileList.getSelectedIndex();

        if (index >= 0) {
          ((DefaultListModel) mRecentFileList.getModel()).remove(index);
        }
      }
    });
    mDeleteRecentFileListButton = new JButton("Delete List");
    mDeleteRecentFileListButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        ((DefaultListModel) mRecentFileList.getModel()).clear();
      }
    });

    // Do the layout - pack all stuff into little small cute boxes
    JPanel fileList = new JPanel();

    fileList.setOpaque(false);
    fileList.setLayout(new BoxLayout(fileList, BoxLayout.X_AXIS));
    fileList.add(Box.createRigidArea(new Dimension(5, 0)));
    fileList.add(mRecentFileScrollPane);
    fileList.add(Box.createRigidArea(new Dimension(5, 0)));

    JPanel recentFileButtons = new JPanel();

    recentFileButtons.setOpaque(false);
    recentFileButtons.setLayout(new BoxLayout(recentFileButtons, BoxLayout.X_AXIS));
    recentFileButtons.add(Box.createHorizontalGlue());
    recentFileButtons.add(mDeleteRecentFileButton);
    recentFileButtons.add(mDeleteRecentFileListButton);
    recentFileButtons.add(Box.createRigidArea(new Dimension(5, 0)));
    mFileListPanel = new JPanel();
    mFileListPanel.setBackground(Color.white);
    mFileListPanel.setLayout(new BoxLayout(mFileListPanel, BoxLayout.Y_AXIS));
    mFileListPanel.setBorder(BorderFactory.createTitledBorder(" Recently Edited Sceneflows "));
    mFileListPanel.add(fileList);
    mFileListPanel.add(recentFileButtons);
  }
  */

}
