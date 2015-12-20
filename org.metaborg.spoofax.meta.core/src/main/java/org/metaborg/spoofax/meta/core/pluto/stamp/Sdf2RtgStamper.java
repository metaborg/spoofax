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

	private BuildRequest<?, OutputPersisted<IStrategoTerm>, ?, ?> parseSdfDefinition;

	public Sdf2RtgStamper(BuildRequest<?, OutputPersisted<IStrategoTerm>, ?, ?> parseSdfDefinition) {
		this.parseSdfDefinition = parseSdfDefinition;
	}

	@Override
	public Stamp stampOf(File p) {
		if (!FileCommands.exists(p))
			return new ValueStamp<>(this, null);

		OutputPersisted<IStrategoTerm> term = BuildManagers.build(parseSdfDefinition);

		if (term == null || term.val == null)
			return LastModifiedStamper.instance.stampOf(p);

		ITermFactory factory = StrategoExecutor.strategoSdfcontext().getFactory();
		Deliteralize deliteralize = new Deliteralize(factory, false);
		IStrategoTerm delit = deliteralize.transform(term.val);
		return new ValueStamp<>(this, delit);
	}

	private static class Deliteralize extends TermTransformer {
		private final ITermFactory factory;

		public Deliteralize(ITermFactory factory, boolean keepAttachments) {
			super(factory, keepAttachments);
			this.factory = factory;
		}

		@Override
		public IStrategoTerm preTransform(IStrategoTerm term) {
			if (term instanceof IStrategoAppl && ((IStrategoAppl) term).getConstructor().getName().equals("lit"))
				return factory.makeAppl(factory.makeConstructor("lit", 1), factory.makeString(""));
			return term;
		}
	}
}
