package de.dfki.vsm.editor;

import de.dfki.vsm.editor.event.NodeSelectedEvent;
import de.dfki.vsm.editor.event.ProjectChangedEvent;
import de.dfki.vsm.model.project.EditorConfig;
import de.dfki.vsm.util.evt.EventDispatcher;
import de.dfki.vsm.util.evt.EventListener;
import de.dfki.vsm.util.evt.EventObject;
import de.dfki.vsm.util.log.LOGDefaultLogger;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.Observer;
import java.util.Timer;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import org.fife.ui.rsyntaxtextarea.*;
import org.fife.ui.rtextarea.ColorBackgroundPainterStrategy;

/**
 * @author Gregor Mehlmann
 * @author Patrick Gebhard
 */
public class CmdBadge extends RSyntaxTextArea implements EventListener, Observer {

    //
    private final LOGDefaultLogger mLogger = LOGDefaultLogger.getInstance();
    private final EventDispatcher mDispatcher = EventDispatcher.getInstance();

    // edit
    private boolean mEditMode = false;
    private final Action wrapper = new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            endEditMode();
        }
    };

    // The node to which the badge is connected
    private final Node mNode;
    private final EditorConfig mEditorConfig;
    private final Timer mVisuTimer;

    private final Font mFont;
    private boolean isFocused;

    /**
     *
     *
     */
    public CmdBadge(Node node) {
        super(30, 40);
        setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        setCodeFoldingEnabled(true);
        setVisible(true);
        setBackground(new Color(235, 235, 235, 0));
        setOpaque(false);
        addFocusListener(new FocusListener() {
            public void focusGained(FocusEvent e) { isFocused = true; }
            public void focusLost(FocusEvent e) { isFocused = false; }
        });
        
        isFocused = false;
        mNode = node;
        mEditorConfig = mNode.getWorkSpace().getEditorConfig();
        mVisuTimer = new Timer("Command-Badge-Visualization-Timer");
        mFont = new Font("Monospaced",
                Font.ITALIC,
                mEditorConfig.sWORKSPACEFONTSIZE);
        setFont(mFont);

        Dimension dimension = new Dimension(10, 10);
        setSize(dimension);
        setLocation(0, 0);
        setLocation(mNode.getLocation().x + (mEditorConfig.sNODEWIDTH / 2)
            - (dimension.width / 2),
            mNode.getLocation().y + mEditorConfig.sNODEHEIGHT);
        setLayout(new BorderLayout());
        //add(textArea, BorderLayout.CENTER);

        //KeyStroke keyStroke = KeyStroke.getKeyStroke("ENTER");
        //Object actionKey = getInputMap(JComponent.WHEN_FOCUSED).get(keyStroke);
        //getActionMap().put(actionKey, wrapper);
        //textArea.requestFocusInWindow();
        update();
    }

    @Override
    public void paintComponent(java.awt.Graphics g) {
        super.paintComponent(g);

        System.out.println("Paint cmdbadge");
        Dimension dimension =
                new Dimension(getWidth(), getHeight());
        //setSize(dimension);
        setLocation(mNode.getLocation().x + (mEditorConfig.sNODEWIDTH / 2) - (dimension.width / 2),
                mNode.getLocation().y + mEditorConfig.sNODEHEIGHT);
        //Graphics2D graphics = (Graphics2D) g;
        //graphics.fillRoundRect(0, 0, dimension.width, dimension.height, 5, 5);
        //graphics.setStroke(new BasicStroke(1.5f));
        //graphics.setColor(Color.BLACK);
        
        if (isFocused) {
            setBackground(new Color(0, 0, 0, 100));
            setForeground(new Color(255, 255, 255, 100));
            setOpaque(true);
        } else {
            setBackground(new Color(175, 175, 175, 0));
            setForeground(new Color(0, 0, 0, 100));
            setOpaque(false);
        }
        //textArea.paint(g);
    }

    /*
    public void setEditMode() {
        mEditMode = true;
        return;/*
        for (TPLTuple<String, AttributedString> s : mStringList) {
            addCmdEditor(s.getFirst());
        }

        ///add(mCmdEditor, BorderLayout.CENTER);
        add(textArea, BorderLayout.CENTER);

        mDispatcher.convey(new NodeSelectedEvent(this, mNode.getDataNode()));
        //mCmdEditor.requestFocusInWindow();
        
    }*/

    /*
     * Resets badge to its default visual behavior
     */
    public synchronized void endEditMode() {

      // TODO: This will never be executed, because mEditMode is never set
        if (mEditMode) {
          mNode.getDataNode().getCmd().setContent(getText());

          mDispatcher.convey(new ProjectChangedEvent(this));
          mDispatcher.convey(new NodeSelectedEvent(this, mNode.getDataNode()));
          mEditMode = false;
        }

        repaint(100);
        update();
    }
/*
    private void addCmdEditor(String text) {

        mCmdEditor.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                mCmdEditor.setForeground(Color.black);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                mCmdEditor.setForeground(Color.black);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {

            }
        });

        mCmdEditor.setFont(mFont);
        mCmdEditor.setText(text);
        mCmdEditor.setOpaque(false);
        mCmdEditor.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        KeyStroke keyStroke = KeyStroke.getKeyStroke("ENTER");
        Object actionKey = mCmdEditor.getInputMap(JComponent.WHEN_FOCUSED).get(keyStroke);

        mCmdEditor.getActionMap().put(actionKey, wrapper);
    }
*/
    public void updateLocation(Point vector) {
        Point location = getLocation();
        setLocation(location.x + vector.x, location.y + vector.y);
    }

    public boolean containsPoint(int x, int y) {
        return getBounds().contains(x, y);
    }

    /**
     * Nullifies the VisalisationTimer thread
     */
    public void stopVisualisation() {
        mVisuTimer.purge();
        mVisuTimer.cancel();

        // mVisuTimer = null;
    }

    @Override
    public void update(java.util.Observable obs, Object obj) {
        update();
    }

    private void update() {
      String content = mNode.getDataNode().getCmd().getContent();

      // Sets visibility of the component to true only if there is something to display
      setVisible(! content.isEmpty());
      setText(content);
      
      if (! content.isEmpty()) {
        Dimension dimension = new Dimension(200, getLineCount()*getLineHeight());
        setSize(dimension);
        setLocation(mNode.getLocation().x + (mEditorConfig.sNODEWIDTH / 2)
            - (dimension.width / 2),
            mNode.getLocation().y + mEditorConfig.sNODEHEIGHT);
      } else {
        setSize(new Dimension(getWidth(), getHeight()));
      }
    }

    public Node getNode() {
        return mNode;
    }
    
  @Override
  public void update(EventObject event) {
    System.out.println("Event happened!!");
  }
}
