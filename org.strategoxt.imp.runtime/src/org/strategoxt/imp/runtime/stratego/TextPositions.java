package org.strategoxt.imp.runtime.stratego;

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
		IToken startTok = getStartTokenLeftComments(node);
		if(startTok==null){
			startTok=node.getLeftIToken();
		}
		IPrsStream tokStream=startTok.getIPrsStream();
		int lineNr=startTok.getLine();
		IToken precedingTok=tokStream.getTokenAt(startTok.getTokenIndex()-1);
		while(precedingTok.getTokenIndex()>0 ){
			if(precedingTok.getEndLine()<lineNr)
				return tokStream.getLineOffset(lineNr-1)+1;
			if(!isLayout(precedingTok))
				return startTok.getStartOffset();
			precedingTok=tokStream.getTokenAt(precedingTok.getTokenIndex()-1);
		}
		if(isLayout(precedingTok))
			return precedingTok.getStartOffset();
		return precedingTok.getEndOffset()+1;
	}
	
	public static int getStartPosCommentBefore(IStrategoAstNode node){
		IToken commentTok = getStartTokenLeftComments(node);
		if(commentTok==null)
			return -1;
		return startPositionAfterTrim(commentTok);
	}

	private static IToken getStartTokenLeftComments(IStrategoAstNode node) {
		IToken start = node.getLeftIToken(); 
		IPrsStream tokenStream=start.getIPrsStream();
		IToken prevNonLayout = getPrevAstToken(start);
		int tokenIndex=start.getTokenIndex()-1;
		IToken commentTok=null;
		int lineIndex=start.getLine()-1;
		while(tokenIndex>=0){
			IToken tok=tokenStream.getTokenAt(tokenIndex);
			if((prevNonLayout!=null && tok.getLine()<=prevNonLayout.getLine()) || tok.getEndLine() < lineIndex || !isLayout(tok))
				break;			
			if(!SGLRToken.isWhiteSpace(tok)){
				commentTok=tok;
				lineIndex=tok.getLine()-1;
			}
			tokenIndex--;
		}
		return commentTok;
	}

	private static boolean isLayout(IToken tok) {
		return TokenKind.valueOf(tok.getKind())==TokenKind.TK_LAYOUT;
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
		int startPos=-1;
		while(tokenIndex<tokenStream.getTokens().size()){
			IToken tok=tokenStream.getTokenAt(tokenIndex);
			if(tok.getLine() > start.getEndLine())
				return startPos;
			if(!isLayout(tok))
				return -1;
			if(!SGLRToken.isWhiteSpace(tok)){
				startPos=startPositionAfterTrim(tok);
			}	
			tokenIndex++;
		}
		return -1;
	}
	
	public static int getEndPosNode(IStrategoAstNode node){
		return node.getRightIToken().getEndOffset()+1; //exclusive end
	}
	
	public static int getEndPosNodeWithLayout(IStrategoAstNode node){
		IToken endTok = getEndTokenCommentAfter(node);
		if(endTok==null){
			endTok=node.getRightIToken();
		}
		IPrsStream tokStream=endTok.getIPrsStream();
		int lineNr=endTok.getEndLine();
		IToken nextTok=tokStream.getTokenAt(endTok.getTokenIndex()+1);
		while(nextTok.getTokenIndex()<tokStream.getTokens().size()-1){
			if(nextTok.getLine()>lineNr)
				return tokStream.getLineOffset(lineNr)+1;
			if(!SGLRToken.isWhiteSpace(nextTok))
				return nextTok.getStartOffset();
			nextTok=tokStream.getTokenAt(nextTok.getTokenIndex()+1);
		}
		if(isLayout(nextTok))
			return nextTok.getEndOffset();
		return nextTok.getStartOffset();
	}
	
	public static int getEndPosCommentBefore(IStrategoAstNode node){
		IToken start = node.getLeftIToken(); 
		IPrsStream tokenStream=start.getIPrsStream();
		IToken prevNonLayout = getPrevAstToken(start);
		int tokenIndex=start.getTokenIndex()-1;
		while(tokenIndex>=0){
			IToken tok=tokenStream.getTokenAt(tokenIndex);
			if((prevNonLayout!=null && tok.getLine()<=prevNonLayout.getLine()) || tok.getEndLine() < start.getLine()-1 || !isLayout(tok))
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
		IToken commentTok = getEndTokenCommentAfter(node);
		if(commentTok==null)
			return -1;
		return endPositionAfterTrim(commentTok);
	}

	private static IToken getEndTokenCommentAfter(IStrategoAstNode node) {
		if(getStartPosCommentAfter(node)==-1)
			return null;
		IToken start = node.getRightIToken(); 
		IPrsStream tokenStream=start.getIPrsStream();
		int tokenIndex=start.getTokenIndex()+1;
		IToken commentTok=null;
		while(tokenIndex<tokenStream.getTokens().size()){
			IToken tok=tokenStream.getTokenAt(tokenIndex);
			if(tok.getLine() > start.getEndLine() || !isLayout(tok))
				break;
			if(!SGLRToken.isWhiteSpace(tok)){
				commentTok=tok;
			}	
			tokenIndex++;
		}
		return commentTok;
	}
	
	public static boolean isNotInTextRange(int pos, ILexStream lexStream) {
		return pos < 0 || pos >= lexStream.getStreamLength();
	}
	
	public static boolean isUnvalidInterval(int pos_start, int pos_end, ILexStream lexStream) {
		return pos_start < 0 || pos_start > pos_end || pos_end >= lexStream.getStreamLength();
	}
}
