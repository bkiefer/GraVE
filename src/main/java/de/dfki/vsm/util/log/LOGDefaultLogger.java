package de.dfki.vsm.util.log;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Gregor Mehlmann
 */
public class LOGDefaultLogger {

    // The Singelton Console Logger Instance
    private static LOGDefaultLogger sInstance = null;
    // Construct The Java Console Logger
    private static final Logger sLogger
            = Logger.getLogger(LOGDefaultLogger.class.getName());

    // Construct The Default Logger
    private LOGDefaultLogger() {
        // Log The Messages From All Levels

    }

    // Get The Singelton Logger Instance
    public static synchronized LOGDefaultLogger getInstance() {
        if (sInstance == null) {
            sInstance = new LOGDefaultLogger();
        }

        return sInstance;
    }

    // Install A Handler
    public final synchronized void install(final Handler handler) {
        sLogger.addHandler(handler);
    }

    // Remove A Handler
    public final synchronized void remove(final Handler handler) {
        sLogger.removeHandler(handler);
    }

    // Log A Severe Message
    public final synchronized void failure(final String msg) {
        sLogger.log(Level.SEVERE, msg, Thread.currentThread().getStackTrace());
    }

    // Log A Warning Message
    public final synchronized void warning(final String msg) {
        sLogger.log(Level.WARNING, msg, Thread.currentThread().getStackTrace());
    }

    // Log An Inform Message
    public final synchronized void message(final String msg) {
        sLogger.log(Level.INFO, msg, Thread.currentThread().getStackTrace());
    }

    // Log A Success Message
    public final synchronized void success(final String msg) {
        sLogger.log(Level.ALL, msg, Thread.currentThread().getStackTrace());
    }
}
