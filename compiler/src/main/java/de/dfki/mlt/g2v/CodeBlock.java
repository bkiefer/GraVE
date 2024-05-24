package de.dfki.mlt.g2v;

import java.beans.JavaBean;
import java.util.ArrayList;
import java.util.List;

import de.dfki.mlt.rudimant.common.ErrorInfo;

@JavaBean
public class CodeBlock {

  public String node; // which node does the code block belong to

  public int start, end; // the line no start and end of the code block

  // to compute the error line (starting from 1), use the following
  // err.getLocation().getBegin().getLine() - start + 1
  public List<ErrorInfo> errors = new ArrayList<>();

  CodeBlock(String n, int s) { node = n; start = s; }

  public String toString() {
    return node + "(" + start + "," + end + ")";
  }
}