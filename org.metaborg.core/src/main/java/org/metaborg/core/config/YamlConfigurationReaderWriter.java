package org.metaborg.core.config;

import jakarta.annotation.Nullable;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

import com.virtlink.commons.configuration2.jackson.JacksonConfiguration;

/**
 * Reads/writes configurations in YAML files.
 */
public class YamlConfigurationReaderWriter extends AConfigurationReaderWriter {
    @Override protected JacksonConfiguration createNew(
        @Nullable HierarchicalConfiguration<ImmutableNode> sourceConfiguration) {
        final JacksonConfiguration config = new YamlConfiguration(sourceConfiguration);
        config.setConversionHandler(new MetaborgConversionHandler());
        return config;
    }
}
