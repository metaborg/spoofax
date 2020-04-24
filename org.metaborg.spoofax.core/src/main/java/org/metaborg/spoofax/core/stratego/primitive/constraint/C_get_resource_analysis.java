package org.metaborg.spoofax.core.stratego.primitive.constraint;

import java.util.List;
import java.util.Optional;

import org.metaborg.spoofax.core.context.constraint.IConstraintContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;

public class C_get_resource_analysis extends ConstraintContextPrimitive {

    public C_get_resource_analysis() {
        super(C_get_resource_analysis.class.getSimpleName());
    }

    @Override protected Optional<? extends IStrategoTerm> call(IConstraintContext context, IStrategoTerm sterm,
            List<IStrategoTerm> sterms, ITermFactory factory) throws InterpreterException {
        if(!TermUtils.isString(sterm)) throw new InterpreterException("Expect a resource path.");

        final String resource = TermUtils.toJavaString(sterm);
        final IStrategoTerm analysis;
        if(context.hasAnalysis(resource)) {
            analysis = context.getAnalysis(resource);
        } else {
            analysis = null;
        }
        return Optional.ofNullable(analysis);
    }

}