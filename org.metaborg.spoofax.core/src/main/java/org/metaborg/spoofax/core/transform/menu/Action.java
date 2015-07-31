package org.metaborg.spoofax.core.transform.menu;

import java.io.Serializable;

import javax.annotation.Nullable;

import org.metaborg.core.language.LanguageIdentifier;

public class Action implements Serializable {
    private static final long serialVersionUID = 727107590910189637L;

    /**
     * Name of the action.
     */
    public final String name;
    /**
     * Identifier of the language of input terms this builder accepts, or null if it accepts any terms.
     */
    public final @Nullable LanguageIdentifier inputLanguageId;
    /**
     * Identifier of the language of output terms this builder creates, or null if unknown.
     */
    public final @Nullable LanguageIdentifier outputLanguageId;
    /**
     * Name of the Stratego strategy this action executes.
     */
    public final String strategy;
    /**
     * Flags for this action.
     */
    public final ActionFlags flags;


    /**
     * Creates an action from a strategy name and flags.
     * 
     * @param name
     *            Name of the action.
     * @param inputLanguage
     *            Language of input terms this builder accepts.
     * @param outputLanguage
     *            Language of input terms this builder accepts.
     * @param strategy
     *            Name of the Stratego strategy this action executes.
     * @param flags
     *            Flags for this action.
     */
    public Action(String name, @Nullable LanguageIdentifier inputLanguageId,
        @Nullable LanguageIdentifier outputLanguageId, String strategy, ActionFlags flags) {
        this.name = name;
        this.inputLanguageId = inputLanguageId;
        this.outputLanguageId = outputLanguageId;
        this.strategy = strategy;
        this.flags = flags;
    }


    @Override public String toString() {
        return "Action [name=" + name + ", input=" + inputLanguageId + ", output=" + outputLanguageId + ", strategy="
            + strategy + ", flags=" + flags + "]";
    }
}
