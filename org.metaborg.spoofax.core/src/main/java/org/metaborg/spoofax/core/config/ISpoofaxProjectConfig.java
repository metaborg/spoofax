package org.metaborg.spoofax.core.config;

import java.util.Collection;

import org.metaborg.core.config.IProjectConfig;
import org.metaborg.core.config.ISourceConfig;

public interface ISpoofaxProjectConfig extends IProjectConfig {

    @Override Collection<ISourceConfig> sources();

    /**
     * @return Whether typesmart dynamic analysis is enabled.
     */
    boolean typesmart();

    /**
     * @return Runtime configuration for languages.
     */
    IRuntimeConfig runtimeConfig();

}