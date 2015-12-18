package org.metaborg.spoofax.core.stratego.primitives;

import java.io.File;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.inject.Inject;

public class ProjectPathPrimitive extends AbstractPrimitive {
    private final IResourceService resourceService;


    @Inject public ProjectPathPrimitive(IResourceService resourceService) {
        super("SSL_EXT_projectpath", 0, 0);

        this.resourceService = resourceService;
    }


    @Override public boolean call(IContext env, Strategy[] strategies, IStrategoTerm[] terms)
        throws InterpreterException {
        final ITermFactory factory = env.getFactory();
        final org.metaborg.spoofax.core.context.IContext context =
            (org.metaborg.spoofax.core.context.IContext) env.contextObject();
        final FileObject resource = context.location();
        final File localFile = resourceService.localFile(resource);
        final IStrategoTerm pathTerm = factory.makeString(localFile.toString());
        env.setCurrent(pathTerm);
        return true;
    }
}
