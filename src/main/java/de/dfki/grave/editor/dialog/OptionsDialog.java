package de.dfki.grave.editor.dialog;

import static de.dfki.grave.Preferences.sUSER_DIR;

//~--- JDK imports ------------------------------------------------------------
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.grave.AppFrame;
import de.dfki.grave.editor.event.ProjectChangedEvent;
import de.dfki.grave.model.project.EditorConfig;
import de.dfki.grave.util.ResourceLoader;
import de.dfki.grave.util.evt.EventDispatcher;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
public class OptionsDialog extends JDialog {

  private static OptionsDialog sSingeltonInstance = null;

  private final Logger mLogger = LoggerFactory.getLogger(OptionsDialog.class);;
  private final AppFrame mEditor = AppFrame.getInstance();
  private final Dimension mLabelDimension = new Dimension(100, 10);
  private final Dimension buttonSize = new Dimension(125, 30);
  private final Dimension textfieldSize = new Dimension(150, 30);
  private JPanel mMainPanel;
  private JPanel mPrefPanel;

  private JPanel mButtonsPanel;
  private JPanel mFileListPanel;
  private JList mRecentFileList;
  private JScrollPane mRecentFileScrollPane;
  private CancelButton mCancelButton;
  private OKButton mOkButton;
  private JLabel mNodeSizeLabel;
  private JSpinner mNodeSizeSpinner;
  private JLabel mWorkspaceFontSizeLabel;
  private JSpinner mWorkspaceFontSizeSpinner;
  private JLabel mScriptFontSizeLabel;
  private JSpinner mScriptFontSizeSpinner;
  private JLabel mScriptFontTypeLabel;

  // private JSpinner mScriptFontTypeSpinner;
  private JComboBox mScriptFontComboBox;
  private JLabel mGridScaleLabel;
  private JSpinner mGridScaleSpinner;
  private JLabel mZoomFactorLabel;
  private JSpinner mZoomFactorSpinner;
  private JCheckBox mGridCheckBox;
  private JCheckBox mShowNodeIDCheckBox;
  private JCheckBox mShowVariablesCheckBox;
  private JCheckBox mShowSmartPathDebugCheckBox;
  private JPanel mGeneralPanel;
  private JLabel mXMLNSLabel;
  private JTextField mXMLNSTextField;
  private JLabel mXMLInstanceLabel;
  private JTextField mXMLInstanceTextField;
  private JLabel mXSDFileLabel;
  private JTextField mXSDFileTextField;
  private JButton mXSDFileButton;
  private JPanel mGraphicsPanel;
  private JPanel mScriptPanel;
  private JButton mDeleteRecentFileListButton;
  private JButton mDeleteRecentFileButton;

  private final EditorConfig mEditorConfig;

  private OptionsDialog() {
    super(AppFrame.getInstance(), "Preferences", false);
    AppFrame.getInstance().addEscapeListener(this);
    mEditorConfig = mEditor.getSelectedProjectEditor().getEditorProject().getEditorConfig();
    initComponents();
    initEditorConfig();

  }

  public static OptionsDialog getInstance() {
    if (sSingeltonInstance == null) {
      sSingeltonInstance = new OptionsDialog();
    }

    sSingeltonInstance.initEditorConfig();

    return sSingeltonInstance;
  }

  private void initComponents() {
    initGeneralPanel();
    initFileListPanel();
    initGraphicsPanel();
    initScriptPanel();
    setBackground(Color.white);

    // initScenePlayerPanel();
    mButtonsPanel = new JPanel();
    mButtonsPanel.setLayout(new BoxLayout(mButtonsPanel, BoxLayout.X_AXIS));
    mButtonsPanel.setOpaque(false);
    mOkButton = new OKButton();
    mOkButton.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        saveEditorConfig(true);
      }
    });
    mCancelButton = new CancelButton();
    mCancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        dispose();
      }
    });

    // Do the layout
    mPrefPanel = new JPanel();
    mPrefPanel.setLayout(new BoxLayout(mPrefPanel, BoxLayout.Y_AXIS));
    //mPrefPanel.add(mGeneralPanel);

    // mPrefPanel.add(mFileListPanel);
    mPrefPanel.add(mGraphicsPanel);
    mPrefPanel.add(mScriptPanel);

    // mPrefPanel.add(mScenePlayerPanel);
    mButtonsPanel.add(Box.createHorizontalGlue());
    mButtonsPanel.add(mCancelButton);
    mButtonsPanel.add(Box.createRigidArea(new Dimension(40, 60)));
    mButtonsPanel.add(mOkButton);
    mButtonsPanel.add(Box.createRigidArea(new Dimension(5, 0)));
    mMainPanel = new JPanel();
    mMainPanel.setBackground(Color.white);
    mMainPanel.setLayout(new BoxLayout(mMainPanel, BoxLayout.Y_AXIS));
    mMainPanel.add(mPrefPanel);
    mMainPanel.add(mButtonsPanel);

    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {

      @Override
      public boolean dispatchKeyEvent(KeyEvent ke) {
        //boolean keyHandled = false;
        if (ke.getID() == KeyEvent.KEY_PRESSED) {
          if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
            saveEditorConfig(true);
          }
        }
        return false;
      }
    });
    add(mMainPanel);
    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    setResizable(false);
    pack();
    setLocation(getParent().getLocation().x + (getParent().getWidth() - getWidth()) / 2,
            getParent().getLocation().y + (getParent().getHeight() - getHeight()) / 2);
  }

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

  private void initGraphicsPanel() {
    mWorkspaceFontSizeLabel = new JLabel("Font Size:");
    mGridCheckBox = new JCheckBox("Draw Grid", true);
    mGridCheckBox.setOpaque(false);
    mGridCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        saveEditorConfig(false);
        mEditor.refresh();
      }
    });

    // Node size stuff
    mNodeSizeLabel = new JLabel("Node Size:");
    mNodeSizeSpinner = new JSpinner(new SpinnerNumberModel(mEditorConfig.sNODEHEIGHT, 20, 200, 2));
    mGridScaleLabel = new JLabel("Grid Scale:");
    mGridScaleSpinner = new JSpinner(new SpinnerNumberModel(mEditorConfig.sGRID_SCALE, 1, 4, .25));
    mZoomFactorLabel = new JLabel("Zoom Factor:");
    mZoomFactorSpinner = new JSpinner(new SpinnerNumberModel(mEditorConfig.sZOOM_FACTOR, .5, 5, .25));
    mWorkspaceFontSizeSpinner = new JSpinner(new SpinnerNumberModel(mEditorConfig.sWORKSPACEFONTSIZE, 8, 16, 1));
    ((JSpinner.NumberEditor) mNodeSizeSpinner.getEditor()).getTextField().setEditable(false);
    ((JSpinner.NumberEditor) mGridScaleSpinner.getEditor()).getTextField().setEditable(false);
    mShowNodeIDCheckBox = new JCheckBox("Draw Node ID", true);
    mShowNodeIDCheckBox.setOpaque(false);
    mShowNodeIDCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        saveEditorConfig(false);
        mEditor.refresh();
      }
    });
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

    // Do the Layout - pack all stuff into little small cute boxesÏ
    JPanel fontAndSize = new JPanel();

    fontAndSize.setOpaque(false);
    fontAndSize.setLayout(new BoxLayout(fontAndSize, BoxLayout.X_AXIS));
    fontAndSize.add(Box.createRigidArea(new Dimension(5, 0)));
    fontAndSize.add(mNodeSizeLabel);
    fontAndSize.add(mNodeSizeSpinner);
    fontAndSize.add(Box.createRigidArea(new Dimension(5, 0)));
    fontAndSize.add(Box.createHorizontalGlue());
    fontAndSize.add(mWorkspaceFontSizeLabel);
    fontAndSize.add(mWorkspaceFontSizeSpinner);
    fontAndSize.add(Box.createRigidArea(new Dimension(5, 0)));
    fontAndSize.add(Box.createHorizontalGlue());
    fontAndSize.add(mGridScaleLabel);
    fontAndSize.add(mGridScaleSpinner);
    fontAndSize.add(Box.createRigidArea(new Dimension(5, 0)));
    fontAndSize.add(mZoomFactorLabel);
    fontAndSize.add(mZoomFactorSpinner);
    fontAndSize.add(Box.createRigidArea(new Dimension(5, 0)));

    JPanel drawOptions = new JPanel();

    drawOptions.setOpaque(false);
    drawOptions.setLayout(new BoxLayout(drawOptions, BoxLayout.Y_AXIS));
    drawOptions.add(Box.createRigidArea(new Dimension(1, 15)));
    drawOptions.add(mGridCheckBox);
    drawOptions.add(mShowNodeIDCheckBox);
    drawOptions.add(mShowVariablesCheckBox);
    drawOptions.add(mShowSmartPathDebugCheckBox);

    JPanel graphicOptions = new JPanel();

    graphicOptions.setOpaque(false);
    graphicOptions.setLayout(new BoxLayout(graphicOptions, BoxLayout.X_AXIS));
    graphicOptions.add(Box.createRigidArea(new Dimension(5, 0)));
    graphicOptions.add(drawOptions);
    graphicOptions.add(Box.createHorizontalGlue());
    graphicOptions.add(Box.createRigidArea(new Dimension(5, 0)));
    mGraphicsPanel = new JPanel();
    mGraphicsPanel.setBackground(Color.white);
    mGraphicsPanel.setLayout(new BoxLayout(mGraphicsPanel, BoxLayout.Y_AXIS));
    mGraphicsPanel.setBorder(BorderFactory.createTitledBorder(" Visual Appearance "));
    mGraphicsPanel.add(Box.createRigidArea(new Dimension(5, 20)));
    mGraphicsPanel.add(fontAndSize);
    mGraphicsPanel.add(graphicOptions);
    mGraphicsPanel.add(Box.createRigidArea(new Dimension(5, 20)));

  }

  private void initScriptPanel() {

    mScriptFontTypeLabel = new JLabel("Font Type:");

    GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
    String[] allFonts = g.getAvailableFontFamilyNames();
    ArrayList<String> fonts = new ArrayList<String>();

    for (String font : allFonts) {
      if (font.contains("Mono")) {
        fonts.add(font);
      }
    }

//      JPanel controlPanel = new JPanel();
    mScriptFontComboBox = new JComboBox(fonts.toArray());
    mScriptFontComboBox.setOpaque(false);
    mScriptFontComboBox.setSelectedItem(mEditorConfig.sSCRIPT_FONT_TYPE);
    mScriptFontComboBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        saveEditorConfig(false);
        mEditor.refresh();
      }
    });
    mScriptFontSizeLabel = new JLabel("Font Size:");
    mScriptFontSizeSpinner = new JSpinner(new SpinnerNumberModel(mEditorConfig.sSCRIPT_FONT_SIZE, 8, 16, 1));

    JPanel fontAndSize = new JPanel();
    fontAndSize.setOpaque(false);
    fontAndSize.setLayout(new BoxLayout(fontAndSize, BoxLayout.X_AXIS));
    fontAndSize.add(Box.createRigidArea(new Dimension(5, 0)));
    fontAndSize.add(Box.createRigidArea(new Dimension(5, 0)));
    fontAndSize.add(Box.createHorizontalGlue());
    fontAndSize.add(mScriptFontTypeLabel);
    fontAndSize.add(Box.createHorizontalGlue());
    fontAndSize.add(mScriptFontComboBox);
    fontAndSize.add(Box.createRigidArea(new Dimension(5, 0)));
    fontAndSize.add(mScriptFontSizeLabel);
    fontAndSize.add(Box.createHorizontalGlue());
    fontAndSize.add(mScriptFontSizeSpinner);
    fontAndSize.add(Box.createRigidArea(new Dimension(5, 0)));
    fontAndSize.add(Box.createHorizontalGlue());

    mScriptPanel = new JPanel();
    mScriptPanel.setBackground(Color.white);
    mScriptPanel.setLayout(new BoxLayout(mScriptPanel, BoxLayout.Y_AXIS));
    mScriptPanel.setBorder(BorderFactory.createTitledBorder(" Script Options "));
    mScriptPanel.add(Box.createRigidArea(new Dimension(5, 20)));
    mScriptPanel.add(fontAndSize);
    mScriptPanel.add(Box.createRigidArea(new Dimension(5, 20)));
  }

  private void saveEditorConfig(boolean dispose) {

    mEditorConfig.sNODEWIDTH = ((SpinnerNumberModel) mNodeSizeSpinner.getModel()).getNumber().intValue();
    mEditorConfig.sNODEHEIGHT = ((SpinnerNumberModel) mNodeSizeSpinner.getModel()).getNumber().intValue();
    mEditorConfig.sGRID_SCALE = ((SpinnerNumberModel) mGridScaleSpinner.getModel()).getNumber().intValue();
    mEditorConfig.sZOOM_FACTOR = ((SpinnerNumberModel) mZoomFactorSpinner.getModel()).getNumber().intValue();

    mEditorConfig.sWORKSPACEFONTSIZE = ((SpinnerNumberModel) mWorkspaceFontSizeSpinner.getModel()).getNumber().intValue();
    mEditorConfig.sSHOWGRID = mGridCheckBox.isSelected();
    mEditorConfig.sSHOWIDSOFNODES = mShowNodeIDCheckBox.isSelected();
    mEditorConfig.sSHOW_VARIABLE_BADGE_ON_WORKSPACE = mShowVariablesCheckBox.isSelected();
    mEditorConfig.sSHOW_SMART_PATH_DEBUG = mShowSmartPathDebugCheckBox.isSelected();
    mEditorConfig.sSCRIPT_FONT_SIZE = ((SpinnerNumberModel) mScriptFontSizeSpinner.getModel()).getNumber().intValue();
    mEditorConfig.sSCRIPT_FONT_TYPE = mScriptFontComboBox.getSelectedItem().toString();

    mEditorConfig.save(mEditor.getSelectedProjectEditor().getEditorProject().getProjectFile());

    EventDispatcher.getInstance().convey(new ProjectChangedEvent(this));
//        EditorInstance.getInstance().save();
    if (dispose) {
      dispose();
    }
  }

  private void initEditorConfig() {
      // TODO: isn't the second time overriding the first?
      ((SpinnerNumberModel) mNodeSizeSpinner.getModel()).setValue(
              mEditorConfig.sNODEWIDTH);
      ((SpinnerNumberModel) mNodeSizeSpinner.getModel()).setValue(
              mEditorConfig.sNODEHEIGHT);
      ((SpinnerNumberModel) mGridScaleSpinner.getModel()).setValue(
              mEditorConfig.sGRID_SCALE);
      ((SpinnerNumberModel) mZoomFactorSpinner.getModel()).setValue(
              mEditorConfig.sZOOM_FACTOR);

      mGridCheckBox.setSelected(mEditorConfig.sSHOWGRID);
      ((SpinnerNumberModel) mScriptFontSizeSpinner.getModel()).setValue(
              mEditorConfig.sSCRIPT_FONT_SIZE);
      mScriptFontComboBox.setSelectedItem(mEditorConfig.sSCRIPT_FONT_TYPE);
      //mLaunchDefaultPlayerCheckBox.setSelected(Boolean.valueOf(mEditorConfig.getProperty(key)));
      mShowNodeIDCheckBox.setSelected(mEditorConfig.sSHOWIDSOFNODES);
      mShowVariablesCheckBox.setSelected(mEditorConfig.sSHOW_VARIABLE_BADGE_ON_WORKSPACE);
      mShowSmartPathDebugCheckBox.setSelected(mEditorConfig.sSHOW_SMART_PATH_DEBUG);
      ((SpinnerNumberModel) mWorkspaceFontSizeSpinner.getModel()).setValue(
              mEditorConfig.sWORKSPACEFONTSIZE);

    // Add specific listeners
    mNodeSizeSpinner.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        saveEditorConfig(false);
        mEditor.refresh();
      }
    });

    // Add specific listeners
    mScriptFontSizeSpinner.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        saveEditorConfig(false);
        mEditor.refresh();
      }
    });

    // Add specific listeners
    mGridScaleSpinner.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        saveEditorConfig(false);
        mEditor.refresh();
      }
    });

    // Add specific listeners
    mWorkspaceFontSizeSpinner.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent e) {
        saveEditorConfig(false);
        mEditor.refresh();
      }
    });
  }
}