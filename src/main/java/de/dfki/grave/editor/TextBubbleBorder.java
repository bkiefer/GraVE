package de.dfki.grave.editor;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.awt.geom.RoundRectangle2D;

import javax.swing.border.AbstractBorder;

@SuppressWarnings("serial")
class TextBubbleBorder extends AbstractBorder {

  private Color color;
  private Color corner;
  private int thickness = 4;
  private int radii = 8;
  private int pointerSize = 7;
  private Insets insets = null;
  private BasicStroke stroke = null;
  private int strokePad;
  private int pointerPad = 4;
  private int cornerWidth = radii;
  private boolean left = true;
  RenderingHints hints;

  TextBubbleBorder(Color color) {
    this(color, 4, 8, 7, color, 8);
  }

  TextBubbleBorder(Color color, int thickness, int radii, int pointerSize,
      Color corner, int cornerWidth) {
    this.thickness = thickness;
    this.radii = radii;
    this.pointerSize = pointerSize;
    this.color = color;
    this.corner = corner;
    this.cornerWidth = cornerWidth;

    stroke = new BasicStroke(thickness);
    strokePad = thickness / 2;

    pointerPad = pointerSize / 2;

    hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);

    int pad = radii/3 + strokePad;
    int bottomPad = pad + pointerSize ;
    insets = new Insets(pad, pad, bottomPad, pad);
  }

  TextBubbleBorder(Color color, int thickness, int radii, int pointerSize,
      Color corner, int cornerWidth, boolean left) {
    this(color, thickness, radii, pointerSize, corner, cornerWidth);
    this.left = left;
  }

  @Override
  public Insets getBorderInsets(Component c) {
    return insets;
  }

  @Override
  public Insets getBorderInsets(Component c, Insets insets) {
    return getBorderInsets(c);
  }

  @Override
  public void paintBorder(Component c, Graphics g, int x, int y, int width,
      int height) {

    Graphics2D g2 = (Graphics2D) g;

    int bottomLineY = height - thickness - pointerSize;

    RoundRectangle2D.Double bubble = new RoundRectangle2D.Double(0 + strokePad,
        0 + strokePad, width - thickness, bottomLineY, radii, radii);

    Polygon pointer = new Polygon();
    
    bottomLineY += thickness / 2;
    int tipY = height - strokePad - thickness / 2;
    if (left) {
      int xbase = strokePad + radii + pointerPad;
      // left point
      pointer.addPoint(xbase, bottomLineY);
      // right point
      pointer.addPoint(xbase + pointerSize, bottomLineY);
      // bottom point
      pointer.addPoint(xbase, tipY);
    } else {
      int xbase = width - (strokePad + radii + pointerPad);
      // left point
      pointer.addPoint(xbase, bottomLineY);
      // right point
      pointer.addPoint(xbase - pointerSize, bottomLineY);
      // bottom point
      pointer.addPoint(xbase - pointerSize, tipY);
    }

    Area area = new Area(bubble);
    area.add(new Area(pointer));

    g2.setRenderingHints(hints);
    // Paint the BG color of the parent, everywhere outside the clip
    // of the text bubble. This does not work here, since it should be 
    // transparent, thus the solution with a JTextField in a JPanel
    /*
    Component parent = c.getParent();
    if (parent != null) {
      Color bg = parent.getBackground();
      Rectangle rect = new Rectangle(0, 0, width, height);
      Area borderRegion = new Area(rect);
      borderRegion.subtract(area);
      g2.setClip(borderRegion);
      g2.setColor(bg);
      g2.fillRect(0, 0, width, height);
      g2.setClip(null);
    }*/

    g2.setColor(color);
    g2.setStroke(stroke);
    g2.draw(pointer);
    g2.fill(pointer);
    g2.draw(area);
    // Draw the resize area
    g2.setColor(corner);
    g2.fillRect(width - cornerWidth, bottomLineY - cornerWidth + thickness / 2, 
        cornerWidth + 1, cornerWidth + 1);
  }
}