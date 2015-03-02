package org.metaborg.spoofax.build.cleardep.stampers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.metaborg.spoofax.build.cleardep.LoggingFilteringIOAgent;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor;
import org.metaborg.spoofax.build.cleardep.StrategoExecutor.ExecutionResult;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermVisitor;
import org.strategoxt.stratego_sdf.parse_sdf_definition_file_0_0;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamp;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;

import com.google.common.base.Objects;

public class Sdf2ParenthesizeStamper implements Stamper {
	private static final long serialVersionUID = 3294157251470549994L;
	
	public final static Sdf2ParenthesizeStamper instance = new Sdf2ParenthesizeStamper();
	
	private Sdf2ParenthesizeStamper() { }

	@Override
	public Stamp stampOf(Path p) {
		if (!FileCommands.exists(p))
			return new TermStamp(Sdf2ParenthesizeStamper.instance, null);

		ITermFactory factory = StrategoExecutor.strategoSdfcontext().getFactory();
		ExecutionResult er = StrategoExecutor.runStratego(true, StrategoExecutor.strategoSdfcontext(), 
				parse_sdf_definition_file_0_0.instance, "parse-sdf-definition", new LoggingFilteringIOAgent(),
				factory.makeString(p.getAbsolutePath()));
		
		if (!er.success)
			return LastModifiedStamper.instance.stampOf(p);

		ParenExtractor parenExtractor = new ParenExtractor(factory);
		parenExtractor.visit(er.result);
		return new Sdf2ParenthesizeStamp(parenExtractor.getRelevantProds(), parenExtractor.getPriorities());
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
	
	public class Sdf2ParenthesizeStamp implements Stamp {
		private static final long serialVersionUID = -8303716484088476176L;

		private final Set<IStrategoTerm> relevantProds;
		private final Set<IStrategoTerm> priorities;
		
		public Sdf2ParenthesizeStamp(Set<IStrategoTerm> relevantProds, Set<IStrategoTerm> priorities) {
			this.relevantProds = relevantProds;
			this.priorities = priorities;
		}
		
		@Override
		public Stamper getStamper() {
			return Sdf2ParenthesizeStamper.instance;
		}
		
		@Override
		public boolean equals(Object o) {
			if (o instanceof Sdf2ParenthesizeStamp) {
				Sdf2ParenthesizeStamp s = (Sdf2ParenthesizeStamp) o;
				return Objects.equal(relevantProds, s.relevantProds) && Objects.equal(priorities, s.priorities);
			}
			return false;
		}
	}
}
