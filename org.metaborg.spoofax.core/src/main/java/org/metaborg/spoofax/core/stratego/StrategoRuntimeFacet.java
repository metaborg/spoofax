package org.metaborg.spoofax.core.stratego;

import java.io.IOException;
import java.util.Collection;

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

    public final Collection<FileObject> ctreeFiles;


    public StrategoRuntimeFacet(Collection<FileObject> ctreeFiles) {
        this.ctreeFiles = ctreeFiles;
    }


    /**
     * Checks if all CTree and JAR files exist, returns errors if not.
     * 
     * @return Errors, or empty if there are no errors.
     * @throws IOException
     *             When a file operation fails.
     */
    public Collection<String> available(IResourceService resourceService) throws IOException {
        final Collection<String> errors = Lists.newLinkedList();
        for(FileObject file : ctreeFiles) {
            if(!file.exists()) {
                final String message = logger.format("Stratego CTree file {} does not exist", file);
                errors.add(message);
            }
        }
        return errors;
    }
}
