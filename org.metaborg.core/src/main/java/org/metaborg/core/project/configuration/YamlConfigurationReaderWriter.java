package org.metaborg.core.project.configuration;

import javax.annotation.Nullable;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import com.virtlink.commons.configuration2.jackson.JacksonConfiguration;

/**
 * Reads/writes configurations in YAML files.
 */
public class YamlConfigurationReaderWriter extends ConfigurationReaderWriter {

    /**
     * {@inheritDoc}
     */
    @Override
    public JacksonConfiguration createConfiguration(@Nullable HierarchicalConfiguration<ImmutableNode> sourceConfiguration) {
        JacksonConfiguration config = new YamlConfiguration(sourceConfiguration);
        config.setConversionHandler(new MetaborgConversionHandler());
        return config;
    }

}
