package org.metaborg.core.project.settings;

import com.google.inject.Inject;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.ILanguageSpecPaths;
import org.metaborg.core.project.ILanguageSpecPathsService;
import org.metaborg.core.project.configuration.ConfigurationBasedConfigService;
import org.metaborg.core.project.configuration.ILanguageSpecConfigService;

/**
 * Stores and retrieves configurations
 * using the {@link Configuration} class.
 */
public final class ConfiguXrationBasedLanguageSpecConfigService<TConfig extends ConfigurationBasedLanguageSpecConfig> extends ConfigurationBasedConfigService<ILanguageSpec, TConfig> implements ILanguageSpecConfigService<TConfig> {

    private ILanguageSpecPathsService<ILanguageSpecPaths> languageSpecPathsService;

    @Inject
    public ConfigurationBasedLanguageSpecConfigService(
            final YamlConfigurationReaderWriter configurationReaderWriter,
            final IConfigurationBasedConfigFactory<TConfig> configFactory,
            final ILanguageSpecPathsService<ILanguageSpecPaths> languageSpecPathsService) {
        super(configurationReaderWriter, configFactory);
        this.languageSpecPathsService = languageSpecPathsService;
    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Nullable
//    @Override
//    public T get(final ILanguageSpec languageSpec) throws IOException,
//            ConfigurationException {
//
//        return readConfigFile(getConfigFile(languageSpec));
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void set(final ILanguageSpec languageSpec, @Nullable final T config) throws
//            IOException, ConfigurationException {
//
//        writeConfigFile(getConfigFile(languageSpec), config);
//    }

//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public TConfig get(FileObject location) throws IOException, ConfigurationException {
//        return null;
//    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected FileObject getConfigFile(final ILanguageSpec languageSpec) throws FileSystemException {
        return this.languageSpecPathsService.get(languageSpec).configFile();
    }

//    /**
//     * Gets the configuration file for the specified root folder.
//     *
//     * @param rootFolder The root folder.
//     * @return The configuration file.
//     * @throws FileSystemException
//     */
//    protected FileObject getConfigFile(final FileObject rootFolder) throws FileSystemException {
//        return languageSpec.location().resolveFile("metaborg.yml");
//    }

}
