package org.metaborg.spoofax.core.service.actions;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import org.metaborg.spoofax.core.language.ILanguageFacet;

import com.google.common.collect.Maps;

/**
 * Represents the actions facet of a language, which manages a list of Stratego strategies that can be executed on
 * programs of a language.
 */
public class ActionsFacet implements ILanguageFacet {
    private final Map<String, Action> actions = Maps.newHashMap();


    /**
     * Gets the action with given name.
     * 
     * @param name
     *            Name of the action.
     * @return Action with given name, or null if there is no action with given name.
     */
    public @Nullable Action get(String name) {
        return actions.get(name);
    }

    /**
     * Gets all actions
     * 
     * @return Iterable over name-action entries.
     */
    public Iterable<Entry<String, Action>> all() {
        return actions.entrySet();
    }

    /**
     * Adds an action with given name.
     * 
     * @param name
     *            Name of the action.
     * @param action
     *            Action to add
     */
    public void add(String name, Action action) {
        actions.put(name, action);
    }

    /**
     * Removes action with given name
     * 
     * @param name
     *            Name of the action to remove
     * @return The removed action, or null if no action was removed.
     */
    public @Nullable Action remove(String name) {
        return actions.remove(name);
    }
}
