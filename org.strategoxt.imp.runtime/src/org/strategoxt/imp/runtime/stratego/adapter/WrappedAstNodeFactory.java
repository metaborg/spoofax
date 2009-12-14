package org.strategoxt.imp.runtime.stratego.adapter;

import static org.spoofax.interpreter.terms.IStrategoTerm.*;

import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.imp.runtime.parser.ast.IntAstNode;
import org.strategoxt.imp.runtime.parser.ast.ListAstNode;
import org.strategoxt.imp.runtime.parser.ast.StringAstNode;
import org.strategoxt.lang.terms.TermFactory;

/**
 * A factory creating ATerms from AST nodes.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 * @author Karl Trygve Kalleberg <karltk add strategoxt.org>
 */
public class WrappedAstNodeFactory extends TermFactory implements ITermFactory {
	
	public IStrategoTerm wrap(IStrategoAstNode node) {
		IStrategoTerm result;
		if (node instanceof IntAstNode) {
			result = new WrappedAstNodeInt(node);
		} else if (node instanceof StringAstNode) {
			result = new WrappedAstNodeString(node);
		} else if (node instanceof ListAstNode) {
			result = new WrappedAstNodeList(this, node, 0);
		} else {
			// TODO: ensure maximal sharing of node constructors
			//       (term constructors are also maximally shared!)
			result = "()".equals(node.getConstructor())
				? new WrappedAstNodeTuple(node)
				: new WrappedAstNodeAppl(this, node);
		}
		IStrategoList annos = node.getAnnotations();
		if (annos != null)
			result = annotateTerm(result, annos);
		return result;
	}
	
	protected IStrategoList wrapList(IStrategoAstNode node, int offset) {
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
		IStrategoAppl result = makeAppl(constructor, ensureLinks(kids, oldTerm));
		
		return (IStrategoAppl) ensureLink(result, oldTerm);
	}
	
	@Override
	public IStrategoTuple replaceTuple(IStrategoTerm[] kids, IStrategoTuple old) {
		// TODO: Optimize - create a WrappedAstNodeTuple with initialized kids field instead
		//       (but ensure it maintains hte WrappedAstNodeLink.ensureLinks behavior)
		return (IStrategoTuple) ensureLink(makeTuple(ensureLinks(kids, old)), old);
	}
	
	@Override
	public IStrategoList replaceList(IStrategoTerm[] terms, IStrategoList old) {
		// TODO: Optimize - create a WrappedAstNodeList with initialized kids field instead
		//       (but ensure it maintains hte WrappedAstNodeLink.ensureLinks behavior)
		// return (IStrategoList) ensureLink(makeList(ensureLinks(kids, old)), old);
		assert terms.length == old.getSubtermCount();
		return replaceList(terms, 0, old);
	}

	/**
	 * @param origin
	 *            The origin term. For lists, this must be the exact
	 *            corresponding term with the same offset. Calling
	 *            {@link IStrategoAstNode#getTerm()} won't suffice.
	 */
	public IStrategoTerm makeLink(IStrategoTerm term, IWrappedAstNode origin) {
		if (term.getTermType() == LIST && term.getSubtermCount() == origin.getSubtermCount()
				&& origin.getTermType() == LIST) {
			return replaceList((IStrategoList) term, (IStrategoList) origin);
		} else {
			return new WrappedAstNodeLink(this, term, origin);
		}
	}
	
	private IStrategoList replaceList(IStrategoTerm[] terms, int index, IStrategoList old) {
		if (index == terms.length) {
			assert old.isEmpty();
			return EMPTY_LIST; // we don't bother linking empty lists	 
		} else {
			IStrategoTerm head = ensureLink(terms[index], old.head());
			IStrategoList tail = replaceList(terms, index + 1, old.tail());
			if (old instanceof WrappedAstNodeList) {
				WrappedAstNodeList oldList = (WrappedAstNodeList) old;
				return new WrappedAstNodeList(oldList.getNode(), oldList.getOffset(), head, tail);
			} else {
				assert !(old instanceof IWrappedAstNode);
				return makeListCons(head, tail);
			}
		}
	}
	
	private IStrategoList replaceList(IStrategoList terms, IStrategoList old) {
		if (terms.isEmpty()) {
			assert old.isEmpty();
			return EMPTY_LIST; // we don't bother linking empty lists	 
		} else {
			IStrategoTerm head = ensureLink(terms.head(), old.head());
			IStrategoList tail = replaceList(terms.tail(), old.tail());
			if (old instanceof WrappedAstNodeList) {
				WrappedAstNodeList oldList = (WrappedAstNodeList) old;
				return new WrappedAstNodeList(oldList.getNode(), oldList.getOffset(), head, tail);
			} else {
				assert !(old instanceof IWrappedAstNode);
				return makeListCons(head, tail);
			}
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
