package org.strategoxt.imp.runtime.stratego.adapter;

import lpg.runtime.IAst;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermPrinter;

/**
 * A constructor application AST node wrapped into an ATerm.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 * @author Karl Trygve Kalleberg <karltk add strategoxt.org>
 */
public class WrappedAstNodeAppl extends WrappedAstNode implements IStrategoAppl {
	private final IStrategoConstructor constructor;

	public IStrategoConstructor getConstructor() {
		return constructor;
	}
	
	protected WrappedAstNodeAppl(WrappedAstNodeFactory factory, IStrategoAstNode node) {
		super(factory, node);
		
		constructor = factory.makeConstructor(node.getConstructor(), node.getChildren().size());
	}
	
	protected WrappedAstNodeAppl(WrappedAstNodeFactory factory, IAst node) {
		super(factory, node);
		
		constructor = factory.makeConstructor(node.getClass().getSimpleName(), node.getChildren().size());
	}
	
	public final IStrategoTerm[] getArguments() {
		return getAllSubterms();
	}

	@Override
	protected boolean slowCompare(IStrategoTerm second) {
		if (second.getTermType() != IStrategoTerm.APPL)
			return false;
		IStrategoAppl snd = (IStrategoAppl) second;
		if (snd.getSubtermCount() != getSubtermCount())
			return false;
		if (!snd.getConstructor().equals(getConstructor()))
			return false;
		if (!snd.getAnnotations().equals(getAnnotations()))
			return false;
		for (int i = 0, sz = getSubtermCount(); i < sz; i++) {
			if (!snd.getSubterm(i).equals(getSubterm(i)))
				return false;
		}
		return true;
	}

	public void prettyPrint(ITermPrinter pp) {
		final String name = constructor.getName() == null ? "<#null>" : constructor.getName();
		pp.print(name);
		pp.print("(");
		final IStrategoTerm[] kids = getAllSubterms();
		if (kids.length > 0) {
			pp.indent(name.length());
			if (kids[0] == null) {
				pp.print("<#null>");
			} else {
				kids[0].prettyPrint(pp);
			}
			for (int i = 1; i < kids.length; i++) {
				pp.print(",");
				if (kids[1] == null)
					pp.print("<#null>");
				else
					kids[i].prettyPrint(pp);
			}
			pp.outdent(name.length());
		}
		pp.println(")");
		printAnnotations(pp);
	}

	public int getTermType() {
		return IStrategoTerm.APPL;
	}
	
	@Override
	public int hashCode() {
        long r = constructorHashCode();
        int accum = 6673;
        for(int i = 0; i < getSubtermCount(); i++) {
            r += getSubterm(i).hashCode() * accum;
            accum *= 7703;
        }
        return (int)(r >> 12);
	}
	
	private int constructorHashCode() {
		return constructor.hashCode() + 5407 * getSubtermCount();
	}
}
