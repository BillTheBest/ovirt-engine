package org.ovirt.engine.ui.webadmin.uicommon;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ovirt.engine.ui.uicommonweb.ILogger;

public class LoggerImpl implements ILogger {

    private static final Logger logger = Logger.getLogger(LoggerImpl.class.getName());

    @Override
    public void Debug(String message) {
        logger.log(Level.FINE, message);
    }

    @Override
    public void Error(String message, RuntimeException ex) {
        logger.log(Level.SEVERE, message, ex);
    }

    @Override
    public void Info(String message) {
        logger.log(Level.INFO, message);
    }

    @Override
    public void Warn(String message) {
        logger.log(Level.WARNING, message);
    }

    public void setLogLevel(String level) {
        logger.setLevel(Level.parse(level));
    }

}
