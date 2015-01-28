package org.metaborg.spoofax.eclipse.language;

import org.apache.commons.vfs2.FileObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.metaborg.spoofax.core.language.ILanguageDiscoveryService;
import org.metaborg.spoofax.eclipse.util.SpoofaxStatus;

public class LoadLanguageJob extends Job {
    private static final Logger logger = LogManager.getLogger(LoadLanguageJob.class);

    private final ILanguageDiscoveryService languageDiscoveryService;
    private final FileObject location;


    public LoadLanguageJob(ILanguageDiscoveryService languageDiscoveryService, FileObject location) {
        super("Loading Spoofax language");

        this.languageDiscoveryService = languageDiscoveryService;
        this.location = location;
    }


    @Override protected IStatus run(IProgressMonitor monitor) {
        try {
            languageDiscoveryService.discover(location);
        } catch(Exception e) {
            final String message = "Could not load language at location " + location;
            logger.error(message, e);
            return SpoofaxStatus.error(message, e);
        }
        return SpoofaxStatus.success();
    }
}
