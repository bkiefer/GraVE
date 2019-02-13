/*
 * SceneflowEditor - GridManager
 */
package de.dfki.grave.editor.panels;

//~--- JDK imports ------------------------------------------------------------
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import de.dfki.grave.editor.Edge;
import de.dfki.grave.editor.Node;
import de.dfki.grave.editor.util.grid.GridConstants;
import de.dfki.grave.editor.util.grid.GridRectangle;
import de.dfki.grave.model.flow.BasicNode;
import de.dfki.grave.model.flow.SuperNode;
import de.dfki.grave.model.project.EditorConfig;

/**
 * @author Patrick
 * This class manages the node placement on the workspace.
 * Additional methods are provided for an intelligent placement
 * of nodes
 *
 * BK: I think this class should consider three dimensions:
 * - node dimension
 * - grid size (as a factor of node dimension: max(height, width))
 * - zoom factor
 *
 * What's the purpose of this class?
 * a) return the "closest" free node position in the grid
 *    1. on the grid (snap to grid active)
 *    2. not covering another node (inactive)
 * b) provide routing hints for edges, either new or during straighten/normalize
 *
 * Positions of GUI elements should be mapped to abstract positions, so that
 * all positions and dimensions are divided by zoomFactor
 */
public class GridManager {

  private static final Logger log = LoggerFactory.getLogger(GridManager.class);

  private HashSet<Point> mPlacedNodes = new HashSet<>();

  private final WorkSpace mWorkSpacePanel;

  private EditorConfig config;


  public GridManager(WorkSpace workSpace, SuperNode s) {
    mWorkSpacePanel = workSpace;
    config = mWorkSpacePanel.getEditorConfig();
  }

  public void clear() {
    mPlacedNodes.clear();
  }

  private int gridWidth() {
    int nodeSize = Math.max(config.sNODEWIDTH, config.sNODEHEIGHT);
    return (int)(nodeSize * config.sGRID_SCALE * config.sZOOM_FACTOR);
  }

  /** Where are the grid points (crosses):
   * (3 * offset + col * gridWidth, 3 * offset + row * gridHeight)
   * where gridWidth = nodeWidth * config.sGRID_SCALE * config.sZOOM_FACTOR
   * and offset = gridWidth / 4
   * Currently, we assume a square grid, and take
   * nodeWidth = nodeHeight = max(nodeWidth, nodeHeight)
   */
  void drawGrid(Graphics2D g2d, Rectangle visibleRect) {
    if (! config.sSHOWGRID) return;
    g2d.setStroke(new BasicStroke(1.0f));
    g2d.setColor(Color.GRAY.brighter());
    int gridWidth = gridWidth();

    // compute row and col of the first and last grid point
    int offset = gridWidth / 4;
    int width = gridWidth / 20;
    int col = (visibleRect.x - offset) / gridWidth;
    int lastCol = (visibleRect.x + visibleRect.width - offset) / gridWidth + 1;
    int lastRow = (visibleRect.y + visibleRect.height - offset) / gridWidth + 1;
    for (int x = 3 * offset + col * gridWidth ; col <= lastCol; ++col, x += gridWidth) {
      int row = (visibleRect.y - offset) / gridWidth;
      for (int y = 3 * offset + row * gridWidth; row <= lastRow; ++row, y += gridWidth) {
        if (mPlacedNodes.contains(new Point(x - 2*offset, y - 2*offset))) {
          g2d.setColor(Color.RED); // for debugging! Should never be visible.
        } else {
          g2d.setColor(Color.GRAY.brighter());
        }
        // draw small cross
        g2d.drawLine(x - width, y, x + width, y);
        g2d.drawLine(x, y - width, x, y + width);
      }
    }
  }

  /*
  private Point findNextFreePosition(int col, int row, int gridWidth, int offset) {
    Point p = new Point(offset + col * gridWidth, offset + row * gridWidth);
    int dist = 1; // use the manhattan distance
    while (! mPlacedNodes.contains(p)) {
      for (int c = col - dist; c <= col + dist; ++c) {
        if (c < 0) continue;
        int rdist = dist - col - c; // remaining distance for rows
        if (row - rdist >= 0) {
          p = new Point(offset + c * gridWidth, offset + (row - rdist) * gridWidth);
          if (! mPlacedNodes.contains(p)) return p;
        }
        p = new Point(offset + c * gridWidth, offset + (row + rdist) * gridWidth);
        if (! mPlacedNodes.contains(p)) return p;
      }
      ++dist;
    }
    return p;
  }
  */

  /** See comments of drawGrid for where the grid points are.
   *
   *  Since the node pos is the top left corner, in mPlacedNodes we use
   *  offset instead of 3*offset, which corresponds to the location in the
   *  models.
   *
   *  Since the inputPoint comes from mouse coordinates, which the user will point
   *  at the grid points, it must be corrected appropriately
   */
  public Point getNodeLocation(Point inputPoint) {
    int gridWidth = gridWidth();
    int offset = gridWidth / 4;
    inputPoint.translate(- gridWidth/2, - gridWidth/2);
    // only 4 points are relevant if * == inputPoint
    /* 1---2
       | * |
       |   |
       3---4 */
    // compute closest point
    int col = (int)(Math.max(inputPoint.x - offset, 0)/gridWidth);
    int row = (int)(Math.max(inputPoint.y - offset, 0)/gridWidth);
    if (Math.abs(offset + col * gridWidth - inputPoint.x)
        > Math.abs(offset + (col+1) * gridWidth - inputPoint.x)) ++col;
    if (Math.abs(offset + row * gridWidth - inputPoint.y)
        > Math.abs(offset + (row+1) * gridWidth - inputPoint.y)) ++row;
    Point p = new Point(offset + col * gridWidth, offset + row * gridWidth);

    // check if p is already in set of grid points, or find alternative
    //p = findNextFreePosition(col, row, gridWidth, offset);
    int dist = 1; // use the manhattan distance
    while (mPlacedNodes.contains(p)) {
      for (int c = col - dist; c <= col + dist; ++c) {
        if (c < 0) continue;
        int rdist = dist - col - c; // remaining distance for rows
        if (row - rdist >= 0) {
          p = new Point(offset + c * gridWidth, offset + (row - rdist) * gridWidth);
          if (! mPlacedNodes.contains(p)) break;
        }
        p = new Point(offset + c * gridWidth, offset + (row + rdist) * gridWidth);
        if (! mPlacedNodes.contains(p)) break;
      }
      ++dist;
    }

    mPlacedNodes.add(p);

    return p;
  }

  public void releaseGridPosition(Point p) {
    if (mPlacedNodes.contains(p)) {
      // System.out.println("point is in use - delete in occupied positions");
      mPlacedNodes.remove(p);
    }
  }
}
