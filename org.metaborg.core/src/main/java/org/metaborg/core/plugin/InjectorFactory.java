package org.metaborg.core.plugin;

import java.util.Collection;

import org.metaborg.core.MetaborgException;
import org.metaborg.util.iterators.Iterables2;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.CreationException;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Utility methods for creating Guice {@link Injector} instances.
 */
public class InjectorFactory {
    public static Iterable<Module> modules(Module module, IModulePluginLoader loader) throws MetaborgException {
        return modules(Iterables2.singleton(module), loader);
    }

    public static Iterable<Module> modules(Iterable<Module> modules, IModulePluginLoader loader) throws MetaborgException {
        final Iterable<Module> pluginModules = loader.modules();
        final Collection<Module> allModules = Lists.newLinkedList();
        Iterables.addAll(allModules, modules);
        Iterables.addAll(allModules, pluginModules);
        return allModules;
    }

    
    public static Injector create(Iterable<Module> modules) throws MetaborgException {
        try {
            final Injector injector = Guice.createInjector(modules);
            return injector;
        } catch(CreationException e) {
            throw new MetaborgException("Could not create injector because of dependency injection errors", e);
        }
    }
    
    public static Injector createChild(Injector parent, Iterable<Module> modules) throws MetaborgException {
        try {
            final Injector injector = parent.createChildInjector(modules);
            return injector;
        } catch(CreationException e) {
            throw new MetaborgException("Could not create child injector because of dependency injection errors", e);
        }
    }
}
