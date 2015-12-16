package org.metaborg.spoofax.core.project;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.project.ILanguageSpecPaths;

/**
 * Specifies the paths used in a Spoofax language specification.
 */
public interface ISpoofaxLanguageSpecPaths extends ILanguageSpecPaths {

    // TODO: Rename to ..Folder

    FileObject generatedSourceDirectory();

    FileObject iconsDirectory() ;

    FileObject libDirectory();

    FileObject syntaxDirectory();

    FileObject editorDirectory();

    FileObject javaDirectory();

    FileObject javaTransDirectory();

    FileObject generatedSyntaxDirectory();

    FileObject transDirectory();

    FileObject cacheDirectory();

    FileObject mainEsvFile();
}
