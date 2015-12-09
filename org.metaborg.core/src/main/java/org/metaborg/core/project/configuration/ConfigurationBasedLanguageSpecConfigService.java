package org.metaborg.core.project.configuration;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.ILanguageSpecPaths;
import org.metaborg.core.project.ILanguageSpecPathsService;

public class ConfigurationBasedLanguageSpecConfigService extends ConfigurationBasedConfigService<ILanguageSpec, ILanguageSpecConfig> implements ILanguageSpecConfigService, ILanguageSpecConfigWriter {

    private final ILanguageSpecPathsService languageSpecPathsService;

    public ConfigurationBasedLanguageSpecConfigService(final ConfigurationReaderWriter configurationReaderWriter, final ILanguageSpecPathsService languageSpecPathsService, final ConfigurationBasedLanguageSpecConfigBuilder configBuilder) {
        super(configurationReaderWriter, configBuilder);

        this.languageSpecPathsService = languageSpecPathsService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected FileObject getConfigFile(ILanguageSpec languageSpec) throws FileSystemException {
        return this.languageSpecPathsService.get(languageSpec).configFile();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ILanguageSpecConfig toConfig(HierarchicalConfiguration<ImmutableNode> configuration) {
        return new ConfigurationBasedLanguageSpecConfig(configuration);
    }

}
