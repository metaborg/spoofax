package org.metaborg.core.build.paths;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.IProject;
import org.metaborg.util.resource.ResourceUtils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class LanguagePathService implements ILanguagePathService {
    private final Set<ILanguagePathProvider> providers;


    @Inject public LanguagePathService(Set<ILanguagePathProvider> providers) {
        this.providers = providers;
    }


    @Override public Iterable<FileObject> sourcePaths(IProject project, String language) {
        final Collection<Iterable<FileObject>> sources = Lists.newArrayList();
        for(ILanguagePathProvider provider : providers) {
            sources.add(provider.sourcePaths(project, language));
        }
        final Iterable<FileObject> allSources = Iterables.concat(sources);
        return allSources;
    }

    @Override public Iterable<FileObject> sourceFiles(IProject project, String language) {
        final Iterable<FileObject> sourcePaths = sourcePaths(project, language);
        return ResourceUtils.expand(sourcePaths);
    }


    @Override public Iterable<FileObject> includePaths(IProject project, String language) {
        final Collection<Iterable<FileObject>> includes = Lists.newArrayList();
        for(ILanguagePathProvider provider : providers) {
            includes.add(provider.includePaths(project, language));
        }
        final Iterable<FileObject> allIncludes = Iterables.concat(includes);
        return allIncludes;
    }

    @Override public Iterable<FileObject> includeFiles(IProject project, String language) {
        final Iterable<FileObject> includePaths = includePaths(project, language);
        return ResourceUtils.expand(includePaths);
    }


    @Override public Iterable<FileObject> sourceAndIncludePaths(IProject project, String language) {
        return Iterables.concat(sourcePaths(project, language), includePaths(project, language));
    }


    @Override public Iterable<FileObject> sourceAndIncludeFiles(IProject project, String language) {
        final Iterable<FileObject> paths = sourceAndIncludePaths(project, language);
        return ResourceUtils.expand(paths);
    }
}
