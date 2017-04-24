package org.metaborg.spoofax.core.config;

import org.metaborg.core.config.IProjectConfig;
import org.metaborg.util.config.NaBL2Config;

public interface ISpoofaxProjectConfig extends IProjectConfig {

    /**
     * @return Whether typesmart dynamic analysis is enabled.
     */
    boolean typesmart();

    /**
     * @return NaBL2 configuration.
     */
    NaBL2Config nabl2Config();

}