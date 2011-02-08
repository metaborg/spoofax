package org.strategoxt.imp.runtime.stratego.adapter;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermPrinter;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 * @author Karl Trygve Kalleberg <karltk add strategoxt.org>
 */
public class WrappedAstNodeList extends WrappedAstNode implements IStrategoList {
	
	private final WrappedAstNodeFactory factory;
	
	private final int offset;
	
	private IStrategoTerm head;
	
	private IStrategoList tail;

	/**
	 * Creates a new WrappedAstNodeList with lazily initialized subterms.
	 */
	protected WrappedAstNodeList(WrappedAstNodeFactory factory, ISimpleTerm node, int offset) {
		super(node);
		this.factory = factory;
		this.offset = offset;
	}

	/**
	 * Creates a new WrappedAstNodeList with the given head and tail.
	 */
	protected WrappedAstNodeList(ISimpleTerm node, int offset, IStrategoTerm head, IStrategoList tail, IStrategoList annos) {
		super(node, annos);
		this.factory = null;
		this.offset = offset;
		this.head = head;
		this.tail = tail;
		assert getSubtermCount() == node.getSubtermCount() - offset;
	}

	public final IStrategoTerm head() {
		if (head == null) {
			ArrayList children = getNode().getChildren();
			head = ((ISimpleTerm) children.get(offset));
		}
		return head;
	}
	
	public IStrategoList tail() {
		if (tail != null) return tail;
		if (head != null && getSubtermCount() == 0) throw new NoSuchElementException();
		tail = factory.wrapList(getNode(), offset + 1);
		return tail;
	}
	
	protected int getOffset() {
		return offset;
	}

	@Override
	protected boolean doSlowMatch(IStrategoTerm second, int commonStorageType) {
        if(second.getTermType() != IStrategoTerm.LIST)
            return false;
        
        final IStrategoList snd = (IStrategoList) second;
        if (getSubtermCount() != snd.getSubtermCount())
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
	
	@Override
	public IStrategoTerm[] getAllSubterms() {
		if (tail == null) {
			// Get children from origin node
			ArrayList children = getNode().getChildren();
			IStrategoTerm[] results = new IStrategoTerm[children.size() - offset];
			for (int i = 0; i < results.length; i++) {
				results[i] = ((ISimpleTerm) children.get(i + offset));
			}
			return results;
		} else {
			// Get children from head and tail
	        int size = size();
	        IStrategoTerm[] clone = new IStrategoTerm[size];
	        IStrategoList list = this;
	        for (int i = 0; i < size; i++) {
	            clone[i] = list.head();
	            list = list.tail();
	        }
	        return clone;
		}
	}
	
	@Override
	public IStrategoTerm getSubterm(int index) {
		return ((ISimpleTerm) getNode().getAllChildren().get(index + offset));
	}
	
	@Override
	public int getSubtermCount() {
		return getNode().getSubtermCount() - offset;
	}

	public final IStrategoTerm get(int index) {
		return getSubterm(index);
	}

	public final boolean isEmpty() {
		return head == null && getSubtermCount() == 0;
	}

	public final int size() {
		return getSubtermCount();
	}

	public int getTermType() {
		return LIST;
	}

	public void prettyPrint(ITermPrinter pp) {
		if (head != null || getSubtermCount() > 0) {
			pp.println("[");
			pp.indent(2);
			head().prettyPrint(pp);
            for (IStrategoList cur = tail(); !cur.isEmpty(); cur = cur.tail()) {
                pp.print(",");
                pp.nextIndentOff();
                cur.head().prettyPrint(pp);
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
		final int prime = 71;
		int result = 1;
		IStrategoTerm head = this.head; 
		if (head == null) {
			ArrayList children = getNode().getChildren();
			int size = children.size() - offset;
			if (size == 0) return prime * prime * result;

			head = ((ISimpleTerm) children.get(offset));
			result = prime * result + head.hashCode();
		} else {
			result = prime * result + head.hashCode();
		}
		IStrategoList tail = this.tail;
		result = prime * result + (tail == null ? tail() : tail).hashCode();
		return result;
	}

	@Deprecated
	public IStrategoList prepend(IStrategoTerm prefix) {
		throw new UnsupportedOperationException();
	}

}
