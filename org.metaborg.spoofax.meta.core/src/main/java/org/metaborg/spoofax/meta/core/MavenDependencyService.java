package org.metaborg.spoofax.meta.core;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.List;
import org.apache.maven.project.MavenProject;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageService;
import org.metaborg.spoofax.core.project.IDependencyService;
import org.metaborg.spoofax.core.project.IProject;
import org.metaborg.util.iterators.Iterables2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MavenDependencyService implements IDependencyService {
    private static final Logger log = LoggerFactory.getLogger(MavenDependencyService.class);
 
    private final ILanguageService languageService;
    private final IMavenProjectService mavenProjectService;

    @Inject
    public MavenDependencyService(ILanguageService languageService,
            IMavenProjectService mavenProjectService) {
        this.languageService = languageService;
        this.mavenProjectService = mavenProjectService;
    }

    @Override
    public Iterable<ILanguage> runtimeDependencies(IProject project) {
        MavenProject mavenProject = mavenProjectService.get(project);
        if ( mavenProject != null ) {
            MavenSpoofaxLanguageDependencies dependencies = new MavenSpoofaxLanguageDependencies(mavenProject);
            return getLanguages(dependencies.getRuntimeDependencies());
        }
        log.error("Project {} should be a Maven project, something went wrong.", project);
        return Iterables2.empty();
    }

    @Override
    public Iterable<ILanguage> compileDependencies(IProject project) {
        MavenProject mavenProject = mavenProjectService.get(project);
        if ( mavenProject != null ) {
            MavenSpoofaxLanguageDependencies dependencies = new MavenSpoofaxLanguageDependencies(mavenProject);
            return getLanguages(dependencies.getCompileDependencies());
        }
        log.error("Project {} should be a Maven project, something went wrong.", project);
        return Iterables2.empty();
    }

    private Iterable<ILanguage> getLanguages(Iterable<LanguageDependency> dependencies) {
        List<ILanguage> languages = Lists.newArrayList();
        for ( LanguageDependency dependency : dependencies ) {
            ILanguage language = languageService.getWithId(dependency.id(), dependency.version());
            if ( language != null ) {
                languages.add(language);
                continue;
            }
            language = languageService.getWithId(dependency.id());
            if ( language != null ) {
                log.warn("Cannot find dependency {}, using version {}.",
                        dependency, language.version());
                languages.add(language);
                continue;
            }
            log.error("Cannot find dependency {}, make sure it is loaded.",
                    dependency);
        }
        return languages;
    }

}
