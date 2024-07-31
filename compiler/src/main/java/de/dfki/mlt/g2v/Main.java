package de.dfki.mlt.g2v;

import static de.dfki.mlt.g2v.Generator.generateAll;
import static de.dfki.mlt.rudimant.common.Configs.*;
import static de.dfki.mlt.rudimant.compiler.RudimantCompiler.process;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import de.dfki.lt.hfc.WrongFormatException;
import de.dfki.mlt.rudimant.common.BasicInfo;
import de.dfki.mlt.rudimant.common.ErrorInfo;
import de.dfki.mlt.rudimant.common.IncludeInfo;
import de.dfki.mlt.rudimant.compiler.CompilerMain;

public class Main {

  private static final Logger logger = LoggerFactory.getLogger(Main.class);

  public static final String CFG_AUTOMATA = "automata";
  public static final String CFG_AUTO_SCENEFLOW = "sceneflow";
  public static final String CFG_AUTO_OUTPUTDIR = "output";
  public static final String CFG_AUTO_ONTOLOGYROOT = "ontology";

  public static final String CODE_INFO_FILE = "CodeBlocks.yml";

  public static List<String> path2names(Path p) {
    ArrayList<String> result = new ArrayList<>();
    for (int i=0; i < p.getNameCount(); ++i) {
      result.add(p.getName(i).toString());
    }
    return result;
  }

  private static void walkIncludes(IncludeInfo info,
      List<CodeInfo> infos, List<CodeBlock> errors) {
    List<String> relp = new ArrayList<String>(Arrays.asList(info.getRelativePath()));
    for (CodeInfo ci: infos) {
      if (ci.relativePath.equals(relp)
          && ci.nodeBlocks.containsKey(info.getLabel())) {
        // This is the corresponding IncludeInfo
        for (ErrorInfo error : info.getErrors()) {
          errors.add(ci.addError(error, info.getLabel()));
        }
      }
    }
    for (BasicInfo i : info.getChildren()) {
      if (i instanceof IncludeInfo) {
        walkIncludes((IncludeInfo)i, infos, errors);
      }
    }
  }

  @SuppressWarnings("unchecked")
  public static List<CodeBlock> compileAll(Map<String, Object> configs)
      throws WrongFormatException, IOException {
    File confDir = (File)configs.get(CFG_CONFIG_DIRECTORY);
    File inFile = confDir.toPath().resolve(
        Path.of((String)configs.get(CFG_INPUT_FILE))).toFile();
    File rudiRoot = inFile.getParentFile();
    List<CodeInfo> infos = new ArrayList<>();
    if (configs.containsKey(CFG_AUTOMATA)) {
      Map<String, Object> automataConfigs =
          (Map<String, Object>)configs.get(CFG_AUTOMATA);
      // compile the automata now
      for (Object name : automataConfigs.keySet()) {
        Map<String, Object> automaton = (Map<String, Object>)automataConfigs.get(name);
        String flowName = (String)automaton.get(CFG_AUTO_SCENEFLOW);
        File sceneflow = new File(flowName);
        if (! sceneflow.isAbsolute()) {
          sceneflow = new File(confDir, flowName);
        }
        Path relativeOutputDir = Path.of((String)automaton.get(CFG_AUTO_OUTPUTDIR));
        if (relativeOutputDir.isAbsolute()) {
          logger.error("output directory for automaton {} must be relative!", name);
          continue;
        }
        Path outputDir = rudiRoot.toPath().resolve(relativeOutputDir);
        Path ontologyRoot = Path.of((String)automaton.get(CFG_AUTO_ONTOLOGYROOT));
        ontologyRoot = confDir.toPath().resolve(ontologyRoot);
        CodeInfo info = generateAll(sceneflow, outputDir.toFile(), ontologyRoot);
        info.relativePath = path2names(relativeOutputDir);
        infos.add(info);
      }
    }
    IncludeInfo info = process(confDir, configs);
    CompilerMain.dumpToYaml(confDir, info);
    // IncludeInfo structure
    // label: name of the file (without extension)
    // relativePath: the whole relative path from the root (dir of topmost rudi)
    // errors: list of errors, with line column and charpos
    if (! infos.isEmpty()) {
      List<CodeBlock> errors = new ArrayList<>();
      // collect the errors in info (in relation to the automata), if necessary
      walkIncludes(info, infos, errors);
      return errors;
    }
    return Collections.emptyList();
  }

  public static void dumpYaml(List<CodeBlock> cis, File outputFile)
      throws IOException {
    Writer w = new FileWriter(outputFile);
    DumperOptions options = new DumperOptions();
    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    Yaml yaml = new Yaml(options);
    yaml.dump(cis, w);
  }

  public static void main(String[] args) throws IOException {

    String pathToSceneflowFile = args[0];
    String pathToVondaProject = args[1];

    if (pathToSceneflowFile.equals("-c")) {
      Map<String, Object> conf = readConfig(pathToVondaProject);
      if (! conf.containsKey(CFG_PRINT_ERRORS)) {
        conf.put(CFG_PRINT_ERRORS, true);
      }
      List<CodeBlock> errors = compileAll(conf);
      dumpYaml(errors, new File(CODE_INFO_FILE));
    } else {
      String outPathRudiFiles = pathToVondaProject + "/src/main/rudi/";
      generateAll(new File(pathToSceneflowFile),
          new File(outPathRudiFiles),
          Path.of(pathToVondaProject, "./src/main/resources/ontology/"));
    }
  }

}
