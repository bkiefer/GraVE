package de.dfki.mlt.g2v;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Formatter implements AutoCloseable {
  private File outPath;

  private BufferedWriter fw;

  private int indent = 0;

  private int lineNo = 1;

  private static final String blanks = "                                        ";

  /* ******************** Formatter code ******************* */

  private void writeNLs(int num) throws IOException {
    lineNo += num;
    for (int i = 0; i < num; ++i) {
      fw.write(System.lineSeparator());
    }
  }

  private int findAllNewlines(String s) {
    int start = 0;
    int num = 0;
    int next;
    while ((next = s.indexOf(System.lineSeparator(), start)) >= 0) {
      start = next + 1;
      ++num;
    }
    return num;
  }

  private void format(String exp, int numLeadingNewlines, int numLeadingTabs,
      int numAppendedNewlines) {
    try {
      writeNLs(numLeadingNewlines);
      fw.write(blanks.substring(0, 2 * numLeadingTabs));
      fw.write(exp);
      lineNo += findAllNewlines(exp);
      writeNLs(numAppendedNewlines + 1);
      fw.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    /*
    String formattedExpression = new String(new char[numLeadingNewlines]).replace("\0", "\n");
    formattedExpression += new String(new char[numLeadingTabs]).replace("\0", "  ");
    formattedExpression += exp;
    formattedExpression += new String(new char[numAppendedNewlines]).replace("\0", "\n");

    return formattedExpression;
    */
  }

  public void raw(String raw, int numLeadingNewlines, int numAppendedNewlines) {
    raw = raw.trim();
    if (raw.isEmpty()) return;
    format(
        raw.replaceAll("\n", System.lineSeparator() + blanks.substring(0, 2 * indent)),
        numLeadingNewlines, indent, numAppendedNewlines);
  }

  public void statement(String line, int numLeadingNewlines, int numAppendedNewlines) {
    format(line + ";", numLeadingNewlines, indent, numAppendedNewlines);
  }

  public void timeoutOpen(String condition, int numLeadingNewlines, int numAppendedNewlines) {
    format("timeout(" + condition + ") {", numLeadingNewlines, indent, numAppendedNewlines);
    ++indent;
  }

  public void ifOpen(String condition, int numLeadingNewlines, int numAppendedNewlines) {
    format("if(" + condition + ") {", numLeadingNewlines, indent, numAppendedNewlines);
    ++indent;
  }

  public void elseOpen(int numLeadingNewlines, int numAppendedNewlines) {
    format("} else {", numLeadingNewlines, indent - 1, numAppendedNewlines);
  }

  public void defOpen(String def, int numLeadingNewlines, int numAppendedNewlines) {
    format(def, numLeadingNewlines, indent, numAppendedNewlines);
    ++indent;
  }

  public void close(int numLeadingNewlines, int numAppendedNewlines) {
    --indent;
    format("}", numLeadingNewlines, indent, numAppendedNewlines);
  }

  public void ruleLabel(String label, int numLeadingNewlines, int numAppendedNewlines) {
    format(label + ":", numLeadingNewlines, indent, numAppendedNewlines);
  }

  public int getCurrentLineNo() {
    return lineNo;
  }

  public void transferResource(String name) {
    try {
      BufferedReader r = new BufferedReader(new InputStreamReader(
          this.getClass().getClassLoader().getResourceAsStream(name),
          StandardCharsets.UTF_8));
      String line;
      while ((line = r.readLine()) != null) {
        fw.append(line);
        fw.append(System.lineSeparator());
        lineNo++;
      }
      r.close();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void clearOutputDirectory() {
    File outDir = this.outPath;
    if (outDir.exists()) {
      for (File f : outDir.listFiles()) {
        f.delete();
      }
    }
  }

  public Formatter(File path, String name) throws IOException {
    this.outPath = path;
    File file = new File(this.outPath, name + ".rudi");
    if (file.exists()) {
      file.delete();
    }

    file.getParentFile().mkdirs();
    file.createNewFile();

    FileWriter writer = new FileWriter(file);
    fw = new BufferedWriter(writer);
  }

  @Override
  public void close() throws IOException {
    fw.close();
  }

}
