package de.dfki.vsm.util.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gregor Mehlmann
 */
public class LOGDefaultLogger {

    // The Singelton Console Logger Instance
    private static LOGDefaultLogger sInstance = null;
    // Construct The Java Console Logger
    private static final Logger sLogger
            = LoggerFactory.getLogger(LOGDefaultLogger.class.getName());

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


    // Log A Severe Message
    public final synchronized void failure(final String msg) {
        sLogger.error(msg);
    }

    // Log A Warning Message
    public final synchronized void warning(final String msg) {
        sLogger.warn(msg);
    }

    // Log An Inform Message
    public final synchronized void message(final String msg) {
        sLogger.info(msg);
    }

    // Log A Success Message
    public final synchronized void success(final String msg) {
        sLogger.info(msg);
    }
}
