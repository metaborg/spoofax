package org.metaborg.spoofax.core.stratego.primitive.constraint;

import java.util.List;
import java.util.Optional;

import org.metaborg.spoofax.core.analysis.constraint.IResourceKey;
import org.metaborg.spoofax.core.analysis.constraint.StringResourceKey;
import org.metaborg.spoofax.core.context.constraint.IConstraintContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

public class C_get_resource_analysis extends ConstraintContextPrimitive {

    public C_get_resource_analysis() {
        super(C_get_resource_analysis.class.getSimpleName());
    }

    @Override protected Optional<? extends IStrategoTerm> call(IConstraintContext context, IStrategoTerm sterm,
            List<IStrategoTerm> sterms, ITermFactory factory) throws InterpreterException {
        final IResourceKey resource = StringResourceKey.fromStrategoTerm(sterm);
        final IStrategoTerm analysis;
        if(context.hasAnalysis(resource)) {
            analysis = context.getAnalysis(resource);
        } else {
            analysis = null;
        }
        return Optional.ofNullable(analysis);
    }

}