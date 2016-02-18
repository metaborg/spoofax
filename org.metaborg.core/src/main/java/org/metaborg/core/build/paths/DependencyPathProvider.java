package org.metaborg.core.build.paths;

import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.build.dependency.IDependencyService;
import org.metaborg.core.config.Export;
import org.metaborg.core.config.Generate;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.project.IProject;
import org.metaborg.util.iterators.Iterables2;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class DependencyPathProvider implements ILanguagePathProvider {
    private final IDependencyService dependencyService;


    @Inject public DependencyPathProvider(IDependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }


    @Override public Iterable<FileObject> sourcePaths(IProject project, String languageName) throws MetaborgException {
        final Iterable<ILanguageComponent> dependencies = dependencyService.compileDeps(project);
        final Collection<FileObject> sources = Lists.newArrayList();
        for(ILanguageComponent dependency : dependencies) {
            final Collection<Generate> generates = dependency.config().generates();
            for(Generate generate : generates) {
                if(languageName.equals(generate.languageName)) {
                    resolve(project.location(), generate.directories, sources);
                }
            }
        }
        return sources;
    }

    @Override public Iterable<FileObject> includePaths(IProject project, String languageName) throws MetaborgException {
        final Iterable<ILanguageComponent> dependencies = dependencyService.sourceDeps(project);
        final Collection<FileObject> includes = Lists.newArrayList();
        for(ILanguageComponent dependency : dependencies) {
            final Collection<Export> exports = dependency.config().exports();
            for(Export export : exports) {
                if(languageName.equals(export.languageName)) {
                    resolve(dependency.location(), Iterables2.singleton(export.directory), includes);
                }
            }
        }

        return includes;
    }


    private void resolve(FileObject basedir, Iterable<String> paths, Collection<FileObject> filesToAppend) {
        for(String path : paths) {
            try {
                filesToAppend.add(basedir.resolveFile(path));
            } catch(FileSystemException ex) {
                throw new MetaborgRuntimeException(ex);
            }
        }
    }
}
