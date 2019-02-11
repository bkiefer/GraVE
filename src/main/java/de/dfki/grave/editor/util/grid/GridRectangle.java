
/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package de.dfki.grave.editor.util.grid;

//~--- JDK imports ------------------------------------------------------------
import java.awt.Rectangle;

import de.dfki.grave.editor.Edge;
import de.dfki.grave.editor.Node;

/**
 *
 * @author Souza Putra
 */
@SuppressWarnings("serial")
public class GridRectangle extends Rectangle {

  public static final int NODE_INTERSECTION = 0;
  public static final int EDGE_INTERSECTION = 1;
  public static final int NO_INTERSECTION = -1;
  private boolean aStarPath = false;
  private final int INITIAL_WEIGHT = 1;
  private int intersectionType = -1;
  private int rowIndex;
  private int columnIndex;
  private int weight;

  public GridRectangle() {
    super();
    weight = INITIAL_WEIGHT;
  }

  public GridRectangle(int x, int y, int width, int height) {
    super(x, y, width, height);
    this.weight = INITIAL_WEIGHT;
  }

  public void setRowIndex(int rowIndex) {
    this.rowIndex = rowIndex;
  }

  public void setColumnIndex(int columnIndex) {
    this.columnIndex = columnIndex;
  }

  public void setWeight(int weight) {
    this.weight = weight;

    // System.out.println("Setting weight: " + weight + " to " + columnIndex + "," + rowIndex);
  }

  public int getRowIndex() {
    return rowIndex;
  }

  public int getColumnIndex() {
    return columnIndex;
  }

  public int getWeight() {
    return weight;
  }

  public void setIndex(int row, int column) {
    this.rowIndex = row;
    this.columnIndex = column;
  }

  public int getIntersectionType() {
    return intersectionType;
  }

  public void setIntersectionType(int intersectionType) {
    this.intersectionType = intersectionType;
  }

  public boolean isaStarPath() {
    return aStarPath;
  }

  public void setaStarPath(boolean aStarPath) {
    this.aStarPath = aStarPath;
  }

  public boolean isIntersectedbyNode(Node node) {
    double gridMinX = getX();
    double gridMaxX = getX() + getWidth();
    double gridMinY = getY();
    double gridMaxY = getY() + getHeight();
    double nodeMinX = node.getX();
    double nodeMaxX = node.getX() + node.getWidth();
    double nodeMinY = node.getY();
    double nodeMaxY = node.getY() + node.getHeight();

    return (gridMaxX >= nodeMinX) && (gridMinX <= nodeMaxX) && (gridMaxY >= nodeMinY) && (gridMinY <= nodeMaxY);
  }

  public boolean isIntersectByRectangle(Edge edge) {
    double x1 = getX() - 2;
    double x2 = getX() + getWidth() + 2;
    double y1 = getY() - 2;
    double y2 = getY() + getHeight() + 2;
    return edge.isIntersectByRectangle(x1, x2, y1, y2);
  }
}
