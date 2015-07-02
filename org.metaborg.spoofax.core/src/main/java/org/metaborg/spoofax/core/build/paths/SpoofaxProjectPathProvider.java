package org.metaborg.spoofax.core.build.paths;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import java.util.Collection;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.build.paths.ILanguagePathProvider;
import org.metaborg.core.project.IProject;

import static org.metaborg.spoofax.core.build.paths.SpoofaxProjectConstants.*;

public class SpoofaxProjectPathProvider implements ILanguagePathProvider {
    private static final String ROOT = ".";

    private final Multimap<String, String> sourcesPerLanguage = ImmutableMultimap.<String, String>builder()
        .putAll(LANG_ESV, DIR_EDITOR).putAll(LANG_SDF, DIR_SYNTAX).putAll(LANG_SDF3, DIR_SYNTAX)
        .putAll(LANG_STRATEGO, DIR_TRANS).putAll(LANG_DYNSEM, DIR_TRANS).build();

    private final Multimap<String, String> includesPerLanguage = ImmutableMultimap.<String, String>builder()
        .putAll(LANG_STRATEGO, ROOT).putAll(LANG_DYNSEM, ROOT).build();

    private final Collection<String> defaultIncludes = Lists.newArrayList(DIR_LIB);


    @Override public Iterable<FileObject> sources(IProject project, String language) {
        return resolve(project.location(), sourcesPerLanguage.get(language));
    }

    @Override public Iterable<FileObject> includes(IProject project, String language) {
        return Iterables.concat(resolve(project.location(), includesPerLanguage.get(language)),
            resolve(project.location(), defaultIncludes));
    }


    private Collection<FileObject> resolve(FileObject basedir, Collection<String> paths) {
        List<FileObject> files = Lists.newArrayList();
        if(paths != null) {
            for(String path : paths) {
                try {
                    files.add(basedir.resolveFile(path));
                } catch(FileSystemException ex) {
                    throw new MetaborgRuntimeException(ex);
                }
            }
        }
        return files;
    }
}
