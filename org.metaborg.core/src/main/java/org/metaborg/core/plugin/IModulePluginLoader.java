package org.metaborg.core.plugin;

import org.metaborg.core.MetaborgException;

import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Loads static {@link Module} plugins which can be passed to a Guice {@link Injector}.
 */
public interface IModulePluginLoader {
    /**
     * @return All modules provided by plugins.
     */
    public abstract Iterable<Module> modules() throws MetaborgException;
}
