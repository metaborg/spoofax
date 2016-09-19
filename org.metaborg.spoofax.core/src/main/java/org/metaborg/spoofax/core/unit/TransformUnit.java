package org.metaborg.spoofax.core.unit;

import org.metaborg.core.action.TransformActionContrib;
import org.metaborg.core.context.IContext;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.unit.IUnit;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class TransformUnit<I extends IUnit> extends UnitWrapper implements ISpoofaxTransformUnit<I> {
    private final TransformContrib contrib;
    private final I inputUnit;
    private final IContext context;
    private final TransformActionContrib action;


    public TransformUnit(Unit unit, TransformContrib contrib, I inputUnit, IContext context,
        TransformActionContrib action) {
        super(unit);
        this.contrib = contrib;
        this.inputUnit = inputUnit;
        this.context = context;
        this.action = action;
    }


    @Override public boolean valid() {
        return contrib.valid;
    }

    @Override public boolean success() {
        return contrib.success;
    }

    @Override public IStrategoTerm ast() {
        return contrib.ast;
    }

    @Override public Iterable<TransformOutput> outputs() {
        return contrib.outputs;
    }

    @Override public Iterable<IMessage> messages() {
        return contrib.messages;
    }

    @Override public I input() {
        return inputUnit;
    }

    @Override public IContext context() {
        return context;
    }

    @Override public TransformActionContrib action() {
        return action;
    }

    @Override public long duration() {
        return contrib.duration;
    }
}
