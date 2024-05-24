package de.dfki.interrupt;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import de.dfki.lt.hfc.WrongFormatException;
import de.dfki.interrupt.Constants;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

/**
 * The main class to start the ReCo / Socrates test environment
 */
public class Main implements Constants {
  public static Map<String, Object> configs;
  public static File confDir;

  final static Object [][] defaults = {
      { CFG_VISUALISE, false , "v" },
      { CFG_ONTOLOGY_FILE, "src/main/resources/ontology/introduction.yml", "o" },
  };

  public static Map<String, Object> defaultConfig() {
    configs = new LinkedHashMap<String, Object>();
    for (Object[] pair : defaults) {
      configs.put((String)pair[0], pair[1]);
    }
    return configs;
  }

  static Object getDefault(String key) {
    Object result = null;
    for (Object[] pair : defaults) {
      if (key.equals(pair[0])) {
        result = pair[1];
        break;
      }
    }
    return result;
  }

  static void setDefault(String key, Map<String, Object> configs) {
    if (! configs.containsKey(key)) {
      configs.put(key, getDefault(key));
    }
  }

  @SuppressWarnings("unchecked")
  public static Map<String, Object> readConfig(String confname)
      throws FileNotFoundException {
    Yaml yaml = new Yaml();
    File confFile = new File(confname);
    confDir = confFile.getParentFile();
    return (Map<String, Object>) yaml.load(new FileReader(confFile));
  }

  public static void main(String[] args)
      throws IOException, WrongFormatException, InterruptedException {
    //BasicConfigurator.configure();

    OptionParser parser = new OptionParser("c:");
    // parser.accepts("help");
    OptionSet options = null;

    //List files = null;
    confDir = new File(".");
    String confName = "config.yml";

    try {
      options = parser.parse(args);
      //files = options.nonOptionArguments();
      if (options.has("c")) {
        confName = (String)options.valueOf("c");
      }
      configs = (confName != null) ? readConfig(confName) : defaultConfig();
    } catch (OptionException ex) {
      usage("Error parsing options: " + ex.getMessage());
    }

    StubClient stub = new StubClient();
    stub.init(confDir, configs);
    stub.startListening();
  }

  private static void usage(String message) {
    System.out.println(message);
    System.out.println("[-c confFile]");
  }
}
