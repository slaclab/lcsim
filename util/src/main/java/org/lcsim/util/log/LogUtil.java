package org.lcsim.util.log;

import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

/**
 * A simple set of utility methods for creating <code>Logger</code> objects.
 *
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public class LogUtil {

    private LogUtil() {
    }

    public static Logger create(final String name, Formatter formatter, final Level level) {
        final Logger logger = Logger.getLogger(name);
        logger.setUseParentHandlers(false);
        logger.setLevel(level);
        if (formatter == null) {
            formatter = new DefaultLogFormatter();
        }
        final Handler handler = new StreamHandler(System.out, formatter) {
            @Override
            public void publish(final LogRecord record) {
                super.publish(record);
                flush();
            }
        };
        handler.setLevel(Level.ALL); // Handler level is always ALL.
        logger.addHandler(handler);
        return logger;
    }

    public static Logger create(final Class<?> klass, final Formatter formatter) {
        return create(klass.getName(), formatter, Level.INFO);
    }

    public static Logger create(final Class<?> klass) {
        return create(klass, null);
    }

    public static Logger create(final Class<?> klass, final Formatter formatter, final Level level) {
        return create(klass.getName(), formatter, level);
    }

    public static void setLevel(final Logger logger, final Level level) {
        logger.setLevel(level);
        for (final Handler handler : logger.getHandlers()) {
            handler.setLevel(level);
        }
    }
}
