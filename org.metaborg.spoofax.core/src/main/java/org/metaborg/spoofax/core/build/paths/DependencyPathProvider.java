package org.metaborg.spoofax.core.build.paths;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.spoofax.core.SpoofaxRuntimeException;
import org.metaborg.spoofax.core.build.dependency.IDependencyService;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.LanguagePathFacet;
import org.metaborg.spoofax.core.project.IProject;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class DependencyPathProvider implements ILanguagePathProvider {
    private final IDependencyService dependencyService;


    @Inject public DependencyPathProvider(IDependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }


    @Override public Iterable<FileObject> sources(IProject project, String language) {
        Iterable<ILanguage> dependencies = dependencyService.compileDependencies(project);
        List<FileObject> sources = Lists.newArrayList();
        for(ILanguage dependency : dependencies) {
            LanguagePathFacet facet = dependency.facet(LanguagePathFacet.class);
            if(facet != null) {
                List<String> paths = facet.sources.get(language);
                if(paths != null) {
                    resolve(project.location(), paths, sources);
                }
            }
        }
        return sources;
    }

    @Override public Iterable<FileObject> includes(IProject project, String language) {
        Iterable<ILanguage> dependencies = dependencyService.runtimeDependencies(project);
        List<FileObject> includes = Lists.newArrayList();
        for(ILanguage dependency : dependencies) {
            LanguagePathFacet facet = dependency.facet(LanguagePathFacet.class);
            if(facet != null) {
                List<String> paths = facet.includes.get(language);
                if(paths != null) {
                    resolve(dependency.location(), paths, includes);
                }
            }
        }
        return includes;
    }


    private void resolve(FileObject basedir, @Nullable List<String> paths, List<FileObject> filesToAppend) {
        if(paths != null) {
            for(String path : paths) {
                try {
                    filesToAppend.add(basedir.resolveFile(path));
                } catch(FileSystemException ex) {
                    throw new SpoofaxRuntimeException(ex);
                }
            }
        }
    }
}
