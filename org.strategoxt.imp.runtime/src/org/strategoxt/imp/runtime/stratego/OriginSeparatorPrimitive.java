package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getLeftToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getRightToken;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokenizer;
import org.strategoxt.imp.runtime.parser.ast.SubListAstNode;

/**
 * @author Maartje de Jonge
 */
public class OriginSeparatorPrimitive extends AbstractOriginPrimitive {

	public OriginSeparatorPrimitive() {
		super("SSL_EXT_origin_separator");
	}

	/**
	 * Returns the separator and its location 
	 * after a list element or sublist (before in case of last element(s)).
	 * Returns null if the separator can not be found.
	 */
	@Override
	protected IStrategoTerm call(IContext env, IStrategoTerm origin) {
		ISimpleTerm originNode=origin.getNode();
		SubListAstNode sublist;
		IStrategoTerm left;
		IStrategoTerm right;
		if(originNode instanceof SubListAstNode){
			sublist = (SubListAstNode) originNode;							
		}
		else{
			ISimpleTerm parent = origin.getNode().getParent();
			if(!(isTermList(parent)))
				return null;
			sublist = SubListAstNode.createSublist((ListAstNode) parent, originNode, originNode, true);
		}
		ListAstNode list = sublist.getCompleteList();
		int lastIndexList = list.getSubtermCount()-1;
		if(sublist.getIndexEnd() < lastIndexList){
			left = sublist.getLastChild();
			right = list.getSubterm(sublist.getIndexEnd()+1);
		}
		else if(sublist.getIndexStart() > 0){
			right = sublist.getFirstChild();
			left = list.getSubterm(sublist.getIndexStart()-1);				
		}
		else
			return null; //complete list has no separator
		
		ITokenizer tokens=getRightToken(left).getTokenizer();
		int startTokenSearch = getRightToken(left).getIndex()+1;
		int endTokenSearch = getLeftToken(right).getIndex()-1;
		int startSeparation=-1;
		int endSeparation=-1;
		int loopIndex=startTokenSearch;
		while (loopIndex <= endTokenSearch) {
			IToken tok = tokens.getTokenAt(loopIndex);
			if(!DocumentStructure.isLayoutToken(tok)){
				if(startSeparation==-1)
					startSeparation=tok.getStartOffset();
				endSeparation=tok.getEndOffset()+1;
			}
			loopIndex++;
		}
		ILexStream lex = getLeftToken(originNode).getInput();
		if(startSeparation!=-1){
			ITermFactory factory = env.getFactory();
			return factory.makeTuple(
					factory.makeInt(startSeparation),
					factory.makeInt(endSeparation),
					factory.makeString(lex.toString(startSeparation, endSeparation-1))
			);
		}
		return null;
	}
}
