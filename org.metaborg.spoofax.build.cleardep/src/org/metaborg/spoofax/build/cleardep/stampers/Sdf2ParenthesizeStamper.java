package org.metaborg.spoofax.build.cleardep.stampers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermVisitor;
import org.sugarj.cleardep.build.BuildManager;
import org.sugarj.cleardep.build.BuildRequest;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamp;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.cleardep.stamp.ValueStamp;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.util.Pair;

public class Sdf2ParenthesizeStamper implements Stamper {
	private static final long serialVersionUID = 3294157251470549994L;
	
	private final BuildRequest<?, IStrategoTerm, ?, ?> parseSdfDefinition;
	
	public Sdf2ParenthesizeStamper(BuildRequest<?, IStrategoTerm, ?, ?> parseSdfDefinition) {
		this.parseSdfDefinition = parseSdfDefinition;
	}

	@Override
	public Stamp stampOf(Path p) {
		if (!FileCommands.exists(p))
			return new ValueStamp<>(this, null);

		IStrategoTerm term = BuildManager.build(parseSdfDefinition);
		
		if (term == null)
			return LastModifiedStamper.instance.stampOf(p);
		
		ITermFactory factory = StrategoExecutor.strategoSdfcontext().getFactory();
		ParenExtractor parenExtractor = new ParenExtractor(factory);
		parenExtractor.visit(term);
		return new ValueStamp<>(this, Pair.create(parenExtractor.getRelevantProds(), parenExtractor.getPriorities()));
	}

	private static class ParenExtractor extends TermVisitor {
		private final Set<IStrategoTerm> relevantProds;
		private final Set<IStrategoTerm> priorities;

		private final ITermFactory factory;
		private final IStrategoTerm noAttrs;
		private List<IStrategoTerm> prods;

		private boolean inPriorities = false;
		
		public ParenExtractor(ITermFactory factory) {
			this.factory = factory;
			this.relevantProds = new HashSet<>();
			this.priorities = new HashSet<>();
			this.noAttrs = factory.makeAppl(factory.makeConstructor("attrs", 1), factory.makeList());
			this.prods = new ArrayList<>();
		}

		@Override
		public void preVisit(IStrategoTerm term) {
			if (term instanceof IStrategoAppl)
				switch (((IStrategoAppl) term).getConstructor().getName()) {
				case "context-free-priorities":
				case "priorities":
					priorities.add(term);
					inPriorities = true;
					break;
				case "prod":
					if (inPriorities) {
						relevantProds.add(term);
						relevantProds.add(noProdAttrs(term));
					}
					else {
						prods.add(term);
					}
				default:
					break;
				}
		}
		
		private IStrategoTerm noProdAttrs(IStrategoTerm term) {
			return factory.makeAppl(factory.makeConstructor("prod", 3), 
					term.getSubterm(0),
					term.getSubterm(1),
					noAttrs);
		}

		@Override
		public void postVisit(IStrategoTerm term) {
			if (term instanceof IStrategoAppl)
				switch (((IStrategoAppl) term).getConstructor().getName()) {
				case "context-free-priorities":
				case "priorities":
					inPriorities = false;
					break;
				default:
					break;
				}
		}
		
		public Set<IStrategoTerm> getPriorities() {
			return priorities;
		}

		public Set<IStrategoTerm> getRelevantProds() {
			if (prods != null) 
				for (IStrategoTerm prod : prods)
					if (relevantProds.contains(noProdAttrs(prod)))
						relevantProds.add(prod);
			prods = null;
			return relevantProds;
		}

	}
}
