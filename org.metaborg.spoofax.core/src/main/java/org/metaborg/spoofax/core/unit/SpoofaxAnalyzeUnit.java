package org.metaborg.spoofax.core.unit;

import org.metaborg.core.analysis.AnalyzeUnitType;
import org.metaborg.core.context.IContext;
import org.metaborg.core.messages.IMessage;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class SpoofaxAnalyzeUnit extends SpoofaxUnitWrapper implements ISpoofaxAnalyzeUnit {
    private final AnalyzeContrib contrib;
    private final ISpoofaxParseUnit parseUnit;
    private final IContext context;


    public SpoofaxAnalyzeUnit(SpoofaxUnit unit, AnalyzeContrib contrib, ISpoofaxParseUnit parseUnit, IContext context) {
        super(unit);
        this.contrib = contrib;
        this.parseUnit = parseUnit;
        this.context = context;
    }


    @Override public boolean valid() {
        return contrib.valid;
    }

    @Override public boolean success() {
        return contrib.success;
    }

    @Override public AnalyzeUnitType type() {
        return contrib.type;
    }

    @Override public IStrategoTerm ast() {
        return contrib.ast;
    }

    @Override public Iterable<IMessage> messages() {
        return contrib.messages;
    }

    @Override public ISpoofaxParseUnit input() {
        return parseUnit;
    }

    @Override public IContext context() {
        return context;
    }

    @Override public long duration() {
        return contrib.duration;
    }
}
