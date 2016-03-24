package org.metaborg.spoofax.core.build.paths;

import static org.metaborg.spoofax.core.SpoofaxConstants.*;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.build.paths.ILanguagePathProvider;
import org.metaborg.core.project.IProject;
import org.metaborg.util.iterators.Iterables2;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

public class BuiltinLanguagePathProvider implements ILanguagePathProvider {
    private static final String ROOT = ".";

    // @formatter:off
    private static final Collection<String> sources = Lists.newArrayList(DIR_LIB, DIR_SRCGEN, ROOT);
    private static final Multimap<String, String> sourcesPerMetaLangName = ImmutableMultimap.<String, String>builder()
        .putAll(LANG_SDF_NAME, DIR_SYNTAX)
        .putAll(LANG_STRATEGO_NAME, DIR_TRANS)
        .putAll(LANG_ESV_NAME, DIR_EDITOR)
        .putAll(LANG_SDF3_NAME, DIR_SYNTAX)
        .putAll(LANG_NABL_NAME, DIR_TRANS)
        .putAll(LANG_TS_NAME, DIR_TRANS)
        .putAll(LANG_DYNSEM_NAME, DIR_TRANS)
        .build();
    // @formatter:on


    @Override public Iterable<FileObject> sourcePaths(IProject project, String languageName) {
        if(isMetaLanguage(languageName)) {
            final Set<String> specificSources = Sets.newHashSet(sourcesPerMetaLangName.get(languageName));
            specificSources.addAll(sources);
            return resolve(project.location(), specificSources);
        } else {
            return Iterables2.singleton(resolve(project.location(), ROOT));
        }
    }

    @Override public Iterable<FileObject> includePaths(IProject project, String languageName) {
        return Iterables2.empty();
    }


    private Collection<FileObject> resolve(FileObject baseDir, Iterable<String> paths) {
        final Collection<FileObject> files = Lists.newLinkedList();
        if(paths != null) {
            for(String path : paths) {
                files.add(resolve(baseDir, path));
            }
        }
        return files;
    }

    private FileObject resolve(FileObject baseDir, String path) {
        try {
            return baseDir.resolveFile(path);
        } catch(FileSystemException ex) {
            throw new MetaborgRuntimeException(ex);
        }
    }
}
