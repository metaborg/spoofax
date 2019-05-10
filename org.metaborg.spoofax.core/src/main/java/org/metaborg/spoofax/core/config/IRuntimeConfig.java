package org.metaborg.spoofax.core.config;

import javax.annotation.Nullable;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

public interface IRuntimeConfig {

    /**
     * @return The runtime configuration for the given language name, or null.
     */
    @Nullable HierarchicalConfiguration<ImmutableNode> get(String langName);

}