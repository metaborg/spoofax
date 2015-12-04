package org.metaborg.core.project.settings;

import com.google.inject.Inject;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.project.ILanguageSpec;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Stores and retrieves configurations
 * using the {@link Configuration} class.
 */
public final class ConfigurationBasedLanguageSpecConfigService<TConfig extends ConfigurationBasedLanguageSpecConfig> extends ConfigurationBasedConfigService<ILanguageSpec, TConfig> implements ILanguageSpecConfigService<TConfig> {

    @Inject
    public ConfigurationBasedLanguageSpecConfigService(
            final YamlConfigurationReaderWriter configurationReaderWriter,
            final IConfigurationBasedConfigFactory<TConfig> configFactory) {
        super(configurationReaderWriter, configFactory);
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

    /**
     * Gets the configuration file for the specified language specification.
     *
     * @param languageSpec The language specification.
     * @return The configuration file.
     * @throws FileSystemException
     */
    @Override
    protected FileObject getConfigFile(final ILanguageSpec languageSpec) throws FileSystemException {
        return languageSpec.location().resolveFile("metaborg.yml");
    }
}
