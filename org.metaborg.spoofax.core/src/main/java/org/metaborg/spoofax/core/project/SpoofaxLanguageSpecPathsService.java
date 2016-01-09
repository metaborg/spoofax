package org.metaborg.spoofax.core.project;

import java.io.IOException;

import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.spoofax.core.project.configuration.ISpoofaxLanguageSpecConfig;
import org.metaborg.spoofax.core.project.configuration.ISpoofaxLanguageSpecConfigService;

import com.google.inject.Inject;

public class SpoofaxLanguageSpecPathsService implements ISpoofaxLanguageSpecPathsService {

    private final ISpoofaxLanguageSpecConfigService languageSpecConfigService;

    @Inject
    public SpoofaxLanguageSpecPathsService(final ISpoofaxLanguageSpecConfigService languageSpecConfigService) {
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
        } catch (IOException e) {
            throw new MetaborgRuntimeException(e);
        }
        return new SpoofaxLanguageSpecPaths(languageSpec.location(), config);
    }

}
