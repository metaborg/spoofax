package org.strategoxt.imp.runtime.parser.ast;

import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getLeftToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getRightToken;
import static org.spoofax.terms.attachments.ParentAttachment.getParent;

import java.util.ArrayList;

import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;

/**
 * An artificial partial list AST node.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class SubListAstNode extends ListAstNode implements IStrategoList {

	private final ListAstNode completeList;

	private int indexStart;
	
	public static IStrategoTerm createSublist(ListAstNode list, ISimpleTerm startChild, ISimpleTerm endChild, boolean cloneFirst) {
		ArrayList<IStrategoTerm> children = new ArrayList<IStrategoTerm>();
		int startOffset = getChildIndex(list, startChild);
		int endOffset = getChildIndex(list, endChild);
		
		for (int i = startOffset; i <= endOffset; i++) {
			IStrategoTerm child = list.getSubterm(i);
			assert getParent(child) != null;
			children.add(child);
		}
		
		IStrategoTerm result = new SubListAstNode(list, list.getElementSort(),
				getLeftToken(startChild), getRightToken(endChild), children, startOffset);
		if (cloneFirst) result = result.cloneIgnoreTokens();
		list.overrideReferences(getLeftToken(list), getRightToken(list), children, result);
		setParent(result, list);
		return result;
	}

	private static int getChildIndex(ListAstNode list, ISimpleTerm child) {
		for (int i = 0; i<list.getSubtermCount(); i++){
			if (child==list.getSubterm(i))
			    return i;
		}
		return -1;
	}

	private SubListAstNode(ListAstNode completeList, String elementSort, IToken leftToken,
			IToken rightToken, ArrayList<IStrategoTerm> children, int indexStart) {
		super(elementSort, leftToken, rightToken, children);
		this.completeList = completeList;
		this.indexStart = indexStart;
	}

	public ListAstNode getCompleteList() {
		return completeList;
	}

	public int getIndexStart() {
		return indexStart;
	}

	public int getIndexEnd() {
		return indexStart + this.getSubtermCount()-1;
	}
}
