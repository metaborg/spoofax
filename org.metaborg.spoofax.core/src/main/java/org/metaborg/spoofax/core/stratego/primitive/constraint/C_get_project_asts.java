package org.metaborg.spoofax.core.stratego.primitive.constraint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.metaborg.spoofax.core.context.constraint.IConstraintContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;

public class C_get_project_asts extends ConstraintContextPrimitive {

    public C_get_project_asts() {
        super(C_get_project_asts.class.getSimpleName());
    }

    @Override protected Optional<? extends IStrategoTerm> call(IConstraintContext context, IStrategoTerm sterm,
            List<IStrategoTerm> sterms, ITermFactory factory) throws InterpreterException {
        final List<IStrategoTuple> entries = new ArrayList<>();
        for(Entry<String, IConstraintContext.Entry> e : context.entrySet()) {
            IStrategoString resource = factory.makeString(e.getKey());
            IStrategoTerm ast = e.getValue().analyzedAst();
            entries.add(factory.makeTuple(resource, ast));
        }
        final IStrategoTerm asts = factory.makeList(entries);
        return Optional.ofNullable(asts);
    }

}