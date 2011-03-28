package org.strategoxt.imp.runtime.parser.ast;

import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getElementSort;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getLeftToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getRightToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.putImploderAttachment;
import static org.spoofax.terms.attachments.ParentAttachment.putParent;

import java.util.ArrayList;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.StrategoListIterator;
import org.spoofax.terms.StrategoWrapped;
import org.spoofax.terms.attachments.ParentAttachment;
import org.strategoxt.imp.runtime.Environment;

/**
 * An artificial partial list AST node.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class StrategoSubList extends StrategoWrapped implements IStrategoList {

	private final IStrategoList completeList;

	private int indexStart;

	private int indexEnd;
	
	public static StrategoSubList createSublist(IStrategoList list, IStrategoTerm firstChild, IStrategoTerm lastChild, boolean updateParents) {
		ArrayList<IStrategoTerm> children = new ArrayList<IStrategoTerm>();
		boolean isStartChildFound = false;
		int indexStart = 0;
		int indexEnd = 0;

		int i = 0;
		for (IStrategoTerm child : StrategoListIterator.iterable(list)) {
			i++;
			if (child == firstChild) {
				indexStart = i;
				isStartChildFound = true;
			}
			if (isStartChildFound) {
				children.add(child);
				if (child == lastChild) {
					indexEnd = i;
					break;
				}
			}
		}
		
		IStrategoList wrapped = Environment.getTermFactory().makeList(children);
		StrategoSubList result = new StrategoSubList(list, wrapped, indexStart, indexEnd);
		
		/* XXX: support updateParents again??
		if (cloneFirst) result = result.cloneIgnoreTokens();
		list.overrideReferences(getLeftToken(list), getRightToken(list), children, result);
		setParent(result, list);
		*/
		
		putParent(result, ParentAttachment.get(list));
		putImploderAttachment(result, true, getElementSort(list), getLeftToken(firstChild), getRightToken(lastChild));
		return result;
	}

	private StrategoSubList(IStrategoList completeList, IStrategoList wrapped, int indexStart, int indexEnd) {
		super(wrapped);
		this.completeList = completeList;
		this.indexStart = indexStart;
		this.indexEnd = indexEnd;
	}

	public IStrategoList getCompleteList() {
		return completeList;
	}

	public int getIndexStart() {
		return indexStart;
	}

	public int getIndexEnd() {
		return indexEnd;
	}

	public IStrategoTerm getFirstChild() {
		return getSubterm(indexStart);
	}

	public IStrategoTerm getLastChild() {
		return getSubterm(indexEnd);
	}
}
