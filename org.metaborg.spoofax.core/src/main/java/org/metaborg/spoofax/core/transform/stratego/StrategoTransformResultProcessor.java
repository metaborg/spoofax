package org.metaborg.spoofax.core.transform.stratego;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.spoofax.core.context.IContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;

public class StrategoTransformResultProcessor {
    private static final Logger logger = LoggerFactory.getLogger(StrategoTransformResultProcessor.class);


    public static FileObject writeFile(IStrategoTerm result, IContext context) {
        if(!(result instanceof IStrategoTuple))
            return null;

        final IStrategoTerm resourceTerm = result.getSubterm(0);
        if(!(resourceTerm instanceof IStrategoString)) {
            logger.error("First term of result tuple {} is not a string, cannot write output file");
        } else {
            final String resourceString = Tools.asJavaString(resourceTerm);
            final IStrategoTerm resultTerm = result.getSubterm(1);
            final String resultContents;
            if(resultTerm.getTermType() == IStrategoTerm.STRING) {
                resultContents = ((IStrategoString) resultTerm).stringValue();
            } else {
                resultContents = resultTerm.toString();
            }

            try {
                final FileObject resource = context.location().resolveFile(resourceString);
                try(OutputStream stream = resource.getContent().getOutputStream()) {
                    IOUtils.write(resultContents, stream);
                } catch(IOException e) {
                    logger.error("Error occured while writing output file", e);
                }
                return resource;
            } catch(FileSystemException e) {
                logger.error("Error occured while writing output file", e);
            }
        }
        
        return null;
    }
}
