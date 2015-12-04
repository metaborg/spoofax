package org.metaborg.core.project.settings;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 * Interface for configurations that are backed
 * by an {@link ImmutableConfiguration} object.
 */
public interface IConfigurationBasedConfig {

    /**
     * Gets the underlying configuration.
     *
     * @return The underlying configuration.
     */
    HierarchicalConfiguration<ImmutableNode> getConfiguration();

}
