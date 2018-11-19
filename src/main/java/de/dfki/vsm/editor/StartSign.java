package de.dfki.vsm.editor;

import static de.dfki.vsm.Preferences.sSTART_SIGN_COLOR;

//~--- JDK imports ------------------------------------------------------------
import java.awt.*;

import javax.swing.JComponent;

import de.dfki.vsm.editor.project.EditorConfig;

/**
 * @author Patrick Gebhard
 * @author Gregor Mehlmann
 */
@SuppressWarnings("serial")
public class StartSign extends JComponent {
  private final Color mColor;
  private final Node mNode;
  private final boolean mOutline;
  private Polygon mHead;
  private int mHalfHeight;
  private int mWidth;
  private int mStrokeSize;

  public StartSign(Node node) {
    this(node, false, sSTART_SIGN_COLOR);
  }

  public StartSign(Node node, boolean mode, Color color) {
    mNode = node;
    mColor = color;
    mOutline = mode;
    update();
  }

  public void update() {
    mHalfHeight = mNode.getWidth() / 6;
    mWidth = mNode.getWidth() / 8;
    mStrokeSize = mNode.getWidth() / 50;
    if (mStrokeSize < 2) mStrokeSize = 2;
    mHead = new Polygon();
    mHead.addPoint(2 * mStrokeSize, 2 * mStrokeSize);
    mHead.addPoint(mWidth + 2 * mStrokeSize, mHalfHeight + 2 * mStrokeSize);
    mHead.addPoint(2 * mStrokeSize, mHalfHeight * 2 + 2 * mStrokeSize);
    mHead.addPoint(mWidth / 2 + mStrokeSize, mHalfHeight + 2 * mStrokeSize);
    setSize(mWidth + 4 * mStrokeSize, mHalfHeight * 2 + 4 * mStrokeSize);

    //
    mNode.getDockingManager().occupyDockPointForStartSign();
  }

  @Override
  public void paintComponent(java.awt.Graphics g) {
    super.paintComponent(g);

    Graphics2D graphics = (Graphics2D) g;

    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    //
    setLocation(mNode.getLocation().x - mWidth - 2 * mStrokeSize,
        mNode.getLocation().y + mNode.getWidth() / 2 - mHalfHeight - 2 * mStrokeSize);

    graphics.setColor(mColor);
    graphics.setStroke(new BasicStroke(mStrokeSize));

    if (mOutline) {
      graphics.drawPolygon(mHead);
    } else {
      graphics.drawPolygon(mHead);
      graphics.fillPolygon(mHead);
    }
  }
}
