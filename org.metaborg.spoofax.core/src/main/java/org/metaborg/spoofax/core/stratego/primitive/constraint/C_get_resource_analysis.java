package org.metaborg.spoofax.core.stratego.primitive.constraint;

import java.util.List;
import java.util.Optional;

import org.metaborg.spoofax.core.context.constraint.IConstraintContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import mb.nabl2.stratego.StrategoBlob;

public class C_get_resource_analysis extends ConstraintContextPrimitive {

    public C_get_resource_analysis() {
        super(C_get_resource_analysis.class.getSimpleName());
    }

    @Override protected Optional<? extends IStrategoTerm> call(IConstraintContext context, IStrategoTerm sterm,
            List<IStrategoTerm> sterms, ITermFactory factory) throws InterpreterException {
        if(!Tools.isTermString(sterm)) {
            throw new InterpreterException("Expect a resource path.");
        }
        final String resource = Tools.asJavaString(sterm);
        final IStrategoTerm analysis;
        switch(context.mode()) {
            case MULTI_FILE:
                analysis = context.hasFinal() ? context.getFinal().analysis : null;
                break;
            case SINGLE_FILE:
                analysis = context.contains(resource) ? context.get(resource).analysis : null;
                break;
            default:
                analysis = null;
                break;
        }
        return Optional.ofNullable(analysis).<IStrategoTerm>map(a -> new StrategoBlob(a));
    }

}