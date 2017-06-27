package org.lcsim.util.log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * <p>
 * The default log formatter for logging in LCSim, primarily from Driver classes.
 * <p>
 * This will display log messages in the following format:<br/>
 * <pre>date | loggerName | methodName | level | message
 * exception traceback (if exists)</pre>
 * <p>
 * The 
 * 
 * @author Jeremy McCormick <jeremym@slac.stanford.edu>
 *
 */
public class DefaultLogFormatter extends Formatter {
       
    public DefaultLogFormatter() {        
    }
          
    public String format(LogRecord record) {
        StringBuilder sb = new StringBuilder();
        
        // Append the header.
        sb.append(new Date(record.getMillis()) + " " +
                record.getLoggerName() + " " + 
                record.getSourceMethodName() + '\n'); 

        // Append the level and message.
        sb.append(record.getLevel().getLocalizedName() + ": " + formatMessage(record) + '\n');
        
        // Append an Exception traceback if applicable.
        if (record.getThrown() != null) {
            try {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            } catch (Exception ex) {
            }
        }        
        return sb.toString();
    }    
}
