/*
 * The MIT License
 *
 * Copyright 2015-2016 thehambone <thehambone93@gmail.com>.
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

package thehambone.blackopsterminalemulator.filesystem.command;

import thehambone.blackopsterminalemulator.LoginShell;
import thehambone.blackopsterminalemulator.Terminal;
import thehambone.blackopsterminalemulator.filesystem.ExecutableFile;
import thehambone.blackopsterminalemulator.filesystem.FileSystem;
import thehambone.blackopsterminalemulator.filesystem.FileSystemObject;
import thehambone.blackopsterminalemulator.filesystem.HomeDirectory;

/**
 * The "cd" command.
 * <p>
 * This command changes the current working directory. If no arguments are
 * provided, it will print the current directory.
 * <p>
 * This command seems buggy by nature in the way it handles complex paths. On
 * most real-world computers, if the user provides a relative path, the shell
 * will attempt to locate the desired directory by starting at the current
 * directory and traversing the nodes around it. The Black Ops Terminal, on the
 * other hand, has no distinction between absolute and relative paths. The
 * terminal will change the current directory to the desired node regardless of
 * how deep it is in the filesystem tree.
 * <p>
 * Created on Nov 28, 2015.
 *
 * @author Wes Hampson <thehambone93@gmail.com>
 */
public class CdCommand extends ExecutableFile
{
    /**
     * Creates a new instance of the {@code CdCommand} class.
     * 
     * @param id the filesystem object id
     */
    public CdCommand(int id)
    {
        super(id, "cd");
    }
    
    /*
     * Prints the current working directory
     */
    private void printCurrentDirectory()
    {
        LoginShell shell = Terminal.getActiveLoginShell();
        Terminal.println(shell.getCurrentDirectory().getPath());
    }
    
    /*
     * Splits a string by the character stored in FILE_SEPARATOR_CHAR. This
     * differs from String.split() in that it preserves empty tokens.
     */
    private String[] tokenizePath(String path)
    {
        // Count tokens
        int tokenCount = 1;
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == FILE_SEPARATOR_CHAR) {
                tokenCount++;
            }
        }
        
        String[] tokens = new String[tokenCount];
        
        // Split the string
        int tokenIndex = 0;
        String tokenBuffer = "";
        char c;
        for (int i = 0; i < path.length(); i++) {
            c = path.charAt(i);
            if (c == FILE_SEPARATOR_CHAR) {
                tokens[tokenIndex++] = tokenBuffer;
                tokenBuffer = "";
            } else {
                tokenBuffer += c;
            }
        }
        tokens[tokenIndex] = tokenBuffer;
        
        return tokens;
    }
    
    @Override
    public void exec(String[] args)
    {
        LoginShell shell = Terminal.getActiveLoginShell();
        FileSystem fileSystem = shell.getSystem().getFileSystem();
        HomeDirectory currentUserHomeDir = shell.getUser().getHomeDirectory();
        
        // Print the current working directory if no arguments provided
        if (args.length == 0) {
            printCurrentDirectory();
            return;
        }
        
        /*
         * The path resolution code below is shared with the "cat" command.
         */
        
        // Split path
        String path = args[0];
        String[] pathTokens = tokenizePath(path);
        
        /*
         * This mocks weird behavhor exhibited by the actual terminal. If there
         * are more than two tokens and first two tokens are empty, the command
         * will behave as if no arguments were supplied.
         */
        if (pathTokens.length > 2
                && (pathTokens[0].isEmpty() && pathTokens[1].isEmpty())) {
            printCurrentDirectory();
            return;
        }
        
        String token;
        FileSystemObject fso;
        FileSystemObject currentObj = shell.getCurrentDirectory();
        boolean wasLastNodePopOperator = false;
        
        /* Loop through the tokenized path; handle each token with respect to
           the previous token
        */
        for (int i = 0; i < pathTokens.length; i++) {
            token = pathTokens[i];
            
            /* Change to root if the fitst token is empty, throw an error if any
               other token is empty
            */
            if (token.isEmpty()) {
                if (i == 0) {
                    currentObj = fileSystem.getRoot();
                    continue;
                } else if (i == pathTokens.length - 1) {
                    continue;
                }
                Terminal.println("Error:  Invalid Path");
                return;
            }
            
            // Ignore if token is current directory operator
            if (token.equals(".")) {
                continue;
            }
            
            /* Move up a node if the token is the "up" operator. For some
               reason, the actual terminal only acknowldges the first "up"
               operator if there are multiple in a row.
            */
            if (token.equals("..")) {
                if (wasLastNodePopOperator) {
                    continue;
                }
                if (currentObj.hasParent()) {
                    currentObj = currentObj.getParent();
                }
                wasLastNodePopOperator = true;
                continue;
            }
            
            fso = fileSystem.getFileSystemObject(token);
            
            
            if (fso == null) {
                Terminal.println("Error:  Invalid Path");
                return;
            }
            
            // Disallow the traversal of other users' homedirs
            if (fso instanceof HomeDirectory && fso != currentUserHomeDir) {
                Terminal.println("Error:  Insufficient Permissions");
                return;
            }
            currentObj = fso;
            wasLastNodePopOperator = false;
        }
        
        // Set the new working directory
        shell.setCurrentDirectory(currentObj);
    }
}
