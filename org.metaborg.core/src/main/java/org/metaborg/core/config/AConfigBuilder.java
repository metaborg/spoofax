package org.metaborg.core.config;

import javax.annotation.Nullable;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ConfigurationUtils;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;

public class AConfigBuilder {

    protected final AConfigurationReaderWriter configReaderWriter;

    protected @Nullable HierarchicalConfiguration<ImmutableNode> configuration;


    public AConfigBuilder(AConfigurationReaderWriter configReaderWriter) {
        this.configReaderWriter = configReaderWriter;
    }


    protected HierarchicalConfiguration<ImmutableNode> cloneConfiguration(IConfig config) {
        // Clone configuration.
        final IConfig iconfig = (IConfig) config;
        final HierarchicalConfiguration<ImmutableNode> apacheConfig = iconfig.getConfig();
        final Configuration clonedConfig = ConfigurationUtils.cloneConfiguration(apacheConfig);
        @SuppressWarnings("unchecked") final HierarchicalConfiguration<ImmutableNode> clonedHierachicalConfig =
                (HierarchicalConfiguration<ImmutableNode>) clonedConfig;
        return clonedHierachicalConfig;
    }

    void setConfiguration(HierarchicalConfiguration<ImmutableNode> configuration) {
        this.configuration = configuration;
    }
 
}