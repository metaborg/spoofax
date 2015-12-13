package org.metaborg.core;

import java.util.Collection;

import org.metaborg.core.plugin.IModulePluginLoader;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Facade for instantiating and accessing the Metaborg API.
 */
public class Metaborg {
    public final Injector injector;

    public Metaborg(MetaborgModule module, IModulePluginLoader loader) throws MetaborgException {
        final Iterable<Module> pluginModules = loader.modules();
        final Collection<Module> modules = Lists.newLinkedList();
        modules.add(module);
        Iterables.addAll(modules, pluginModules);
        try {
            injector = Guice.createInjector(modules);
        } catch(CreationException e) {
            throw new MetaborgException("Could not instantiate Metaborg API because of dependency injection errors", e);
        }
    }
}
