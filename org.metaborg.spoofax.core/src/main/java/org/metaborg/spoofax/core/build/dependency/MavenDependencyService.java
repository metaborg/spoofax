package org.metaborg.spoofax.core.build.dependency;

import java.util.Collection;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageService;
import org.metaborg.spoofax.core.language.LanguageIdentifier;
import org.metaborg.spoofax.core.language.LanguageVersion;
import org.metaborg.spoofax.core.project.IMavenProjectService;
import org.metaborg.spoofax.core.project.IProject;
import org.metaborg.util.iterators.Iterables2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.inject.Inject;


public class MavenDependencyService implements IDependencyService {
    private static final Logger logger = LoggerFactory.getLogger(MavenDependencyService.class);

    private final ILanguageService languageService;
    private final IMavenProjectService mavenProjectService;


    @Inject public MavenDependencyService(ILanguageService languageService, IMavenProjectService mavenProjectService) {
        this.languageService = languageService;
        this.mavenProjectService = mavenProjectService;
    }


    @Override public Iterable<ILanguage> compileDependencies(IProject project) {
        final MavenProject mavenProject = mavenProjectService.get(project);
        if(mavenProject != null) {
            return getLanguages(compileDependencies(mavenProject));
        }
        logger.trace(
            "No corresponding Maven project found for project {}, using all active languages as compile dependencies",
            project);
        return languageService.getAllActive();
    }

    @Override public Iterable<ILanguage> runtimeDependencies(IProject project) {
        final MavenProject mavenProject = mavenProjectService.get(project);
        if(mavenProject != null) {
            return getLanguages(runtimeDependencies(mavenProject));
        }
        logger.trace("No corresponding Maven project found for project {}, using disabling runtime dependencies",
            project);
        return Iterables2.empty();
    }


    private Iterable<LanguageIdentifier> compileDependencies(MavenProject project) {
        final Collection<LanguageIdentifier> dependencies = Lists.newLinkedList();
        for(Plugin plugin : project.getModel().getBuild().getPlugins()) {
            if(SpoofaxMavenConstants.PLUGIN_NAME.equals(plugin.getArtifactId())) {
                for(Dependency dependency : plugin.getDependencies()) {
                    if(SpoofaxMavenConstants.PACKAGING_TYPE.equalsIgnoreCase(dependency.getType())) {
                        dependencies.add(new LanguageIdentifier(dependency.getArtifactId(), LanguageVersion
                            .parse(dependency.getVersion())));
                    }
                }
            }
        }
        return dependencies;
    }

    private Iterable<LanguageIdentifier> runtimeDependencies(MavenProject project) {
        final Collection<LanguageIdentifier> dependencies = Lists.newLinkedList();
        for(Dependency dependency : project.getModel().getDependencies()) {
            if(SpoofaxMavenConstants.PACKAGING_TYPE.equalsIgnoreCase(dependency.getType())) {
                dependencies.add(new LanguageIdentifier(dependency.getArtifactId(), LanguageVersion.parse(dependency
                    .getVersion())));
            }
        }
        return dependencies;
    }

    private Iterable<ILanguage> getLanguages(Iterable<LanguageIdentifier> dependencies) {
        final List<ILanguage> languages = Lists.newArrayList();
        for(LanguageIdentifier dependency : dependencies) {
            ILanguage language = languageService.getWithId(dependency.id(), dependency.version());
            if(language != null) {
                languages.add(language);
                continue;
            }
            language = languageService.getWithId(dependency.id());
            if(language != null) {
                final LanguageVersion version = language.version();
                if(version.major() != 0 || version.minor() != 0 || version.patch() != 0) {
                    logger.warn("Cannot find dependency {}, using version {}.", dependency, language.version());
                }
                languages.add(language);
                continue;
            }
            logger.error("Cannot find dependency {}, make sure it is loaded.", dependency);
        }
        return languages;
    }
}
