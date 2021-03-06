/*
 * The MIT License
 *
 * Copyright 2015-2016 Wes Hampson <thehambone93@gmail.com>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package thehambone.blackopsterminalemulator;

import java.util.HashMap;
import java.util.Map;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import thehambone.blackopsterminalemulator.filesystem.ExecutableFile;
import thehambone.blackopsterminalemulator.filesystem.command.AliciaCommand;
import thehambone.blackopsterminalemulator.filesystem.command.CatCommand;
import thehambone.blackopsterminalemulator.filesystem.command.CdCommand;
import thehambone.blackopsterminalemulator.filesystem.command.ClearCommand;
import thehambone.blackopsterminalemulator.filesystem.command.DOACommand;
import thehambone.blackopsterminalemulator.filesystem.command.DebugCommand;
import thehambone.blackopsterminalemulator.filesystem.command.DecodeCommand;
import thehambone.blackopsterminalemulator.filesystem.command.DirCommand;
import thehambone.blackopsterminalemulator.filesystem.command.EncodeCommand;
import thehambone.blackopsterminalemulator.filesystem.command.FoobarCommand;
import thehambone.blackopsterminalemulator.filesystem.command.HelloCommand;
import thehambone.blackopsterminalemulator.filesystem.command.HelpCommand;
import thehambone.blackopsterminalemulator.filesystem.command.LoginCommand;
import thehambone.blackopsterminalemulator.filesystem.command.MailCommand;
import thehambone.blackopsterminalemulator.filesystem.command.MoreCommand;
import thehambone.blackopsterminalemulator.filesystem.command.RloginCommand;
import thehambone.blackopsterminalemulator.filesystem.command.WhoCommand;
import thehambone.blackopsterminalemulator.filesystem.command.ZorkCommand;
import thehambone.blackopsterminalemulator.io.Logger;
import thehambone.blackopsterminalemulator.io.ResourceLoader;
import thehambone.blackopsterminalemulator.util.UncaughtExceptionHandler;

/**
 * This class handles program initialization.
 * <p>
 * Created on Nov 17, 2015.
 *
 * @author Wes Hampson
 */
public class Main
{
    public static final String PROGRAM_TITLE
            = "BLOTE";
    public static final String PROGRAM_SLOGAN
            = "The Call of Duty: Black Ops terminal emulator.";
    public static final String PROGRAM_SLOGAN_HTML
            = "<html>"
            + "The <i>Call of Duty: Black Ops</i> terminal emulator."
            + "</html>";
    public static final String PROGRAM_VERSION
            = "1.0-alpha";
    public static final String PROGRAM_AUTHOR = "Wes Hampson";
    public static final String PROGRAM_AUTHOR_EMAIL = "thehambone93@gmail.com";
    public static final String PROGRAM_COPYRIGHT
            = "Copyright (C) 2015-2016 " + PROGRAM_AUTHOR + ".";
    
    private static boolean debug = false;
    
    /**
     * Program entry point.
     * 
     * @param args command-line arguments
     */
    public static void main(String[] args)
    {
        /* TODO:
         * -finish adding resources
         * -TEST TEST TEST (test screen rendering on Mac OS X and Linux)
         */
        
        Logger.info(PROGRAM_TITLE);
        Logger.info("Version %s\n", PROGRAM_VERSION);
        Logger.info("Created by %s\n", PROGRAM_AUTHOR);
        
        initUncaughtExceptionHandler();
        initLookAndFeel();
        parseCommandLine(args);
        
        Map<String, Class<? extends ExecutableFile>> executables;
        Server lastServer;
        UserAccount lastUser;
        
        /* Load terminal configuration.
           Config _must_ be loaded in the following order:
               servers
               executables
               filesystem
               users
               mail */
        ResourceLoader.loadMOTD();
        lastServer = ResourceLoader.loadServerConfiguration();
        executables = registerExecutables();
        ResourceLoader.loadFileSystemConfiguration(executables);
        lastUser = ResourceLoader.loadUserConfiguration();
        ResourceLoader.loadMailConfiguration();
        
        launchTerminal(new LoginShell(lastServer, lastUser));
    }
    
    /**
     * Checks whether debug mode is on.
     * 
     * @return {@code true} if debug mode is enabled, {@code false} otherwise
     */
    public static boolean isDebugModeEnabled()
    {
        return debug;
    }
    
    /**
     * Checks whether the JVM is running on the Windows operating system.
     * 
     * @return {@code true} if Windows is being used, {@code false} otherwise
     */
    public static boolean isWindows()
    {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.contains("win");
    }
    
    /*
     * Processes options passed in via the command-line.
     */
    private static void parseCommandLine(String[] opts)
    {
        if (opts.length == 0) {
            return;
        }
        
        /* Format: '--option' or '--option=ARGUMENT' */
        String arg = "";
        String[] optArg;
        for (String opt : opts) {
            // Split option and argument by equals sign
            optArg = opt.split("=");
            opt = optArg[0];
            
            // Concatenate remaining argument tokens into one
            if (optArg.length > 1) {
                arg = "";
                for (int i = 1; i < optArg.length; i++) {
                    arg += optArg[i];
                }
            }
            
            // Parse options
            switch (opt) {
                case "--debug":
                    debug = true;
                    Logger.info("Debug mode enabled.");
                    break;
                case "--data-dir":
                    ResourceLoader.setDataDirectory(arg);
                    Logger.info("Data directory set to '%s'\n", arg);
                    break;
            }
        }
    }
    
    /*
     * Invokes the default login shell and shows the terminal window.
     */
    private static void launchTerminal(LoginShell defaultLoginShell)
    {
        Logger.info("Launching terminal...");
        
        Terminal.setTitle(PROGRAM_TITLE + " " + PROGRAM_VERSION
                + (debug ? " (debug)" : ""));
        
        Terminal.show();
        Terminal.printMOTD();
        
        // Reinvoke the login shell if the user exits. 
        while (true) {
            Terminal.pushLoginShell(defaultLoginShell);
            defaultLoginShell.exec();
            Terminal.printMOTD();
            Terminal.print(defaultLoginShell.getPrompt());
        }
    }
    
    /*
     * Initializes terminal commands by associating command names with their
     * respective classes.
     */
    public static
    Map<String, Class<? extends ExecutableFile>> registerExecutables()
    {
        Map<String, Class<? extends ExecutableFile>> exes = new HashMap<>();
        exes.put("alicia", AliciaCommand.class);
        exes.put("cat", CatCommand.class);
        exes.put("cd", CdCommand.class);
        exes.put("clear", ClearCommand.class);
        if (debug) {
            exes.put("debug", DebugCommand.class);
        }
        exes.put("decode", DecodeCommand.class);
        exes.put("dir", DirCommand.class);
        exes.put("doa", DOACommand.class);
        exes.put("encode", EncodeCommand.class);
        exes.put("foobar", FoobarCommand.class);
        exes.put("hello", HelloCommand.class);
        exes.put("help", HelpCommand.class);
        exes.put("mail", MailCommand.class);
        exes.put("login", LoginCommand.class);
        exes.put("more", MoreCommand.class);
        exes.put("rlogin", RloginCommand.class);
        exes.put("who", WhoCommand.class);
        exes.put("zork", ZorkCommand.class);
        
        return exes;
    }
    
    /*
     * Adds a handler for unchecked exceptions. When an unchecked exception is
     * thrown, the user will be shown an error message.
     */
    private static void initUncaughtExceptionHandler()
    {
        Thread.setDefaultUncaughtExceptionHandler(
                new UncaughtExceptionHandler());
    }
    
    /*
     * Sets the application look and feel to match the system's look and feel.
     */
    private static void initLookAndFeel()
    {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            Logger.error("Failed to initalize system look and feel: %s: %s\n",
                    ex.getClass().getName(), ex.getMessage());
        }
    }
}
