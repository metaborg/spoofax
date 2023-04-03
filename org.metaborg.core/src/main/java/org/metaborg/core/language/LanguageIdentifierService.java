package org.metaborg.core.language;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.build.dependency.IDependencyService;
import org.metaborg.core.language.dialect.IDialectIdentifier;
import org.metaborg.core.language.dialect.IdentifiedDialect;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.util.Strings;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.inject.Inject;

public class LanguageIdentifierService implements ILanguageIdentifierService {
    private static final ILogger logger = LoggerUtils.logger(LanguageIdentifierService.class);

    private final ILanguageService languageService;
    private final IDialectIdentifier dialectIdentifier;
    private final IProjectService projectService;
    private final IDependencyService dependencyService;


    @Inject public LanguageIdentifierService(ILanguageService languageService, IDialectIdentifier dialectIdentifier,
        IProjectService projectService, IDependencyService dependencyService) {
        this.languageService = languageService;
        this.dialectIdentifier = dialectIdentifier;
        this.projectService = projectService;
        this.dependencyService = dependencyService;
    }


    @Override public boolean identify(FileObject resource, ILanguageImpl language) {
        final Iterable<IdentificationFacet> facets = language.facets(IdentificationFacet.class);
        if(Iterables2.isEmpty(facets)) {
            logger.trace("Cannot identify if {} is of {}, language does not have an identification facet", resource,
                language);
            return false;
        }
        boolean identified = false;
        for(IdentificationFacet facet : facets) {
            identified = identified || facet.identify(resource);
        }
        return identified;
    }

    @Override public @Nullable ILanguageImpl identify(FileObject resource) {
        return identify(resource, projectService.get(resource));
    }

    @Nullable @Override public ILanguageImpl identify(FileObject resource, @Nullable IProject project) {
        if(project != null) {
            try {
                final Iterable<ILanguageComponent> dependencies = dependencyService.compileDeps(project);
                final Iterable<ILanguageImpl> impls = LanguageUtils.toImpls(dependencies);
                ILanguageImpl impl = identify(resource, impls);
                if(impl == null) {
                    // Try with all active languages if identification with dependencies fails
                    impl = identify(resource, LanguageUtils.allActiveImpls(languageService));
                }
                return impl;
            } catch(MetaborgException e) {
                return identify(resource, LanguageUtils.allActiveImpls(languageService));
            }
        } else {
            return identify(resource, LanguageUtils.allActiveImpls(languageService));
        }
    }

    @Override public @Nullable IdentifiedResource identifyToResource(FileObject resource) {
        return identifyToResource(resource, projectService.get(resource));
    }

    @Override public @Nullable IdentifiedResource identifyToResource(FileObject resource, @Nullable IProject project) {
        if(project != null) {
            try {
                final Iterable<ILanguageComponent> dependencies = dependencyService.compileDeps(project);
                final Iterable<ILanguageImpl> impls = LanguageUtils.toImpls(dependencies);
                IdentifiedResource identified = identifyToResource(resource, impls);
                if(identified == null) {
                    // Try with all active languages if identification with dependencies fails
                    identified = identifyToResource(resource, LanguageUtils.allActiveImpls(languageService));
                }
                return identified;
            } catch(MetaborgException e) {
                return identifyToResource(resource, LanguageUtils.allActiveImpls(languageService));
            }
        } else {
            return identifyToResource(resource, LanguageUtils.allActiveImpls(languageService));
        }
    }
    
    @Override public @Nullable ILanguageImpl identify(FileObject resource,
        Iterable<? extends ILanguageImpl> languages) {
        final IdentifiedResource identified = identifyToResource(resource, languages);
        if(identified == null) {
            return null;
        }
        return identified.dialectOrLanguage();
    }

    @Override public @Nullable IdentifiedResource identifyToResource(FileObject resource,
        Iterable<? extends ILanguageImpl> impls) {
        // Ignore directories.
        try {
            if(resource.getType() == FileType.FOLDER) {
                return null;
            }
        } catch(FileSystemException e) {
            logger.error("Cannot identify {}, cannot determine its file type", e, resource);
            return null;
        }

        // Try to identify using the dialect identifier first.
        try {
            final IdentifiedDialect dialect = dialectIdentifier.identify(resource);
            if(dialect != null) {
                return new IdentifiedResource(resource, dialect);
            }
        } catch(MetaborgException e) {
            logger.error("Cannot identify dialect of {}", e, resource);
            return null;
        } catch(MetaborgRuntimeException e) {
            // Ignore
        }

        // Identify using identification facet.
        final Set<ILanguage> identifiedLanguages = new LinkedHashSet<>();
        ILanguageImpl identifiedImpl = null;
        for(ILanguageImpl impl : impls) {
            if(identify(resource, impl)) {
                identifiedLanguages.add(impl.belongsTo());
                identifiedImpl = impl;
            }
        }

        if(identifiedLanguages.size() > 1) {
            throw new IllegalStateException("Resource " + resource + " identifies to multiple languages: "
                + Strings.tsJoin(identifiedLanguages, ", "));
        }

        if(identifiedImpl == null) {
            return null;
        }

        return new IdentifiedResource(resource, null, identifiedImpl);
    }


    @Override public boolean available(ILanguageImpl impl) {
        final Iterable<IdentificationFacet> facets = impl.facets(IdentificationFacet.class);
        if(Iterables2.isEmpty(facets)) {
            return false;
        }
        return true;
    }
}
