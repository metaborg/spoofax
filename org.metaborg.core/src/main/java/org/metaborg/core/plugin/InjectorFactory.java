package org.metaborg.core.plugin;

import java.util.Collection;

import org.metaborg.core.MetaborgException;

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
    public static Iterable<Module> modules(IModulePluginLoader loader, Iterable<Module> modules)
        throws MetaborgException {
        final Collection<Module> allModules = Lists.newArrayList(modules);
        Iterables.addAll(allModules, loader.modules());
        return allModules;
    }

    public static Iterable<Module> modules(IModulePluginLoader loader, Module... modules) throws MetaborgException {
        final Collection<Module> allModules = Lists.newArrayList(modules);
        Iterables.addAll(allModules, loader.modules());
        return allModules;
    }


    public static Injector create(Iterable<Module> modules) throws MetaborgException {
        try {
            return Guice.createInjector(modules);
        } catch(CreationException e) {
            throw new MetaborgException("Could not create injector because of dependency injection errors", e);
        }
    }

    public static Injector create(Module... modules) throws MetaborgException {
        try {
            return Guice.createInjector(modules);
        } catch(CreationException e) {
            throw new MetaborgException("Could not create injector because of dependency injection errors", e);
        }
    }

    public static Injector createChild(Injector parent, Iterable<Module> modules) throws MetaborgException {
        try {
            return parent.createChildInjector(modules);
        } catch(CreationException e) {
            throw new MetaborgException("Could not create child injector because of dependency injection errors", e);
        }
    }

    public static Injector createChild(Injector parent, Module... modules) throws MetaborgException {
        try {
            return parent.createChildInjector(modules);
        } catch(CreationException e) {
            throw new MetaborgException("Could not create child injector because of dependency injection errors", e);
        }
    }
}
