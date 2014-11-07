package org.metaborg.spoofax.core.service.actions;

import java.util.Map;

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
        // TODO: throw exception if action does not exist.
        return actions.get(name);
    }

    /**
     * Gets all actions
     * 
     * @return Iterable over actions.
     */
    public Iterable<Action> all() {
        return actions.values();
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
        // TODO: throw exception if action already exists.
        actions.put(name, action);
    }

    /**
     * Adds all actions in given mapping.
     * 
     * @param actions
     *            The actions to add.
     */
    public void addAll(Map<String, Action> actions) {
        // TODO: throw exception if action already exists.
        actions.putAll(actions);
    }

    /**
     * Removes action with given name
     * 
     * @param name
     *            Name of the action to remove
     * @return The removed action, or null if no action was removed.
     */
    public @Nullable Action remove(String name) {
        // TODO: throw exception if action does not exist.
        return actions.remove(name);
    }
}
