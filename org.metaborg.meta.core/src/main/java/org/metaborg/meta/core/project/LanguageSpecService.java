package org.metaborg.meta.core.project;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.IProject;
import org.metaborg.meta.core.config.ILanguageSpecConfig;
import org.metaborg.meta.core.config.ILanguageSpecConfigService;

import com.google.inject.Inject;

public class LanguageSpecService implements ILanguageSpecService {
    private final ILanguageSpecConfigService configService;


    @Inject public LanguageSpecService(ILanguageSpecConfigService configService) {
        this.configService = configService;
    }


    @Override public boolean available(IProject project) {
        if(project instanceof ILanguageSpec) {
            return true;
        }

        try {
            if(!configService.available(project.location())) {
                return false;
            }
        } catch(IOException e) {
            return false;
        }

        return true;
    }

    @Override public @Nullable ILanguageSpec get(IProject project) {
        if(project instanceof ILanguageSpec) {
            return (ILanguageSpec) project;
        }

        final FileObject location = project.location();
        final ILanguageSpecConfig config;
        try {
            if(!configService.available(location)) {
                return null;
            }
            config = configService.get(location);
            if(config == null) {
                // Configuration should never be null if it is available, but sanity check anyway.
                return null;
            }
        } catch(IOException e) {
            return null;
        }

        return new LanguageSpecWrapper(config, project);
    }
}
