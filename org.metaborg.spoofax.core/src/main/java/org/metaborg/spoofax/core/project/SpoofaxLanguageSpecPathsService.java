package org.metaborg.spoofax.core.project;

import com.google.inject.Inject;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.ILanguageSpecPathsService;
import org.metaborg.core.project.configuration.ILanguageSpecConfigService;
import org.metaborg.spoofax.core.project.configuration.ISpoofaxLanguageSpecConfig;

import java.io.IOException;

public class SpoofaxLanguageSpecPathsService implements ILanguageSpecPathsService<ISpoofaxLanguageSpecPaths> {

    private final ILanguageSpecConfigService<ISpoofaxLanguageSpecConfig> languageSpecConfigService;

    @Inject
    public SpoofaxLanguageSpecPathsService(final ILanguageSpecConfigService<ISpoofaxLanguageSpecConfig> languageSpecConfigService) {
        this.languageSpecConfigService = languageSpecConfigService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ISpoofaxLanguageSpecPaths get(ILanguageSpec languageSpec) {
        ISpoofaxLanguageSpecConfig config = null;
        try {
            config = this.languageSpecConfigService.get(languageSpec);
        } catch (IOException | ConfigurationException e) {
            throw new MetaborgRuntimeException(e);
        }
        return new SpoofaxLanguageSpecPaths(languageSpec.location(), config);
    }

}
