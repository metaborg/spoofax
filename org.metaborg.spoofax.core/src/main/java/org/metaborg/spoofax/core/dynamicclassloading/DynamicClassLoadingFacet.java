package org.metaborg.spoofax.core.dynamicclassloading;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.IFacet;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

/**
 * Represents the DynamicClassLoading facet of a language.
 */
public class DynamicClassLoadingFacet implements IFacet {
    private static final ILogger logger = LoggerUtils.logger(DynamicClassLoadingFacet.class);

    public final Collection<FileObject> jarFiles;


    public DynamicClassLoadingFacet(Collection<FileObject> jarFiles) {
        this.jarFiles = jarFiles;
    }


    /**
     * Checks if all CTree and JAR files exist, returns errors if not.
     * 
     * @return Errors, or empty if there are no errors.
     * @throws IOException
     *             When a file operation fails.
     */
    public Collection<String> available(IResourceService resourceService) throws IOException {
        final Collection<String> errors = new LinkedList<>();
        for(FileObject file : jarFiles) {
            if(!file.exists()) {
                final String message = logger.format("JAR file {} does not exist", file);
                errors.add(message);
            } else {
                final File localFile = resourceService.localFile(file);
                try(final JarFile jarFile = new JarFile(localFile, false, ZipFile.OPEN_READ)) {
                    if(!jarFile.entries().hasMoreElements()) {
                        final String message = logger.format("JAR file {} is empty", file);
                        errors.add(message);
                    }
                }
            }
        }
        return errors;
    }
}
