package org.strategoxt.imp.runtime.stratego;

import java.util.ArrayList;

import lpg.runtime.ILexStream;
import lpg.runtime.IPrsStream;
import lpg.runtime.IToken;

import org.strategoxt.imp.runtime.parser.tokens.SGLRToken;
import org.strategoxt.imp.runtime.parser.tokens.TokenKind;
import org.strategoxt.imp.runtime.stratego.adapter.IStrategoAstNode;

public class TextPositions {
	
	public static int getStartPosNode(IStrategoAstNode node){
		return node.getLeftIToken().getStartOffset();//inclusive start
	}
	
	public static int getStartPosNodeWithLayout(IStrategoAstNode node){
		return 0;
	}
	
	public static int getStartPosCommentBefore(IStrategoAstNode node){
		IToken start = node.getLeftIToken(); 
		IPrsStream tokenStream=start.getIPrsStream();
		IToken prevNonLayout = getPrevAstToken(start);
		int tokenIndex=start.getTokenIndex()-1;
		IToken commentTok=null;
		int lineIndex=start.getLine()-1;
		while(tokenIndex>=0){
			IToken tok=tokenStream.getTokenAt(tokenIndex);
			if((prevNonLayout!=null && tok.getLine()<=prevNonLayout.getLine()) || tok.getEndLine() < lineIndex || TokenKind.valueOf(tok.getKind())!=TokenKind.TK_LAYOUT)
				break;			
			if(!SGLRToken.isWhiteSpace(tok)){
				commentTok=tok;
				lineIndex=tok.getLine()-1;
			}
			tokenIndex--;
		}
		if(commentTok==null)
			return -1;
		return startPositionAfterTrim(commentTok);
	}

	private static IToken getPrevAstToken(IToken start) {
		IToken prevNonLayout=null;
		IPrsStream tokenStream=start.getIPrsStream();
		for (int i = start.getTokenIndex()-1; i >= 0; i--) {
			IToken preceedingTok=tokenStream.getTokenAt(i);
			if(!SGLRToken.isWhiteSpace(preceedingTok)){
				prevNonLayout=preceedingTok;
			}
		}
		return prevNonLayout;
	}

	
	public static int getStartPosCommentAfter(IStrategoAstNode node){
		IToken start = node.getRightIToken(); 
		IPrsStream tokenStream=start.getIPrsStream();
		int tokenIndex=start.getTokenIndex()+1;
		while(tokenIndex<tokenStream.getTokens().size()){
			IToken tok=tokenStream.getTokenAt(tokenIndex);
			if(tok.getLine() > start.getEndLine() || TokenKind.valueOf(tok.getKind())!=TokenKind.TK_LAYOUT)
				return -1;
			if(!SGLRToken.isWhiteSpace(tok)){
				return startPositionAfterTrim(tok);
			}	
			tokenIndex++;
		}
		return -1;
	}
	
	public static int getEndPosNode(IStrategoAstNode node){
		return node.getRightIToken().getEndOffset()+1; //exclusive end
	}
	
	public static int getEndPosNodeWithLayout(IStrategoAstNode node){
		return 0;
	}
	
	public static int getEndPosCommentBefore(IStrategoAstNode node){
		IToken start = node.getLeftIToken(); 
		IPrsStream tokenStream=start.getIPrsStream();
		IToken prevNonLayout = getPrevAstToken(start);
		int tokenIndex=start.getTokenIndex()-1;
		while(tokenIndex>=0){
			IToken tok=tokenStream.getTokenAt(tokenIndex);
			if((prevNonLayout!=null && tok.getLine()<=prevNonLayout.getLine()) || tok.getEndLine() < start.getLine()-1 || TokenKind.valueOf(tok.getKind())!=TokenKind.TK_LAYOUT)
				return -1;			
			if(!SGLRToken.isWhiteSpace(tok))
				return endPositionAfterTrim(tok);
			tokenIndex--;
		}
		return -1;
	}
		
	private static int endPositionAfterTrim(IToken tok){
		IPrsStream tokStream=tok.getIPrsStream();
		String tokText=tokStream.getTokenText(tok.getTokenIndex());
		String lo_correctedText=tokText.replaceAll("\\s+$", "");
		int endPosCorrection=tokText.length()-lo_correctedText.length();
		return tok.getEndOffset()+1-endPosCorrection;
	}
	
	private static int startPositionAfterTrim(IToken tok){
		IPrsStream tokStream=tok.getIPrsStream();
		String tokText=tokStream.getTokenText(tok.getTokenIndex());
		String lo_correctedText=tokText.replaceAll("^+\\s", "");
		int correction=tokText.length()-lo_correctedText.length();
		return tok.getStartOffset()+correction;
	}
	
	public static int getEndPosCommentAfter(IStrategoAstNode node){
		if(getStartPosCommentAfter(node)==-1)
			return -1;
		IToken start = node.getRightIToken(); 
		IPrsStream tokenStream=start.getIPrsStream();
		int tokenIndex=start.getTokenIndex()+1;
		IToken commentTok=null;
		while(tokenIndex<tokenStream.getTokens().size()){
			IToken tok=tokenStream.getTokenAt(tokenIndex);
			if(tok.getLine() > start.getEndLine() || TokenKind.valueOf(tok.getKind())!=TokenKind.TK_LAYOUT)
				break;
			if(!SGLRToken.isWhiteSpace(tok)){
				commentTok=tok;
			}	
			tokenIndex++;
		}
		return endPositionAfterTrim(commentTok);
	}
	
	public static boolean isNotInTextRange(int pos, ILexStream lexStream) {
		return pos < 0 || pos >= lexStream.getStreamLength();
	}
	
	public static boolean isUnvalidInterval(int pos_start, int pos_end, ILexStream lexStream) {
		return pos_start < 0 || pos_start > pos_end || pos_end >= lexStream.getStreamLength();
	}
}
