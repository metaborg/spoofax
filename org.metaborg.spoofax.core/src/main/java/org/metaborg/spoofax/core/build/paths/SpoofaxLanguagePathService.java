package org.metaborg.spoofax.core.build.paths;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.project.IProject;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

public class SpoofaxLanguagePathService implements ILanguagePathService {
    private final Set<ILanguagePathProvider> providers;


    @Inject public SpoofaxLanguagePathService(Set<ILanguagePathProvider> providers) {
        this.providers = providers;
    }


    @Override public Iterable<FileObject> sources(IProject project, String language) {
        final Collection<Iterable<FileObject>> sources = Lists.newArrayList();
        for(ILanguagePathProvider provider : providers) {
            sources.add(provider.sources(project, language));
        }
        final Iterable<FileObject> allSources = Iterables.concat(sources);
        return allSources;
    }

    @Override public Iterable<FileObject> includes(IProject project, String language) {
        final Collection<Iterable<FileObject>> includes = Lists.newArrayList();
        for(ILanguagePathProvider provider : providers) {
            includes.add(provider.includes(project, language));
        }
        final Iterable<FileObject> allIncludes = Iterables.concat(includes);
        return allIncludes;
    }

    @Override public Iterable<FileObject> sourcesAndIncludes(IProject project, String language) {
        return Iterables.concat(sources(project, language), includes(project, language));
    }
}
