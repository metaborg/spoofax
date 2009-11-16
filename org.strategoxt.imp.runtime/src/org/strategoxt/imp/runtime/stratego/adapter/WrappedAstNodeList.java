package org.strategoxt.imp.runtime.stratego.adapter;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermPrinter;
import org.strategoxt.imp.runtime.Environment;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 * @author Karl Trygve Kalleberg <karltk add strategoxt.org>
 */
public class WrappedAstNodeList extends WrappedAstNode implements IStrategoList {
	
	private final int offset;
	
	private WrappedAstNodeList tail;
	
	protected WrappedAstNodeList(WrappedAstNodeFactory factory, IStrategoAstNode node) {
		this(factory, node, 0);
	}

	protected WrappedAstNodeList(WrappedAstNodeFactory factory, IStrategoAstNode node, int offset) {
		super(factory, node);
		this.offset = offset;
	}

	@Override
	protected boolean doSlowMatch(IStrategoTerm second, int commonStorageType) {
        if(second.getTermType() != IStrategoTerm.LIST)
            return false;
        
        final IStrategoList snd = (IStrategoList) second;
        if (size() != snd.size())
            return false;
        
        if (!isEmpty()) {
        	IStrategoTerm head = head();
        	IStrategoTerm head2 = snd.head();
        	if (head != head2 && !head.match(head2))
        		return false;
        	
        	IStrategoList tail = tail();
        	IStrategoList tail2 = snd.tail();
        
	        for (IStrategoList cons = tail, cons2 = tail2; !cons.isEmpty(); cons = cons.tail(), cons2 = cons2.tail()) {
	            IStrategoTerm consHead = cons.head();
				IStrategoTerm cons2Head = cons2.head();
				if (consHead != cons2Head && !consHead.match(cons2Head))
	                return false;
	        }
        }
        
        IStrategoList annotations = getAnnotations();
        IStrategoList secondAnnotations = second.getAnnotations();
        return annotations == secondAnnotations || annotations.match(secondAnnotations);
	}

	public final IStrategoTerm get(int index) {
		return getSubterm(index);
	}
	
	@Override
	public IStrategoTerm getSubterm(int index) {
		return super.getSubterm(index + offset);
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
	
	@Override
	public int getSubtermCount() {
		return super.getSubtermCount() - offset;
	}

	public IStrategoList tail() {
		if (tail != null) return tail;
		if (getSubtermCount() == 0) return null;
		tail = getFactory().wrapList(getNode(), offset + 1);
		return tail;
	}

	public int getTermType() {
		return LIST;
	}

	public void prettyPrint(ITermPrinter pp) {
		int sz = size();
		if (sz > 0) {
			pp.println("[");
			pp.indent(2);
			get(0).prettyPrint(pp);
			for (int i = 1; i < sz; i++) {
				pp.print(",");
				pp.nextIndentOff();
				get(i).prettyPrint(pp);
				pp.println("");
			}
			pp.println("");
			pp.print("]");
			pp.outdent(2);

		} else {
			pp.print("[]");
		}
		printAnnotations(pp);
	}

	@Override
	public int hashFunction() {
    	/* UNDONE: BasicStrategoTerm hash; should use cons/nil hash instead
        long hc = 4787;
        for (IStrategoList cur = this; !cur.isEmpty(); cur = cur.tail()) {
            hc *= cur.head().hashCode();
        }
        return (int)(hc >> 2);
        */
		final int prime = 71;
		int result = 1;
		IStrategoTerm head = head();
		result = prime * result + ((head == null) ? 0 : head.hashCode());
		IStrategoList tail = tail();
		result = prime * result + ((tail == null) ? 0 : tail.hashCode());
		return result;
	}

	@Deprecated
	public IStrategoList prepend(IStrategoTerm prefix) {
		return Environment.getTermFactory().makeList(prefix, this);
	}

}
