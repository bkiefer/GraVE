package de.dfki.grave.editor.dialog;

//~--- JDK imports ------------------------------------------------------------
import java.awt.Color;
import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.*;

import de.dfki.grave.app.AppFrame;

/**
 * @author Sergio Soto
 *
 */
@SuppressWarnings("serial")
public class NewProjectDialog extends JDialog {

  // panels
  private JPanel mMainPanel = null;
  private JPanel mButtonsPanel = null;
  private JPanel mConfigPanel = null;

  // buttons
  private OKButton mOkButton = null;
  private CancelButton mCancelButton = null;

  // text fields
  private HintTextField mNameTextField = null;

  // Labels
  private JLabel errorMsg;
  private JLabel lblName;

  private String[] name;

  public NewProjectDialog(String[] newName) {
    super(AppFrame.getInstance(), "New Project", true);
    name = newName;
    AppFrame.addEscapeListener(this);
    initComponents();
    setVisible(true);
  }

  private void initComponents() {

    // create contentfields and set inital content
    Dimension tSize = new Dimension(250, 30);
    Dimension labelSize = new Dimension(120, 30);

    setBackground(Color.white);
    mNameTextField = new HintTextField("Enter Project Name");
    mNameTextField.setMinimumSize(tSize);
    mNameTextField.setPreferredSize(tSize);
    mNameTextField.setText(name[0]);
    mNameTextField.selectAll();

    mNameTextField.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        okActionPerformed();
      }
    });

    mNameTextField.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        //mNameTextField.setBorder(BorderFactory.createLineBorder(Color.blue));
        errorMsg.setForeground(Color.white);
      }
    });
    // create buttons
    mOkButton = new OKButton();
    mOkButton.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        okActionPerformed();
      }
    });

    // mOkButton.setSelected(true);
    mCancelButton = new CancelButton();
    mCancelButton.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override
      public void mouseClicked(java.awt.event.MouseEvent evt) {
        cancelActionPerformed();
      }
    });

    // create config panel
    mConfigPanel = new JPanel();
    mConfigPanel.setOpaque(false);
    mConfigPanel.setLayout(new BoxLayout(mConfigPanel, BoxLayout.Y_AXIS));

    JPanel namePanel = new JPanel();

    namePanel.setOpaque(false);
    namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.X_AXIS));
    namePanel.add(Box.createRigidArea(new Dimension(5, 0)));
    lblName = new JLabel("Project Name: ");
    lblName.setPreferredSize(labelSize);
    namePanel.add(lblName);
    namePanel.add(Box.createRigidArea(new Dimension(5, 0)));
    namePanel.add(mNameTextField);
    namePanel.add(Box.createRigidArea(new Dimension(50, 0)));

    // compose config panel
    mConfigPanel.add(namePanel);
    mConfigPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    mConfigPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    errorMsg = new JLabel("Information Required");
    errorMsg.setForeground(Color.white);
    mConfigPanel.add(errorMsg);

    // compose panels
    mButtonsPanel = new JPanel();
    mButtonsPanel.setOpaque(false);
    mButtonsPanel.setLayout(new BoxLayout(mButtonsPanel, BoxLayout.X_AXIS));
    mButtonsPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 40));
    mButtonsPanel.add(Box.createHorizontalGlue());
    mButtonsPanel.add(mCancelButton);
    mButtonsPanel.add(Box.createRigidArea(new Dimension(50, 1)));
    mButtonsPanel.add(mOkButton);
    mButtonsPanel.add(Box.createRigidArea(new Dimension(30, 0)));
    mMainPanel = new JPanel();
    mMainPanel.setLayout(new BoxLayout(mMainPanel, BoxLayout.Y_AXIS));
    mMainPanel.setOpaque(false);
    mMainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
    mMainPanel.add(mConfigPanel);
    mMainPanel.add(Box.createVerticalGlue());
    mMainPanel.add(mButtonsPanel);
    mMainPanel.add(Box.createRigidArea(new Dimension(0, 20)));

    //Key listener need to gain focus on the text field
    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {

      @Override
      public boolean dispatchKeyEvent(KeyEvent ke) {
        if (ke.getID() == KeyEvent.KEY_PRESSED) {
          if (ke.getKeyCode() == KeyEvent.VK_ENTER) {
            okActionPerformed();
          }
        }
        return false;
      }
    });
//        addKeyListener(new KeyAdapter() {
//
//            @Override
//            public void keyPressed(KeyEvent e)
//            {
//                System.out.println(e.getKeyChar());
//                System.out.println(e.isConsumed());
//                System.out.println(e.isActionKey());
//                System.out.println(e.getKeyLocation());
//                if(e.isActionKey())
//                {
//                    System.out.println("is action key");
//                }
//            }
//        });
    add(mMainPanel);
    setResizable(false);
    pack();

    setLocation(getParent().getLocation().x + (getParent().getWidth() - getWidth()) / 2,
            getParent().getLocation().y + (getParent().getHeight() - getHeight()) / 2);
    mNameTextField.requestFocus();
  }

  protected void okActionPerformed() {
    if (validateValues()) {
      dispose();
    }
  }

  protected void cancelActionPerformed() {
    name[0] = null;
    dispose();
  }

  private boolean validateValues() {
    if (mNameTextField.getText().length() == 0) {
      mNameTextField.setBorder(BorderFactory.createLineBorder(Color.red));
      errorMsg.setForeground(Color.red);
      return false;
    }

    name[0] = mNameTextField.getText();
    errorMsg.setForeground(Color.white);
    return true;
  }

}
