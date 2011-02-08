package org.strategoxt.imp.runtime.stratego.adapter;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermPrinter;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class WrappedAstNodeTuple extends WrappedAstNodeParent implements IStrategoTuple {

	protected WrappedAstNodeTuple(ISimpleTerm node) {
		super(node);
		node.setConstructor(""); // ensure interned string is used
	}

	@Override
	protected boolean doSlowMatch(IStrategoTerm second, int commonStorageType) {
        if (second.getTermType() != IStrategoTerm.TUPLE)
            return false;

        IStrategoTuple snd = (IStrategoTuple) second;
        if (size() != snd.size())
            return false;

        // TODO: optimize WrappedAstNodeTuple.doSlowMatch
        //       (getAllSubterms() creates new arrays)
        IStrategoTerm[] kids = getAllSubterms();
        IStrategoTerm[] secondKids = snd.getAllSubterms();
        if (kids != secondKids) {
	        for (int i = 0, sz = kids.length; i < sz; i++) {
	            IStrategoTerm kid = kids[i];
				IStrategoTerm secondKid = secondKids[i];
				if (kid != secondKid && !kid.match(secondKid)) {
	                return false;
	            }
	        }
        }
        
        IStrategoList annotations = getAnnotations();
        IStrategoList secondAnnotations = second.getAnnotations();
        return annotations == secondAnnotations || annotations.match(secondAnnotations);
	}

	public final IStrategoTerm get(int index) {
		return getSubterm(index);
	}

	public final IStrategoTerm head() {
		return getSubtermCount() == 0 ? null : get(0);
	}

	public final boolean isEmpty() {
		return getSubtermCount() == 0;
	}

	public final int size() {
		return getSubtermCount();
	}

	public int getTermType() {
		return TUPLE;
	}

	public void prettyPrint(ITermPrinter pp) {
        int sz = size();
        if(sz > 0) {
            pp.println("(");
            pp.indent(2);
            get(0).prettyPrint(pp);
            for(int i = 1; i < sz; i++) {
                pp.print(",");
                pp.nextIndentOff();
                get(i).prettyPrint(pp);
                pp.println("");
            }
            pp.println("");
            pp.print(")");
            pp.outdent(2);

        } else {
            pp.print("()");
        }
        printAnnotations(pp);
	}

	@Override
	public int hashFunction() {
        long hc = 4831;
        IStrategoTerm[] kids = getAllSubterms();
        for(int i=0; i< kids.length;i++) {
            hc *= kids[i].hashCode();
        }
        return (int)(hc >> 10);
	}

}
