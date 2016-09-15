package org.metaborg.spoofax.core.unit;

import javax.annotation.Nullable;

import org.metaborg.core.messages.IMessage;
import org.metaborg.core.unit.IUnitContrib;
import org.metaborg.util.iterators.Iterables2;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class TransformContrib implements IUnitContrib {
    public final boolean valid;
    public final boolean success;
    public final @Nullable IStrategoTerm ast;
    public final Iterable<TransformOutput> outputs;
    public final Iterable<IMessage> messages;
    public final long duration;


    public TransformContrib(boolean valid, boolean success, @Nullable IStrategoTerm ast,
        Iterable<TransformOutput> outputs, Iterable<IMessage> messages, long duration) {
        this.valid = valid;
        this.success = success;
        this.ast = ast;
        this.outputs = outputs;
        this.messages = messages;
        this.duration = duration;
    }

    public TransformContrib() {
        this(true, true, null, null, Iterables2.<IMessage>empty(), -1);
    }


    @Override public String id() {
        return "transform";
    }
}
