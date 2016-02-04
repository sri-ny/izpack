package com.izforge.izpack.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * A variant of the default Java logging SimpleFormatter, not being so messy
 * @author rkrell
 */
public class LogFormatter extends Formatter
{
    private final String lineSeparator = System.getProperty("line.separator");

    /**
     * Format the given LogRecord.
     *
     * @param record the log record to be formatted.
     * @return a formatted log record
     */
    @Override
    public synchronized String format(LogRecord record)
    {
        StringBuilder sb = new StringBuilder();
        if (Debug.isDEBUG())
        {
            if (record.getSourceClassName() != null)
            {
                sb.append(record.getSourceClassName());
            }
            else
            {
                sb.append(record.getLoggerName());
            }
            if (record.getSourceMethodName() != null)
            {
                sb.append(" ");
                sb.append(record.getSourceMethodName());
            }

            sb.append(lineSeparator);
            sb.append(record.getLevel().getLocalizedName());
            sb.append(": ");
        }

        // Append log message
        String message = formatMessage(record);
        sb.append(message);

        // Append stacktrace
        if (Debug.isSTACKTRACE() && (record.getThrown() != null))
        {
            sb.append(lineSeparator);
            try
            {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                sb.append(sw.toString());
            }
            catch (Exception ignored) {}
        }

        sb.append(lineSeparator);

        return sb.toString();
    }
}
