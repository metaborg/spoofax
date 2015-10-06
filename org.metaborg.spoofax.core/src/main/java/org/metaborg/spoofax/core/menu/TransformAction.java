package org.metaborg.spoofax.core.menu;

import javax.annotation.Nullable;

import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.menu.IAction;
import org.metaborg.core.transform.NestedNamedGoal;

public class TransformAction implements IAction {
    /**
     * Name of the action.
     */
    public final String name;

    /**
     * Goal of the action.
     */
    public final NestedNamedGoal goal;

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
    public final TransformActionFlags flags;


    public TransformAction(String name, NestedNamedGoal goal, @Nullable LanguageIdentifier inputLanguageId,
        @Nullable LanguageIdentifier outputLanguageId, String strategy, TransformActionFlags flags) {
        this.name = name;
        this.goal = goal;
        this.inputLanguageId = inputLanguageId;
        this.outputLanguageId = outputLanguageId;
        this.strategy = strategy;
        this.flags = flags;
    }


    @Override public String name() {
        return name;
    }

    @Override public NestedNamedGoal goal() {
        return goal;
    }


    @Override public String toString() {
        return "Action [name=" + name + ", input=" + inputLanguageId + ", output=" + outputLanguageId + ", strategy="
            + strategy + ", flags=" + flags + "]";
    }
}
