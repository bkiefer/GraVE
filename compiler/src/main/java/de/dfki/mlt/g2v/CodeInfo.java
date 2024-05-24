package de.dfki.mlt.g2v;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.dfki.mlt.rudimant.common.ErrorInfo;


public class CodeInfo {
  public List<String> relativePath;

  // a map from node name to code block in this super node
  public Map<String, List<CodeBlock>> nodeBlocks;

  // the currently treated block, or null
  private volatile CodeBlock block;
  // the list of blocks for the current super node, a shortcut
  private volatile List<CodeBlock> blocks;

  public CodeInfo() {
    nodeBlocks = new HashMap<>();
  }

  public void addBlock(String node, int startLine) {
    block = new CodeBlock(node, startLine);
  }

  public void startNode(String sup) {
    blocks = new ArrayList<>();
    nodeBlocks.put(sup, blocks);
  }

  public CodeBlock getBlock(String node, int line) {
    List<CodeBlock> blks = nodeBlocks.get(node);
    int pos = Collections.binarySearch(blks,
        null, new Comparator<CodeBlock>() {
      @Override
      public int compare(CodeBlock o1, CodeBlock o2) {
        // TODO Auto-generated method stub
        return o1.start > line ? 1 : (o1.end < line ? -1 : 0) ;
      }
    });
    return (pos >= 0) ? blks.get(pos) : null;
  }

  public void endBlock(int currentLineNo) {
    if (currentLineNo > block.start) {
      block.end = currentLineNo;
      blocks.add(block);
      block = null;
    }
  }

  public CodeBlock addError(ErrorInfo err, String node) {
    int errBegin = err.getLocation().getBegin().getLine();
    CodeBlock cb = getBlock(node, errBegin);
    cb.errors.add(err);
    return cb;
  }
}
