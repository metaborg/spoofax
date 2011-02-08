package org.strategoxt.imp.runtime.stratego.adapter;

import static org.spoofax.interpreter.terms.IStrategoTerm.APPL;
import static org.spoofax.interpreter.terms.IStrategoTerm.INT;
import static org.spoofax.interpreter.terms.IStrategoTerm.LIST;
import static org.spoofax.interpreter.terms.IStrategoTerm.MUTABLE;
import static org.spoofax.interpreter.terms.IStrategoTerm.STRING;
import static org.spoofax.interpreter.terms.IStrategoTerm.TUPLE;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.TermFactory;

/**
 * A factory creating ATerms from AST nodes.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 * @author Karl Trygve Kalleberg <karltk add strategoxt.org>
 */
public class WrappedAstNodeFactory extends TermFactory implements ITermFactory {
	
	public IStrategoTerm wrap(IStrategoTerm node) {
		IStrategoTerm result;
		switch (node.getTermType()) {
			case INT:
				result = new WrappedAstNodeInt(node);
				break;
			case STRING:
				result = new WrappedAstNodeString(node);
				break;
			case LIST:
				result = new WrappedAstNodeList(this, node, 0);
				break;
			case APPL:
				String constructor = node.getConstructor();
				result = constructor == null || constructor.length() == 0
					? new WrappedAstNodeTuple(node)
					: new WrappedAstNodeAppl(this, node);
				break;
			case TUPLE:
				result = new WrappedAstNodeTuple(node);
				break;
			default:
				throw new IllegalStateException("Could not convert node of type " + node.getClass().getName() + " to a term");
		}
		IStrategoList annos = node.getAnnotations();
		if (annos != null)
			result = annotateTerm(result, annos);
		return result;
	}
	
	protected IStrategoList wrapList(IStrategoTerm node, int offset) {
		return new WrappedAstNodeList(this, node, offset);
	}
	
	// PARSING
	
	// ANNOTATIONS
	
	@Override
	public IStrategoTerm annotateTerm(IStrategoTerm term, IStrategoList annotations) {
		if (term instanceof WrappedAstNode) {
			return ((WrappedAstNode) term).getAnnotatedWith(annotations);
		} else {
			return super.annotateTerm(term, annotations);
		}
	}
	
	// ORIGIN TRACKING
	
	@Override
	public IStrategoAppl replaceAppl(IStrategoConstructor constructor, IStrategoTerm[] kids,
			IStrategoAppl oldTerm) {
		
		// TODO: Optimize - create a WrappedAstNodeAppl with initialized kids field instead
		//       (but ensure it maintains hte WrappedAstNodeLink.ensureLinks behavior)
		IStrategoList annos = oldTerm.getAnnotations();
		IStrategoAppl result = makeAppl(constructor, ensureLinks(kids, oldTerm), annos);
		
		return (IStrategoAppl) ensureLink(result, oldTerm);
	}
	
	@Override
	public IStrategoTuple replaceTuple(IStrategoTerm[] kids, IStrategoTuple old) {
		// TODO: Optimize - create a WrappedAstNodeTuple with initialized kids field instead
		//       (but ensure it maintains hte WrappedAstNodeLink.ensureLinks behavior)
		IStrategoList annos = old.getAnnotations();
		return (IStrategoTuple) ensureLink(makeTuple(ensureLinks(kids, old), annos), old);
	}
	
	@Override
	public IStrategoList replaceList(IStrategoTerm[] terms, IStrategoList old) {
		// TODO: Optimize - create a WrappedAstNodeList with initialized kids field instead
		//       (but ensure it maintains hte WrappedAstNodeLink.ensureLinks behavior)
		// return (IStrategoList) ensureLink(makeList(ensureLinks(kids, old)), old);
		assert terms.length == old.getSubtermCount();
		return replaceList(terms, 0, old, old.getAnnotations());
	}

	/**
	 * @param origin
	 *            The origin term. For lists, this must be the exact
	 *            corresponding term with the same offset. Calling
	 *            {@link IStrategoTerm#getTerm()} won't suffice.
	 */
	public IStrategoTerm makeLink(IStrategoTerm term, IWrappedAstNode origin) {
		if (term.getTermType() == LIST && term.getSubtermCount() == origin.getSubtermCount()
				&& origin.getTermType() == LIST) {
			return makeListLink((IStrategoList) term, (IStrategoList) origin);
		} else {
			return new WrappedAstNodeLink(this, term, origin);
		}
	}
	
	/**
	 * Replaces all subterms in a list,
	 * maintaining only the outer annotations.
	 */
	private IStrategoList replaceList(IStrategoTerm[] terms, int index, IStrategoList old, IStrategoList annos) {
		if (index == terms.length) {
			assert old.isEmpty();
			 // we don't bother linking empty lists
			return annos == null
				? EMPTY_LIST
				: (IStrategoList) annotateTerm(EMPTY_LIST, annos); 
		} else {
			IStrategoTerm head = ensureLink(terms[index], old.head());
			IStrategoList tail = replaceList(terms, index + 1, old.tail(), null);
			if (old instanceof WrappedAstNodeList) {
				WrappedAstNodeList oldList = (WrappedAstNodeList) old;
				return new WrappedAstNodeList(oldList.getNode(), oldList.getOffset(), head, tail, annos);
			} else {
				// UNDONE: assert !(old instanceof IWrappedAstNode);
				return makeListCons(head, tail, annos);
			}
		}
	}
	
	/**
	 * Adds origin tracking information to all subterms of a list.
	 * May add origin tracking information to list Cons nodes.
	 */
	private IStrategoList makeListLink(IStrategoList terms, IStrategoList old) {
		if (terms instanceof IWrappedAstNode) {
			assert terms.getStorageType() != MUTABLE; // children will be wrapped as well
			return terms;
		} else if (terms.isEmpty()) {
			assert old.isEmpty();
			// We don't bother linking empty lists
			return terms;
		} else {
			IStrategoTerm head = terms.head();
			IStrategoList tail = terms.tail();
			IStrategoTerm newHead = ensureLink(head, old.head());
			IStrategoList newTail = makeListLink(tail, old.tail());
			
			/* UNDONE: Origin tracking for Cons nodes
			           (relatively expensive, and who cares about them?)
			if (old instanceof WrappedAstNodeList) {
				WrappedAstNodeList oldList = (WrappedAstNodeList) old;
				return new WrappedAstNodeList(oldList.getNode(), oldList.getOffset(), head, tail, terms.getAnnotations());
			}
			*/
			if (head == newHead && tail == newTail) return terms;
			return makeListCons(newHead, newTail, terms.getAnnotations());
		}
	}
	
	protected IStrategoTerm[] ensureLinks(IStrategoTerm[] kids, IStrategoTerm oldTerm) {
		assert oldTerm.getTermType() != LIST; // has an expensive getAllSubterms()
		IStrategoTerm[] oldKids = oldTerm.getAllSubterms();
		if (oldKids == kids) return kids; // no changes; happens with interpreter's all
		for (int i = 0; i < kids.length; i++) {
			kids[i] = ensureLink(kids[i], oldTerm.getSubterm(i));
		}
		return kids;
		/* Before opimization (avoid array copy and exit if kids == oldTerm.getAllSubterms())
		IStrategoTerm[] linkedKids = new IStrategoTerm[kids.length];
		
		for (int i = 0; i < kids.length; i++) {
			linkedKids[i] = ensureLink(kids[i], oldTerm.getSubterm(i));
		}
		return linkedKids;
		*/
	}
	
	protected IStrategoTerm ensureLink(IStrategoTerm term, IStrategoTerm oldTerm) {
		if (term instanceof IWrappedAstNode) {
			assert term.getStorageType() != MUTABLE; // children will be wrapped as well
			return term;
		} else if (oldTerm instanceof IWrappedAstNode) {
			return makeLink(term, (IWrappedAstNode) oldTerm);
		} else {
			// TODO: Add a link to the parent node for children that do not have tracking info?
			//       probably not a good idea
			return term;
		}
	}
}
