package org.metaborg.core.project.settings;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import javax.annotation.Nullable;

/**
 * Creates a {@link ImmutableConfiguration} based config.
 */
public interface IConfigurationBasedConfigFactory<T extends IConfigurationBasedConfig> {

    /**
     * Creates a new instance of the {@link IConfigurationBasedConfig} interface.
     *
     * @param configuration The configuration that provides the properties; or <code>null</code>.
     * @return The created object.
     */
    T create(@Nullable HierarchicalConfiguration<ImmutableNode> configuration);

}
