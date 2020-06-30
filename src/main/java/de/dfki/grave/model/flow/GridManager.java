package de.dfki.grave.model.flow;

//~--- JDK imports ------------------------------------------------------------
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.dfki.grave.editor.panels.WorkSpace;

/**
 * @author Patrick
 * This class manages the node placement on the workspace.
 * Additional methods are provided for an intelligent placement
 * of nodes
 *
 * This class considers two dimensions:
 * - max node dimension
 * - grid factor
 *
 * What's the purpose of this class?
 * a) return the "closest" free node position in the grid
 *    1. on the grid (snap to grid active)
 *    2. not covering another node (inactive)
 * b) provide routing hints for edges, either new or during straighten/normalize
 *
 * Inside this class, all positions are in model coordinates, and all methods
 * take positions in model coordinates as in and output.
 */
public class GridManager {
  private static final Logger logger = LoggerFactory.getLogger(GridManager.class);

  private HashMap<Position, BasicNode> mPlacedNodes = new HashMap<>();

  private WorkSpace mWorkspace;

  public GridManager(WorkSpace ws) {
    mWorkspace = ws;
  }

  public void clear() {
    mPlacedNodes.clear();
  }

  public int gridWidth() {
    return (int)(mWorkspace.getEditorConfig().sNODEWIDTH * 
        mWorkspace.getEditorConfig().sGRID_SCALE);
  }

  /** Compute the closest free point by increasing the manhattan distance
   *  incrementally and checking all points in the same distance first
   */
  private Position getClosestFreePosition(Position p) {
    // check if p is already in set of grid points, or find alternative
    int dist = 0; // the manhattan distance for the current round
    int x = p.getXPos();
    int y = p.getYPos();
    int col = x; // x and y coordinates of the start point
    int row = y;
    while (true) {
      // systematically check all possible column distances
      for (int colDist = -dist; colDist <= dist; ++colDist) {
        x = col + colDist;
        if (x < 0) continue;
        p.setXPos(x);
        // remaining distance for rows
        int rowDist = dist - Math.abs(colDist) ;
        y = row - rowDist; // try both variants
        p.setYPos(y); 
        if (y >= 0 && ! mPlacedNodes.containsKey(p))
          return p;
        y = row + rowDist;
        p.setYPos(y);
        if (y >= 0 && ! mPlacedNodes.containsKey(p))
          return p;
      }
      ++dist;
    }
  }
  
  /** Compute a point column, row from a point in view coordinates */
  private Position getGridPoint(Position p) {
    // only 4 points are relevant if * == inputPoint
    /* 1---2
       | * |
       |   |
       3---4 */
    // compute closest point
    int gridWidth = gridWidth();
    return new Position(
        Math.round((float)Math.max(p.getXPos(), 0)/gridWidth),
        Math.round((float)Math.max(p.getYPos(), 0)/gridWidth));
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
  public Position getNodeLocation(Position p) {
    int gridWidth = gridWidth();
    // compute closest point, first getting start point closest to p in terms of
    // column and row, and then looking for a free grid point closest to that
    Position newPos = getClosestFreePosition(getGridPoint(p));
    newPos.setXPos(newPos.getXPos() * gridWidth);
    newPos.setYPos(newPos.getYPos() * gridWidth);
    return newPos;
  }

  /** Who does currently own this grid position? */
  public BasicNode positionOccupiedBy(Position p) {
    return mPlacedNodes.get(getGridPoint(p));
  }
  
  /** Release the grid point at view position p */
  private void occupyGridPosition(Position p, BasicNode n) {
    Position colRow = getGridPoint(p);
    BasicNode occupant = mPlacedNodes.get(colRow);
    if (occupant == null) {
      //logger.info("Occupy " + colRow);
      mPlacedNodes.put(colRow, n);
    } else {
      if (occupant != n)
        logger.error("Grid point to be occupied by two nodes: {} and {}",
            occupant, n);
    }
  }
  
  public void occupyGridPosition(BasicNode n) {
    occupyGridPosition(n.getPosition(), n);
  }

  /** Release the grid point at view position p */
  private void releaseGridPosition(Position p, BasicNode n) {
    Position colRow = getGridPoint(p);
    BasicNode occupant = mPlacedNodes.get(colRow);
    if (occupant == n) {
      //logger.info("Release " + colRow);
      mPlacedNodes.remove(colRow);
    } else {
      logger.info("Release request by wrong node: {} instead of {}", n, occupant);
    }
  }
  /** Release the grid point at view position p */
  public void releaseGridPosition(BasicNode n) {
    releaseGridPosition(n.getPosition(), n);
  }
}
