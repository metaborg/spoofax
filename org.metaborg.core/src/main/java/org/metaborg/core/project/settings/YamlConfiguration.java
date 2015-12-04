package org.metaborg.core.project.settings;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.virtlink.commons.configuration2.jackson.JacksonConfiguration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

/**
 * Configuration that uses YAML files.
 */
public class YamlConfiguration extends JacksonConfiguration {

    /**
     * Initializes a new instance of the {@link YamlConfiguration} class.
     */
    public YamlConfiguration() {
        this(null);
    }

    /**
     * Initializes a new instance of the {@link YamlConfiguration} class.
     *
     * @param config The configuration whose nodes to copy.
     */
    public YamlConfiguration(
            final HierarchicalConfiguration<ImmutableNode> config) {
        super(new YAMLFactory(), config);
    }

}
