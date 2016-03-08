package org.metaborg.core.plugin;

import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Plugin that creates {@link Module}s that are passed to Metaborg's Guice {@link Injector} at startup.
 */
public interface IServiceModulePlugin {
    /**
     * @return All modules provided by this plugin.
     */
    Iterable<Module> modules();
}
