package org.strategoxt.imp.runtime.stratego;

import lpg.runtime.ILexStream;
import lpg.runtime.IPrsStream;
import lpg.runtime.IToken;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.imp.runtime.parser.ast.AstNode;
import org.strategoxt.imp.runtime.parser.ast.AstNodeFactory;
import org.strategoxt.imp.runtime.parser.ast.ListAstNode;
import org.strategoxt.imp.runtime.parser.ast.SubListAstNode;
import org.strategoxt.imp.runtime.stratego.adapter.IStrategoAstNode;
import org.strategoxt.imp.runtime.stratego.adapter.IWrappedAstNode;

/**
 * @author Maartje de Jonge
 */
public class OriginSeparatorWithLayoutPrimitive extends AbstractOriginPrimitive {

	public OriginSeparatorWithLayoutPrimitive() {
		super("SSL_EXT_origin_separator_with_lo");
	}

	/**
	 * Returns the separator and its location plus layout 
	 * after a list element or sublist (before in case of last element(s)).
	 * Returns null if the separator can not be found.
	 */
	@Override
	protected IStrategoTerm call(IContext env, IWrappedAstNode node) {
		IStrategoAstNode originNode=node.getNode();
		SubListAstNode sublist;
		AstNode left;
		AstNode right;
		if(originNode instanceof SubListAstNode){
			sublist = (SubListAstNode) originNode;							
		}
		else{
			IStrategoAstNode parent = node.getNode().getParent();
			if(!(parent instanceof ListAstNode))
				return null;
			sublist = (SubListAstNode) new AstNodeFactory().createSublist((ListAstNode) parent, originNode, originNode, true);
		}
		ListAstNode list = sublist.getCompleteList();
		int lastIndexList = list.getChildren().size()-1;
		boolean atEnd=false;
		if(sublist.getIndexEnd() < lastIndexList){
			left = sublist.getLastChild();
			right = list.getChildren().get(sublist.getIndexEnd()+1);
		}
		else if(sublist.getIndexStart() > 0){
			right = sublist.getFirstChild();
			left = list.getChildren().get(sublist.getIndexStart()-1);
			atEnd=true;
		}
		else
			return null; //complete list has no separator
		
		IPrsStream tokens=left.getRightIToken().getIPrsStream();
		int startTokenSearch = left.getRightIToken().getTokenIndex()+1;
		int endTokenSearch = right.getLeftIToken().getTokenIndex()-1;
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
		ILexStream lex = originNode.getLeftIToken().getILexStream();
		int endOfSep=endSeparation;
		int startOfSep=startSeparation;
		if(startSeparation!=-1){
			if(sublist.getIndexStart() > 0){
				String potentialLayout = lex.toString(left.getRightIToken().getEndOffset()+1, startOfSep-1);
				int newlineIndex=potentialLayout.lastIndexOf('\n');
				if(newlineIndex>=0){
					startOfSep=left.getRightIToken().getEndOffset()+1+newlineIndex+1;
				}
			}
			if(!atEnd){
				String potentialLayout_r = lex.toString(endOfSep, right.getLeftIToken().getStartOffset()-1);
				int newlineIndex_r=potentialLayout_r.lastIndexOf('\n');
				if(newlineIndex_r>=0){
					endOfSep=endOfSep+1+newlineIndex_r;
				}
			}		
			ITermFactory factory = env.getFactory();
			return factory.makeTuple(
					factory.makeInt(startOfSep),
					factory.makeInt(endOfSep),
					factory.makeString(lex.toString(startOfSep, endOfSep-1))
			);
		}
		return null;
	}
}
