package org.strategoxt.imp.runtime.stratego;

import lpg.runtime.IPrsStream;
import lpg.runtime.IToken;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.imp.runtime.parser.tokens.SGLRToken;
import org.strategoxt.imp.runtime.parser.tokens.TokenKind;
import org.strategoxt.imp.runtime.stratego.adapter.IWrappedAstNode;

/**
 * Extracts all comment lines directly in front of the current node
 * @author Maartje de Jonge
 */
public class OriginLeftCommentLinesPrimitive extends AbstractPrimitive {
	
	private static final String NAME = "SSL_EXT_origin_left_comment";
	

	public OriginLeftCommentLinesPrimitive() {
		super(NAME, 0, 1);
	}
	
	@Override
	public final boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
		
		if (tvars[0] instanceof IWrappedAstNode) {
			IStrategoTerm result = call(env, (IWrappedAstNode) tvars[0]);
			if (result != null) {
				env.setCurrent(result);
				return true;
			}
		}
		return false;
	}
	
	private IStrategoTerm call(IContext env, IWrappedAstNode node) {		
		IToken start = node.getNode().getLeftIToken(); 
		IPrsStream tokenStream=start.getIPrsStream();
		IToken comment=null;
		IToken previousNodeToken=null;
		int tokenIndex=start.getTokenIndex()-1;
		while(tokenIndex>=0 && previousNodeToken==null){
			IToken tok=tokenStream.getTokenAt(tokenIndex);
			if(!SGLRToken.isWhiteSpace(tok)){
				if(comment==null && TokenKind.valueOf(tok.getKind())==TokenKind.TK_LAYOUT){
					comment=tok;
				}
				if(TokenKind.valueOf(tok.getKind())!=TokenKind.TK_LAYOUT){
					previousNodeToken=tok;
				}
			}
			tokenIndex--;
		}
		if(comment!=null && (previousNodeToken==null || previousNodeToken.getEndLine() < comment.getLine())){
			String commentText=tokenStream.getTokenText(comment.getTokenIndex());
			return env.getFactory().makeTuple(
					env.getFactory().makeInt(comment.getStartOffset()),
					env.getFactory().makeInt(comment.getEndOffset()+1),
					env.getFactory().makeString(commentText)
			);
			//return env.getFactory().makeString(commentText.trim());
		}
		return null;
	}

}
