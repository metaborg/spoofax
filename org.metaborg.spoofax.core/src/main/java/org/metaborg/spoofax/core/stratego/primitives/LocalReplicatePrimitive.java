package org.metaborg.spoofax.core.stratego.primitives;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.resource.IResourceService;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

public class LocalReplicatePrimitive extends AbstractPrimitive {
    private final IResourceService resourceService;


    @Inject public LocalReplicatePrimitive(IResourceService resourceService) {
        super("local_replicate", 0, 0);
        this.resourceService = resourceService;
    }


    @Override public boolean call(IContext env, Strategy[] strategies, IStrategoTerm[] terms)
        throws InterpreterException {
        final IStrategoTerm current = env.current();
        if(!(current instanceof IStrategoString)) {
            return false;
        }

        final IStrategoString currentStr = (IStrategoString) current;
        final String path = currentStr.stringValue();
        final FileObject resource = resourceService.resolve(path);
        final File localFile = resourceService.localFile(resource);
        env.setCurrent(env.getFactory().makeString(localFile.getPath()));
        return true;
    }
}
