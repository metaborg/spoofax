package org.metaborg.spoofax.core.project.settings;

import com.google.inject.Inject;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.settings.*;

import javax.annotation.Nullable;
import java.io.IOException;

///**
// * Stores and retrieves configurations
// * using the {@link Configuration} class.
// */
//public final class ConfigurationBasedSpoofaxLanguageSpecConfigService extends ConfigurationBasedLanguageSpecConfigService implements ISpoofaxLanguageSpecConfigService {
//
//
//    /**
//     * {@inheritDoc}
//     */
//    @Nullable
//    public ISpoofaxLanguageSpecConfig get(ILanguageSpec languageSpec) throws IOException {
//
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    public void set(ILanguageSpec languageSpec, @Nullable ISpoofaxLanguageSpecConfig config) throws IOException {
//
//    }

//    @Inject
//    public ConfigurationBasedSpoofaxLanguageSpecConfigService(
//            final YamlConfigurationReaderWriter configurationReaderWriter,
//            final IConfigurationBasedConfigFactory<ConfigurationBasedLanguageSpecConfig> configFactory) {
//        super(configurationReaderWriter, configFactory);
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Nullable
//    @Override
//    public ILanguageSpecConfig get(final ILanguageSpec languageSpec) throws IOException,
//            ConfigurationException {
//
//        return readConfigFile(getConfigFile(languageSpec));
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void set(final ILanguageSpec languageSpec, @Nullable final ILanguageSpecConfig config) throws
//            IOException, ConfigurationException {
//        if (!(config instanceof ConfigurationBasedLanguageSpecConfig))
//            throw new IllegalArgumentException("The configuration is not of type ConfigurationBasedLanguageSpecConfig.");
//
//        writeConfigFile(getConfigFile(languageSpec), (ConfigurationBasedLanguageSpecConfig)config);
//    }
//
//    /**
//     * Gets the configuration file for the specified language specification.
//     *
//     * @param languageSpec The language specification.
//     * @return The configuration file.
//     * @throws FileSystemException
//     */
//    private FileObject getConfigFile(final ILanguageSpec languageSpec) throws FileSystemException {
//        return languageSpec.location().resolveFile("metaborg.yml");
//    }
//}
