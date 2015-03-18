package org.metaborg.spoofax.eclipse.language;

import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageCache;
import org.metaborg.spoofax.eclipse.util.StatusUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LanguageInvalidatedJob extends Job {
    private static final Logger logger = LoggerFactory.getLogger(LanguageInvalidatedJob.class);

    private final Set<ILanguageCache> languageCaches;

    private final ILanguage language;


    public LanguageInvalidatedJob(Set<ILanguageCache> languageCaches, ILanguage language) {
        super("Language invalidated");

        this.languageCaches = languageCaches;
        this.language = language;
    }

    @Override protected IStatus run(IProgressMonitor monitor) {
        logger.debug("Running language invalidated job for {}", language);

        for(ILanguageCache languageCache : languageCaches) {
            languageCache.invalidateCache(language);
        }

        return StatusUtils.success();
    }
}
