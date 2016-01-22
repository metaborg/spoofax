package org.metaborg.core.build.paths;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.build.dependency.INewDependencyService;
import org.metaborg.core.language.FacetContribution;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.LanguagePathFacet;
import org.metaborg.core.project.ILanguageSpec;

import javax.annotation.Nullable;
import java.util.Collection;

public class NewDependencyPathProvider implements INewLanguagePathProvider {
    private final INewDependencyService dependencyService;


    @Inject public NewDependencyPathProvider(INewDependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }


    @Override public Iterable<FileObject> sourcePaths(ILanguageSpec languageSpec, String languageName) throws MetaborgException {
        final Iterable<ILanguageComponent> dependencies = dependencyService.compileDependencies(languageSpec);
        final Collection<FileObject> sources = Lists.newArrayList();
        for(ILanguageComponent dependency : dependencies) {
            final Iterable<LanguagePathFacet> facets = dependency.facets(LanguagePathFacet.class);
            for(LanguagePathFacet facet : facets) {
                final Collection<String> paths = facet.sources.get(languageName);
                if(paths != null) {
                    resolve(languageSpec.location(), paths, sources);
                }
            }
        }
        return sources;
    }

    @Override public Iterable<FileObject> includePaths(ILanguageSpec languageSpec, String languageName) throws MetaborgException {
        final Iterable<ILanguageComponent> dependencies = dependencyService.runtimeDependencies(languageSpec);
        final Collection<FileObject> includes = Lists.newArrayList();
        for(ILanguageComponent dependency : dependencies) {
            final Iterable<FacetContribution<LanguagePathFacet>> facets =
                dependency.facetContributions(LanguagePathFacet.class);
            for(FacetContribution<LanguagePathFacet> facetContribution : facets) {
                final Collection<String> paths = facetContribution.facet.includes.get(languageName);
                if(paths != null) {
                    resolve(facetContribution.contributor.location(), paths, includes);
                }
            }
        }


        return includes;
    }


    private void resolve(FileObject basedir, @Nullable Collection<String> paths, Collection<FileObject> filesToAppend) {
        if(paths != null) {
            for(String path : paths) {
                try {
                    filesToAppend.add(basedir.resolveFile(path));
                } catch(FileSystemException ex) {
                    throw new MetaborgRuntimeException(ex);
                }
            }
        }
    }
}
