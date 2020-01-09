/*
 * SceneflowEditor - GridManager
 */
package de.dfki.grave.editor.panels;

//~--- JDK imports ------------------------------------------------------------
import java.awt.*;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.grave.editor.Node;
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
 *
 * Inside this class, all positions are in view coordinates, and all methods
 * take points in view coordinates as in and output.
 */
public class GridManager {
  private static final Logger logger = LoggerFactory.getLogger(GridManager.class);

  private HashMap<Point, Node> mPlacedNodes = new HashMap<>();

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
    int col = (visibleRect.x - offset) / gridWidth;
    int lastCol = (visibleRect.x + visibleRect.width - offset) / gridWidth + 1;
    int lastRow = (visibleRect.y + visibleRect.height - offset) / gridWidth + 1;
    for (int x = 3 * offset + col * gridWidth ; col <= lastCol; ++col, x += gridWidth) {
      int row = (visibleRect.y - offset) / gridWidth;
      for (int y = 3 * offset + row * gridWidth; row <= lastRow; ++row, y += gridWidth) {
        if (mPlacedNodes.containsKey(new Point(col, row))) {
          g2d.setColor(Color.RED); // for debugging! Should never be visible.
        } else {
          g2d.setColor(Color.GRAY.brighter());
        }
        // draw small cross
        int width = gridWidth / 20;
        g2d.drawLine(x - width, y, x + width, y);
        g2d.drawLine(x, y - width, x, y + width);
      }
    }
  }

  /** Compute a point column, row from a point in view coordinates */
  public Point getGridPoint(Point p) {
    // only 4 points are relevant if * == inputPoint
    /* 1---2
       | * |
       |   |
       3---4 */
    // compute closest point
    int gridWidth = gridWidth();
    int offset = gridWidth / 4;
    return new Point(Math.round((float)Math.max(p.x - offset, 0)/gridWidth),
                     Math.round((float)Math.max(p.y - offset, 0)/gridWidth));
  }

  /** Compute the closest free point by increasing the manhattan distance
   *  incrementally and checking all points in the same distance first
   */
  private Point getClosestFreePoint(Point p) {
    // check if p is already in set of grid points, or find alternative
    int dist = 0; // the manhattan distance for the current round
    int col = p.x; // x and y coordinates of the start point
    int row = p.y;
    while (true) {
      // systematically check all possible column distances
      for (int colDist = -dist; colDist <= dist; ++colDist) {
        p.x = col + colDist;
        if (p.x < 0) continue;
        // remaining distance for rows
        int rowDist = dist - Math.abs(colDist) ;
        p.y = row - rowDist; // try both variants
        if (p.y >= 0 && ! mPlacedNodes.containsKey(p))
          return p;
        p.y = row + rowDist;
        if (p.y >= 0 && ! mPlacedNodes.containsKey(p))
          return p;
      }
      ++dist;
    }
  }

  /** See comments of drawGrid for where the grid points are.
   *
   *  Since the node pos is the top left corner, in mPlacedNodes we use
   *  offset instead of 3*offset, which corresponds to the location in the
   *  models.
   *
   *  Since the inputPoint comes from mouse coordinates, which the user will point
   *  at the grid points, it must be corrected appropriately
   *
   *  @param p a point in view coordinates
   *  @return a free grid position closest to inputPoint in view coordinates
   */
  public Point getNodeLocation(Point p) {
    int gridWidth = gridWidth();
    int offset = gridWidth / 4;
    // compute closest point, first getting start point closest to p in terms of
    // column and row, and then looking for a free grid point closest to that
    Point colRow = getClosestFreePoint(getGridPoint(p));
    return new Point(offset + colRow.x * gridWidth, offset + colRow.y * gridWidth);
  }

  /** Who does currently own this grid position? */
  public Node positionOccupiedBy(Point p) {
    return mPlacedNodes.get(getGridPoint(p));
  }

  /** Release the grid point at view position p */
  public void occupyGridPosition(Point p, Node n) {
    Point colRow = getGridPoint(p);
    Node occupant = mPlacedNodes.get(colRow);
    if (occupant == null) {
      //logger.info("Occupy " + colRow);
      mPlacedNodes.put(colRow, n);
    } else {
      if (occupant != n)
        logger.error("Grid point to be occupied by two nodes: {} and {}",
            occupant, n);
    }
  }

  /** Release the grid point at view position p */
  public void releaseGridPosition(Point p, Node n) {
    Point colRow = getGridPoint(p);
    Node occupant = mPlacedNodes.get(colRow);
    if (occupant == n) {
      //logger.info("Release " + colRow);
      mPlacedNodes.remove(colRow);
    } else {
      logger.info("Release request by wrong node: {} instead of {}", n, occupant);
    }
  }
}
