package org.metaborg.core.project.settings;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import javax.annotation.Nullable;

public class ConfigurationBasedLanguageSpecConfigFactory implements IConfigurationBasedConfigFactory<ConfigurationBasedLanguageSpecConfig> {
    @Override
    public ConfigurationBasedLanguageSpecConfig create(@Nullable final HierarchicalConfiguration<ImmutableNode> configuration) {
        return new ConfigurationBasedLanguageSpecConfig(configuration);
    }
}
