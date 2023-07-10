package org.metaborg.spoofax.core.stratego.primitive;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxPrimitive;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import javax.inject.Inject;
import org.spoofax.terms.util.TermUtils;

public class AbsolutePathPrimitive extends ASpoofaxPrimitive {
    private final IResourceService resourceService;


    @Inject public AbsolutePathPrimitive(IResourceService resourceService) {
        super("absolute_path", 0, 1);
        this.resourceService = resourceService;
    }


    @Override protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
        ITermFactory factory, IContext context) {
        if(!(TermUtils.isString(current))) {
            return null;
        }
        String path = TermUtils.toJavaString(current);

        IStrategoTerm basePathTerm = tvars[0];
        if(!(TermUtils.isString(basePathTerm))) {
            return null;
        }
        String basePath = TermUtils.toJavaString(basePathTerm);

        final FileObject base = resourceService.resolve(basePath);
        final FileObject abs = resourceService.resolve(base, path);
        return factory.makeString(abs.getName().getURI());
    }
}
