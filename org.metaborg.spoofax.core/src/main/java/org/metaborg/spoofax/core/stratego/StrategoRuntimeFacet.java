package org.metaborg.spoofax.core.stratego;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.IFacet;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.collect.Lists;

/**
 * Represents the Stratego runtime facet of a language.
 */
public class StrategoRuntimeFacet implements IFacet {
    private static final ILogger logger = LoggerUtils.logger(StrategoRuntimeFacet.class);

    public final Iterable<FileObject> ctreeFiles;
    public final Iterable<FileObject> jarFiles;


    public StrategoRuntimeFacet(Iterable<FileObject> ctreeFiles, Iterable<FileObject> jarFiles) {
        this.ctreeFiles = ctreeFiles;
        this.jarFiles = jarFiles;
    }


    /**
     * Checks if all CTree and JAR files exist, returns errors if not.
     * 
     * @return Errors, or empty if there are no errors.
     * @throws IOException
     *             When a file operation fails.
     */
    public Iterable<String> available(IResourceService resourceService) throws IOException {
        final Collection<String> errors = Lists.newLinkedList();
        for(FileObject file : ctreeFiles) {
            if(!file.exists()) {
                final String message = logger.format("Stratego CTree file {} does not exist", file);
                errors.add(message);
            }
        }
        for(FileObject file : jarFiles) {
            if(!file.exists()) {
                final String message = logger.format("Stratego JAR file {} does not exist", file);
                errors.add(message);
            } else {
                final File localFile = resourceService.localFile(file);
                try(final JarFile jarFile = new JarFile(localFile, false, ZipFile.OPEN_READ)) {
                    if(!jarFile.entries().hasMoreElements()) {
                        final String message = logger.format("Stratego JAR file {} is empty", file);
                        errors.add(message);
                    }
                }
            }
        }
        return errors;
    }
}
