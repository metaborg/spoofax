package org.metaborg.spoofax.build.cleardep.stampers;

import org.metaborg.spoofax.build.cleardep.LoggingFilteringIOAgent;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor.ExecutionResult;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermTransformer;
import org.strategoxt.stratego_sdf.parse_sdf_definition_file_0_0;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamp;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;

public class Sdf2RtgStamper implements Stamper {
	private static final long serialVersionUID = 3294157251470549994L;
	
	public final static Sdf2RtgStamper instance = new Sdf2RtgStamper();
	
	private Sdf2RtgStamper() { }

	@Override
	public Stamp stampOf(Path p) {
		if (!FileCommands.exists(p))
			return new TermStamp(Sdf2RtgStamper.instance, null);

		ITermFactory factory = StrategoExecutor.strategoSdfcontext().getFactory();
		ExecutionResult er = StrategoExecutor.runStratego(true, StrategoExecutor.strategoSdfcontext(), 
				parse_sdf_definition_file_0_0.instance, "parse-sdf-definition", new LoggingFilteringIOAgent(),
				factory.makeString(p.getAbsolutePath()));
		
		if (!er.success)
			return LastModifiedStamper.instance.stampOf(p);

		Deliteralize deliteralize = new Deliteralize(factory, false);
		IStrategoTerm delit = deliteralize.transform(er.result);
		return new TermStamp(Sdf2RtgStamper.instance, delit);
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
