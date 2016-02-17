package org.metaborg.spoofax.meta.core.project;

import java.io.IOException;

import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.meta.core.project.ILanguageSpec;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfig;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfigService;

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
