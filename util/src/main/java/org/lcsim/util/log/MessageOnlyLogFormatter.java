package org.lcsim.util.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * A log <code>Formatter</code> that prints only the message and error tracebacks.
 *
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 */
public class MessageOnlyLogFormatter extends Formatter {

    /**
     * Class constructor.
     */
    public MessageOnlyLogFormatter() {
    }

    /**
     * Format the <code>LogRecord</code> for printing
     *
     * @param record the <code>LogRecord</code> to format
     */
    @Override
    public String format(final LogRecord record) {
        final StringBuilder sb = new StringBuilder();

        // Append the message.
        sb.append(formatMessage(record) + '\n');

        // Append a traceback if there was an error thrown.
        if (record.getThrown() != null) {
            try {
                final StringWriter sw = new StringWriter();
                final PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            } catch (final Exception ex) {
            }
        }
        return sb.toString();
    }
}
