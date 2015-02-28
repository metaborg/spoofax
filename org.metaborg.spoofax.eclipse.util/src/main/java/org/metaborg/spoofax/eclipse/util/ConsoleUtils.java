package org.metaborg.spoofax.eclipse.util;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

public class ConsoleUtils {
    public static MessageConsole get(String name) {
        final ConsolePlugin consolePlugin = ConsolePlugin.getDefault();
        final IConsoleManager consoleManager = consolePlugin.getConsoleManager();
        final IConsole[] existingConsoles = consoleManager.getConsoles();
        for(int i = 0; i < existingConsoles.length; i++)
            if(name.equals(existingConsoles[i].getName()))
                return (MessageConsole) existingConsoles[i];

        final MessageConsole newConsole = new MessageConsole(name, null);
        consoleManager.addConsoles(new IConsole[] { newConsole });
        return newConsole;
    }
}
