package org.metaborg.spoofax.meta.core.pluto.stamp;

import java.io.File;

import org.metaborg.spoofax.meta.core.pluto.StrategoExecutor;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermTransformer;
import org.sugarj.common.FileCommands;

import build.pluto.builder.BuildManagers;
import build.pluto.builder.BuildRequest;
import build.pluto.output.OutputPersisted;
import build.pluto.stamp.LastModifiedStamper;
import build.pluto.stamp.Stamp;
import build.pluto.stamp.Stamper;
import build.pluto.stamp.ValueStamp;

public class Sdf2RtgStamper implements Stamper {
    private static final long serialVersionUID = -8516817559822107040L;

    private BuildRequest<?, OutputPersisted<IStrategoTerm>, ?, ?> parseSdf;

    
    public Sdf2RtgStamper(BuildRequest<?, OutputPersisted<IStrategoTerm>, ?, ?> parseSdf) {
        this.parseSdf = parseSdf;
    }

    
    @Override public Stamp stampOf(File p) {
        if(!FileCommands.exists(p))
            return new ValueStamp<>(this, null);

        final OutputPersisted<IStrategoTerm> term;
        try {
            term = BuildManagers.build(parseSdf);
        } catch(Throwable e) {
            return LastModifiedStamper.instance.stampOf(p);
        }

        if(term == null || term.val == null) {
            return LastModifiedStamper.instance.stampOf(p);
        }

        final ITermFactory factory = StrategoExecutor.strategoSdfcontext().getFactory();
        final Deliteralize deliteralize = new Deliteralize(factory, false);
        final IStrategoTerm delit = deliteralize.transform(term.val);
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
