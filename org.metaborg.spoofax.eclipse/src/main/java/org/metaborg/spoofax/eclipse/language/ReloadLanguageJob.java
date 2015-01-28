package org.metaborg.spoofax.eclipse.language;

import org.apache.commons.vfs2.FileObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageDiscoveryService;
import org.metaborg.spoofax.core.language.ILanguageService;
import org.metaborg.spoofax.eclipse.util.SpoofaxStatus;

public class ReloadLanguageJob extends Job {
    private static final Logger logger = LogManager.getLogger(ReloadLanguageJob.class);

    private final ILanguageService languageService;
    private final ILanguageDiscoveryService languageDiscoveryService;
    private final FileObject location;


    public ReloadLanguageJob(ILanguageService languageService,
        ILanguageDiscoveryService languageDiscoveryService, FileObject location) {
        super("Reloading Spoofax language");

        this.languageService = languageService;
        this.languageDiscoveryService = languageDiscoveryService;
        this.location = location;
    }


    @Override protected IStatus run(IProgressMonitor monitor) {
        final ILanguage language = languageService.get(location.getName());
        if(language != null) {
            languageService.destroy(language);
            try {
                languageDiscoveryService.discover(location);
            } catch(Exception e) {
                final String message = "Could not load language at location " + location;
                logger.error(message, e);
                return SpoofaxStatus.error(message, e);
            }
        } else {
            final String message =
                "Failed to unload language at location" + location + " because it does not exist";
            logger.error(message);
            return SpoofaxStatus.error(message);
        }
        return SpoofaxStatus.success();
    }
}
