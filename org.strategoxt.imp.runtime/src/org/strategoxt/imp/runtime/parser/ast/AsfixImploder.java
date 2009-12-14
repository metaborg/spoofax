package org.strategoxt.imp.runtime.parser.ast;

import static java.lang.Math.*;
import static org.spoofax.jsglr.Term.*;
import static org.strategoxt.imp.runtime.parser.tokens.TokenKind.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import lpg.runtime.IToken;
import lpg.runtime.PrsStream;

import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.TermConverter;
import org.spoofax.jsglr.RecoveryConnector;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.ParseErrorHandler;
import org.strategoxt.imp.runtime.parser.tokens.SGLRToken;
import org.strategoxt.imp.runtime.parser.tokens.SGLRTokenizer;
import org.strategoxt.imp.runtime.parser.tokens.TokenKindManager;

import aterm.ATerm;
import aterm.ATermAppl;
import aterm.ATermInt;
import aterm.ATermList;
import aterm.pure.ATermListImpl;

/**
 * Implodes an Asfix tree to AstNode nodes and IToken tokens.
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
	
	private static final Map<ATerm, AstNode> implodedCache =
		Collections.synchronizedMap(new WeakHashMap<ATerm, AstNode>());
	
	protected final AstNodeFactory factory = new AstNodeFactory();
	
	private final ProductionAttributeReader reader = new ProductionAttributeReader();
	
	private final TermConverter converter = new TermConverter(Environment.getTermFactory());
	
	private final TokenKindManager tokenManager;
	
	protected SGLRTokenizer tokenizer;
	
	/** Character offset for the current implosion. */ 
	protected int offset;
	
	private int nonMatchingOffset = NONE;
	
	private char nonMatchingChar, nonMatchingCharExpected;
	
	protected boolean inLexicalContext;
	
    public AsfixImploder(TokenKindManager tokenManager) {
		this.tokenManager = tokenManager;
	}
	
	public AstNode implode(ATerm asfix, SGLRTokenizer tokenizer) {
		this.tokenizer = tokenizer;
		
		// TODO: Return null if imploded tree has null constructor??
		
		AstNode result = implodedCache.get(asfix);
		if (result != null && tokenizer.getStartOffset() != 0) // HACK: tokenizer is sometimes cached empty?
			return result;
		
		Debug.startTimer();

		if (!(asfix instanceof ATermAppl || ((ATermAppl) asfix).getName().equals("parsetree")))
			throw new IllegalArgumentException("Parse tree expected");
		
		if (offset != 0 || tokenizer.getStartOffset() != 0)
			throw new IllegalStateException("Race condition in AsfixImploder");
		
		ATerm top = (ATerm) asfix.getChildAt(PARSE_TREE);
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
			Debug.log("Parsed " + result.toString());
		}
		
		implodedCache.put(asfix, result);
		assert implodedCache.get(asfix) == result;

		return result;
	}
	
	/**
	 * Implode any appl(_, _).
	 */
	protected AstNode implodeAppl(ATerm term) {
		ATermAppl appl = resolveAmbiguities(term);
		ATermAppl prod = termAt(appl, APPL_PROD);
		ATermList lhs = termAt(prod, PROD_LHS);
		ATermAppl rhs = termAt(prod, PROD_RHS);
		ATermAppl attrs = termAt(prod, PROD_ATTRS);
		ATermList contents = termAt(appl, APPL_CONTENTS);
		ATermList annos = appl.getAnnotations();
		// TODO: use annos as annotations for resulting terms
		
		IToken prevToken = tokenizer.currentToken();
		
		// Enter lexical context if this is a lex node
		boolean lexicalStart = !inLexicalContext && AsfixAnalyzer.isLexicalNode(rhs);
		
		if (lexicalStart) inLexicalContext = true;
		
		if (!inLexicalContext && "sort".equals(rhs.getName()) && lhs.getLength() == 1 && termAt(contents, 0).getType() == ATerm.INT) {
			return setAnnos(createIntTerminal(contents, rhs), annos);
		}
		
		boolean isList = !inLexicalContext && AsfixAnalyzer.isList(rhs);
		boolean isVar  = !inLexicalContext && !isList && AsfixAnalyzer.isVariableNode(rhs);
		
		if (isVar) inLexicalContext = true;
		
		// Recurse the tree (and set children if applicable)
		ArrayList<AstNode> children =
			implodeChildNodes(contents);
		
		if (lexicalStart || isVar) {
			return setAnnos(createStringTerminal(lhs, rhs, attrs), annos);
		} else if (inLexicalContext) {
			return null; // don't create tokens inside lexical context; just create one big token at the top
		} else {
			return setAnnos(createNodeOrInjection(lhs, rhs, attrs, prevToken, children, isList), annos);
		}
	}
	
	private AstNode setAnnos(AstNode node, ATermList annos) {
		if (node != null && annos != null && !annos.isEmpty()) {
			IStrategoTerm termAnnos = converter.convert(Environment.getWrappedATermFactory().wrapTerm(annos));
			node.setAnnotations((IStrategoList) termAnnos);
		}
		return node;
	}

	protected ArrayList<AstNode> implodeChildNodes(ATermList contents) {
		ArrayList<AstNode> results = inLexicalContext
				? null
				: new ArrayList<AstNode>(
						min(EXPECTED_NODE_CHILDREN, contents.getChildCount()));

		for (int i = 0; i < contents.getLength(); i++) {
			ATerm child = contents.elementAt(i);

			if (isInt(child)) {
				consumeLexicalChar((ATermInt) child);
			} else {
				// Recurse
				AstNode childNode = implodeAppl(child);

				if (childNode != null)
					results.add(childNode);
			}
		}

		return results;
	}

	private StringAstNode createStringTerminal(ATermList lhs, ATermAppl rhs, ATermAppl attrs) {
		inLexicalContext = false;
		IToken token = tokenizer.makeToken(offset, tokenManager.getTokenKind(lhs, rhs), true);
		String sort = reader.getSort(rhs);
		
		if (sort == null) return null;
		
		// Debug.log("Creating node ", sort, " from ", SGLRTokenizer.dumpToString(token));
		
		return factory.createStringTerminal(getPaddedLexicalValue(attrs, token), sort, token);
	}
	
	private IntAstNode createIntTerminal(ATermList contents, ATermAppl rhs) {
		IToken token = tokenizer.makeToken(offset, tokenManager.getTokenKind(contents, rhs), true);
		String sort = reader.getSort(rhs);
		int value = intAt(contents, 0);
		return factory.createIntTerminal(sort, token, value);
	}

	private AstNode createNodeOrInjection(ATermList lhs, ATermAppl rhs, ATermAppl attrs,
			IToken prevToken, ArrayList<AstNode> children, boolean isList) {
		
		String constructor = reader.getConsAttribute(attrs);
		String sort = reader.getSort(rhs);
		
		if(constructor == null) {
			if (isList) {
				return createNode(attrs, sort, null, prevToken, children, true);
			}
			
			ATerm ast = reader.getAstAttribute(attrs);
			if (ast != null) {
				return createAstNonTerminal(rhs, prevToken, children, ast);
			} else if (children.size() == 0) {
				return createNode(attrs, sort, "None", prevToken, children, false);
			} else if ("opt".equals(applAt(rhs, 0).getName())) {
				assert children.size() == 1;
				AstNode child = children.get(0);
				return new AstNode(sort, child.getLeftIToken(), child.getRightIToken(), "Some", children);
			} else {
				// Injection
				assert children.size() == 1;
				return children.get(0);
			}
		} else {
			tokenizer.makeToken(offset, tokenManager.getTokenKind(lhs, rhs));
			return createNode(attrs, sort, constructor, prevToken, children, isList);
		}
	}

	/** Implode a context-free node. */
	private AstNode createNode(ATermAppl attrs, String sort, String constructor, IToken prevToken,
			ArrayList<AstNode> children, boolean isList) {
		
		IToken left = getStartToken(prevToken);
		IToken right = getEndToken(left, tokenizer.currentToken());
		
		/*
		if (Debug.ENABLED) {
			String name = isList ? "list" : sort;
			Debug.log("Creating node ", name, ":", constructor, AstNode.getSorts(children), " from ", SGLRTokenizer.dumpToString(left, right));
		}
		*/
		
		if (isList) {
			return factory.createList(sort, left, right, children);
		} else if (constructor == null && children.size() == 1 && children.get(0).getSort() == AstNode.STRING_SORT) {
			// Child node was a <string> node (rare case); unpack it and create a new terminal
			assert left == right && children.get(0).getChildren().size() == 0;
			return factory.createStringTerminal(getPaddedLexicalValue(attrs, left), sort, left);
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
			result.append(SGLRToken.toString(startToken, startToken));
			return result.toString();
		} else {
			return null; // lazily load token string value
		}
	}

	/** Implode a context-free node with an {ast} annotation. */
	private AstNode createAstNonTerminal(ATermAppl rhs, IToken prevToken, ArrayList<AstNode> children, ATerm ast) {
		IToken left = getStartToken(prevToken);
		IToken right = getEndToken(left, tokenizer.currentToken());
		AstAnnoImploder imploder = new AstAnnoImploder(factory, children, left, right);
		return imploder.implode(ast, reader.getSort(rhs));
	}
	
	/**
	 * Resolve or ignore any ambiguities in the parse tree.
	 */
	protected ATermAppl resolveAmbiguities(final ATerm node) {
		if (!"amb".equals(((ATermAppl) node).getName()))
			return (ATermAppl) node;
		
		final ATermListImpl ambs = termAt(node, 0);
		
		ATermAppl lastNonAvoid = null;
		ATermAppl firstOption = null;
		boolean multipleNonAvoids = false;
		
	alts:
		for (int i = 0; i < ambs.getLength(); i++) {
			ATermAppl prod = resolveAmbiguities(termAt(ambs, i));
			if (firstOption == null) firstOption = prod;
			ATermAppl appl = termAt(prod, APPL_PROD);
			ATermAppl attrs = termAt(appl, PROD_ATTRS);
			
			if ("attrs".equals(attrs.getName())) {
				ATermList attrList = termAt(attrs, 0);
				
				for (int j = 0; j < attrList.getLength(); j++) {
					ATerm attr = termAt(attrList, j);
					if (isAppl(attr) && "prefer".equals(asAppl(attr).getName())) {
						return prod;
					} else if (isAppl(attr) && "avoid".equals(asAppl(attr).getName())) {
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
	
	private static void reportUnresolvedAmb(ATermList ambs) {
		Debug.log("Ambiguity found during implosion: ");
		
		for (ATerm amb : ambs) {
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
			int index = prevToken.getTokenIndex();
			
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
		int begin = startToken.getTokenIndex();
		
		for (int i = lastToken.getTokenIndex(); i > begin; i--) {
			lastToken = parseStream.getTokenAt(i);
			if (lastToken.getKind() != TK_LAYOUT.ordinal()
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
	}
}
