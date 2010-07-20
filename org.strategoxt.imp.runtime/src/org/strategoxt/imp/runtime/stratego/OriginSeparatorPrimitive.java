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
		if(sublist.getIndexEnd() < lastIndexList){
			left = sublist.getLastChild();
			right = list.getChildren().get(sublist.getIndexEnd()+1);
		}
		else if(sublist.getIndexStart() > 0){
			right = sublist.getFirstChild();
			left = list.getChildren().get(sublist.getIndexStart()-1);				
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
