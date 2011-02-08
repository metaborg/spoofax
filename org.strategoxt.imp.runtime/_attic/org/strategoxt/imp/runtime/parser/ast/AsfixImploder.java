package org.strategoxt.imp.runtime.parser.ast;

import static java.lang.Math.max;
import static org.spoofax.terms.Term.applAt;
import static org.spoofax.terms.Term.asAppl;
import static org.spoofax.terms.Term.intAt;
import static org.spoofax.terms.Term.isTermAppl;
import static org.spoofax.terms.Term.isInt;
import static org.spoofax.terms.Term.termAt;
import static org.strategoxt.imp.runtime.parser.ast.AsfixAnalyzer.*;
import static org.strategoxt.imp.runtime.parser.ast.ProductionAttributeReader.*;
import static org.strategoxt.imp.runtime.parser.tokens.TokenKind.TK_ERROR;
import static org.strategoxt.imp.runtime.parser.tokens.TokenKind.TK_LAYOUT;
import static org.strategoxt.imp.runtime.parser.tokens.TokenKindManager.isKeywordChar;

import java.util.ArrayList;

import org.spoofax.jsglr.client.imploder.IToken;
import lpg.runtime.PrsStream;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.RecoveryConnector;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.ParseErrorHandler;
import org.strategoxt.imp.runtime.parser.tokens.KeywordRecognizer;
import org.strategoxt.imp.runtime.parser.tokens.SGLRTokenizer;
import org.strategoxt.imp.runtime.parser.tokens.TokenKindManager;

import org.spoofax.interpreter.terms.IStrategoTerm;
import aterm.ATermAppl;
import aterm.ATermInt;
import org.spoofax.interpreter.terms.IStrategoList;
import aterm.pure.IStrategoList;

/**
 * Implodes an Asfix tree to IStrategoTerm nodes and IToken tokens.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class AsfixImploder {

	private static final int EXPECTED_NODE_CHILDREN = 5;
	
	protected static final int PARSE_TREE = 0;
	
	protected static final int APPL_PROD = 0;
	
	protected static final int APPL_CONTENTS = 1;

	protected static final int PROD_LHS = 0;
	
	protected static final int PROD_RHS = 1;

	protected static final int PROD_ATTRS = 2;
	
	private static final int NONE = -1;
	
	protected final AstNodeFactory factory = new AstNodeFactory();
	
	private final ProductionAttributeReader reader = new ProductionAttributeReader();
	
	private final TokenKindManager tokenManager;
	
	protected SGLRTokenizer tokenizer;
	
	/** Character offset for the current implosion. */ 
	protected int offset;
	
	private int nonMatchingOffset = NONE;
	
	private char nonMatchingChar, nonMatchingCharExpected, prevChar;
	
	protected boolean inLexicalContext;
	
    public AsfixImploder(TokenKindManager tokenManager) {
		this.tokenManager = tokenManager;
	}
	
	public IStrategoTerm implode(IStrategoTerm asfix, SGLRTokenizer tokenizer) {
		this.tokenizer = tokenizer;
		
		// FIXME: static fields are used in helper classes that refer to Environment.getATermFactory()
		if (asfix.getFactory() != Environment.getATermFactory())
			throw new IllegalArgumentException("Argument IStrategoTerm does not use the Environment.getATermFactory() factory");
		
		// TODO: Return null if imploded tree has null constructor??
		
		if (tokenizer.getCachedAst() != null)
			return tokenizer.getCachedAst();
		
		Debug.startTimer();

		if (!(asfix instanceof ATermAppl || ((ATermAppl) asfix).getName().equals("parsetree")))
			throw new IllegalArgumentException("Parse tree expected");
		
		if (offset != 0 || tokenizer.getStartOffset() != 0)
			throw new IllegalStateException("Race condition in AsfixImploder (" + tokenizer.getLexStream().getFileName() + "; might be caused by stack overflow)");
		
		IStrategoTerm top = (IStrategoTerm) asfix.getChildAt(PARSE_TREE);
		IStrategoTerm result;
		offset = 0;
		inLexicalContext = false;
		
		try {
			result = implodeAppl(top);
		} finally {
			tokenizer.endStream();
			offset = 0;
			nonMatchingOffset = NONE;
		}
		
		if (Debug.ENABLED) {
			Debug.stopTimer("Parse tree imploded");
			// Disabled; printing big trees causes delays
			// Debug.log("Parsed " + result.toString());
		}
		
		tokenizer.setCachedAst(result);
		return result;
	}
	
	/**
	 * Implode any appl(_, _).
	 */
	protected IStrategoTerm implodeAppl(IStrategoTerm term) {
		// Note that this method significantly impacts our stack usage;
		// method extraction should be carefully considered...
		
		ATermAppl appl = resolveAmbiguities(term);
		if (AMB_FUN == appl.getIStrategoConstructor())
			return implodeAmbAppl(appl);
		
		ATermAppl prod = termAt(appl, APPL_PROD);
		IStrategoList lhs = termAt(prod, PROD_LHS);
		ATermAppl rhs = termAt(prod, PROD_RHS);
		ATermAppl attrs = termAt(prod, PROD_ATTRS);
		IStrategoList contents = termAt(appl, APPL_CONTENTS);
		IToken prevToken = tokenizer.currentToken();
		int lastOffset = offset;
		
		// Enter lexical context if this is a lex node
		boolean lexicalStart = !inLexicalContext && AsfixAnalyzer.isLexicalNode(rhs);
		
		if (lexicalStart) inLexicalContext = true;
		
		if (!inLexicalContext
				&& (SORT_FUN == rhs.getIStrategoConstructor() || PARAMETERIZED_SORT_FUN == rhs.getIStrategoConstructor())
				&& lhs.getLength() == 1 && termAt(contents, 0).getType() == IStrategoTerm.INT) {
			return setAnnos(createIntTerminal(contents, rhs), appl.getAnnotations());
		}
		
		boolean isList = !inLexicalContext && AsfixAnalyzer.isList(rhs);
		boolean isVar  = !inLexicalContext && !isList && AsfixAnalyzer.isVariableNode(rhs);
		
		if (isVar) inLexicalContext = true;
		
		ArrayList<IStrategoTerm> children = null;
		if (!inLexicalContext)
			children = new ArrayList<IStrategoTerm>(max(EXPECTED_NODE_CHILDREN, contents.getChildCount()));

		// Recurse
		for (int i = 0; i < contents.getLength(); i++) {
			IStrategoTerm child = contents.elementAt(i);
			if (isInt(child)) {
				consumeLexicalChar((ATermInt) child);
			} else {
				IStrategoTerm childNode = implodeAppl(child);
				if (childNode != null) children.add(childNode);
			}
		}
		
		if (lexicalStart || isVar) {
			return setAnnos(createStringTerminal(lhs, rhs, attrs), appl.getAnnotations());
		} else if (inLexicalContext) {
			createLayoutToken(rhs, lastOffset);
			return null; // don't create tokens inside lexical context; just create one big token at the top
		} else {
			return setAnnos(createNodeOrInjection(lhs, rhs, attrs, prevToken, children, isList), appl.getAnnotations());
		}
	}
	
	protected AmbAstNode implodeAmbAppl(ATermAppl node) { 
		final IStrategoList ambs = termAt(node, 0);
		final ArrayList<IStrategoTerm> results = new ArrayList<IStrategoTerm>();
		
		final int oldOffset = offset;
		final int oldBeginOffset = tokenizer.getStartOffset();
		final boolean oldLexicalContext = inLexicalContext;
		
		for (IStrategoTerm amb : ambs) {
			// Restore lexical state for each branch
			offset = oldOffset;
			tokenizer.setStartOffset(oldBeginOffset);
			inLexicalContext = oldLexicalContext;
			
			IStrategoTerm result = implodeAppl(amb);
			if (result == null)
				return null;
			results.add(result);
		}
		
		return new AmbAstNode(results);
	}
	
	private IStrategoTerm setAnnos(IStrategoTerm node, IStrategoList annos) {
		if (node != null && annos != null && !annos.isEmpty()) {
			IStrategoTerm termAnnos = Environment.getATermConverter().convert(annos);
			node.setAnnotations((IStrategoList) termAnnos);
		}
		return node;
	}

	private IStrategoTerm createStringTerminal(IStrategoList lhs, ATermAppl rhs, ATermAppl attrs) {
		inLexicalContext = false;
		String sort = reader.getSort(rhs);
		IToken token = tokenizer.makeToken(offset, tokenManager.getTokenKind(lhs, rhs), sort != null);
		
		if (sort == null) return null;
		
		// Debug.log("Creating node ", sort, " from ", SGLRTokenizer.dumpToString(token));
		
		IStrategoTerm result = factory.createStringTerminal(sort, getPaddedLexicalValue(attrs, token), token);
		String constructor = reader.getMetaVarConstructor(rhs);
		if (constructor != null) {
			ArrayList<IStrategoTerm> children = new ArrayList<IStrategoTerm>(1);
			children.add(result);
			result = factory.createNonTerminal(sort, constructor, token, token, children);
		}
		return result;
	}
	
	private IntAstNode createIntTerminal(IStrategoList contents, ATermAppl rhs) {
		IToken token = tokenizer.makeToken(offset, tokenManager.getTokenKind(contents, rhs), true);
		String sort = reader.getSort(rhs);
		int value = intAt(contents, 0);
		return factory.createIntTerminal(sort, token, value);
	}

	private IStrategoTerm createNodeOrInjection(IStrategoList lhs, ATermAppl rhs, ATermAppl attrs,
			IToken prevToken, ArrayList<IStrategoTerm> children, boolean isList) {
		
		String constructor = reader.getConsAttribute(attrs);
		String sort = reader.getSort(rhs);
		
		if(constructor == null) {
			if (isList) {
				return createNode(attrs, sort, null, prevToken, children, true, false);
			}
			
			IStrategoTerm ast = reader.getAstAttribute(attrs);
			if (ast != null) {
				return createAstNonTerminal(rhs, prevToken, children, ast);
			} else if (OPT_FUN == applAt(rhs, 0).getIStrategoConstructor()) {
				if (children.size() == 0) {
					return createNode(attrs, sort, "None", prevToken, children, false, false);
				} else {
					assert children.size() == 1;
					return createNode(attrs, sort, "Some", prevToken, children, false, false);
				}
			} else if (children.size() == 1) {
				// Injection
				// TODO: efficiently store injection sort for use by content completion?
				//       would be needed to distinguish FoldingRules and Sorts in "folding" sections...
				//       maybe only if the content proposer demands it?
				// TODO: also, avoid semantics for deprecated?
				return children.get(0);
			} else {
				// Constructor-less application (tuple)
				return createNode(attrs, sort, TupleAstNode.CONSTRUCTOR, prevToken, children, false, true);
			}
		} else {
			tokenizer.makeToken(offset, tokenManager.getTokenKind(lhs, rhs));
			return createNode(attrs, sort, constructor, prevToken, children, isList, false);
		}
	}

	/** Implode a context-free node. */
	private IStrategoTerm createNode(ATermAppl attrs, String sort, String constructor, IToken prevToken,
			ArrayList<IStrategoTerm> children, boolean isList, boolean isTuple) {
		
		IToken left = getStartToken(prevToken);
		IToken right = getEndToken(left, tokenizer.currentToken());
		
		/*
		if (Debug.ENABLED) {
			String name = isList ? "list" : sort;
			Debug.log("Creating node ", name, ":", constructor, IStrategoTerm.getSorts(children), " from ", SGLRTokenizer.dumpToString(left, right));
		}
		*/
		
		if (isList) {
			return factory.createList(sort, left, right, children);
		} else if (constructor == null && children.size() == 1 && children.get(0).getSort() == IStrategoTerm.STRING_SORT) {
			// Child node was a <string> node (rare case); unpack it and create a new terminal
			assert left == right && children.get(0).getSubtermCount() == 0;
			return factory.createStringTerminal(sort, getPaddedLexicalValue(attrs, left), left);
		} else {
			return factory.createNonTerminal(sort, constructor, left, right, children);
		}
	}
	
	/**
	 * Gets the padded lexical value for {indentpadding} lexicals, or returns null.
	 */
	private String getPaddedLexicalValue(ATermAppl attrs, IToken startToken) {
		if (reader.isIndentPaddingLexical(attrs)) {
			char[] inputChars = tokenizer.getLexStream().getInputChars();
			int lineStart = startToken.getStartOffset() - 1;
			if (lineStart < 0) return null;
			while (lineStart >= 0) {
				char c = inputChars[lineStart--];
				if (c == '\n' || c == '\r') {
					lineStart++;
					break;
				}
			}
			StringBuilder result = new StringBuilder();
			result.append(inputChars, lineStart, startToken.getStartOffset() - lineStart - 1);
			for (int i = 0; i < result.length(); i++) {
				char c = result.charAt(i);
				if (c != ' ' && c != '\t') result.setCharAt(i, ' ');
			}
			result.append(startToken.toString());
			return result.toString();
		} else {
			return null; // lazily load token string value
		}
	}

	/** Implode a context-free node with an {ast} annotation. */
	private IStrategoTerm createAstNonTerminal(ATermAppl rhs, IToken prevToken, ArrayList<IStrategoTerm> children, IStrategoTerm ast) {
		IToken left = getStartToken(prevToken);
		IToken right = getEndToken(left, tokenizer.currentToken());
		AstAnnoImploder imploder = new AstAnnoImploder(factory, children, left, right);
		return imploder.implode(ast, reader.getSort(rhs));
	}
	
	/**
	 * Resolve or ignore any ambiguities in the parse tree.
	 */
	protected ATermAppl resolveAmbiguities(final IStrategoTerm node) {
		if (AMB_FUN != ((ATermAppl) node).getIStrategoConstructor())
			return (ATermAppl) node;
		
		final IStrategoList ambs = termAt(node, 0);
		
		ATermAppl lastNonAvoid = null;
		ATermAppl firstOption = null;
		boolean multipleNonAvoids = false;
		
	alts:
		for (int i = 0; i < ambs.getLength(); i++) {
			ATermAppl prod = resolveAmbiguities(termAt(ambs, i));
			if (firstOption == null) firstOption = prod;
			ATermAppl appl = termAt(prod, APPL_PROD);
			ATermAppl attrs = termAt(appl, PROD_ATTRS);
			
			if (ATTRS_FUN == attrs.getIStrategoConstructor()) {
				IStrategoList attrList = termAt(attrs, 0);
				
				for (int j = 0; j < attrList.getLength(); j++) {
					IStrategoTerm attr = termAt(attrList, j);
					if (isTermAppl(attr) && PREFER_FUN == asAppl(attr).getIStrategoConstructor()) {
						return prod;
					} else if (isTermAppl(attr) && AVOID_FUN == asAppl(attr).getIStrategoConstructor()) {
						continue alts;
					}
				}
				
				if (lastNonAvoid == null) {
					lastNonAvoid = prod;
				} else {
					multipleNonAvoids = true;
				}
			}
		}
		
		if (!multipleNonAvoids) {
			return lastNonAvoid != null ? lastNonAvoid : firstOption;
		} else {
			if (Debug.ENABLED && !inLexicalContext) reportUnresolvedAmb(ambs);
			return firstOption;
		}
	}
	
	private static void reportUnresolvedAmb(IStrategoList ambs) {
		Debug.log("Ambiguity found during implosion: ");
		
		for (IStrategoTerm amb : ambs) {
			String ambString = amb.toString();
			if (ambString.length() > 1000) ambString = ambString.substring(0, 1000) + "...";
			Debug.log("  amb: ", ambString);
		}
	}
	
	/** Get the token after the previous node's ending token, or null if N/A. */
	private IToken getStartToken(IToken prevToken) {
		PrsStream parseStream = tokenizer.getParseStream();
		
		if (prevToken == null) {
			return parseStream.getSize() == 0 ? null
			                                  : parseStream.getTokenAt(0);
		} else {
			int index = prevToken.getIndex();
			
			if (parseStream.getSize() - index <= 1) {
				// Create new empty token
				// HACK: Assume TK_LAYOUT kind for empty tokens in AST nodes
				return tokenizer.makeToken(offset, TK_LAYOUT, true);
			} else {
				return parseStream.getTokenAt(index + 1); 
			}
		}
	}
	
	/** Get the last no-layout token for an AST node. */
	private IToken getEndToken(IToken startToken, IToken lastToken) {
		PrsStream parseStream = tokenizer.getParseStream();
		int begin = startToken.getIndex();
		
		for (int i = lastToken.getIndex(); i > begin; i--) {
			lastToken = parseStream.getTokenAt(i);
			if (lastToken.getKind() != TK_LAYOUT
					|| lastToken.getStartOffset() == lastToken.getEndOffset()-1)
				break;
		}
		
		return lastToken;
	}
	
	/** Consume a character of a lexical terminal. */
	protected final void consumeLexicalChar(ATermInt character) {
		char[] inputChars = tokenizer.getLexStream().getInputChars();
		if (offset >= inputChars.length) {
			if (nonMatchingOffset != NONE) {
				Environment.logException(new ImploderException("Character in parse tree after end of input stream: "
						+ (char) character.getInt()
						+ " - may be caused by unexcepted character in parse tree at position "
						+ nonMatchingChar 	+ ": " + nonMatchingChar + " instead of "
						+ nonMatchingCharExpected));
			}
		    // UNDONE: Strict lexical stream checking
			// throw new ImploderException("Character in parse tree after end of input stream: " + (char) character.getInt());
			// a forced reduction may have added some extra characters to the tree;
			inputChars[inputChars.length - 1] = ParseErrorHandler.UNEXPECTED_EOF_CHAR;
			return;
		}
		
		char parsedChar = (char) character.getInt();
		char inputChar = inputChars[offset];
		
		if (parsedChar != inputChar) {
			if (RecoveryConnector.isLayoutCharacter(parsedChar)) {
				// Remember that the parser skipped the current character
				// for later error reporting. (Cannot modify the immutable
				// parse tree here; changing the original stream instead.)
				inputChars[offset] = ParseErrorHandler.SKIPPED_CHAR;
				createSkippedToken(inputChars, inputChar);
				offset++;
			} else {
				// UNDONE: Strict lexical stream checking
				// throw new IllegalStateException("Character from asfix stream (" + parsedChar
				//	 	+ ") must be in lex stream (" + inputChar + ")");
			    // instead, we allow the non-matching character for now, and hope
			    // we can pick up the right track later
				// TODO: better way to report skipped fragments in the parser
				//       this isn't 100% reliable
				if (nonMatchingOffset == NONE) {
					nonMatchingOffset = offset;
					nonMatchingChar = parsedChar;
					nonMatchingCharExpected = inputChar;
				}
				inputChars[offset] = ParseErrorHandler.SKIPPED_CHAR;
			}
		} else {
			offset++;
		}
		prevChar = inputChar;
	}

	/**
	 * Creates an artificial token at keyword boundaries
	 * inside skipped regions of code.
	 * Required for keyword highlighting with {@link KeywordRecognizer}.
	 */
	private void createSkippedToken(char[] inputChars, char inputChar) {
		boolean isInputKeywordChar = isKeywordChar(inputChar);
		if (offset > 0 && offset - 1 > tokenizer.getStartOffset()) {
			if ((isInputKeywordChar && !isKeywordChar(prevChar))
					|| (!isInputKeywordChar && isKeywordChar(prevChar))) {
				tokenizer.makeToken(offset - 1, TK_ERROR, false);
			}
		}
		if (offset + 1 < inputChars.length) {
			char nextChar = inputChars[offset + 1];
			if ((isInputKeywordChar && !isKeywordChar(nextChar))
					|| (!isInputKeywordChar && isKeywordChar(nextChar))) {
				tokenizer.makeToken(offset + 1, TK_ERROR, false);
			}
		}
	}

	/**
	 * Creates an artificial token for every water-based recovery
	 * and for comments within layout.
	 */
	private void createLayoutToken(ATermAppl rhs, int lastOffset) {
		// Create separate tokens for >1 char layout lexicals (e.g., comments)
		if (offset > lastOffset + 1 && AsfixAnalyzer.isLexLayout(rhs)) {
			if (tokenizer.getStartOffset() <= lastOffset)
				tokenizer.makeToken(lastOffset, TK_LAYOUT, false);
			tokenizer.makeToken(offset, TK_LAYOUT, false);
		} else {
			String sort = reader.getSort(rhs);
			if ("WATERTOKEN".equals(sort) || "WATERTOKENSEPARATOR".equals(sort)) {
				if (tokenizer.getStartOffset() <= lastOffset)
					tokenizer.makeToken(lastOffset, TK_LAYOUT, false);
				tokenizer.makeToken(offset, TK_ERROR, false);
			}
		}
	}
}
