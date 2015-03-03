package org.metaborg.spoofax.build.cleardep.stampers;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermTransformer;
import org.sugarj.cleardep.BuildUnit;
import org.sugarj.cleardep.build.BuildManager;
import org.sugarj.cleardep.build.BuildRequest;
import org.sugarj.cleardep.output.SimpleOutput;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamp;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.cleardep.stamp.ValueStamp;
import org.sugarj.common.ATermCommands;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;

public class PPGenStamper implements Stamper {
	private static final long serialVersionUID = 3294157251470549994L;
	
	private final BuildRequest<?, SimpleOutput<IStrategoTerm>, ?, ?> parseSdfDefinition;
	
	public PPGenStamper(BuildRequest<?, SimpleOutput<IStrategoTerm>, ?, ?> parseSdfDefinition) {
		this.parseSdfDefinition = parseSdfDefinition;
	}

	@Override
	public Stamp stampOf(Path p) {
		if (!FileCommands.exists(p))
			return new ValueStamp<>(this, null);

		BuildManager manager = BuildManager.acquire();
		IStrategoTerm term;
		try {
			BuildUnit<SimpleOutput<IStrategoTerm>> unit = manager.require(parseSdfDefinition);
			term = unit.getBuildResult().val;
		} catch (IOException e) {
			return LastModifiedStamper.instance.stampOf(p);
		}
		
		if (term == null)
			return LastModifiedStamper.instance.stampOf(p);
		
		ITermFactory factory = StrategoExecutor.strategoSdfcontext().getFactory();
		CFProdExtractor cfProdExtractor = new CFProdExtractor(factory);
		cfProdExtractor.transform(term);
		return new ValueStamp<>(this, cfProdExtractor.getRelevantProds());
	}

	private static class CFProdExtractor extends TermTransformer {
		private final Set<IStrategoTerm> relevantProds;

		private final ITermFactory factory;

		private boolean inContextFreeSyntax = false;
		
		public CFProdExtractor(ITermFactory factory) {
			super(factory, false);
			this.factory = factory;
			this.relevantProds = new HashSet<>();
		}

		@Override
		public IStrategoTerm preTransform(IStrategoTerm term) {
			if (term instanceof IStrategoAppl)
				switch (((IStrategoAppl) term).getConstructor().getName()) {
				case "context-free-syntax":
					inContextFreeSyntax = true;
					break;
				case "sort":
					if (inContextFreeSyntax)
						return factory.makeAppl(factory.makeConstructor("sort", 1), factory.makeString(""));
					break;
				default:
					break;
				}
			return term;
		}
		
		@Override
		public IStrategoTerm postTransform(IStrategoTerm term) {
			if (term instanceof IStrategoAppl)
				switch (((IStrategoAppl) term).getConstructor().getName()) {
				case "context-free-syntax":
					inContextFreeSyntax = false;
					break;
				case "prod":
					if (inContextFreeSyntax) {
						IStrategoAppl attrTerm = (IStrategoAppl) term.getSubterm(2);
						if (ATermCommands.isApplication(attrTerm, "attrs")) {
							for (IStrategoTerm attr : (IStrategoList) attrTerm.getSubterm(0))
								if (ATermCommands.isApplication(attr, "term") && ATermCommands.isApplication(attr.getSubterm(0), "cons")) {
									relevantProds.add(term);
									break;
								}
						}
					}
					break;
				default:
					break;
				}
			return term;
		}
		
		public Set<IStrategoTerm> getRelevantProds() {
			return relevantProds;
		}

	}
}
