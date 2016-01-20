package org.metaborg.spoofax.core.build.paths;

import static org.metaborg.spoofax.core.SpoofaxConstants.*;

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

/**
 * @deprecated Use {@link NewBuiltinLanguagePathProvider} instead.
 */
@SuppressWarnings("deprecation")
@Deprecated
public class BuiltinLanguagePathProvider implements ILanguagePathProvider {
    private static final String ROOT = ".";

    // @formatter:off
    private static final Multimap<String, String> sourcesPerMetaLangName = ImmutableMultimap.<String, String>builder()
        .putAll(LANG_SDF_NAME, ROOT, DIR_SYNTAX, DIR_LIB, DIR_SRCGEN)
        .putAll(LANG_STRATEGO_NAME, ROOT, DIR_TRANS, DIR_LIB)
        .putAll(LANG_ESV_NAME, DIR_EDITOR, DIR_SRCGEN)
        .putAll(LANG_SDF3_NAME, ROOT, DIR_SYNTAX)
        .putAll(LANG_NABL_NAME, ROOT)
        .putAll(LANG_TS_NAME, ROOT)
        .putAll(LANG_DYNSEM_NAME, ROOT, DIR_TRANS, DIR_LIB, DIR_SRCGEN)
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
