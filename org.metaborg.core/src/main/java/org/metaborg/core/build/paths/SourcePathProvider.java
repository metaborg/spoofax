package org.metaborg.core.build.paths;

import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.config.ISourceConfig;
import org.metaborg.core.config.ISourceVisitor;
import org.metaborg.core.project.IProject;
import org.metaborg.util.iterators.Iterables2;

import com.google.common.collect.Lists;

public class SourcePathProvider implements ILanguagePathProvider {

    @Override public Iterable<FileObject> sourcePaths(IProject project, String languageName) {
        final Collection<FileObject> sources = Lists.newArrayList();
        for(ISourceConfig source : project.config().sources()) {
            source.accept(ISourceVisitor.of(langSource -> {
                if(langSource.language.equals(languageName)) {
                    sources.add(resolve(project.location(), langSource.directory));
                }
            }, allLangSource -> {
                sources.add(resolve(project.location(), allLangSource.directory));
            }));
        }
        return sources;
    }

    @Override public Iterable<FileObject> includePaths(IProject project, String languageName) {
        return Iterables2.empty();
    }

    private FileObject resolve(FileObject baseDir, String path) {
        try {
            return baseDir.resolveFile(path);
        } catch(FileSystemException ex) {
            throw new MetaborgRuntimeException(ex);
        }
    }

}