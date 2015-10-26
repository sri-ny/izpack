package com.izforge.izpack.util;

import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.api.exception.UserInterruptException;

import jline.Terminal;
import jline.UnsupportedTerminal;
import jline.console.ConsoleReader;
import jline.console.completer.CandidateListCompletionHandler;
import jline.console.completer.FileNameCompleter;
import jline.internal.Log;

/**
 * I/O streams to support prompting and keyboard input from the console.
 *
 * @author Tim Anderson
 */
public class Console
{
    private static final Logger logger = Logger.getLogger(Console.class.getName());

    private  static final java.io.Console console = System.console();

    /**
     * Console reader.
     */
    private ConsoleReader consoleReader;

    /**
     * Check if consoleReader failed to load.
     */
    private boolean consoleReaderFailed = false;

    /**
     * File name completer allows for tab completion on files and directories.
     */
    private final FileNameCompleter fileNameCompleter = new FileNameCompleter();

    /**
     * Constructs a <tt>Console</tt> with <tt>System.in</tt> and <tt>System.out</tt> as the I/O streams.
     */
    public Console()
    {
        try
        {
            Log.setOutput(new PrintStream(new OutputStream() {
                @Override
                public void write(int b) throws IOException
                {
                }
            }));
            this.consoleReader = new ConsoleReader("IzPack", new FileInputStream(FileDescriptor.in), System.out, null);
            this.consoleReader.setHandleUserInterrupt(true);
            this.consoleReader.setPaginationEnabled(true);
            Terminal terminal = consoleReader.getTerminal();
            if (terminal == null || terminal instanceof UnsupportedTerminal)
            {
                consoleReader.shutdown();
                throw new Throwable("Terminal not initialized");
            }
        }
        catch (Throwable t)
        {
            consoleReaderFailed = true;
            logger.log(Level.WARNING, "Cannot initialize the console reader. Default to regular input stream.", t);
        }

    }

    /**
     * Read a character from the console.
     * @return The character, or -1 if an EOF is received.
     * @throws IOException If an I/O error occurs
     */
    public int read() throws IOException
    {
        if (consoleReaderFailed)
        {
            return console.reader().read();
        }
        else
        {
            return consoleReader.readCharacter();
        }
    }

    /**
     * Reads a line of text.  A line is considered to be terminated by any one
     * of a line feed ('\\n'), a carriage return ('\\r'), or a carriage return
     * followed immediately by a linefeed.
     *
     * @return a String containing the contents of the line, not including any line-termination characters, or
     *         null if the end of the stream has been reached
     * @throws IOException if an I/O error occurs
     */
    public String readLine() throws IOException
    {
        if (consoleReaderFailed)
        {
            return readLineDefaultInput();
        }
        else
        {
            try
            {
                return consoleReader.readLine();
            }
            catch (jline.console.UserInterruptException e)
            {
                throw new UserInterruptException("CTRL-C pressed", e);
            }
        }
    }

    /**
     * Flush the console output stream.
     *
     * @throws IOException if an I/O error occurs
     */
    public void flush() throws IOException
    {
        if (consoleReaderFailed)
        {
            console.flush();
        }
        else
        {
            consoleReader.flush();
        }
    }

    public void paginate(String text) throws IOException
    {
        if (consoleReaderFailed)
        {
            paginateText(text);
        }
        else
        {
            consoleReader.printColumns(getLines(text));
        }
        flush();
    }

    private List<CharSequence> getLines(String text)
    {
        List<CharSequence> lines = new LinkedList<CharSequence>();
        StringTokenizer line = new StringTokenizer(text, "\n");
        while (line.hasMoreTokens())
        {
            String token = line.nextToken();
            lines.add(token);
        }
        return lines;
    }

    /**
     * Pages through the supplied text.
     * This simulates the behavior of {@link ConsoleReader} when paginating text to get the same look & feel
     * regardless whether it can be initialized.
     *
     * @param text    the text to display
     * @return <tt>true</tt> if paginated through, <tt>false</tt> if terminated
     * @throws IOException
     */
    private void paginateText(String text) throws IOException
    {
        final ResourceBundle resources = ResourceBundle.getBundle(CandidateListCompletionHandler.class.getName());
        final Collection<? extends CharSequence> items = getLines(text);

        int width = 80;
        int height = 24;

        int maxWidth = 0;
        for (CharSequence item : items) {
            maxWidth = Math.max(maxWidth, item.length());
        }
        maxWidth = maxWidth + 3;

        int showLines = height - 1; // page limit
        StringBuilder buff = new StringBuilder();
        for (CharSequence item : items) {
            if ((buff.length() + maxWidth) > width) {
                println(buff.toString());
                buff.setLength(0);

                if (--showLines == 0) {
                    // Overflow
                    print(resources.getString("DISPLAY_MORE"));
                    int c = read();
                    if (c == '\r' || c == '\n') {
                        // one step forward
                        showLines = 1;
                    }
                    else if (c != 'q') {
                        // page forward
                        showLines = height - 1;
                    }

                    print('\b', resources.getString("DISPLAY_MORE").length());
                    if (c == 'q') {
                        // cancel
                        break;
                    }
                }
            }

            // NOTE: toString() is important here due to AnsiString being retarded
            buff.append(item.toString());
            for (int i = 0; i < (maxWidth - item.length()); i++) {
                buff.append(' ');
            }
        }

        if (buff.length() > 0) {
            println(buff.toString());
        }
    }

    private void print(final char c, final int num) throws IOException {
        if (num == 1) {
            print(String.valueOf(c));
        }
        else {
            char[] chars = new char[num];
            Arrays.fill(chars, c);
            print(String.copyValueOf(chars));
        }
    }

    /**
     * Prints a message to the console.
     *
     * @param message the message to print
     */
    public void print(String message)
    {
        if (console != null)
        {
            console.printf("%s", message);
            console.flush();
        }
        else
        {
            // Fix tests
            System.out.print(message);
        }
    }

    /**
     * Prints a new line.
     */
    public void println()
    {
        if (console != null)
        {
            console.printf("\n");
        }
        else
        {
            // Fix UserInputConsoleTest
            System.out.println();
        }
    }

    /**
     * Prints a message to the console with a new line.
     *
     * @param message the message to print
     */
    public void println(String message)
    {
        if (console != null)
        {
            console.printf("%s\n", message);
        }
        else
        {
            // Fix tests
            System.out.println(message);
        }
    }

    /**
     * Displays a prompt and waits for numeric input.
     *
     * @param prompt the prompt to display
     * @param min    the minimum allowed value
     * @param max    the maximum allowed value
     * @param eof    the value to return if end of stream is reached
     * @return a value in the range of <tt>from..to</tt>, or <tt>eof</tt> if the end of stream is reached
     */
    public int prompt(String prompt, int min, int max, int eof)
    {
        return prompt(prompt, min, max, min - 1, eof);
    }

    /**
     * Displays a prompt and waits for numeric input.
     *
     * @param prompt       the prompt to display
     * @param min          the minimum allowed value
     * @param max          the maximum allowed value
     * @param defaultValue the default value to use, if no input is entered. Use a value {@code < min} if there is no
     *                     default
     * @param eof          the value to return if end of stream is reached
     * @return a value in the range of <tt>from..to</tt>, or <tt>eof</tt> if the end of stream is reached
     */
    public int prompt(String prompt, int min, int max, int defaultValue, int eof)
    {
        int result = min - 1;
        try
        {
            do
            {
                println(prompt);
                String value = readLine();
                if (value != null)
                {
                    value = value.trim();
                    if (value.isEmpty() && defaultValue >= min)
                    {
                        // use the default value
                        result = defaultValue;
                        break;
                    }
                    try
                    {
                        result = Integer.valueOf(value);
                    }
                    catch (NumberFormatException ignore)
                    {
                        // loop round to try again
                    }
                }
                else
                {
                    // end of stream
                    result = eof;
                    break;
                }
            }
            while (result < min || result > max);
        }
        catch (IOException e)
        {
            logger.log(Level.WARNING, e.getMessage(), e);
            result = eof;
        }
        return result;
    }

    /**
     * Displays a prompt and waits for input.
     * Allows auto completion of files and directories.
     * Except a path to a file or directory.
     * Ensure to expand the tilde character to the user's home directory.
     * If the input ends with a file separator we will trim it to keep consistency.
     *
     * @param prompt       the prompt to display
     * @param defaultValue the default value to use, if no input is entered
     * @return the user input value; if the user input is empty (return key pressed) return defaultValue
     */
    public String promptLocation(String prompt, String defaultValue)
    {
        if (consoleReaderFailed)
        {
            return prompt(prompt, defaultValue);
        }
        String result;
        consoleReader.addCompleter(fileNameCompleter);

        println(prompt);
        try
        {
            while ((result = consoleReader.readLine().trim()) != null)
            {
                if (result.startsWith("~"))
                {
                    result = result.replace("~", System.getProperty("user.home"));
                }
                if (result.endsWith(File.separator) && result.length() > 1)
                {
                    result = result.substring(0, result.length()-1);
                }
                if (result.isEmpty())
                {
                    result = defaultValue;
                }
                break;
            }
        }
        catch (IOException e)
        {
            result = null;
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        finally
        {
            consoleReader.removeCompleter(fileNameCompleter);
        }

        return result;
    }

    /**
     * Displays a prompt and waits for input.
     * Expects a password, characters with be mased with the echoCharacter "*"
     *
     * @param prompt       the prompt to display
     * @param defaultValue the default value to use, if no input is entered
     * @return the user input value; if the user input is empty (return key pressed) return defaultValue
     */
    public String promptPassword(String prompt, String defaultValue)
    {
        if (consoleReaderFailed)
        {
            char[] passwd;
            try
            {
                passwd = readPasswordDefaultInput(defaultValue, "%s\n", prompt);
                return new String(passwd);
            }
            catch (IOException e)
            {
                return defaultValue;
            }
        }

        int ch;
        String result = "";

        String backspace = "\b \b";
        String echoCharacter = "*";
        StringBuilder stringBuilder = new StringBuilder();

        println(prompt);
        boolean submitted = false;
        try
        {
            while(!submitted)
            {
                switch (ch = consoleReader.readCharacter())
                {
                    case -1:
                    case '\n':
                    case '\r':
                        println("");
                        result = stringBuilder.toString();
                        submitted = true;
                        break;
                    case KeyEvent.VK_BACK_SPACE:
                    case KeyEvent.VK_DELETE:
                        if (stringBuilder.length() > 0)
                        {
                            print(backspace);
                            stringBuilder.setLength(stringBuilder.length() - 1);
                        }
                        break;
                    default:
                        print(echoCharacter);
                        stringBuilder.append((char) ch);
                }
            }
        }
        catch (IOException e)
        {
            result = null;
            logger.log(Level.WARNING, e.getMessage(), e);
        }

        if(result != null && result.isEmpty())
        {
            result = defaultValue;
        }
        return result;
    }

    /**
     * Displays a prompt and waits for input.
     *
     * @param prompt       the prompt to display
     * @param defaultValue the default value to use, if no input is entered
     * @return the user input value; if the user input is empty (return key pressed) return defaultValue
     */
    public String prompt(String prompt, String defaultValue)
    {
        String result = null;
        try
        {
            println(prompt);
            result = readLine();
            if (result != null && result.isEmpty())
            {
                result = defaultValue;
            }
        }
        catch (IOException e)
        {
            result = null;
            logger.log(Level.WARNING, e.getMessage(), e);
        }
        return result;
    }

    /**
     * Prompts for a value from a set of values.
     *
     * @param prompt the prompt to display
     * @param values the valid values
     * @return the user input value; if the user input is empty (return key pressed) return defaultValue
     */
    public String prompt(String prompt, String[] values)
    {
        return prompt(prompt, values, "");
    }

    /**
     * Prompts for a value from a set of values.
     *
     * @param prompt the prompt to display
     * @param values the valid values
     * @param defaultValue  the default value to return when the user input is empty
     * @return the user input value; if the user input is empty (return key pressed) return defaultValue
     */
    public String prompt(String prompt, String[] values, String defaultValue)
    {
        while (true)
        {
            String input = prompt(prompt, defaultValue);
            if (input == null)
            {
                return input;
            }
            else
            {
                for (String value : values)
                {
                    if (value.equalsIgnoreCase(input))
                    {
                        return value;
                    }
                }
            }
        }
    }

    private String readLineDefaultInput() throws IOException {
        return console.readLine();
    }

    private char[] readPasswordDefaultInput(String defaultValue, String format, Object... args)
            throws IOException {
        char[] result;
        if (console != null)
        {
           result = console.readPassword(format, args);
           if (result.length == 0)
           {
               result = defaultValue!=null?defaultValue.toCharArray():null;
           }
        }
        else
        {
            // Fix ConsolePasswordGroupFieldTest
            String line = readLine();
            result = line!=null ? line.toCharArray() : null;
        }
        return result;
    }

    public void useDefaultInput()
    {
        consoleReaderFailed = true;
    }
}
