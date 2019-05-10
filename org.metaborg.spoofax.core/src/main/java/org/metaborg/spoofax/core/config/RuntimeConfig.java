package org.metaborg.spoofax.core.config;

import javax.annotation.Nullable;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

public class RuntimeConfig implements IRuntimeConfig {

    final HierarchicalConfiguration<ImmutableNode> config;

    public RuntimeConfig(HierarchicalConfiguration<ImmutableNode> config) {
        this.config = config;
    }

    @Override public @Nullable HierarchicalConfiguration<ImmutableNode> get(String langName) {
        return config.containsKey(langName) ? config.configurationAt(langName) : null;
    }

}