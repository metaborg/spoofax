package org.metaborg.core.config;

import javax.annotation.Nullable;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.tree.ImmutableNode;

public class AConfig implements IConfig {

    protected final HierarchicalConfiguration<ImmutableNode> config;


    public AConfig(HierarchicalConfiguration<ImmutableNode> config) {
        this.config = config;
    }


    @Override public HierarchicalConfiguration<ImmutableNode> getConfig() {
        return this.config;
    }

    protected @Nullable HierarchicalConfiguration<ImmutableNode> configurationAt(String key, boolean supportUpdates) {
        try {
            return config.configurationAt(key, supportUpdates);
        } catch(ConfigurationRuntimeException ex) {
            return null;
        }
    }

}