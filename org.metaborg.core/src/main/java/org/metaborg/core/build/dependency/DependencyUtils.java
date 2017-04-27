package org.metaborg.core.build.dependency;

import javax.annotation.Nullable;

import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.LanguageName;
import org.metaborg.core.language.LanguageUtils;
import org.metaborg.core.project.IProject;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

public class DependencyUtils {
    private static ILogger logger = LoggerUtils.logger(DependencyUtils.class);

    public static @Nullable ILanguageImpl getCompileLanguage(IProject project, LanguageName language,
            IDependencyService dependencyService) throws MissingDependencyException {
        ILanguageImpl impl = null;
        for(ILanguageImpl candidateImpl : LanguageUtils.toImpls(dependencyService.compileDeps(project))) {
            if(candidateImpl.id().name().equals(language)) {
                if(impl != null && !candidateImpl.equals(impl)) {
                    logger.error("Project depends on multiple implementations of {}: {} and {}.", language, impl.id(),
                            candidateImpl.id());
                    throw new IllegalStateException();
                }
                impl = candidateImpl;
            }
        }
        return impl;
    }

}