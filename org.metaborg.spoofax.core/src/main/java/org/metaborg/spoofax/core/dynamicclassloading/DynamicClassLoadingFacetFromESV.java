package org.metaborg.spoofax.core.dynamicclassloading;

import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.spoofax.core.esv.ESVReader;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.resource.ResourceUtils;
import org.spoofax.interpreter.terms.IStrategoAppl;

import com.google.common.collect.Sets;

public class DynamicClassLoadingFacetFromESV {
    private static final ILogger logger = LoggerUtils.logger(DynamicClassLoadingFacetFromESV.class);


    public static DynamicClassLoadingFacet create(IStrategoAppl esv, FileObject location) throws FileSystemException {
        final Set<FileObject> strategoFiles = providerResources(esv, location);
        // Use LinkedHashSet to maintain ordering.
        final Set<FileObject> jarFiles = Sets.newLinkedHashSet();
        for(FileObject strategoFile : strategoFiles) {
            final String extension = strategoFile.getName().getExtension();
            switch (extension) {
                case "jar":
                    jarFiles.add(strategoFile);
                    break;
                case "ctree":
                    break;
                default:
                    logger.warn("Stratego provider file {} has unknown extension {}, ignoring", strategoFile, extension);
                    break;
            }
        }

        return new DynamicClassLoadingFacet(jarFiles);
    }


    private static Set<FileObject> providerResources(IStrategoAppl esv, FileObject location) throws FileSystemException {
        // Use LinkedHashSet to maintain ordering.
        final Set<FileObject> attachedFiles = Sets.newLinkedHashSet();
        for(IStrategoAppl s : ESVReader.collectTerms(esv, "SemanticProvider")) {
            attachedFiles.add(ResourceUtils.resolveFile(location, ESVReader.termContents(s)));
        }
        return attachedFiles;
    }
}
