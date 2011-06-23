package org.strategoxt.imp.runtime.stratego;

import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getLeftToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getRightToken;
import static org.spoofax.terms.attachments.ParentAttachment.getParent;

import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokenizer;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.jsglr.client.imploder.Token;
import org.strategoxt.imp.runtime.parser.ast.StrategoSubList;

/**
 * Provides access to the layout structure (text fragments and offsets) surrounding a node
 * Offers support for text reconstruction algorithm
 * 
 * @author Maartje de Jonge
 */
public class LayoutStructure {
	
	private final ISimpleTerm node;
	private final ISimpleTerm listParent;
	private final ITokenizer tokens;
	
	//suffix data
	private int suffixStartIndex; //possible invalid index (if node contains rightmost token)
	private int commentsAfterExclEndIndex; //possible invalid index (if node contains rightmost token)
	private int suffixSeparationExclEndIndex; //possible invalid (if node contains rightmost token)
	
	//prefix data
	private int prefixEndIndex; //possible invalid index (if node contains leftmost token)
	private int commentsBeforeStartIndex; //valid index (0 if node contains leftmost token)
	private int prefixSeparationStartIndex; //valid index (0 if node contains leftmost token)

	public LayoutStructure(IStrategoTerm node) {
		this.node = node;
		tokens = getLeftToken(node).getTokenizer();
		listParent = getParentList(); //could be null
		analyzeSuffix();
		analyzePrefix();
		//logAnalysisResults();
	}

	/**
	 * Comments preceding and associated with the node
	 */
	public String getCommentsBefore() {
		String layoutPrefix = getLayoutPrefix();
		return trimWsPreservingLastNewline(layoutPrefix);
	}

	/**
	 * Comments succeeding and associated with the node 
	 * whereby the suffix separator (if any) is changed by spaces
	 */
	public String getCommentsAfter() {
		String layoutSuffix = getLayoutSuffix();
		return trimWsPreservingLastNewline(layoutSuffix);
	}

	/**
	 * Text fragment preceeding the node that starts from comment(s) before
	 */
	public String getLayoutPrefix() {
		return layoutFragmentWithOutSeparator(this.commentsBeforeStartIndex, this.prefixEndIndex);
		//assert: Separator not in fragment
	}

	/**
	 * Text fragment succeeding the node that ends with comment(s) after
	 * whereby the suffix separator (if any) is changed by spaces
	 */
	public String getLayoutSuffix() {
		return layoutFragmentWithOutSeparator(this.suffixStartIndex, this.commentsAfterExclEndIndex-1);
	}

	/**
	 * Text fragment between comment(s) before start offset and comments after end offset,
	 * whereby the suffix separator (if any) is changed by spaces
	 */
	public String getTextWithLayout() {
		return getTokenString(commentsBeforeStartIndex, getRightToken(node).getIndex())+getLayoutSuffix();
	}

	/**
	 * Indentation of the start line of the node, consist of spaces and tabs.
	 * In case other tokens precede the node, an extra TAB is added
	 */
	public String getIndentation() {
		String indent = getIndentString();
		assert(indent.replaceAll("[ \t]", "").equals(""));
		return indent;
	}

	/**
	 * Separation between first and second (TODO: average) list element:
	 * - consists of newlines, spaces, tabs and separator (if any)
	 * - null in case (parent) element is not a list, or is a list with 0 or 1 element
	 */
	public String getSeparation() {
		return getSeparationString();
	}

	/**
	 * For elements in the middle of a list: 
	 * - start offset of preceding comment (if any), or node (otherwise)
	 * - equal to "insert before offset/layout prefix/text with layout" start offset
	 * For last list element: 
	 * - min. start offset of node/comm before/prefix-separator + preceding ws
	 */
	public int getDeletionStartOffset() {
		if(isLastListElement()){
			assert(isValidTokenIndex(prefixSeparationStartIndex));
			return getTokenAt(prefixSeparationStartIndex).getStartOffset();
		}
		return getInsertBeforeOffset();
	}

	/**
	 * For elements in the middle of a list: 
	 * - max. end offset of node/comment after/suffix-separator + succeeding ws
	 * For last list element: 
	 * - end offset comment after (or node)
	 * - equal to insert-at-end offset (minus \n for line comments)
	 * - do not delete \n of a line comment (this is used as vertical layout)
	 */
	public int getDeletionEndOffset() {
		if(isLastListElement()){
			int deletionEndOffset = getInsertAtEndOffset()-1;
			return keepNewlineOfLineComment(deletionEndOffset);
		}
		if(isValidTokenIndex(suffixSeparationExclEndIndex)){
			int deletionEndOffset = getTokenAt(suffixSeparationExclEndIndex).getStartOffset()-1;
			return deletionEndOffset;
		}
		assert(getRightToken(node).getIndex() == tokens.getTokenCount()-1);
		return getRightToken(node).getEndOffset();
	}

	/**
	 * Start offset of preceding comment (if any) or node
	 */
	public int getInsertBeforeOffset() {
		assert this.commentsBeforeStartIndex <= getLeftToken(node).getStartOffset();
		return getTokenAt(this.commentsBeforeStartIndex).getStartOffset();
	}

	/**
	 * End offset+1 of succeeding comment (if any) or node
	 */
	public int getInsertAtEndOffset() {
		if(isValidTokenIndex(commentsAfterExclEndIndex))
			return getTokenAt(this.commentsAfterExclEndIndex).getStartOffset();
		assert(getRightToken(node).getIndex() == tokens.getTokenCount()-1);
		return getRightToken(node).getEndOffset()+1;
	}	
	
	/**
	 * Layout fragment with separator replaced by spaces
	 * @param loIndexStart token index of start layout fragment (inclusive)
	 * @param loIndexEnd token index of end layout fragment (inclusive)
	 */
	private String layoutFragmentWithOutSeparator(int loIndexStart, int loIndexEnd){
		String result = "";
		for (int i = loIndexStart; i <= loIndexEnd; i++) {
			if(!isValidTokenIndex(i)){ break;}
			if(isLayout(i))
				result += getTokenString(i);
			else { //separator expected
				assert(isSeparatorToken(i));
				result += createSpaces(getTokenAt(i).getLength());
			}
		}
		return result;
	}

	private String createSpaces(int length) {
		String spaces="";
		for (int i = 0; i < length; i++) {
			spaces +=" ";
		}
		return spaces;
	}

	private int keepNewlineOfLineComment(int deletionEndOffset) {
		while (Character.isWhitespace(getCharAt(deletionEndOffset))){ //newline at the end of line comment
			deletionEndOffset --;
		}
		return deletionEndOffset;
	}

	/**
	 * a) Always after separator, always after ast characters before node
	 * b) No empty line between end of comment string and start of node
	 * c) if comment is on the same line, then there is no preceding sibling on the same line except when (d)
	 * d) "separator" - "comment - node" on the same line, then comment associated to node
	 * e) if comment starts on same line as preceding ast token, then comment NOT associated to node 
	 * f) grouping???
	 */
	private void analyzePrefix() {
		prefixEndIndex = getLeftToken(node).getIndex()-1; //possible invalid index
		final int prefixStartIndex = getPrefixIndexStart(prefixEndIndex); //possible invalid index
		commentsBeforeStartIndex = getLeftToken(node).getIndex();
		int tokenIndex = commentsBeforeStartIndex - 1;
		int lastNonEmptyLine = getLeftToken(node).getLine();
		int preceedingAstLine = -1;
		if(isValidTokenIndex(prefixStartIndex-1)){
			preceedingAstLine=getTokenAt(prefixStartIndex-1).getEndLine();
		}
		final boolean sameLineSiblings = preceedingSibEndLine()  == getLeftToken(node).getLine();
		while(isValidTokenIndex(tokenIndex) && tokenIndex >= prefixStartIndex){
			if(isComment(tokenIndex)){
				int commentEndLine = getTokenAt(tokenIndex).getEndLine();
				int commentStartLine = getTokenAt(tokenIndex).getLine();
				if(lastNonEmptyLine - commentEndLine > 1)
					break; //line between (b)
				else if ((preceedingAstLine == commentStartLine) && !sameLineSiblings) {
					break; //e comment associated to preceding astnode
				}
				else {
					commentsBeforeStartIndex = tokenIndex;
					lastNonEmptyLine = getTokenAt(tokenIndex).getLine();
				}
			}
			if(isSeparatorToken(tokenIndex-1)){
				break; //a) only comments after separator are included
			}
			if(tokenIndex == prefixStartIndex && sameLineSiblings){
				commentsBeforeStartIndex = getLeftToken(node).getIndex(); //(c,d) comments fall between siblings
				break;
			}
			tokenIndex --;
		}
		final int prefixSeparatorIndex = getIndexSeparator(prefixStartIndex, prefixEndIndex);//-1 in case not exists
		
		prefixSeparationStartIndex = prefixSeparatorIndex ==-1? commentsBeforeStartIndex : prefixSeparatorIndex;
		while (isWhitespace(prefixSeparationStartIndex-1)) {
			prefixSeparationStartIndex --; //start of next node fragment
		}
	}

	/**
	 * a) Always on the same line as node
	 * b) If succeeding sibling on the same line, then: comments after separator are excluded
	 * c) If succeeding sibling on the same line, then: comments only included if they are in front of separator 
	 * d) If no succeeding sibling on the same line, then comment attaches to node (even when separator between node and comment) 
	 */
	private void analyzeSuffix() {
		suffixStartIndex = getRightToken(node).getIndex() + 1; //possible invalid index
		final int suffixEndIndex = getSuffixIndexEnd(suffixStartIndex); //possible invalid index
		final boolean sameLineSiblings = 
			tokensOnSameLine(suffixStartIndex -1, suffixEndIndex + 1) &&
			!isLastListElement();
		commentsAfterExclEndIndex = suffixStartIndex;
		int tokenIndex = commentsAfterExclEndIndex;
		//sets comment after end index
		while(isValidTokenIndex(tokenIndex) && tokenIndex <= suffixEndIndex + 1){
			if(getTokenAt(tokenIndex).getLine() != getRightToken(node).getEndLine()){
				break; //a) comment not on same line
			}
			if(sameLineSiblings && tokenIndex == suffixEndIndex + 1){
				//(c) same line, no separator: 
				//comments is not associated to preceeding or succeeding sibling
				commentsAfterExclEndIndex = suffixStartIndex; 
				break;
			}
			if(sameLineSiblings && isSeparatorToken(tokenIndex)){
				break; //b) exclude comments after separator between siblings on same line
			}
			if(isComment(tokenIndex)){
				commentsAfterExclEndIndex = tokenIndex + 1; //d) consume comments
			}
			tokenIndex ++;
		}
		final int suffixSeparatorIndex = getIndexSeparator(suffixStartIndex, suffixEndIndex);//-1 in case not exists
		suffixSeparationExclEndIndex = Math.max(commentsAfterExclEndIndex, suffixSeparatorIndex + 1);
		while (isWhitespace(suffixSeparationExclEndIndex)) {
			suffixSeparationExclEndIndex ++; //start of next node fragment
		}
	}

	private boolean isFirstListElement() {
		if(listParent == null)
			return false;
		if(node instanceof StrategoSubList){
			return ((StrategoSubList)node).getIndexStart() == 0;
		}
		return listParent.getSubterm(0) == node;
	}
	
	private int preceedingSibEndLine() {
		if(isFirstListElement())
			return -1;
		if(listParent == null)
			return -1;
		int indexPreceedingSib = -1;
		if(node instanceof StrategoSubList){
			indexPreceedingSib = ((StrategoSubList)node).getIndexStart() - 1;
			assert(listParent == ((StrategoSubList)node).getCompleteList());
		}
		else{
			for (int i = 1; i < listParent.getSubtermCount(); i++) {
				if(listParent.getSubterm(i)==node)
					indexPreceedingSib = i-1;
			}
		}
		assert(indexPreceedingSib >= 0 && indexPreceedingSib < listParent.getSubtermCount());
		ISimpleTerm preceedingNode = listParent.getSubterm(indexPreceedingSib);
		if(!ImploderAttachment.hasImploderOrigin(preceedingNode))
			return -1;
		return getRightToken(preceedingNode).getEndLine();
	}

	private boolean isLastListElement() {
		if(listParent == null)
			return false;
		if(node instanceof StrategoSubList){
			return ((StrategoSubList)node).getIndexEnd() == listParent.getSubtermCount()-1;
		}
		return listParent.getSubterm(listParent.getSubtermCount()-1) == node;
	}
	
	
	private boolean tokensOnSameLine(int i, int j) {
		if(isValidTokenIndex(i) && isValidTokenIndex(j)){
			return getTokenAt(i).getLine() == getTokenAt(j).getLine();
		}
		return false;
	}

	private IToken getTokenAt(int i) {
		assert(isValidTokenIndex(i));
		return tokens.getTokenAt(i);
	}

	private char getCharAt(int offset) {
		assert(offset < tokens.getInput().length());
		return tokens.getInput().charAt(offset);
	}

	private boolean isValidTokenIndex(int j){
		return j>=0 && j < tokens.getTokenCount();
	}

	private int getSuffixIndexEnd(int suffixStartIndex) {
		int suffixEndIndex = suffixStartIndex;
		while (notAssociatedToAstNode(suffixEndIndex + 1)) {
			assert(isValidTokenIndex(suffixEndIndex));
			suffixEndIndex ++;
		}
		return suffixEndIndex;
	}

	private int getPrefixIndexStart(int prefixEndTokenIndex) {
		int prefixStartIndex = prefixEndTokenIndex;
		while (notAssociatedToAstNode(prefixStartIndex-1)) {
			assert(isValidTokenIndex(prefixStartIndex));
			prefixStartIndex--;
		}
		return prefixStartIndex;
	}

	private int getIndexSeparator(int suffixStartIndex, int suffixEndIndex) {
		for (int i = suffixEndIndex; i >= suffixStartIndex; i--) {
			if(isSeparatorToken(i))
				return i;
		}
		return -1;
	}


	private boolean isSeparatorToken(int i) {
		return isAssociatedToListParent(i) && !isLayout(i);
	}

	private boolean notAssociatedToAstNode(int tokenIndex){
		return 
			(isLayout(tokenIndex) || isAssociatedToListParent(tokenIndex));
	}
	
	private boolean isWhitespace(int tokenIndex) {
		return isLayout(tokenIndex) && !isComment(tokenIndex);
	}

	private boolean isComment(int tokenIndex) {
		return 
			isValidTokenIndex(tokenIndex) && 
			isLayout(tokenIndex) && 
			!Token.isWhiteSpace(getTokenAt(tokenIndex));
	}

	private boolean isLayout(int tokenIndex) {
		return
			isValidTokenIndex(tokenIndex) &&
			getTokenAt(tokenIndex).getKind() == IToken.TK_LAYOUT;
	}

	private boolean isAssociatedToListParent(int tokenIndex) {
		return 
			isValidTokenIndex(tokenIndex) &&
			listParent != null && 
			getTokenAt(tokenIndex).getAstNode() == listParent;
	}


	private IStrategoTerm getParentList() {
		if(node instanceof StrategoSubList){
			return ((StrategoSubList)node).getCompleteList();
		}
		if (node.isList() && node.getSubtermCount() > 0)
			return (IStrategoTerm) node;
		if (getParent(node) == null)
			return null;
		return  getParent(node).isList()? getParent(node) : null;
	}
	
	private String trimWsPreservingLastNewline(String layoutSuffix) {
		String commentEnd = "";
		if(layoutSuffix.endsWith("\n"))
			commentEnd = "\n";
		return layoutSuffix.trim() + commentEnd; //preserve line comment end
	}

	private String getIndentString() {
		int offset = getLeftToken(node).getStartOffset();
		String input = tokens.getInput();
		String indentation = "";
		for (int i = offset-1; i >= 0; i--) {
			char character = input.charAt(i);
			if(character == '\n')
				return indentation;
			else if(character == ' ' || character == '\t')
				indentation = character + indentation;
			else
				indentation = "\t"; //node does not start on line, next line has more indent
		}
		return indentation;
	}
	
	public String getSeparationString() { //TODO: find average separation?
		if(listParent != null && listParent.getSubtermCount() > 1){
			IToken startToken = getRightToken(listParent.getSubterm(0));
			IToken endToken = getLeftToken(listParent.getSubterm(1));
			return getSeparationString(startToken.getIndex(), endToken.getIndex());
		}
		return null;
	}
	
	private String getSeparationString(int tokenIndexStart, int tokenIndexEnd) {
		String separation="";
		String layoutText="";
		boolean commentSeen = false;
		boolean commentLine = false;
		for (int i = tokenIndexStart +1; i < tokenIndexEnd; i++) {
			IToken token = tokens.getTokenAt(i);
			String tokenText = getTokenString(i);
			if(!isComment(i)){
				if(!commentSeen)
					separation += tokenText;
				else
					layoutText += tokenText;
			}
			else { 
				commentSeen = true;
				layoutText = ""; //layout between comments is not part of separation
				if(tokenText.endsWith("\n"))
					layoutText = "\n";
				if(token.getLine() != getTokenAt(tokenIndexStart).getEndLine() && token.getEndLine() != getTokenAt(tokenIndexEnd).getLine())
					commentLine = true;
			}
		}
		if(commentLine){
			separation = separation.replaceFirst("\n[ \t]*", "");
		}
		return separation + layoutText;
	}
	
	private String getTokenString(int tokenIndex){
		assert(isValidTokenIndex(tokenIndex));
		IToken t = getTokenAt(tokenIndex);
		int startOffset = t.getStartOffset();
		int endOffset = t.getEndOffset();
		return tokens.toString(startOffset, endOffset); 
	}

	private String getTokenString(int tokenIndexStart, int tokenIndexEnd){
		assert(isValidTokenIndex(tokenIndexStart));
		assert(isValidTokenIndex(tokenIndexEnd));
		int startOffset = getTokenAt(tokenIndexStart).getStartOffset();
		int endOffset = getTokenAt(tokenIndexEnd).getEndOffset();
		return tokens.toString(startOffset, endOffset); 
	}
	
	/*
	private void logAnalysisResults() {
		System.out.println("comm before:" + this.getCommentsBefore() + "#");
		System.out.println("comm after:" + this.getCommentsAfter() + "#");
		System.out.println("lo prefix:" + this.getLayoutPrefix() + "#");
		System.out.println("lo suffix:" + this.getLayoutSuffix() + "#");
		System.out.println("full text:" + this.getTextWithLayout() + "#");

		System.out.println("indent:" + this.getIndentation() + "#");
		System.out.println("separation:" + this.getSeparation() + "#");

		System.out.println("del-start:" + this.getDeletionStartOffset());
		System.out.println("del-end:" + this.getDeletionEndOffset());
		System.out.println(tokens.toString(getDeletionStartOffset(), getDeletionEndOffset())+"#");
		System.out.println("insert-before:" + this.getInsertBeforeOffset());
		System.out.println("insert-at-end:" + this.getInsertAtEndOffset());
		System.out.println(tokens.toString(getInsertBeforeOffset(), getInsertAtEndOffset())+"#");
		System.out.println(tokens.toString(5,5) + "#--------------------");
	}
	*/

}

