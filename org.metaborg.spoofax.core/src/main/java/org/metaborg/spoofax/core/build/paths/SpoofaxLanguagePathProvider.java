package org.metaborg.spoofax.core.build.paths;

import static org.metaborg.spoofax.core.SpoofaxProjectConstants.*;

import java.util.Collection;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.build.paths.ILanguagePathProvider;
import org.metaborg.core.project.IProject;
import org.metaborg.util.iterators.Iterables2;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class SpoofaxLanguagePathProvider implements ILanguagePathProvider {
    private static final String ROOT = ".";

    // @formatter:off
    private static final Multimap<String, String> sourcesPerMetaLangName = ImmutableMultimap.<String, String>builder()
        .putAll(LANG_SDF_NAME, DIR_SYNTAX, DIR_LIB)
        .putAll(LANG_STRATEGO_NAME, ROOT, DIR_TRANS, DIR_LIB)
        .putAll(LANG_ESV_NAME, DIR_EDITOR)
        .putAll(LANG_SDF3_NAME, DIR_SYNTAX)
        .putAll(LANG_NABL_NAME, ROOT)
        .putAll(LANG_TS_NAME, ROOT)
        .putAll(LANG_DYNSEM_NAME, ROOT, DIR_TRANS, DIR_LIB)
        .build();
    // @formatter:on


    @Override public Iterable<FileObject> sourcePaths(IProject project, String languageName) {
        if(isMetaLanguage(languageName)) {
            return resolve(project.location(), sourcesPerMetaLangName.get(languageName));
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
