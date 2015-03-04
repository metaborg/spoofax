package org.metaborg.spoofax.build.cleardep.stampers;

import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermTransformer;
import org.sugarj.cleardep.build.BuildManager;
import org.sugarj.cleardep.build.BuildRequest;
import org.sugarj.cleardep.output.SimpleOutput;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamp;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.cleardep.stamp.ValueStamp;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;

public class Sdf2RtgStamper implements Stamper {
	private static final long serialVersionUID = -8516817559822107040L;
	
	private BuildRequest<?, SimpleOutput<IStrategoTerm>, ?, ?> parseSdfDefinition;
	
	public Sdf2RtgStamper(BuildRequest<?, SimpleOutput<IStrategoTerm>, ?, ?> parseSdfDefinition) {
		this.parseSdfDefinition = parseSdfDefinition;
	}

	@Override
	public Stamp stampOf(Path p) {
		if (!FileCommands.exists(p))
			return new ValueStamp<>(this, null);

		SimpleOutput<IStrategoTerm> output = BuildManager.build(parseSdfDefinition);
		IStrategoTerm term = output.val;
		
		if (term == null)
			return LastModifiedStamper.instance.stampOf(p);
		
		ITermFactory factory = StrategoExecutor.strategoSdfcontext().getFactory();
		Deliteralize deliteralize = new Deliteralize(factory, false);
		IStrategoTerm delit = deliteralize.transform(term);
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
