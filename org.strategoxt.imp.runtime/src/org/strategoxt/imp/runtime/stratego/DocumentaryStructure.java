package org.strategoxt.imp.runtime.stratego;

import java.util.ArrayList;

import lpg.runtime.ILexStream;
import lpg.runtime.IPrsStream;
import lpg.runtime.IToken;

import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.parser.tokens.SGLRToken;
import org.strategoxt.imp.runtime.parser.tokens.TokenKind;
import org.strategoxt.imp.runtime.stratego.adapter.IStrategoAstNode;
import org.strategoxt.imp.runtime.stratego.adapter.IWrappedAstNode;

public class DocumentaryStructure {
	
	private String seperatingWhitespace;
	private ArrayList<IToken> commentsBefore;
	private ArrayList<IToken> commentsAfter;
	private String indentNode;
	private int indentValueNode;
	private int startLayoutBefore;
	private IStrategoAstNode node;
	
	public TextFragment textWithLayout(){
		TextFragment originText=new TextFragment();
		originText.setStart(startLayoutBefore);
		TextFragment commentAfter = getCommentsAfter();
		if(commentAfter==null){
			originText.setEnd(node.getRightIToken().getEndOffset()+1);
		}
		else{
			originText.setEnd(commentAfter.getEnd());
			if(commentAfter.getText(getLexStream()).endsWith("\n") 
					&& originText.getText(getLexStream()).startsWith("\n")){
				originText.setStart(startLayoutBefore+1); //correction in case the line-comment ends with \n
			}
		}
		return originText;
	}

	private ILexStream getLexStream() {
		return node.getLeftIToken().getILexStream();
	}
	
	public TextFragment getCommentsBefore() {
		return createTextFragment(this.commentsBefore);
	}

	public TextFragment getCommentsAfter() {
		return createTextFragment(this.commentsAfter);
	}

	private TextFragment createTextFragment(ArrayList<IToken> tokenList) {
		if(tokenList.isEmpty())
			return null;
		TextFragment commentBlock=new TextFragment();
		commentBlock.setStart(tokenList.get(0).getStartOffset());
		commentBlock.setEnd(tokenList.get(tokenList.size()-1).getEndOffset());
		return commentBlock;
	}
	
	public String getIndentNode() {
		return indentNode;
	}
	
	public String getSeperatingWhitespace() {
		return seperatingWhitespace;
	}
	
	public DocumentaryStructure(IWrappedAstNode node) {
		initialize();
		this.node=node.getNode();
		analysize();
	}
	
	private void initialize() {
		commentsAfter=new ArrayList<IToken>();
		commentsBefore=new ArrayList<IToken>();
		indentNode="";
		indentValueNode=0;
		seperatingWhitespace=" ";
		startLayoutBefore=-1;
	}

	private IToken getPrecedingNonLOToken(IToken startToken){
		IPrsStream tokenStream=startToken.getIPrsStream();
		int loopIndex=startToken.getTokenIndex()-1;
		while (loopIndex>=0) {
			IToken precedingToken = tokenStream.getTokenAt(loopIndex);
			if (!isLayoutToken(precedingToken)) {
				return precedingToken;
			}
			loopIndex--;
		}
		return null;
	}
	
	private void analysize(){
		setCommentsBefore();
		IToken startToken = node.getLeftIToken();
		IPrsStream tokenStream=startToken.getIPrsStream();
		indentNode=getIndentation(getLineText(tokenStream, startToken.getLine()));
		indentValueNode=getIndentValue(getLineText(tokenStream, startToken.getLine()));
		setSeperatingWhitespace(startToken);
		System.out.println("["+seperatingWhitespace+"]");
		System.out.println("["+indentNode+"]");
		System.out.println("["+indentValueNode+"]");
		setCommentsAfter();
	}

	private void setCommentsAfter() {
		IToken startToken = node.getRightIToken();
		IPrsStream tokenStream=startToken.getIPrsStream();
		ArrayList<IToken> followingComments=new ArrayList<IToken>();
		IToken nextToken=getNextToken(tokenStream, startToken);
		while(!isEOFToken(nextToken) && isLayoutToken(nextToken) && nextToken.getLine()==startToken.getLine()){
			if (!SGLRToken.isWhiteSpace(nextToken))
				followingComments.add(nextToken); 
			nextToken=getNextToken(tokenStream, nextToken);
		}
		if(isEOFToken(nextToken) || nextToken.getLine() > startToken.getLine() || isLiteralToken(nextToken))
			commentsAfter=followingComments; //TODO: <node> <literal> <comment> <newline> (typical pattern for seperators)
		System.out.println(commentsAfter);
	}

	private IToken getNextToken(IPrsStream tokenStream, IToken nextToken) {
		if(nextToken.getTokenIndex() >= tokenStream.getSize())
			return null;
		return tokenStream.getTokenAt(nextToken.getTokenIndex()+1);
	}

	private void setCommentsBefore() {
		IToken startToken = node.getLeftIToken();
		IPrsStream tokenStream=startToken.getIPrsStream();

		//preceding non-layout token
		IToken precedingNonLayoutToken=getPrecedingNonLOToken(startToken);
		//System.out.println(precedingNonLayoutToken);
		
		//empty lines before
		int emptyLineOffset = getEmptyLineOffset(startToken, tokenStream, precedingNonLayoutToken);
		
		
		// block backwards pointing comments. Comment points backwards if: 
		//1. comment starts on the same line as the preceding non layout token 
		//and 2. not: literal(=preceding-non-lo) - comment - starttoken on the same line
		int eolOffset=-1;
		if(emptyLineOffset==-1  && precedingNonLayoutToken.getEndOffset()>0){//no empty lines
			if(!(precedingNonLayoutToken.getEndLine()==startToken.getLine() && isLiteralToken(precedingNonLayoutToken))){
				int lineIndex = precedingNonLayoutToken.getEndLine();
				eolOffset=tokenStream.getLineOffset(lineIndex);
				//System.out.println(new TextFragment(eolOffset, eolOffset+10).getText());
			}
		}
		
		//comments-before
		ArrayList<IToken> precedingComments=new ArrayList<IToken>();
		int endIndex;
		if(precedingNonLayoutToken!=null)
			endIndex=precedingNonLayoutToken.getTokenIndex()+1;
		else
			endIndex=0;
		int loopIndex=startToken.getTokenIndex()-1;
		IToken precedingToken=tokenStream.getTokenAt(loopIndex);
		while (loopIndex>=endIndex && precedingToken.getStartOffset()>= Math.max(emptyLineOffset, eolOffset) && isLayoutToken(precedingToken)) {
			if (!SGLRToken.isWhiteSpace(precedingToken))
				precedingComments.add(0, precedingToken); 
			loopIndex--;
			precedingToken = tokenStream.getTokenAt(loopIndex);
		}
		//System.out.println("comm before: ");
		for (IToken commentTok : precedingComments) {
			//System.out.println(commentTok.toString());
		}	
		//System.out.println();
		
		//Comments may attach to sublists rather then single nodes
		if(!precedingComments.isEmpty()){
			if(precedingComments.get(0).getLine()!=startToken.getLine()){
				int lindex=precedingComments.get(0).getLine();
				String precedingLine = getLineText(tokenStream, lindex-1);
				//System.out.println(precedingLine);
				String commentLine=getLineText(tokenStream, lindex);
				String nodeLine=getLineText(tokenStream, startToken.getLine());
				//System.out.println(commentLine);
				String followingLine = getLineText(tokenStream, node.getRightIToken().getEndLine()+1);
				//System.out.println(followingLine);
				if(getIndentValue(precedingLine)< getIndentValue(commentLine) && getIndentValue(nodeLine) > getIndentValue(followingLine)){
					//System.out.println("Comment refers to node");
					commentsBefore=precedingComments;
				}
			}
			else{
				//System.out.println("Comment refers to node");
				commentsBefore=precedingComments;
			}				
		}
	}

	private void setSeperatingWhitespace(IToken startToken) {
		int endOffsetSepWS;
		if(commentsBefore.isEmpty())
			endOffsetSepWS=startToken.getStartOffset();
		else
			endOffsetSepWS=commentsBefore.get(0).getStartOffset();
		int startOffsetSepWS=endOffsetSepWS;
		ILexStream lexStream=startToken.getILexStream();
		while (startOffsetSepWS > 0 && (Character.isWhitespace(lexStream.getCharValue(startOffsetSepWS-1)))) {
			startOffsetSepWS--;
		}
		if(endOffsetSepWS-1 > startOffsetSepWS){
			seperatingWhitespace=lexStream.toString(startOffsetSepWS, endOffsetSepWS-1);
			seperatingWhitespace=correctWSIndentation(seperatingWhitespace, ""); //removes indentation
		}
		else
			seperatingWhitespace=" ";
		startLayoutBefore=startOffsetSepWS;
	}

	private String correctWSIndentation(String line, String indent) {
		int newLineIndex=line.lastIndexOf('\n');
		if(newLineIndex>=0){
			return line.substring(0, newLineIndex+1)+indent;
		}
		return line;
	}

	private int getEmptyLineOffset(IToken startToken, IPrsStream tokenStream,
			IToken precedingNonLayoutToken) {
		int emptyLineOffset=-1;
		if(precedingNonLayoutToken!=null){
			int endLineIndex;
			endLineIndex=precedingNonLayoutToken.getLine();
			int startLine=startToken.getLine();
			for (int i = startLine-1; i > endLineIndex; i--) {
				int start=tokenStream.getLineOffset(i-1);
				if(i-1==0)//first line
					start=0;
				TextFragment line=new TextFragment(start, tokenStream.getLineOffset(i));
				//System.out.println(line.getText());
				boolean isEmptyLine=line.getText(startToken.getILexStream()).trim().equals("");
				if(isEmptyLine){
					emptyLineOffset=start;
					break;
				}
			}
		}
		else
			emptyLineOffset=0;
		//if(emptyLineOffset>=0)
			//System.out.println(new TextFragment(emptyLineOffset, emptyLineOffset+10).getText());
		return emptyLineOffset;
	}

	private String getLineText(IPrsStream tokenStream, int lindex) {
		if(lindex<=0)
			return null;
		int start=tokenStream.getLineOffset(lindex-1)+1;
		if(lindex-1==0)//first line
			start=0;
		int end=tokenStream.getLineOffset(lindex);
		if(lindex==tokenStream.getLineCount()+1){//lastline
			end=tokenStream.getILexStream().getStreamLength()-1;
		}
		TextFragment line=new TextFragment(start, end);				
		String lineText=line.getText(tokenStream.getILexStream());
		return lineText;
	}
	
	private static int getIndentValue(String line) {
		if(line==null)
			return -1;
		int result=0;
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c == ' ') {
				result++;
			}
			else if (c == '\t') {
				result+=4;//TODO: use editor settings 
			}
			else if (c=='\n')
				return 0;//empty lines have 0 indent
			if (!Character.isWhitespace(c))
				return result;
		}
		return 0;
	}
	
	private static String getIndentation(String line) {
		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c != ' ' && c != '\t') {
				return line.substring(0, i);
			}
		}
		return "";
	}
	
	private static boolean isLiteralToken(IToken tok) {
		return TokenKind.valueOf(tok.getKind())==TokenKind.TK_OPERATOR;
	}
	
	private static boolean isEOFToken(IToken tok) {
		return TokenKind.valueOf(tok.getKind())==TokenKind.TK_EOF;
	}
	
	private static boolean isLayoutToken(IToken tok) {
		return TokenKind.valueOf(tok.getKind())==TokenKind.TK_LAYOUT;
	}
	
	public class TextFragment{
		private int start;
		private int end;
		
		public int getStart() {
			return start;
		}
		public void setStart(int start) {
			this.start = start;
		}
		
		public int getEnd() {
			return end;
		}
		
		public void setEnd(int end) {
			this.end = end;
		}
		
		public TextFragment(){
			//default constructor
		}
		
		public TextFragment(int startOffset, int endOffset){
			start=startOffset;
			end=endOffset;
		}
		
		public String getText(ILexStream lexStream) {
			if(start==end)
				return "";
			if(DocumentaryStructure.isUnvalidInterval(start, end, lexStream))
				return null;
			String textfragment=lexStream.toString(start, end-1);
			return textfragment;
		}

	}
	
	public static boolean isUnvalidInterval(int pos_start, int pos_end, ILexStream lexStream) {
		return pos_start < 0 || pos_start > pos_end || pos_end >= lexStream.getStreamLength();
	}
}
