package org.metaborg.core.menu;

import java.util.List;

import javax.annotation.Nullable;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageImpl;

/**
 * Interface for service that combines menus for language implementations.
 */
public interface IMenuService {
    /**
     * Gets the top-level menu items for given language implementation.
     * 
     * @param language
     *            Language implementation to get menu items for.
     * @return Top-level menu items.
     */
    public abstract Iterable<IMenuItem> menuItems(ILanguageImpl language);

    /**
     * Gets a named action in given language implementation. Actions are nested inside menus, which can be retrieved by
     * providing a list of names.
     * 
     * @param language
     *            Language implementation to get action for.
     * @param names
     *            List of menu names which the action is nested in, followed by the name of the action.
     * @return Action, or null if it was not found.
     * @throws MetaborgException
     *             When multiple actions with the same name can be found.
     */
    public abstract @Nullable IAction nestedAction(ILanguageImpl language, List<String> names) throws MetaborgException;

    /**
     * Gets a named action in given language implementation, and also provides the component that contributes this
     * action. Actions are nested inside menus, which can be retrieved by providing a list of names.
     * 
     * @param language
     *            Language implementation to get action for.
     * @param names
     *            List of menu names which the action is nested in, followed by the name of the action.
     * @return Action contribution, or null if it was not found.
     * @throws MetaborgException
     *             When multiple actions with the same name can be found.
     */
    public abstract @Nullable ActionContribution nestedActionContribution(ILanguageImpl language, List<String> names)
        throws MetaborgException;

    /**
     * Gets a named action in given language implementation.
     * 
     * @param language
     *            Language implementation to get action for.
     * @param name
     *            Name of the action to get.
     * @return Action, or null if it was not found.
     * @throws MetaborgException
     *             When multiple actions with the same name can be found.
     */
    public abstract @Nullable IAction action(ILanguageImpl language, String name) throws MetaborgException;

    /**
     * Gets a named action in given language implementation.
     * 
     * @param language
     *            Language implementation to get action for.
     * @param name
     *            Name of the action to get.
     * @return Action contribution, or null if it was not found.
     * @throws MetaborgException
     *             When multiple actions with the same name can be found.
     */
    public abstract @Nullable ActionContribution actionContribution(ILanguageImpl language, String name)
        throws MetaborgException;
}
