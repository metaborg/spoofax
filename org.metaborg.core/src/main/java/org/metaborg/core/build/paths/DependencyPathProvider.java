package org.metaborg.core.build.paths;

import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.build.dependency.IDependencyService;
import org.metaborg.core.config.IExport;
import org.metaborg.core.config.IGenerate;
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
            final Collection<IGenerate> generates = dependency.config().generates();
            for(IGenerate generate : generates) {
                if(languageName.equals(generate.languageName())) {
                    resolve(project.location(), Iterables2.singleton(generate.directory()), sources);
                }
            }
        }
        return sources;
    }

    @Override public Iterable<FileObject> includePaths(IProject project, String languageName) throws MetaborgException {
        final Iterable<ILanguageComponent> dependencies = dependencyService.sourceDeps(project);
        final Collection<FileObject> includes = Lists.newArrayList();
        for(ILanguageComponent dependency : dependencies) {
            final Collection<IExport> exports = dependency.config().exports();
            for(IExport export : exports) {
                if(languageName.equals(export.languageName())) {
                    final String directory = export.directory();
                    final String file = export.file();
                    if(directory != null) {
                        resolve(dependency.location(), Iterables2.singleton(directory), includes);
                    } else if(file != null) {
                        resolve(dependency.location(), Iterables2.singleton(file), includes);
                    }
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
