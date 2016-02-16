package org.metaborg.spoofax.core.project;

import java.io.IOException;

import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.spoofax.core.project.configuration.ISpoofaxLanguageSpecConfig;
import org.metaborg.spoofax.core.project.configuration.ISpoofaxLanguageSpecConfigService;

import com.google.inject.Inject;

public class SpoofaxLanguageSpecPathsService implements ISpoofaxLanguageSpecPathsService {
    private final ISpoofaxLanguageSpecConfigService languageSpecConfigService;


    @Inject public SpoofaxLanguageSpecPathsService(ISpoofaxLanguageSpecConfigService languageSpecConfigService) {
        this.languageSpecConfigService = languageSpecConfigService;
    }


    @Override public ISpoofaxLanguageSpecPaths get(ILanguageSpec languageSpec) {
        final ISpoofaxLanguageSpecConfig config;
        try {
            config = this.languageSpecConfigService.get(languageSpec);
        } catch(IOException e) {
            throw new MetaborgRuntimeException(e);
        }
        return new SpoofaxLanguageSpecPaths(languageSpec.location(), config);
    }
}
