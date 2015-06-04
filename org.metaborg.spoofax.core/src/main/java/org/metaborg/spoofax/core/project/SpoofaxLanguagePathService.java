package org.metaborg.spoofax.core.project;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.List;
import java.util.Set;
import org.apache.commons.vfs2.FileObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpoofaxLanguagePathService implements ILanguagePathService {
    private static final Logger log = LoggerFactory.getLogger(SpoofaxLanguagePathService.class);

    private final Set<ILanguagePathProvider> providers;

    @Inject public SpoofaxLanguagePathService(Set<ILanguagePathProvider> providers) {
        this.providers = providers;
    }

    @Override
    public Iterable<FileObject> sources(IProject project, String language) {
        List<Iterable<FileObject>> sources = Lists.newArrayList();
        for ( ILanguagePathProvider provider : providers ) {
            sources.add(provider.sources(project, language));
        }
        Iterable<FileObject> allSources = Iterables.concat(sources);
        return allSources;
    }

    @Override
    public Iterable<FileObject> includes(IProject project, String language) {
        List<Iterable<FileObject>> includes = Lists.newArrayList();
        for ( ILanguagePathProvider provider : providers ) {
            includes.add(provider.includes(project, language));
        }
        Iterable<FileObject> allIncludes = Iterables.concat(includes);
        return allIncludes;
    }

    @Override
    public Iterable<FileObject> sourcesAndIncludes(IProject project, String language) {
        return Iterables.concat(sources(project, language), includes(project, language));
    }
    
}
