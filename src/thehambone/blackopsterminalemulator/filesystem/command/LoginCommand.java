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

package thehambone.blackopsterminalemulator.filesystem.command;

import thehambone.blackopsterminalemulator.LoginShell;
import thehambone.blackopsterminalemulator.Server;
import thehambone.blackopsterminalemulator.Terminal;
import thehambone.blackopsterminalemulator.filesystem.ExecutableFile;

/**
 * The "login" command.
 * <p>
 * This command invokes the login sequence on the current server.
 * <p>
 * Created on Nov 28, 2015.
 *
 * @author Wes Hampson
 */
public class LoginCommand extends ExecutableFile
{
    /**
     * Creates a new instance of the {@code LoginCommand} class.
     * 
     * @param id the filesystem object id
     */
    public LoginCommand(int id)
    {
        super(id, "login");
    }
    
    @Override
    public void exec(String[] args)
    {
        Server system = Terminal.getActiveLoginShell().getSystem();
        
        // Shouldn't ever happen, but in the rare event that it does...
        if (system == null) {
            Terminal.println("Error:  unknown system");
            return;
        }
        
        LoginShell newShell = system.login();
        
        // Execute new shell if the login was successful
        if (newShell != null) {
            Terminal.pushLoginShell(newShell);
            newShell.exec();
        }
    }
}
