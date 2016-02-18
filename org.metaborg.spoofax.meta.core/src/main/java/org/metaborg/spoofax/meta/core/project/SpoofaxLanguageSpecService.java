package org.metaborg.spoofax.meta.core.project;

import java.io.IOException;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfig;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfigService;

import com.google.inject.Inject;

public class SpoofaxLanguageSpecService implements ISpoofaxLanguageSpecService {
    private final ISpoofaxLanguageSpecConfigService configService;


    @Inject public SpoofaxLanguageSpecService(ISpoofaxLanguageSpecConfigService configService) {
        this.configService = configService;
    }


    @Override public boolean available(IProject project) {
        if(project instanceof ISpoofaxLanguageSpec) {
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

    @Override public @Nullable ISpoofaxLanguageSpec get(IProject project) {
        if(project instanceof ISpoofaxLanguageSpec) {
            return (ISpoofaxLanguageSpec) project;
        }

        final FileObject location = project.location();
        final ISpoofaxLanguageSpecConfig config;
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

        final ISpoofaxLanguageSpecPaths paths = new SpoofaxLanguageSpecPaths(location, config);

        return new SpoofaxLanguageSpecWrapper(config, paths, project);
    }
}
