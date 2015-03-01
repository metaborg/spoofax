package org.metaborg.spoofax.build.cleardep.stampers;

import java.util.Objects;

import org.spoofax.interpreter.terms.IStrategoTerm;
import org.sugarj.cleardep.stamp.Stamp;
import org.sugarj.cleardep.stamp.Stamper;

public class TermStamp implements Stamp {
	private static final long serialVersionUID = 3141110117353111640L;
	
	private final Stamper stamper;
	private final IStrategoTerm term;
	
	public TermStamp(Stamper stamper, IStrategoTerm term) {
		this.stamper = stamper;
		this.term = term;
	}
	
	@Override
	public Stamper getStamper() {
		return stamper;
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof TermStamp && Objects.equals(term, ((TermStamp) o).term);
	}
}
