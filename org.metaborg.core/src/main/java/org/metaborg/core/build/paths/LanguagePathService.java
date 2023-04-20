package org.metaborg.core.build.paths;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.IdentifiedResource;
import org.metaborg.core.project.IProject;
import org.metaborg.util.collection.ImList;
import org.metaborg.util.functions.CheckedFunction1;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.resource.ResourceUtils;

import com.google.inject.Inject;

public class LanguagePathService implements ILanguagePathService {
    private static final ILogger logger = LoggerUtils.logger(LanguagePathService.class);

    private final ILanguageIdentifierService languageIdentifierService;
    private final Set<ILanguagePathProvider> providers;


    @Inject public LanguagePathService(ILanguageIdentifierService languageIdentifierService,
        Set<ILanguagePathProvider> providers) {
        this.languageIdentifierService = languageIdentifierService;
        this.providers = providers;
    }


    @Override public Set<FileObject> sourcePaths(IProject project, String languageName) {
        return getFileObjects("source", provider -> provider.sourcePaths(project, languageName));
    }

    @Override public Set<FileObject> includePaths(IProject project, String languageName) {
        return getFileObjects("include", provider -> provider.includePaths(project, languageName));
    }

    public Set<FileObject> getFileObjects(String pathKind,
        CheckedFunction1<ILanguagePathProvider, Iterable<FileObject>, MetaborgException> getPaths) {
        final SortedSet<FileObject> sources = new TreeSet<>(Comparator.reverseOrder());
        for(ILanguagePathProvider provider : providers) {
            try {
                final Iterable<FileObject> providedSources = getPaths.apply(provider);
                Iterables2.addAll(sources, providedSources);
            } catch(MetaborgException e) {
                logger.error("Getting " + pathKind
                        + " paths from provider {} failed unexpectedly, skipping this provider", e,
                    provider);
            }
        }
        return Collections.unmodifiableSortedSet(sources);
    }

    @Override public Iterable<FileObject> sourceAndIncludePaths(IProject project, String languageName) {
        final ImList.Mutable<FileObject> paths = ImList.Mutable.of();
        // make sure source paths come before include paths
        paths.addAll(sourcePaths(project, languageName));
        paths.addAll(includePaths(project, languageName));
        return paths.freeze();
    }


    @Override public Iterable<IdentifiedResource> sourceFiles(IProject project, ILanguageImpl language) {
        final Iterable<FileObject> sourcePaths = sourcePaths(project, language.belongsTo().name());
        return toFiles(sourcePaths, language);
    }

    @Override public Iterable<IdentifiedResource> includeFiles(IProject project, ILanguageImpl language) {
        final Iterable<FileObject> includePaths = includePaths(project, language.belongsTo().name());
        return toFiles(includePaths, language);
    }

    @Override public Iterable<IdentifiedResource> sourceAndIncludeFiles(IProject project, ILanguageImpl language) {
        final Iterable<FileObject> paths = sourceAndIncludePaths(project, language.belongsTo().name());
        return toFiles(paths, language);
    }


    @Override public Collection<IdentifiedResource> toFiles(Iterable<FileObject> paths, ILanguageImpl language) {
        final Collection<FileObject> files = ResourceUtils.expand(paths);
        final Collection<IdentifiedResource> identifiedFiles = new ArrayList<>(files.size());
        for(FileObject file : files) {
            final IdentifiedResource identifiedFile =
                languageIdentifierService.identifyToResource(file, Iterables2.singleton(language));
            if(identifiedFile != null) {
                identifiedFiles.add(identifiedFile);
            }
        }
        return identifiedFiles;
    }
}
