package org.metaborg.spoofax.core.stratego.primitives;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.spoofax.core.context.scopegraph.IScopeGraphContext;
import org.metaborg.spoofax.core.context.scopegraph.IScopeGraphUnit;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.inject.Inject;

public class GetAnalysisPrimitive extends AbstractPrimitive {
    
    private final IResourceService resourceService;
    
    @Inject public GetAnalysisPrimitive(IResourceService resourceService) {
        super("SSL_EXT_get_analysis", 0, 0);
        this.resourceService = resourceService;
    }


    @Override public boolean call(IContext env, Strategy[] strategies, IStrategoTerm[] terms)
        throws InterpreterException {
        IScopeGraphContext context ;
        try {
            context = (IScopeGraphContext) env.contextObject();
        } catch (ClassCastException ex) {
            throw new InterpreterException("get-analysis needs a scopegraph context.",ex);
        }
        FileObject source = resourceService.resolve(Tools.asJavaString(env.current()));
        IScopeGraphUnit unit = context.units().get(source);
        if(unit == null) {
            return false;
        }
        IStrategoTerm analysis = unit.analysis();
        if(analysis == null) {
            analysis = context.analysis();
        }
        if(analysis == null) {
            return false;
        }
        env.setCurrent(analysis);
        return true;
    }
}
