package org.metaborg.core.language;

import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.build.dependency.IDependencyService;
import org.metaborg.core.language.dialect.IDialectIdentifier;
import org.metaborg.core.language.dialect.IdentifiedDialect;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.core.project.ILanguageSpecService;
import org.metaborg.core.project.IProjectService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class LanguageIdentifierService implements ILanguageIdentifierService {
    private static final ILogger logger = LoggerUtils.logger(LanguageIdentifierService.class);

    private final ILanguageService languageService;
    private final IDialectIdentifier dialectIdentifier;
    private final IProjectService projectService;
    private final ILanguageSpecService languageSpecService;
    private final IDependencyService dependencyService;


    @Inject public LanguageIdentifierService(ILanguageService languageService, IDialectIdentifier dialectIdentifier,
        IProjectService projectService, ILanguageSpecService languageSpecService, IDependencyService dependencyService) {
        this.languageService = languageService;
        this.dialectIdentifier = dialectIdentifier;
        this.projectService = projectService;
        this.languageSpecService = languageSpecService;
        this.dependencyService = dependencyService;
    }


    @Override public boolean identify(FileObject resource, ILanguageImpl language) {
        final Iterable<IdentificationFacet> facets = language.facets(IdentificationFacet.class);
        if(Iterables.isEmpty(facets)) {
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
        return identify(resource, languageSpecService.get(projectService.get(resource)));
    }

    @Nullable
    @Override
    public ILanguageImpl identify(FileObject resource, @Nullable ILanguageSpec languageSpec) {
        if(languageSpec != null) {
            try {
                final Iterable<ILanguageComponent> dependencies = dependencyService.compileDependencies(languageSpec);
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
        return identifyToResource(resource, languageSpecService.get(projectService.get(resource)));
    }

    @Override public @Nullable IdentifiedResource identifyToResource(FileObject resource, @Nullable ILanguageSpec languageSpec) {
        final Iterable<ILanguageImpl> dependencies = compileDependencies(languageSpec);
        return identifyToResource(resource, dependencies);
    }
    
    private Iterable<ILanguageImpl> compileDependencies(ILanguageSpec languageSpec) {
        if(languageSpec != null) {
            try {
                final Iterable<ILanguageComponent> dependencies = dependencyService.compileDependencies(languageSpec);
                return LanguageUtils.toImpls(dependencies);
            } catch(MetaborgException e) {
                return LanguageUtils.allActiveImpls(languageService);
            }
        } else {
            return LanguageUtils.allActiveImpls(languageService);
        }
    }

    @Override public @Nullable ILanguageImpl identify(FileObject resource, Iterable<? extends ILanguageImpl> languages) {
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
        final Set<ILanguage> identifiedLanguages = Sets.newLinkedHashSet();
        ILanguageImpl identifiedImpl = null;
        for(ILanguageImpl impl : impls) {
            if(identify(resource, impl)) {
                identifiedLanguages.add(impl.belongsTo());
                identifiedImpl = impl;
            }
        }

        if(identifiedLanguages.size() > 1) {
            throw new IllegalStateException("Resource " + resource + " identifies to multiple languages: "
                + Joiner.on(", ").join(identifiedLanguages));
        }

        if(identifiedImpl == null) {
            return null;
        }

        return new IdentifiedResource(resource, null, identifiedImpl);
    }


    @Override public boolean available(ILanguageImpl impl) {
        final Iterable<IdentificationFacet> facets = impl.facets(IdentificationFacet.class);
        if(Iterables.isEmpty(facets)) {
            return false;
        }
        return true;
    }
}
