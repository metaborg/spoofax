package org.strategoxt.imp.runtime.stratego;

import java.util.ArrayList;

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
 * Extracts all comment lines directly after the current node on the same line
 * @author Maartje de Jonge
 */
public class OriginRightLineCommentPrimitive extends AbstractPrimitive {
	
	private static final String NAME = "SSL_EXT_origin_right_comment";
	

	public OriginRightLineCommentPrimitive() {
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
		IToken start = node.getNode().getRightIToken(); 
		IPrsStream tokenStream=start.getIPrsStream();
		ArrayList<IToken> comments=new ArrayList<IToken>();
		int tokenIndex=start.getTokenIndex()+1;
		boolean hasComment=false;
		while(tokenIndex<tokenStream.getSize() && tokenStream.getLine(tokenIndex)==start.getEndLine()){
			IToken tok=tokenStream.getTokenAt(tokenIndex);			
			if(TokenKind.valueOf(tok.getKind())==TokenKind.TK_LAYOUT){
				comments.add(tok);
				if(!SGLRToken.isWhiteSpace(tok)){
					hasComment=true;
				}
			}			
			tokenIndex++;
		}
		if(hasComment){
			String commentText="";
			for (IToken comm_txt : comments) {
				commentText+=tokenStream.getTokenText(comm_txt.getTokenIndex());
			}
			return env.getFactory().makeTuple(
					env.getFactory().makeInt(comments.get(0).getStartOffset()),
					env.getFactory().makeInt(comments.get(comments.size()-1).getEndOffset()),
					env.getFactory().makeString(commentText.replaceAll("\\s+$", ""))
			);
			//return env.getFactory().makeString(commentText.replaceAll("\\s+$", ""));
		}
		return null;
	}

}
