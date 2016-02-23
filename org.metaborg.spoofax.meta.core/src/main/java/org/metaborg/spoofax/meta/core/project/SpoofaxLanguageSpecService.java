package org.metaborg.spoofax.meta.core.project;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.config.ConfigException;
import org.metaborg.core.project.IProject;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfig;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfigService;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpec;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpecPaths;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpecService;
import org.metaborg.spoofax.meta.core.project.SpoofaxLanguageSpecPaths;
import org.metaborg.spoofax.meta.core.project.SpoofaxLanguageSpecWrapper;

import com.google.inject.Inject;

public class SpoofaxLanguageSpecService implements ISpoofaxLanguageSpecService {
    private final ISpoofaxLanguageSpecConfigService configService;


    @Inject public SpoofaxLanguageSpecService(ISpoofaxLanguageSpecConfigService configService) {
        this.configService = configService;
    }


    @Override public boolean available(IProject project) {
        if(project instanceof ISpoofaxLanguageSpec || configService.available(project.location())) {
            return true;
        }
        return false;
    }

    @Override public @Nullable ISpoofaxLanguageSpec get(IProject project) throws ConfigException {
        if(project instanceof ISpoofaxLanguageSpec) {
            return (ISpoofaxLanguageSpec) project;
        }

        final FileObject location = project.location();
        final ISpoofaxLanguageSpecConfig config;
        if(!configService.available(location)) {
            return null;
        }
        config = configService.get(location);
        if(config == null) {
            // Configuration should never be null if it is available, but sanity check anyway.
            return null;
        }

        final ISpoofaxLanguageSpecPaths paths = new SpoofaxLanguageSpecPaths(location, config);

        return new SpoofaxLanguageSpecWrapper(config, paths, project);
    }
}
