package org.metaborg.spoofax.core.stratego;

import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.spoofax.core.esv.ESVReader;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoAppl;

import com.google.common.collect.Sets;

public class StrategoRuntimeFacetFromESV {
    private static final ILogger logger = LoggerUtils.logger(StrategoRuntimeFacetFromESV.class);


    public static @Nullable StrategoRuntimeFacet create(IStrategoAppl esv, FileObject location) throws FileSystemException {
        final Set<FileObject> strategoFiles = providerResources(esv, location);
        // Use LinkedHashSet to maintain ordering.
        final Set<FileObject> ctreeFiles = Sets.newLinkedHashSet();
        final Set<FileObject> jarFiles = Sets.newLinkedHashSet();
        for(FileObject strategoFile : strategoFiles) {
            final String extension = strategoFile.getName().getExtension();
            if(extension.equals("jar")) {
                jarFiles.add(strategoFile);
            } else if(extension.equals("ctree")) {
                ctreeFiles.add(strategoFile);
            } else {
                logger.warn("Stratego provider file {} has unknown extension {}, ignoring", strategoFile, extension);
            }
        }

        if(ctreeFiles.isEmpty() && jarFiles.isEmpty()) {
            return null;
        }
        
        final StrategoRuntimeFacet facet = new StrategoRuntimeFacet(ctreeFiles, jarFiles);
        return facet;
    }


    private static Set<FileObject> providerResources(IStrategoAppl esv, FileObject location) throws FileSystemException {
        // Use LinkedHashSet to maintain ordering.
        final Set<FileObject> attachedFiles = Sets.newLinkedHashSet();
        for(IStrategoAppl s : ESVReader.collectTerms(esv, "SemanticProvider")) {
            attachedFiles.add(location.resolveFile(ESVReader.termContents(s)));
        }
        return attachedFiles;
    }
}
