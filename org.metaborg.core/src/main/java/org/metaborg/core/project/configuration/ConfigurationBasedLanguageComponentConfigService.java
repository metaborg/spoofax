package org.metaborg.core.project.configuration;

import com.google.inject.Inject;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.ex.ConfigurationRuntimeException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.ILanguageSpecPaths;
import org.metaborg.core.project.ILanguageSpecPathsService;

import javax.annotation.Nullable;
import java.io.IOException;

public class ConfigurationBasedLanguageComponentConfigService extends ConfigurationBasedConfigService<ILanguageComponent, ILanguageComponentConfig> implements ILanguageComponentConfigService, ILanguageComponentConfigWriter {

    public static final String CONFIG_FILE = "metaborg.yml";
    private final ConfigurationBasedLanguageComponentConfigBuilder configBuilder;

    @Inject
    public ConfigurationBasedLanguageComponentConfigService(final ConfigurationReaderWriter configurationReaderWriter, final ConfigurationBasedLanguageComponentConfigBuilder configBuilder) {
        super(configurationReaderWriter);
        this.configBuilder = configBuilder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected FileObject getConfigFile(ILanguageComponent languageComponent) throws FileSystemException {
        return getConfigFile(languageComponent.location());
    }

    /**
     * Gets the configuration file for the specified root folder.
     *
     * @param rootFolder The root folder.
     * @return The configuration file.
     * @throws FileSystemException
     */
    private FileObject getConfigFile(FileObject rootFolder) throws FileSystemException {
        return rootFolder.resolveFile(CONFIG_FILE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ILanguageComponentConfig toConfig(HierarchicalConfiguration<ImmutableNode> configuration) {
        return new ConfigurationBasedLanguageComponentConfig(configuration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HierarchicalConfiguration<ImmutableNode> fromConfig(ILanguageComponentConfig config) {
        if (!(config instanceof IConfigurationBasedConfig)) {
            this.configBuilder.reset();
            this.configBuilder.copyFrom(config);
            config = this.configBuilder.build();
        }
        return ((IConfigurationBasedConfig)config).getConfiguration();
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public ILanguageComponentConfig get(FileObject rootFolder) throws IOException {
        return getFromConfigFile(getConfigFile(rootFolder));
    }
}
