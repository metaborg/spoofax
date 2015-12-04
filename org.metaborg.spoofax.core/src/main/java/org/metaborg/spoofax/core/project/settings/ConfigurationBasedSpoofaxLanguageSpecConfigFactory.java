package org.metaborg.spoofax.core.project.settings;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.metaborg.core.project.settings.ConfigurationBasedLanguageComponentConfig;
import org.metaborg.core.project.settings.IConfigurationBasedConfigFactory;

import javax.annotation.Nullable;

public class ConfigurationBasedSpoofaxLanguageSpecConfigFactory implements IConfigurationBasedConfigFactory<ConfigurationBasedSpoofaxLanguageSpecConfig> {
    @Override
    public ConfigurationBasedSpoofaxLanguageSpecConfig create(@Nullable final HierarchicalConfiguration<ImmutableNode> configuration) {
        return new ConfigurationBasedSpoofaxLanguageSpecConfig(configuration);
    }
}
