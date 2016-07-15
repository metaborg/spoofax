package org.metaborg.spoofax.core.shell;

import javax.annotation.Nullable;

import org.metaborg.core.language.IFacet;

/**
 * Facet for the interactive shell of a language.
 */
public class ShellFacet implements IFacet {
    private final String commandPrefix;
    private final String evaluationMethod;
    private final String shellStartSymbol;

    public ShellFacet(@Nullable String commandPrefix, @Nullable String evaluationMethod,
                      String shellStartSymbol) {
        this.commandPrefix = commandPrefix;
        this.evaluationMethod = evaluationMethod;
        this.shellStartSymbol = shellStartSymbol;
    }

    /**
     * @return The prefix for all commands entered in the REPL.
     */
    public @Nullable String getCommandPrefix() {
        return commandPrefix;
    }

    /**
     * @return The evaluation method to use.
     */
    public @Nullable String getEvaluationMethod() {
        return evaluationMethod;
    }

    /**
     * @return The start symbol for shell-specific language syntax.
     */
    public String getShellStartSymbol() {
        return shellStartSymbol;
    }

    @Override
    public String toString() {
        return "ShellFacet [commandPrefix=" + commandPrefix + ", evaluationMethod="
               + evaluationMethod + ", shellStartSymbol=" + shellStartSymbol + "]";
    }

}
