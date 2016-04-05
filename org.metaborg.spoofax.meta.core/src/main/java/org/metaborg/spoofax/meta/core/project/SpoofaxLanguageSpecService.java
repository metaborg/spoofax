package org.metaborg.spoofax.meta.core.project;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.config.ConfigException;
import org.metaborg.core.config.ConfigRequest;
import org.metaborg.core.messages.StreamMessagePrinter;
import org.metaborg.core.project.IProject;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfig;
import org.metaborg.spoofax.meta.core.config.ISpoofaxLanguageSpecConfigService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.inject.Inject;

public class SpoofaxLanguageSpecService implements ISpoofaxLanguageSpecService {
    private static final ILogger logger = LoggerUtils.logger(SpoofaxLanguageSpecService.class);

    private final ISourceTextService sourceTextService;
    private final ISpoofaxLanguageSpecConfigService configService;


    @Inject public SpoofaxLanguageSpecService(ISourceTextService sourceTextService,
        ISpoofaxLanguageSpecConfigService configService) {
        this.sourceTextService = sourceTextService;
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
        if(!configService.available(location)) {
            return null;
        }

        final ConfigRequest<ISpoofaxLanguageSpecConfig> configRequest = configService.get(location);
        if(!configRequest.valid()) {
            logger.error("Errors occurred when retrieving language specification configuration from project {}",
                project);
            configRequest.reportErrors(new StreamMessagePrinter(sourceTextService, false, false, logger));
            throw new ConfigException("Configuration for language specification at " + project + " is invalid");
        }

        final ISpoofaxLanguageSpecConfig config = configRequest.config();
        if(config == null) {
            // Configuration should never be null if it is available, but sanity check anyway.
            return null;
        }

        return new SpoofaxLanguageSpecWrapper(config, project);
    }
}
