package org.metaborg.spoofax.core.project;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.ILanguageSpec;
import org.metaborg.spoofax.core.project.settings.ISpoofaxLanguageSpecConfig;

import static org.metaborg.spoofax.core.SpoofaxConstants.*;
import static org.metaborg.spoofax.core.SpoofaxConstants.DIR_CACHE;
import static org.metaborg.spoofax.core.SpoofaxConstants.DIR_EDITOR;

/**
 * Service that provides the structure (folders and files) of a language specification.
 */
public interface ILanguageSpecStructureService {



    FileObject getGeneratedSourceDirectory(FileObject location, ISpoofaxLanguageSpecConfig config);

    FileObject getOutputDirectory(FileObject location, ISpoofaxLanguageSpecConfig config);

    FileObject getIconsDirectory(FileObject location, ISpoofaxLanguageSpecConfig config) ;

    FileObject getLibDirectory(FileObject location, ISpoofaxLanguageSpecConfig config);

    FileObject getSyntaxDirectory(FileObject location, ISpoofaxLanguageSpecConfig config);

    FileObject getEditorDirectory(FileObject location, ISpoofaxLanguageSpecConfig config);

    FileObject getJavaDirectory(FileObject location, ISpoofaxLanguageSpecConfig config);

    FileObject getJavaTransDirectory(FileObject location, ISpoofaxLanguageSpecConfig config);

    FileObject getGeneratedSyntaxDirectory(FileObject location, ISpoofaxLanguageSpecConfig config);

    FileObject getTransDirectory(FileObject location, ISpoofaxLanguageSpecConfig config);

    FileObject getCacheDirectory(FileObject location, ISpoofaxLanguageSpecConfig config);

    FileObject getMainESVFile(FileObject location, ISpoofaxLanguageSpecConfig config);

//
//
//    FileObject getGeneratedSourceDirectory(ILanguageSpec languageSpec);
//
//    FileObject getOutputDirectory(ILanguageSpec languageSpec);
//
//    FileObject getIconsDirectory(ILanguageSpec languageSpec) ;
//
//    FileObject getLibDirectory(ILanguageSpec languageSpec);
//
//    FileObject getSyntaxDirectory(ILanguageSpec languageSpec);
//
//    FileObject getEditorDirectory(ILanguageSpec languageSpec);
//
//    FileObject getJavaDirectory(ILanguageSpec languageSpec);
//
//    FileObject getJavaTransDirectory(ILanguageSpec languageSpec);
//
//    FileObject getGeneratedSyntaxDirectory(ILanguageSpec languageSpec);
//
//    FileObject getTransDirectory(ILanguageSpec languageSpec);
//
//    FileObject getCacheDirectory(ILanguageSpec languageSpec);
//
//    FileObject getMainESVFile(ILanguageSpec languageSpec);
//

}
