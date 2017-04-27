package org.metaborg.spoofax.core.config;

import java.util.Collection;

import org.metaborg.core.config.IProjectConfig;
import org.metaborg.core.config.ISourceConfig;
import org.metaborg.meta.nabl2.config.NaBL2Config;

public interface ISpoofaxProjectConfig extends IProjectConfig {

    @Override Collection<ISourceConfig> sources();

    /**
     * @return Whether typesmart dynamic analysis is enabled.
     */
    boolean typesmart();

    /**
     * @return NaBL2 configuration.
     */
    NaBL2Config nabl2Config();

}