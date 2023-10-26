package org.metaborg.spoofax.core.stratego.primitive;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxPrimitive;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import org.spoofax.terms.util.TermUtils;

public class LocalReplicatePrimitive extends ASpoofaxPrimitive {
    private final IResourceService resourceService;


    @jakarta.inject.Inject @javax.inject.Inject public LocalReplicatePrimitive(IResourceService resourceService) {
        super("local_replicate", 0, 0);
        this.resourceService = resourceService;
    }


    @Override protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
        ITermFactory factory, IContext context) {
        if(!(TermUtils.isString(current))) {
            return null;
        }
        final IStrategoString currentStr = (IStrategoString) current;
        final String path = currentStr.stringValue();
        final FileObject resource = resourceService.resolve(path);
        final File localFile = resourceService.localFile(resource);
        return factory.makeString(localFile.getPath());
    }
}
