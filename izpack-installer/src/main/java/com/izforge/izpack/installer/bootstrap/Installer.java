/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2003 Jonathan Halliday
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.installer.bootstrap;

import com.izforge.izpack.api.data.AutomatedInstallData;
import com.izforge.izpack.api.data.Overrides;
import com.izforge.izpack.core.data.DefaultOverrides;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.installer.automation.AutomatedInstaller;
import com.izforge.izpack.installer.console.ConsoleInstallerAction;
import com.izforge.izpack.installer.container.impl.AutomatedInstallerContainer;
import com.izforge.izpack.installer.container.impl.InstallerContainer;
import com.izforge.izpack.util.Debug;
import com.izforge.izpack.util.StringTool;
import org.apache.commons.io.FilenameUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * The program entry point. Selects between GUI and text install modes.
 *
 * @author Jonathan Halliday
 * @author René Krell
 */
public class Installer
{
    /**
     * Used to keep track of the current installation mode.
     */
    private static int installerMode = 0;
    private static Logger logger;

    public static final int INSTALLER_GUI = 0, INSTALLER_AUTO = 1, INSTALLER_CONSOLE = 2;

    public static final String LOGGING_CONFIGURATION = "/com/izforge/izpack/installer/logging/logging.properties";

    /*
     * The main method (program entry point).
     *
     * @param args The arguments passed on the command-line.
     */
    public static void main(String[] args)
    {
        try
        {
            initializeLogging();
            Installer installer = new Installer();
            installer.start(args);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private static void initializeLogging()
    {
        LogManager manager = LogManager.getLogManager();
        InputStream stream;
        try
        {
            stream = Installer.class.getResourceAsStream(LOGGING_CONFIGURATION);
            if (stream != null)
            {
                manager.readConfiguration(stream);
                //System.out.println("Read logging configuration from resource " + LOGGING_CONFIGURATION);
            }
            else
            {
                //System.err.println("Logging configuration resource " + LOGGING_CONFIGURATION + " not found");
            }
        }
        catch (IOException e)
        {
            //System.err.println("Error loading logging configuration resource " + LOGGING_CONFIGURATION + ": " + e);
        }

        Logger rootLogger = Logger.getLogger("com.izforge.izpack");
        rootLogger.setUseParentHandlers(false);
        if (Debug.isDEBUG())
        {
            rootLogger.setLevel(Level.FINE);
        }
        else
        {
            rootLogger.setLevel(Level.INFO);
        }

        logger = Logger.getLogger(Installer.class.getName());
        logger.info("Logging initialized at level '" + rootLogger.getLevel() + "'");
    }

    private void start(String[] args)
    {
        logger.info("Commandline arguments: " + StringTool.stringArrayToSpaceSeparatedString(args));

        // OS X tweaks
        if (System.getProperty("mrj.version") != null)
        {
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "IzPack");
            System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
            System.setProperty("com.apple.mrj.application.live-resize", "true");
        }

        try
        {
            Iterator<String> args_it = Arrays.asList(args).iterator();

            int type = INSTALLER_GUI;
            ConsoleInstallerAction consoleAction = ConsoleInstallerAction.CONSOLE_INSTALL;
            String path = null;
            String langcode = null;
            String media = null;
            String defaultsFile = null;

            while (args_it.hasNext())
            {
                String arg = args_it.next().trim();
                try
                {
                    if ("-console".equalsIgnoreCase(arg))
                    {
                        type = INSTALLER_CONSOLE;
                    }
                    else if ("-auto".equalsIgnoreCase(arg))
                    {
                        type = INSTALLER_AUTO;
                    }
                    else if ("-defaults-file".equalsIgnoreCase(arg))
                    {
                        defaultsFile = args_it.next().trim();
                    }
                    else if ("-options-template".equalsIgnoreCase(arg))
                    {
                        //TODO Make this available also for GUI installations.
                        type = INSTALLER_CONSOLE;
                        consoleAction = ConsoleInstallerAction.CONSOLE_GEN_TEMPLATE;
                        path = args_it.next().trim();
                    }
                    else if ("-options".equalsIgnoreCase(arg))
                    {
                        //TODO Make this available also for GUI installations.
                        type = INSTALLER_CONSOLE;
                        consoleAction = ConsoleInstallerAction.CONSOLE_FROM_TEMPLATE;
                        path = args_it.next().trim();
                    }
                    else if ("-options-system".equalsIgnoreCase(arg))
                    {
                        //TODO Make this available also for GUI installations.
                        type = INSTALLER_CONSOLE;
                        consoleAction = ConsoleInstallerAction.CONSOLE_FROM_SYSTEMPROPERTIES;
                    }
                    else if ("-options-auto".equalsIgnoreCase(arg))
                    {
                        //TODO Make this available also for GUI installations.
                        type = INSTALLER_CONSOLE;
                        consoleAction = ConsoleInstallerAction.CONSOLE_FROM_SYSTEMPROPERTIESMERGE;
                        path = args_it.next().trim();
                    }
                    else if ("-language".equalsIgnoreCase(arg))
                    {
                        langcode = args_it.next().trim();
                    }
                    else if ("-media".equalsIgnoreCase(arg))
                    {
                        media = args_it.next().trim();
                    }
                    else
                    {
                        type = INSTALLER_AUTO;
                        path = arg;
                    }
                }
                catch (NoSuchElementException e)
                {
                    logger.log(Level.SEVERE, "Option \"" + arg + "\" requires an argument", e);
                    System.exit(1);
                }
            }

            Overrides defaults = getDefaults(defaultsFile);
            if (type == INSTALLER_AUTO && path == null && defaults == null)
            {
                logger.log(Level.SEVERE,
                        "Unattended installation mode needs either a defaults file specified by '-defaults-file'" +
                        " or an installation record XML file as argument");
                System.exit(1);
            }

            launchInstall(type, consoleAction, path, langcode, media, defaults, args);

        }
        catch (Exception e)
        {
            logger.log(Level.SEVERE, e.getMessage(), e);
            System.exit(1);
        }
    }

    public Overrides getDefaults(String path) throws IOException
    {
        File overridePropFile = null;

        if (path != null)
        {
            overridePropFile = new File(path);
        }
        else
        {
            try
            {
                File jarFile = new File(
                        DefaultVariables.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                String jarDir = jarFile.getParentFile().getPath();
                overridePropFile = new File(jarDir, FilenameUtils.getBaseName(jarFile.getPath()) + ".defaults");
                if (!overridePropFile.exists())
                {
                    overridePropFile = null;
                }
            }
            catch (URISyntaxException e) { /* Should not happen */ }
        }

        if (overridePropFile != null)
        {
            return new DefaultOverrides(overridePropFile);
        }

        return null;
    }

    private void launchInstall(int type, ConsoleInstallerAction consoleAction, String path, String langCode,
                               String mediaDir, Overrides defaults, String[] args) throws Exception
    {
        // if headless, just use the console mode
        if (type == INSTALLER_GUI && GraphicsEnvironment.isHeadless())
        {
            type = INSTALLER_CONSOLE;
        }

        installerMode = type;

        switch (type)
        {
            case INSTALLER_GUI:
                InstallerGui.run(langCode, mediaDir, defaults);
                break;

            case INSTALLER_AUTO:
                launchAutomatedInstaller(path, mediaDir, defaults, args);
                break;

            case INSTALLER_CONSOLE:
                InstallerConsole.run(consoleAction, path, langCode, mediaDir, defaults, args);
                break;
        }
    }

    /**
     * Launches an {@link AutomatedInstaller}.
     *
     * @param path     the input file path
     * @param mediaDir the multi-volume media directory. May be <tt>null</tt>
     * @param defaults the overrides, pre-initialized with a file name but not loaded
     * @param args more command line arguments
     * @throws Exception for any error
     */
    private void launchAutomatedInstaller(String path, String mediaDir, Overrides defaults, String[] args) throws Exception
    {
        InstallerContainer container = new AutomatedInstallerContainer();

        if (defaults != null)
        {
            defaults.setInstallData(container.getComponent(AutomatedInstallData.class));
            defaults.load();
            logger.info("Loaded " + defaults.size() + " override(s) from " + defaults.getFile());

            DefaultVariables variables = container.getComponent(DefaultVariables.class);
            variables.setOverrides(defaults);
        }

        AutomatedInstaller automatedInstaller = container.getComponent(AutomatedInstaller.class);
        automatedInstaller.init(path, mediaDir, args);
        automatedInstaller.doInstall();
    }

    public static int getInstallerMode() {
        return installerMode;
    }

}
