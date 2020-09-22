package org.metaborg.spoofax.core.config;

import java.util.Collection;

import org.metaborg.core.config.IProjectConfig;
import org.metaborg.core.config.ISourceConfig;

import mb.nabl2.config.NaBL2Config;

public interface ISpoofaxProjectConfig extends IProjectConfig {

    @Override Collection<ISourceConfig> sources();

    /**
     * @return NaBL2 configuration.
     */
    NaBL2Config nabl2Config();

    boolean statixConcurrent();

}