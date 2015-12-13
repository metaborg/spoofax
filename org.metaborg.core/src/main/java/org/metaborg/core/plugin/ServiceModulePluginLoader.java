package org.metaborg.core.plugin;

import java.util.Collection;
import java.util.ServiceLoader;

import org.metaborg.core.MetaborgException;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Module;

/**
 * Module plugin loader using Java's {@link ServiceLoader}.
 */
public class ServiceModulePluginLoader implements IModulePluginLoader {
    @Override public Iterable<Module> modules() throws MetaborgException {
        try {
            final ServiceLoader<IServiceModulePlugin> modulePlugins = ServiceLoader.load(IServiceModulePlugin.class);
            final Collection<Module> modules = Lists.newLinkedList();
            for(IServiceModulePlugin plugin : modulePlugins) {
                Iterables.addAll(modules, plugin.modules());
            }
            return modules;
        } catch(Exception e) {
            throw new MetaborgException("Unhandled exception while loading module plugins with Java's ServiceLoader", e);
        }
    }
}
