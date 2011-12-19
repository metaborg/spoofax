package org.strategoxt.imp.runtime.services;

import static java.lang.Math.min;
import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_SPACES_FOR_TABS;
import static org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants.EDITOR_TAB_WIDTH;
import static org.eclipse.ui.texteditor.ITextEditorExtension3.SMART_INSERT;
import static org.spoofax.jsglr.client.imploder.IToken.TK_EOF;
import static org.spoofax.jsglr.client.imploder.IToken.TK_ERROR;
import static org.spoofax.jsglr.client.imploder.IToken.TK_IDENTIFIER;
import static org.spoofax.jsglr.client.imploder.IToken.TK_KEYWORD;
import static org.spoofax.jsglr.client.imploder.IToken.TK_LAYOUT;
import static org.spoofax.jsglr.client.imploder.IToken.TK_NUMBER;
import static org.spoofax.jsglr.client.imploder.IToken.TK_OPERATOR;
import static org.spoofax.jsglr.client.imploder.IToken.TK_RESERVED;
import static org.spoofax.jsglr.client.imploder.IToken.TK_STRING;
import static org.spoofax.jsglr.client.imploder.IToken.TK_UNKNOWN;
import static org.spoofax.jsglr.client.imploder.IToken.TK_VAR;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getLeftToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getRightToken;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.imp.editor.UniversalEditor;
import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.IAutoEditStrategy;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Point;
import org.spoofax.NotImplementedException;
import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokenizer;
import org.strategoxt.imp.runtime.EditorState;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.SGLRParseController;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class AutoEditStrategy implements IAutoEditStrategy, VerifyKeyListener {
	
	private static boolean justProcessedKeyEvent;
		
	private final ILanguageSyntaxProperties syntax;
	
	private final String[][] allFences;
	
	private final int maxOpenFenceLength;
	
	private final int maxCloseFenceLength;
	
	private static UniversalEditor lastEditor;
	
	private IParseController controller;
	
	private UniversalEditor editor;
	
	private int lastAutoInsertedFenceLine;
	
	private int lastAutoInsertedFencesLength;
	
	private String lastAutoInsertedFenceLineStart;
	
	private String lastAutoInsertedFenceLineEnd;

	private boolean allowAutoRemoveFence;
	
	private Stack<Integer> lastAutoInsertionOpenFences = new Stack<Integer>();
	
	private EditScenarioCollector editScenarioCollector;

	public AutoEditStrategy(ILanguageSyntaxProperties syntax) {
		this.syntax = syntax;
		
		allFences = syntax instanceof SyntaxProperties
				? ((SyntaxProperties) syntax).getAllFences()
				: syntax.getFences();
		
		int maxOpenFenceLength = 0;
		int maxCloseFenceLength = 0;
		for (String[] fencePair : allFences) {
			if (fencePair[0].length() > maxOpenFenceLength)
				maxOpenFenceLength = fencePair[0].length();
			if (fencePair[1].length() > maxCloseFenceLength)
				maxCloseFenceLength = fencePair[1].length();
		}
		
		this.maxOpenFenceLength = maxOpenFenceLength;
		this.maxCloseFenceLength = maxCloseFenceLength;
		editScenarioCollector = new EditScenarioCollector();
	}
	
	public void initialize(IParseController controller) {
		this.controller = controller;
	}
	
	public void customizeDocumentCommand(IDocument document, DocumentCommand command) {
		try {
			indentPastedContent(document, command);
				
		} catch (BadLocationException e) {
			Environment.logException("Could not determine auto edit strategy", e);
		} catch (RuntimeException e) {
			Environment.logException("Could not apply auto edit strategy", e);
		}
	}

	public void verifyKey(VerifyEvent event) {
		try {
			String input = new String(new char[] { event.character });
			Point selection = getEditor().getSelection(); 
			if(controller instanceof SGLRParseController) {
				((SGLRParseController)controller).getParser().setCursorLocation(selection.x);
				editScenarioCollector.collectEditorFile(((SGLRParseController)controller).getCurrentAst(), selection.x);
			}
			ISourceViewer viewer = getEditor().getServiceControllerManager().getSourceViewer();
			if (event.widget instanceof StyledText
					&& indentAfterNewline(viewer, viewer.getDocument(), selection.x, selection.y, input)) {
				// Make sure caret is visible (urgh)
				((StyledText) event.widget).invokeAction(ST.LINE_UP);
				((StyledText) event.widget).invokeAction(ST.LINE_DOWN);
				event.doit = false;
			} else if (insertClosingFence(viewer, viewer.getDocument(), selection.x, selection.y, input)
					|| skipClosingFence(viewer, viewer.getDocument(), selection.x, selection.y, input)
					|| undoClosingFence(viewer, viewer.getDocument(), selection.x, selection.y, input)) {
				event.doit = false;
			}
		} catch (BadLocationException e) {
			Environment.logException("Could not determine auto edit strategy", e);
			justProcessedKeyEvent = false;
		} catch (RuntimeException e) {
			Environment.logException("Could not apply auto edit strategy", e);
			justProcessedKeyEvent = false;
		}
	}
	
	protected static boolean pollJustProcessedKeyEvent() {
		boolean result = justProcessedKeyEvent;
		justProcessedKeyEvent = false;
		return result;
	}

	protected void indentPastedContent(IDocument document, DocumentCommand command) throws BadLocationException {
		// UNDONE: Disabled smart pasting for now
		if ("true".equals("true"))
			return;
		if (isIndentable(command)) {
			String lineStart = getLineBeforeOffset(document, command.offset);
			if (lineStart.trim().length() > 0) {
				// Sanity check: only indent based on empty lines
				// (don't do it if the pasted content may be a new definition which
				// may have to be dedented)
				return;
			}
			String indentation = getIndentation(lineStart);
			//if (isCloseFenceLine(lineStart)) (noop with above sanity check)
			//	indentation += createIndentationLevel();
			command.text = setIndentation(command.text, indentation);
		}
	}

	/**
	 * Indent after a open fence and move the closing fence to the right place.
	 * Also handles indentation after indentation triggers (which are just
	 * opening fences).
	 */
	protected boolean indentAfterNewline(ISourceViewer viewer, IDocument document, int offset, int length, String input) throws BadLocationException {
		// TODO: support matching fences for indentation:
		//   "\"" "\""
		//   (may be very tricky, when detecting if something is a closing " or opening ")
		if (input.equals("\n") || input.equals("\r") || input.equals("\r\n")) {
			justProcessedKeyEvent = true;
			String lineStart = getLineBeforeOffset(document, offset);
			String lineEnd = getLineAfterOffset(document, offset, length);
			String upToCursor;
			String closeFence = getCloseFenceForOpenFenceLine(lineStart);
			if (closeFence != null) {
				if (isCloseFenceLine(lineEnd, closeFence)) {
					upToCursor = "\n" + getIndentation(lineStart, true) + createIndentationLevel();
					document.replace(offset, length, upToCursor + "\n" + getIndentation(lineStart, true));
				} else {
					upToCursor = "\n" + getIndentation(lineStart, true) + createIndentationLevel();
					document.replace(offset, length, upToCursor);
				}
			} else {
				upToCursor = "\n" + getIndentation(lineStart, true);
				document.replace(offset, length, upToCursor);
			}
			viewer.setSelectedRange(offset + upToCursor.length(), 0);
			// viewer.setSelectedRange(offset - 1, 0);
			return true;
		}
		return false;
	}
	
	protected boolean insertClosingFence(ISourceViewer viewer, IDocument document, int offset, int length, String input) throws BadLocationException {
		// TODO: proper newline after multiline insertion"
		//   "c\nthen\n\ts\nend" may need an extra \n
		
		// TODO: respect word boundaries
		//    insert only if "\bif" is typed
		
		// TODO: respect Eclipse preference for inserting brackets
		
		if (input.length() == 0)
			return false;

		String lineEnd = getLineAfterOffset(document, offset, length);
		if (getEditor().getInsertMode() == SMART_INSERT
				|| stripCommentsAndLayout(lineEnd).length() == 0) {
			
			// Backtrack to see if a fence was typed in
			// for (int i = 0; i < maxOpenFenceLength && offset - i >= 0; i++) {
			for (int i = min(maxOpenFenceLength - 1, offset); i >= 0; i--) {
				String openFence = document.get(offset - i, i) + input;
				String closeFence = getMatchingCloseFence(openFence);
				if (closeFence != null && closeFence.length() > 0) {
					if (isParsedAsLexicalOrLayout(document, offset, input)
							|| isIdentifierAfterOffset(document, offset + length)
							|| isOpenFenceAfterOffset(document, offset + length))
						return false;
					justProcessedKeyEvent = true;
					String lineStart = getLineBeforeOffset(document, offset);
					closeFence = formatInsertedText(closeFence, lineStart);
					document.replace(offset, length, input + closeFence);
					IRegion selection = getInsertedTextSelection(offset + input.length(), closeFence);
					viewer.setSelectedRange(selection.getOffset(), selection.getLength());
					lastAutoInsertedFenceLine = document.getLineOfOffset(offset);
					lastAutoInsertedFenceLineStart = lineStart;
					lastAutoInsertedFenceLineEnd = closeFence + lineEnd;
					if (lastAutoInsertedFencesLength == 0)
						allowAutoRemoveFence = true;
					lastAutoInsertedFencesLength += closeFence.length();
					allowAutoRemoveFence = allowAutoRemoveFence
							&& openFence.length() == 1 && closeFence.length() == 1;
					lastAutoInsertionOpenFences.push(offset - i + input.length());
					return true;
				}
			}
 		}
		return false;
	}
	
	private boolean isIdentifierAfterOffset(IDocument document, int offset) throws BadLocationException {
		final int ASSUMED_IDENTIFIER_SIZE = 6;

		if (syntax instanceof SyntaxProperties) {
			Pattern identifierPattern = ((SyntaxProperties) syntax).getIdentifierLexical();
		
			for (int i = offset, max = document.getLength(); i < max; i++) {
				char c = document.getChar(i);
				if (c == '\n' || c == '\r')
					return false;
				if (Character.isWhitespace(c))
					continue;
				int end = min(max, i + ASSUMED_IDENTIFIER_SIZE);
				String word = document.get(i, end - i);
				return identifierPattern.matcher(word).lookingAt();
			}
		}
		return false;
	}
	
	/**
	 * Tests {@link #isFenceAfterOffset()} succeeds for any of the opening fences.
	 * Inefficient. 
	 */
	private boolean isOpenFenceAfterOffset(IDocument document, int offset) throws BadLocationException {
		for (String[] fencePair : allFences) {
			String openFence = fencePair[0];
			if (isFenceAfterOffset(document, offset, openFence))
				return true;
		}
		return false;
	}
	
	/**
	 * Tests if the offset starts with a particular closing fence,
	 * ignoring whitespace, comments, and lexicals.
	 * 
	 * @see #stripCommentsAndLayout(String)
	 */
	private boolean isFenceAfterOffset(IDocument document, int offset, String fence) throws BadLocationException {
		for (int max = document.getLength(); offset < max; offset++) {
			char c = document.getChar(offset);
			if (c == '\n' || c == '\r' || !Character.isWhitespace(c))
				break;
		}
		
		int endOffset = offset + fence.length();
		return endOffset < document.getLength() && document.get(offset, endOffset - offset).equals(fence);
	}

	public static String formatInsertedText(String text, String lineStart) {
		return text.replace("\\n", "\n" + getIndentation(lineStart, true))
				.replace("\\t", createIndentationLevel())
				.replace("\\\"", "\"")
				.replace("\\\\", "\\");
	}

	private IRegion getInsertedTextSelection(int startOffset, String insertedText) {
		// TODO: improve or eliminate getInsertedTextSelection()
		//  - support selections in strings like "(abc)"
		//  - move to SyntaxProperties 
		//  - maybe use this for content completion selections?
		if (syntax instanceof SyntaxProperties) {
			Matcher matcher = ((SyntaxProperties) syntax).getIdentifierLexical().matcher(insertedText);
			return new Region(startOffset, matcher.lookingAt() ? matcher.end() : 0);
		} else {
			return new Region(startOffset, 0);
		}
	}
	
	/**
	 * Skip automatically inserted closing fences when the user
	 * types them in again.
	 */
	protected boolean skipClosingFence(ISourceViewer viewer, IDocument document, int offset, int length, String input) throws BadLocationException {
		if (lastAutoInsertedFencesLength > 0 && document.getLineOfOffset(offset) == lastAutoInsertedFenceLine) {
			String lineEnd = getLineAfterOffset(document, offset, length);
			if (lastAutoInsertedFenceLineEnd.startsWith(input)
					&& lineEnd.equals(lastAutoInsertedFenceLineEnd)
					&& getLineBeforeOffset(document, offset).startsWith(lastAutoInsertedFenceLineStart)) {
				justProcessedKeyEvent = true;
				lastAutoInsertedFenceLineEnd = lastAutoInsertedFenceLineEnd.substring(input.length());
				lastAutoInsertedFencesLength--;
				if (!lastAutoInsertionOpenFences.isEmpty())
					lastAutoInsertionOpenFences.pop();
				viewer.setSelectedRange(offset + input.length(), length);
				return true;
			} else if (!lineEnd.endsWith(lastAutoInsertedFenceLineEnd)) {
				lastAutoInsertedFencesLength = 0;
				lastAutoInsertionOpenFences.clear();
			}
		} else {
			lastAutoInsertedFencesLength = 0;
			lastAutoInsertionOpenFences.clear();
		}
		return false;
	}
	
	/**
	 * Undo automatically inserted closing fences when the user
	 * deletes the opening fence.
	 */
	protected boolean undoClosingFence(ISourceViewer viewer, IDocument document, int offset, int length, String input) throws BadLocationException {
		if (lastAutoInsertedFencesLength > 0 && allowAutoRemoveFence
				&& document.getLineOfOffset(offset) == lastAutoInsertedFenceLine) {
			if ("\b".equals(input)) {
				String deletedChar = document.get(offset - 1, 1);
				String closeFence = document.get(offset, 1);
				String expectedCloseFence = getMatchingCloseFence(deletedChar);
				if (offset == lastAutoInsertionOpenFences.peek() && closeFence.equals(expectedCloseFence)) {
					justProcessedKeyEvent = true;
					lastAutoInsertedFencesLength--;
					lastAutoInsertedFenceLineEnd = lastAutoInsertedFenceLineEnd.substring(1);
					lastAutoInsertionOpenFences.pop();
					if (getLineBeforeOffset(document, offset).equals(lastAutoInsertedFenceLineStart))
						lastAutoInsertedFenceLineStart = lastAutoInsertedFenceLineStart.substring(0, lastAutoInsertedFenceLineStart.length() - 1);
					document.replace(offset - 1, 2, "");
					return true;
				}
			} else if (!lastAutoInsertionOpenFences.isEmpty() && lastAutoInsertionOpenFences.peek() > offset) {
				allowAutoRemoveFence = false;
			}
		}
		return false;
	}
	
	private UniversalEditor getEditor() {
		assert controller != null;
		if (editor == null) {
			EditorState editorState = EditorState.getEditorFor(controller);
			editor = editorState.getEditor();
			lastEditor = editor;
		}
		return editor;
	}

	public static String getLineBeforeOffset(IDocument document, int offset) throws BadLocationException {
		IRegion region = document.getLineInformationOfOffset(offset);
		return document.get(region.getOffset(), offset - region.getOffset());
	}

	public static String getLineAfterOffset(IDocument document, int offset, int length) throws BadLocationException {
		IRegion region = document.getLineInformationOfOffset(offset + length);
		int startOffset = offset + length;
		int endOffset = region.getOffset() + region.getLength();
		return document.get(startOffset, endOffset - startOffset);
	}

	private static boolean isIndentable(DocumentCommand command) {
		String text = command.text;
		return text.length() > 1
				&& (text.startsWith(" ") || text.startsWith("\t") || text.contains("\n"))
				&& text.trim().length() > 0;
	}
	
	public static String setIndentation(String text, String indentation) {
		String oldIndentation = getMultiLineIndentation(text);
		text = removeIndentation(text, oldIndentation.toCharArray());
		
		return indentation + text.replace("\n", "\n" + indentation);
	}

	private static String removeIndentation(String text, char[] indentation) {
		StringBuilder result = new StringBuilder();
		int tabWidth = getTabWidth();
		for (String line : text.split("\n")) {
			result.append(removeSingleLineIndentation(line, indentation, tabWidth) + "\n");
		}
		result.deleteCharAt(result.length() - 1);
		
		text = result.toString();
		return text;
	}

	private static String removeSingleLineIndentation(String line, char[] indentation, int tabWidth) {
		int lineOffset = 0;
		for (char charToStrip : indentation) {
			if (lineOffset == line.length())
				break;
			if (charToStrip == '\t') {
				if (line.charAt(0) == '\t') {
					lineOffset++;
				} else {
					// TODO: Better support for mixed tabs and spaces when pasting text?
					for (int i = 0; i < tabWidth; i++) {
						if (line.charAt(lineOffset) != ' ')
							break;
						lineOffset++;
					}
				}
			} else if (line.charAt(lineOffset) == ' '){
				lineOffset++;
			} else {
				break;
			}
		}
		return line.substring(lineOffset);
	}

	private static String getMultiLineIndentation(String text) {
		String result = getIndentation(text);
		for (String line : text.split("\n")) {
			if (line.trim().length() != 0) {
				result = getIndentation(line);
				break;
			}
		}
		return result;
	}
	
	private static String createIndentationLevel() {
		// TODO: Respect tabs vs. spaces Eclipse preference
		return useSpacesInsteadOfTabs() ? createSpacesIndentationLevel() : "\t";
	}
	
	private static boolean useSpacesInsteadOfTabs() {
		IPreferenceStore preferences = lastEditor.getThePreferenceStore();
		return preferences != null && 
			preferences.getBoolean(EDITOR_SPACES_FOR_TABS);
	}

	private static String createSpacesIndentationLevel() {
		StringBuilder result = new StringBuilder();
		int tabWidth = getTabWidth();
		for (int i = 0; i < tabWidth; i++) {
			result.append(' ');
		}
		return result.toString();
	}
	
	public static int getTabWidth() {
		IPreferenceStore preferences;
		if(lastEditor!=null){
			preferences = lastEditor.getThePreferenceStore();
		}
		else{
			preferences = EditorState.getActiveEditor().getEditor().getThePreferenceStore();
		}
		return preferences.getInt(EDITOR_TAB_WIDTH); // PreferenceCache.tabWidth;
	}
	
	private static String getIndentation(String line) {
		return getIndentation(line, false);
	}
	
	private static String getIndentation(String line, boolean considerPrefix) {
		int i = 0;
		
		// HACK: support Stratego-like prefix semicolons
		if (considerPrefix)
			line = line.replace(';', ' ').replace(',', ' ');
		
		for (int length = line.length(); i < length; i++) {
			char c = line.charAt(i);
			if (c != ' ' && c != '\t') {
				return line.substring(0, i);
			}
		}
		
		return i == line.length() ? line : "";
	}
	
	/**
	 * Tests if the line ends with an opening fence,
	 * ignoring whitespace, comments, and lexicals.
	 * 
	 * @return the matching closing fence, or null if no open fence on this line
	 * 
	 * @see #stripCommentsAndLayout(String)
	 */
	private String getCloseFenceForOpenFenceLine(String line) {
		line = stripCommentsAndLayout(line);
		for (int i = 1; i <= maxOpenFenceLength && i <= line.length(); i++) {
			int offset = line.length() - i;
			String openFence = line.substring(offset, line.length());
			String closeFence = getMatchingCloseFence(openFence);
			if (closeFence != null
					&& (!isIdentifier(openFence) || offset == 0 || !isIdentifier(line.substring(offset - 1, offset))))
				return closeFence;
		}
		return null;
	}
	
	/**
	 * Tests if the line starts with a particular closing fence,
	 * ignoring whitespace, comments, and lexicals.
	 * 
	 * @see #stripCommentsAndLayout(String)
	 */
	private boolean isCloseFenceLine(String line, String fence) {
		line = stripCommentsAndLayout(line);
		for (int i = 1; i <= maxCloseFenceLength && i <= line.length(); i++) {
			String closeFence = line.substring(0, i);
			if (closeFence.equals(fence)
					&& (!isIdentifier(closeFence) || i == line.length() || !isIdentifier(line.substring(i, i + 1))))
				return true;
		}
		return false;	
	}
	
	private boolean isIdentifier(String text) {
		if (syntax instanceof SyntaxProperties) {
			Matcher matcher = ((SyntaxProperties) syntax).getIdentifierLexical().matcher(text);
			return matcher.matches();
		} else {
			throw new NotImplementedException();
		}
	}
	
	private String getMatchingCloseFence(String text) {
		if (text.length() > maxOpenFenceLength)
			return null;
		for (String[] fencePair : allFences) {
			if (text.equals(fencePair[0]))
				return fencePair[1];
		}
		return null;
	}
	
	@SuppressWarnings("unused")
	private String getMatchingOpenFence(String text) {
		if (text.length() > maxCloseFenceLength)
			return null;
		for (String[] fencePair : allFences) {
			if (text.equals(fencePair[1]))
				return fencePair[0];
		}
		return null;
	}
	
	/**
	 * Dumb stripping of comments and layout, ignoring string literals and the like.
	 */
	private String stripCommentsAndLayout(String line) {
		final String singleLineCommentPrefix = syntax.getSingleLineCommentPrefix();
		if (singleLineCommentPrefix != null) {
			int lineCommentStart = line.indexOf(singleLineCommentPrefix);
			if (lineCommentStart != -1)
				line = line.substring(0, lineCommentStart);
		}
		// TODO: strip block comments
		return line.trim();
	}
	
	/**
	 * Determines if inserting the text, inserted at the given point,
	 * would be parsed as a lexical or comment.
	 * 
	 * Currently looks at the tokenkind at the given offset:
	 * string and layout tokens lead to a result of true.
	 */
	private boolean isParsedAsLexicalOrLayout(IDocument document, int offset, String text) throws BadLocationException {
		// TODO: better robustness of isParsedAsLexicalOrLayout if parsed AST is not up to date (like ContentProposer has)
		if (controller.getCurrentAst() == null)
			return false;
		ISimpleTerm node = (ISimpleTerm) controller.getSourcePositionLocator().findNode(controller.getCurrentAst(), offset);
		if (node == null)
			return false;
		ITokenizer tokens = getLeftToken(node).getTokenizer();
		
		for (int i = getLeftToken(node).getIndex(), max = getRightToken(node).getIndex(); i <= max; i++) {
			IToken token = tokens.getTokenAt(i);
			if (token.getStartOffset() <= offset && offset <= token.getEndOffset()) {
				switch (token.getKind()) {
					case TK_STRING:
						if (isSameLine(document, offset, token))
							return true;
						continue;
					case TK_LAYOUT:
						if (token.toString().trim().length() > 0 && isSameLine(document, offset, token))
							return true; // part of a comment
						continue;
					case TK_ERROR:
						// TODO: test if part of comment? can't test if part of string...
						// IRegion line = document.getLineInformationOfOffset(command.offset);
						// String lineString = document.get(line.getOffset(), line.getLength());
						// lineString = start + ContentProposer.COMPLETION_TOKEN + end
					case TK_IDENTIFIER:
						/* UNDONE: Detect string literals even if their lexical pattern uses sorts instead of char classes
						           (e.g., in the SDF syntax, strings are defined as "\"" StrChar* "\"")
						String tokenText = token.toString();
						if ((tokenText.startsWith("\"") && tokenText.endsWith("\""))
								|| (tokenText.startsWith("'") && tokenText.endsWith("'"))) {
							return true;
						} else {
							continue;
						}
						*/
						if (isSameLine(document, offset, token) && isIdentifier(token.toString())) // sanity check
							return true; // either a string or just not a keyword
						continue;
					case TK_NUMBER: case TK_OPERATOR:
					case TK_VAR: case TK_EOF: case TK_UNKNOWN: case TK_RESERVED:
					case TK_KEYWORD:
						continue;
					default:
						Environment.logException("Uknown token kind: " + token.getKind());
				}
			}
		}
		return false;
	}
	
	/**
	 * Sanity check: ensure token and cursor are on the same line.
	 */
	private static boolean isSameLine(IDocument document, int offset, IToken token) {
		try {
			return document.getLineOfOffset(offset) + 1 == token.getLine();
		} catch (BadLocationException e) {
			return false;
		}
	}
}
