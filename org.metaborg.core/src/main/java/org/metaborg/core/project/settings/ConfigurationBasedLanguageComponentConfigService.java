package org.metaborg.core.project.settings;

import com.google.inject.Inject;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Stores and retrieves configurations
 * using the {@link Configuration} class.
 */
public final class ConfigurationBasedLanguageComponentConfigService<TConfig extends ConfigurationBasedLanguageComponentConfig> extends ConfigurationBasedConfigService<ILanguageComponent, TConfig> implements ILanguageComponentConfigService<TConfig> {

    @Inject
    public ConfigurationBasedLanguageComponentConfigService(
            final YamlConfigurationReaderWriter configurationReaderWriter,
            final IConfigurationBasedConfigFactory<TConfig> configFactory) {
        super(configurationReaderWriter, configFactory);
    }
//
//    @Override
//    protected FileObject getConfigFile(final ILanguageComponent languageComponent) throws FileSystemException {
//        return null;
//    }

//    /**
//     * {@inheritDoc}
//     */
//    @Nullable
//    @Override
//    public T get(final ILanguageComponent languageComponent) throws IOException,
//            ConfigurationException {
//        return readConfigFile(getConfigFile(languageComponent));
//    }
//
//    /**
//     * {@inheritDoc}
//     */
//    @Override
//    public void set(final ILanguageComponent languageComponent, @Nullable final T config) throws
//            IOException, ConfigurationException {
//        writeConfigFile(getConfigFile(languageComponent), config);
//    }

    /**
     * Gets the configuration file for the specified language component.
     *
     * @param languageComponent The language component.
     * @return The configuration file.
     * @throws FileSystemException
     */
    @Override
    protected FileObject getConfigFile(final ILanguageComponent languageComponent) throws FileSystemException {
        return languageComponent.location().resolveFile("metaborg.yml");
    }
}
