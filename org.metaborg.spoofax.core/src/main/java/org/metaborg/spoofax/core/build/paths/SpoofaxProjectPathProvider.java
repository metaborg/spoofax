package org.metaborg.spoofax.core.build.paths;

import static org.metaborg.spoofax.core.SpoofaxProjectConstants.DIR_EDITOR;
import static org.metaborg.spoofax.core.SpoofaxProjectConstants.DIR_LIB;
import static org.metaborg.spoofax.core.SpoofaxProjectConstants.DIR_SYNTAX;
import static org.metaborg.spoofax.core.SpoofaxProjectConstants.DIR_TRANS;
import static org.metaborg.spoofax.core.SpoofaxProjectConstants.LANG_NAME_DYNSEM;
import static org.metaborg.spoofax.core.SpoofaxProjectConstants.LANG_NAME_ESV;
import static org.metaborg.spoofax.core.SpoofaxProjectConstants.LANG_NAME_SDF;
import static org.metaborg.spoofax.core.SpoofaxProjectConstants.LANG_NAME_SDF3;
import static org.metaborg.spoofax.core.SpoofaxProjectConstants.LANG_NAME_STRATEGO;

import java.util.Collection;
import java.util.List;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.build.paths.ILanguagePathProvider;
import org.metaborg.core.project.IProject;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class SpoofaxProjectPathProvider implements ILanguagePathProvider {
    private static final String ROOT = ".";

    // @formatter:off
    private static final Multimap<String, String> sourcesPerLanguageName = ImmutableMultimap.<String, String>builder()
        .putAll(LANG_NAME_ESV, DIR_EDITOR)
        .putAll(LANG_NAME_SDF, DIR_SYNTAX)
        .putAll(LANG_NAME_SDF3, DIR_SYNTAX)
        .putAll(LANG_NAME_STRATEGO, ROOT, DIR_TRANS)
        .putAll(LANG_NAME_DYNSEM, ROOT, DIR_TRANS)
        .build();
    // @formatter:on

    private static final Multimap<String, String> includesPerLanguageName = ImmutableMultimap.<String, String>builder()
        .build();

    private static final Collection<String> defaultIncludes = Lists.newArrayList(DIR_LIB);


    @Override public Iterable<FileObject> sourcePaths(IProject project, String language) {
        return resolve(project.location(), sourcesPerLanguageName.get(language));
    }

    @Override public Iterable<FileObject> includePaths(IProject project, String language) {
        return Iterables.concat(resolve(project.location(), includesPerLanguageName.get(language)),
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
