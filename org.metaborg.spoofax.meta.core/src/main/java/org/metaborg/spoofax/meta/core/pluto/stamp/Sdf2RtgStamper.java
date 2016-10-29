package org.metaborg.spoofax.meta.core.pluto.stamp;

import java.io.File;
import java.io.IOException;

import org.metaborg.core.syntax.ParseException;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermTransformer;
import org.sugarj.common.FileCommands;

import build.pluto.stamp.LastModifiedStamper;
import build.pluto.stamp.Stamp;
import build.pluto.stamp.Stamper;
import build.pluto.stamp.ValueStamp;

public class Sdf2RtgStamper implements Stamper {
    private static final long serialVersionUID = -8516817559822107040L;

    private final SpoofaxContext context;


    public Sdf2RtgStamper(SpoofaxContext context) {
        this.context = context;
    }


    @Override public Stamp stampOf(File file) {
        if(!FileCommands.exists(file)) {
            return new ValueStamp<>(this, null);
        }

        final IStrategoTerm term;
        try {
            term = context.parse(file);
        } catch(ParseException | IOException e) {
            return LastModifiedStamper.instance.stampOf(file);
        }
        if(term == null) {
            return LastModifiedStamper.instance.stampOf(file);
        }


        final Deliteralize deliteralize = new Deliteralize(context.termFactory(), false);
        final IStrategoTerm delit = deliteralize.transform(term);
        return new ValueStamp<>(this, delit);
    }


    private static class Deliteralize extends TermTransformer {
        private final ITermFactory factory;


        public Deliteralize(ITermFactory factory, boolean keepAttachments) {
            super(factory, keepAttachments);
            this.factory = factory;
        }


        @Override public IStrategoTerm preTransform(IStrategoTerm term) {
            if(term instanceof IStrategoAppl && ((IStrategoAppl) term).getConstructor().getName().equals("lit"))
                return factory.makeAppl(factory.makeConstructor("lit", 1), factory.makeString(""));
            return term;
        }
    }
}
