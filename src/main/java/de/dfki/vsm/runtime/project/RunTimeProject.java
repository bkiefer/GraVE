package de.dfki.vsm.runtime.project;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

import de.dfki.vsm.model.project.AgentConfig;
import de.dfki.vsm.model.project.PluginConfig;
import de.dfki.vsm.model.project.ProjectConfig;
import de.dfki.vsm.model.sceneflow.chart.SceneFlow;
import de.dfki.vsm.model.visicon.VisiconConfig;
import de.dfki.vsm.util.log.LOGDefaultLogger;
import de.dfki.vsm.util.xml.XMLUtilities;

/**
 * @author Gregor Mehlmann
 */
public class RunTimeProject {

    protected  boolean isNewProject = false;
    // The singelton logger instance
    protected final LOGDefaultLogger mLogger
            = LOGDefaultLogger.getInstance();

    // The project Path (added PG 11.4.2016);
    private String mProjectPath = "";
    // The sceneflow of the project
    private final SceneFlow mSceneFlow = new SceneFlow();
    // The project configuration of the project
    private final ProjectConfig mProjectConfig = new ProjectConfig();
    // The visicon configuration of the project
    private final VisiconConfig mVisiconConfig = new VisiconConfig();

    // Construct an empty runtime project
    public RunTimeProject() {
        // Do nothing
    }

    // Construct a project from a directory
    public RunTimeProject(final File file) {
        // Remember Path
        mProjectPath = file.getPath();
        // Call the local parsing method
        parse(mProjectPath);
    }

    public boolean isNewProject() {
        return isNewProject;
    }

    // Get the path of the project (added PG 11.4.2016)
    public final String getProjectPath() {
        return mProjectPath;
    }

    public void setProjectPath(String s) {
        mProjectPath = s;
    }

    // Get the name of the project's configuration
    public final String getProjectName() {
        return mProjectConfig.getProjectName();
    }

    // Set the name in the project's configuration
    public final void setProjectName(final String name) {
        mProjectConfig.setProjectName(name);
    }

    // Get a specific config from the map of plugins
    public final PluginConfig getPluginConfig(final String name) {
        return mProjectConfig.getPluginConfig(name);
    }

    // Get the list of all configured agents in the project configuation (agged PG 8.4.2016)
    public final ArrayList<String> getAgentNames() {
        return mProjectConfig.getAgentNames();
    }

    // Get a specific config from the map of plugins
    public final AgentConfig getAgentConfig(final String name) {
        return mProjectConfig.getAgentConfig(name);
    }

    // Get the sceneflow of the project
    public final SceneFlow getSceneFlow() {
        return mSceneFlow;
    }

    // Get the visicon of the project
    public final VisiconConfig getVisicon() {
        return mVisiconConfig;
    }

    // Get the project configuration (added PG 15.4.2016)
    public final ProjectConfig getProjectConfig() {
        return mProjectConfig;
    }

    public boolean parse(final String file) {
        // Check if the file is null
        if (file == null) {
            // Print an error message
            mLogger.failure("Error: Cannot parse runtime project from a bad file");
            // Return false at error
            return false;
        }
        // remember Path (e.g. EditorProject calls this without instantiation of
        // the RunTimeProject class, so mProjectPath is (re)set her (PG 11.4.2016)
        mProjectPath = file;

        // Parse the project from file
        if (parseProjectConfig(file)) {
            // Initialize the scene player

            //
            return parseSceneFlow(file)
                && parseVisiconConfig(file);
        } else {
            return false;
        }

    }

    // First attempt to parse Visual SceneMaker project files for information only
    // added PG 14.4.2016
    public boolean parseForInformation(final String file) {
        // Check if the file is null
        if (file == null) {
            // Print an error message
            mLogger.failure("Error: Cannot parse for information a runtime project from a bad file");
            // Return false at error
            return false;
        }
        // remember Path (e.g. EditorProject calls this without instantiation of
        // the RunTimeProject class, so mProjectPath is (re)set her (PG 11.4.2016)
        mProjectPath = file;

        // Parse the project from file
        return (parseProjectConfig(file)
                && parseSceneFlow(file)
                && parseSceneScript(file)
                && parseVisiconConfig(file));
    }

    // Write the project data to a directory
    public boolean write(final File file) {
        // Check if the file is null
        if (file == null) {
            // Print an error message
            mLogger.failure("Error: Cannot write runtime project into a bad file");
            // Return false at error
            return false;
        }
        // Get the absolute file for the directory
        final File base = file.getAbsoluteFile();
        // Check if the project directory does exist
        if (!base.exists()) {
            // Print a warning message in this case
            mLogger.warning("Warning: Creating a new runtime project directory '" + base + "'");
            // Try to create a project base directory
            if (!base.mkdir()) {
                // Print an error message
                mLogger.failure("Failure: Cannot create a new runtime project directory '" + base + "'");
                // Return false at error
                return false;
            }
        }
        if(mProjectPath.equals("")){
            mProjectPath = file.getPath();
        }
        // Save the project to the base directory
        return (writeProjectConfig(base)
                && writeSceneFlow(base)
                && writeSceneScript(base)
                && writeVisiconConfig(base));
    }

    // Load the executors of the project

    // TODO: Load Plugins and call methods on them via the evaluator in the interpreter
    // Make a new command type in the syntax for that purpose
    public final Object call(final String name, final String method) {
        return null;
    }

    private boolean parseProjectConfig(final String path) {
        InputStream inputStream = null;
        final File file = new File(path, "project.xml");
        if (file.exists()) {
            try {
                inputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                mLogger.failure("Error: Cannot find project configuration file '" + file + "'");
            }
        } else {
            inputStream = ClassLoader.getSystemResourceAsStream(path + System.getProperty("file.separator") + "project.xml");
            if (inputStream == null) {
                // Print an error message in this case
                mLogger.failure("Error: Cannot find project configuration file  ");
                // Return failure if it does not exist
                return false;
            }

        }

        if (!XMLUtilities.parseFromXMLStream(mProjectConfig, inputStream)) {
            mLogger.failure("Error: Cannot parse project configuration file  in path" + path);
            return false;
        }

        mLogger.message("Loaded project from path '" + path + "':\n" + mProjectConfig);
        // Return success if the project was loaded
        return true;
    }

    public boolean parseProjectConfigFromString(String xml) {
        //Parse the config file for project from a string
        InputStream stream = new ByteArrayInputStream(xml.getBytes());
        if (!XMLUtilities.parseFromXMLStream(mProjectConfig, stream)) {
            mLogger.failure("Error: Cannot parse agent");
            return false;
        }
        return true;
    }

    private boolean writeProjectConfig(final File base) {
        // Create the project configuration file
        final File file = new File(base, "project.xml");
        // Check if the configuration does exist
        if (!file.exists()) {
            // Print a warning message in this case
            mLogger.warning("Warning: Creating the new project configuration file '" + file + "'");
            // Create a new configuration file now
            try {
                // Try to create a new configuration file
                if (!file.createNewFile()) {
                    // Print an error message in this case
                    mLogger.warning("Warning: There already exists a project configuration file '" + file + "'");
                }
            } catch (final IOException exc) {
                // Print an error message in this case
                mLogger.failure("Failure: Cannot create the new project configuration file '" + file + "'");
                // Return failure if it does not exist
                return false;
            }
        }
        // Write the project configuration file
        if (!XMLUtilities.writeToXMLFile(mProjectConfig, file, "UTF-8")) {
            // Print an error message in this case
            mLogger.failure("Error: Cannot write project configuration file '" + file + "'");
            // Return failure if it does not exist
            return false;
        }
        // Print an information message in this case
        mLogger.message("Saved project configuration file '" + file + "':\n" + mProjectConfig);
        // Return success if the project was saved
        return true;
    }

    private boolean parseSceneFlow(final String path) {
        InputStream inputStream = null;
        final File file = new File(path, "sceneflow.xml");
        // Check if the configuration file does exist
        if (file.exists()) {
            try {
                inputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                mLogger.failure("Error: Cannot find sceneflow configuration file '" + file + "'");
            }
        } else {
            inputStream = ClassLoader.getSystemResourceAsStream(path + System.getProperty("file.separator") + "sceneflow.xml");
            if (inputStream == null) {
                // Print an error message in this case
                mLogger.failure("Error: Cannot find sceneflow configuration file   project ");
                // Return failure if it does not exist
                return false;
            }

        }

        if (!XMLUtilities.parseFromXMLStream(mSceneFlow, inputStream)) {
            mLogger.failure("Error: Cannot parse sceneflow file  in path" + path);
            return false;
        }
        // Perform all the postprocessing steps
        mSceneFlow.establishStartNodes();
        mSceneFlow.establishTargetNodes();
        mSceneFlow.establishAltStartNodes();
        // Print an information message in this case
        //mLogger.message("Loaded sceneflow configuration file in path '" + path + "'");
        // Return success if the project was loaded
        return true;

    }

    private boolean writeSceneFlow(final File base) {
        // Create the sceneflow configuration file
        final File file = new File(base, "sceneflow.xml");
        // Check if the configuration file does exist
        if (!file.exists()) {
            // Print a warning message in this case
            mLogger.warning("Warning: Creating the new sceneflow configuration file '" + file + "'");
            // Create a new configuration file now
            try {
                // Try to create a new configuration file
                if (!file.createNewFile()) {
                    // Print an error message in this case
                    mLogger.warning("Warning: There already exists a sceneflow configuration file '" + file + "'");
                }
            } catch (final IOException exc) {
                // Print an error message in this case
                mLogger.failure("Failure: Cannot create the new sceneflow configuration file '" + file + "'");
                // Return failure if it does not exist
                return false;
            }
        }
        // Write the sceneflow configuration file
        if (!XMLUtilities.writeToXMLFile(mSceneFlow, file, "UTF-8")) {
            // Print an error message in this case
            mLogger.failure("Error: Cannot write sceneflow configuration file '" + file + "'");
            // Return failure if it does not exist
            return false;
        }
        // Print an information message in this case
        //mLogger.message("Saved sceneflow configuration file '" + file + "':\n" + mSceneFlow);
        // Return success if the project was saved
        return true;
    }

    private boolean parseSceneScript(final String path) {
        InputStream inputStream = null;

        final File file = new File(path, "scenescript.xml");
        // Check if the configuration file does exist
        if (file.exists()) {
            try {
                inputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                mLogger.failure("Error: Cannot find scenescript configuration file '" + file + "'");
            }
        } else {
            inputStream = ClassLoader.getSystemResourceAsStream(path + System.getProperty("file.separator") + "scenescript.xml");
            if (inputStream == null) {
                // Print an error message in this case
                mLogger.failure("Error: Cannot find scenescript configuration file  ");
                // Return failure if it does not exist
                return false;
            }

        }

        // Print an information message in this case
        //mLogger.message("Loaded scenescript configuration file in path'" + path + "':\n" + mSceneScript);
        //mLogger.message("Loaded scenescript configuration file in path'" + path + "'");

        // Return success if the project was loaded
        return true;
    }

    private boolean writeSceneScript(final File base) {
        // Create the scenescript configuration file
        final File file = new File(base, "scenescript.xml");
        // Check if the configuration file does exist
        if (!file.exists()) {
            // Print a warning message in this case
            mLogger.warning("Warning: Creating the new scenescript configuration file '" + file + "'");
            // Create a new configuration file now
            try {
                // Try to create a new configuration file
                if (!file.createNewFile()) {
                    // Print an error message in this case
                    mLogger.warning("Warning: There already exists a scenescript configuration file '" + file + "'");
                }
            } catch (final IOException exc) {
                // Print an error message in this case
                mLogger.failure("Failure: Cannot create the new scenescript configuration file '" + file + "'");
                // Return failure if it does not exist
                return false;
            }
        }

        // Print an information message in this case
        //mLogger.message("Saved scenescript configuration file '" + file + "':\n" + mSceneScript);
        // Return success if the project was saved
        return true;
    }

    private boolean parseVisiconConfig(final String path) {

        InputStream inputStream = null;

        final File file = new File(path, "visicon.xml");
        // Check if the configuration file does exist
        if (file.exists()) {
            try {
                inputStream = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                mLogger.failure("Error: Cannot find visicon configuration file '" + file + "'");
            }
        } else {
            inputStream = ClassLoader.getSystemResourceAsStream(path + System.getProperty("file.separator") + "visicon.xml");
            if (inputStream == null) {
                // Print an error message in this case
                mLogger.failure("Error: Cannot find visicon configuration file  ");
                // Return failure if it does not exist
                return false;
            }

        }
        if (!XMLUtilities.parseFromXMLStream(mVisiconConfig, inputStream)) {
            mLogger.failure("Error: Cannot parse visicon configuration file  in path" + path);
            return false;
        }

        //mLogger.message("Loaded visicon configuration file in path'" + path + "'");
        // Return success if the project was loaded
        return true;
    }

    private boolean writeVisiconConfig(final File base) {
        // Create the visicon configuration file
        final File file = new File(base, "visicon.xml");
        // Check if the configuration file does exist
        if (!file.exists()) {
            // Print a warning message in this case
            mLogger.warning("Warning: Creating the new visicon configuration file '" + file + "'");
            // Create a new configuration file now
            try {
                // Try to create a new configuration file
                if (!file.createNewFile()) {
                    // Print an error message in this case
                    mLogger.warning("Warning: There already exists a visicon configuration file '" + file + "'");
                }
            } catch (final IOException exc) {
                // Print an error message in this case
                mLogger.failure("Failure: Cannot create the new visicon configuration file '" + file + "'");
                // Return failure if it does not exist
                return false;
            }
        }
        // Write the visicon configuration file
        if (!XMLUtilities.writeToXMLFile(mVisiconConfig, file, "UTF-8")) {
            // Print an error message in this case
            mLogger.failure("Error: Cannot write visicon configuration file '" + file + "'");
            // Return failure if it does not exist
            return false;
        }
        // Print an information message in this case
        //mLogger.message("Saved visicon configuration file '" + file + "':\n" + mVisiconConfig);
        // Return success if the project was saved
        return true;
    }

    // Get the hash code of the project
    protected synchronized int getHashCode() {
        int hashCode = ((mSceneFlow == null)
                ? 0
                : mSceneFlow.getHashCode());
        // TODO: Why Is The Hash Computed
        // Only Based On The SceneFlow's
        // Hash And Not Based Also On The
        // Other Project Data Structures?
        return hashCode;
    }

    public void deletePlugin(PluginConfig plugin) {
        deleteRelatedAgents(plugin);
        mProjectConfig.deleteDevice(plugin);
    }

    private void deleteRelatedAgents(PluginConfig plugin) {
        Iterator<AgentConfig> iterator = getProjectConfig().getAgentConfigList().iterator();
        while (iterator.hasNext()) {
            AgentConfig agent = iterator.next();
            if (agent.getDeviceName().equals(plugin.getPluginName())) {
                iterator.remove();
            }
        }
    }

    public void deleteAgent(AgentConfig agent) {
        mProjectConfig.deleteAgent(agent);
    }
}
