package org.metaborg.spoofax.meta.core.pluto.stamp;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.metaborg.spoofax.meta.core.pluto.StrategoExecutor;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermTransformer;
import org.sugarj.common.ATermCommands;
import org.sugarj.common.FileCommands;

import build.pluto.builder.BuildManagers;
import build.pluto.builder.BuildRequest;
import build.pluto.output.OutputPersisted;
import build.pluto.stamp.LastModifiedStamper;
import build.pluto.stamp.Stamp;
import build.pluto.stamp.Stamper;
import build.pluto.stamp.ValueStamp;

public class PPGenStamper implements Stamper {
	private static final long serialVersionUID = 3294157251470549994L;
	
	private final BuildRequest<?, OutputPersisted<IStrategoTerm>, ?, ?> parseSdfDefinition;
	
	public PPGenStamper(BuildRequest<?, OutputPersisted<IStrategoTerm>, ?, ?> parseSdfDefinition) {
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
		CFProdExtractor cfProdExtractor = new CFProdExtractor(factory);
		cfProdExtractor.transform(term.val);
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
