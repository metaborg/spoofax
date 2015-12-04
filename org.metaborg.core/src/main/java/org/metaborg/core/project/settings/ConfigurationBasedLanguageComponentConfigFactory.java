package org.metaborg.core.project.settings;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import javax.annotation.Nullable;

public class ConfigurationBasedLanguageComponentConfigFactory implements IConfigurationBasedConfigFactory<ConfigurationBasedLanguageComponentConfig> {
    @Override
    public ConfigurationBasedLanguageComponentConfig create(@Nullable final HierarchicalConfiguration<ImmutableNode> configuration) {
        return new ConfigurationBasedLanguageComponentConfig(configuration);
    }
}
