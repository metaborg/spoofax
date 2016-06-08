package org.metaborg.spoofax.core.shell;

import javax.annotation.Nullable;

import org.metaborg.core.language.IFacet;

/**
 * Facet for the interactive shell of a language.
 */
public class ShellFacet implements IFacet {
    private final String commandPrefix;
    private final String evaluationMethod;

    public ShellFacet(@Nullable String commandPrefix, @Nullable String evaluationMethod) {
        this.commandPrefix = commandPrefix;
        this.evaluationMethod = evaluationMethod;
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

    @Override
    public String toString() {
        return "ShellFacet [commandPrefix=" + commandPrefix + ", evaluationMethod="
               + evaluationMethod + "]";
    }

}
