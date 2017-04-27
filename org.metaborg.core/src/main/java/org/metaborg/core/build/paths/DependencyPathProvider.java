package org.metaborg.core.build.paths;

import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.build.dependency.IDependencyService;
import org.metaborg.core.config.IExportConfig;
import org.metaborg.core.config.IExportVisitor;
import org.metaborg.core.config.IGenerateConfig;
import org.metaborg.core.config.LangDirExport;
import org.metaborg.core.config.LangFileExport;
import org.metaborg.core.config.ResourceExport;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.LanguageName;
import org.metaborg.core.project.IProject;
import org.metaborg.util.iterators.Iterables2;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class DependencyPathProvider implements ILanguagePathProvider {
    private final IDependencyService dependencyService;


    @Inject public DependencyPathProvider(IDependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }


    @Override public Iterable<FileObject> sourcePaths(final IProject project, final LanguageName languageName) throws MetaborgException {
        final Iterable<ILanguageComponent> dependencies = dependencyService.compileDeps(project);
        final Collection<FileObject> sources = Lists.newArrayList();
        for(ILanguageComponent dependency : dependencies) {
            final Collection<IGenerateConfig> generates = dependency.config().generates();
            for(IGenerateConfig generate : generates) {
                if(languageName.equals(generate.languageName())) {
                    resolve(project.location(), Iterables2.singleton(generate.directory()), sources);
                }
            }
        }
        return sources;
    }

    @Override public Iterable<FileObject> includePaths(final IProject project, final LanguageName languageName)
        throws MetaborgException {
        final Iterable<ILanguageComponent> dependencies = dependencyService.sourceDeps(project);
        final Collection<FileObject> includes = Lists.newArrayList();
        for(final ILanguageComponent dependency : dependencies) {
            final Collection<IExportConfig> exports = dependency.config().exports();
            for(IExportConfig export : exports) {
                export.accept(new IExportVisitor() {
                    @Override public void visit(LangDirExport export) {
                        if(languageName.equals(export.language)) {
                            resolve(dependency.location(), Iterables2.singleton(export.directory), includes);
                        }
                    }

                    @Override public void visit(LangFileExport export) {
                        if(languageName.equals(export.language)) {
                            resolve(dependency.location(), Iterables2.singleton(export.file), includes);
                        }
                    }

                    @Override public void visit(ResourceExport export) {
                        // Ignore resource exports
                    }
                });
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
