package org.metaborg.meta.core.plugin;

import org.metaborg.core.plugin.IServiceModulePlugin;

import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Plugin that creates {@link Module}s that are passed to MetaBorg meta's Guice {@link Injector} at startup.
 */
public interface IServiceMetaModulePlugin extends IServiceModulePlugin {
    /**
     * @return All meta modules provided by this plugin.
     */
    Iterable<Module> modules();
}
