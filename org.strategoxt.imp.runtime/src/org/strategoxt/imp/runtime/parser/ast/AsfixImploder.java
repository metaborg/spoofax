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

import org.strategoxt.imp.runtime.Debug;
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
	
	private static final Map<ATerm, AstNode> implodedCache =
		Collections.synchronizedMap(new WeakHashMap<ATerm, AstNode>());
	
	protected final AstNodeFactory factory = new AstNodeFactory();
	
	private final ProductionAttributeReader reader = new ProductionAttributeReader();
	
	private final TokenKindManager tokenManager;
	
	protected SGLRTokenizer tokenizer;
	
	/** Character offset for the current implosion. */ 
	protected int offset;
	
	protected boolean lexicalContext;
	
    public AsfixImploder(TokenKindManager tokenManager) {
		this.tokenManager = tokenManager;
	}
	
	public AstNode implode(ATerm asfix, SGLRTokenizer tokenizer) {
		this.tokenizer = tokenizer;
		
		// TODO: Return null if imploded tree has null constructor??
		
		AstNode result = implodedCache.get(asfix);
		if (result != null) return result;
		
		Debug.startTimer();

		if (!(asfix instanceof ATermAppl || ((ATermAppl) asfix).getName().equals("parsetree")))
			throw new IllegalArgumentException("Parse tree expected");
		
		assert offset == 0 && tokenizer.getStartOffset() == 0 : "Race condition in AsfixImploder";
		
		ATerm top = (ATerm) asfix.getChildAt(PARSE_TREE);
		offset = 0;
		lexicalContext = false;
		
		try {
			result = implodeAppl(top);
		} finally {
			tokenizer.endStream();
			offset = 0;
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
		
		IToken prevToken = tokenizer.currentToken();
		
		// Enter lexical context if this is a lex node
		boolean lexicalStart = !lexicalContext
			&& ("lex".equals(rhs.getName()) || AsfixAnalyzer.isLiteral(rhs)
			    || AsfixAnalyzer.isLayout(rhs));
		
		if (lexicalStart) lexicalContext = true;
		
		if (!lexicalContext && "sort".equals(rhs.getName()) && lhs.getLength() == 1 && termAt(contents, 0).getType() == ATerm.INT) {
			return createIntTerminal(contents, rhs);
		}
		
		boolean isList = !lexicalContext && AsfixAnalyzer.isList(rhs);
		boolean isVar  = !lexicalContext && !isList && "varsym".equals(rhs.getName());
		
		if (isVar) lexicalContext = true;
		
		// Recurse the tree (and set children if applicable)
		ArrayList<AstNode> children =
			implodeChildNodes(contents);
		
		if (lexicalStart || isVar) {
			return createStringTerminal(lhs, rhs);
		} else if (lexicalContext) {
			return null; // don't create tokens inside lexical context; just create one big token at the top
		} else {
			return createNonTerminalOrInjection(lhs, rhs, attrs, prevToken, children, isList);
		}
	}

	protected ArrayList<AstNode> implodeChildNodes(ATermList contents) {
		ArrayList<AstNode> results = lexicalContext
				? null
				: new ArrayList<AstNode>(
						min(EXPECTED_NODE_CHILDREN, contents.getChildCount()));

		for (int i = 0; i < contents.getLength(); i++) {
			ATerm child = contents.elementAt(i);

			if (isInt(child)) {
				implodeLexical((ATermInt) child);
			} else {
				// Recurse
				AstNode childNode = implodeAppl(child);

				if (childNode != null)
					results.add(childNode);
			}
		}

		return results;
	}

	private StringAstNode createStringTerminal(ATermList lhs, ATermAppl rhs) {
		lexicalContext = false;
		IToken token = tokenizer.makeToken(offset, tokenManager.getTokenKind(lhs, rhs), true);
		String sort = reader.getSort(rhs);
		
		if (sort == null) return null;
		
		//Debug.log("Creating node ", sort, " from ", SGLRTokenizer.dumpToString(token));	
		
		return factory.createStringTerminal(sort, token);
	}
	
	private IntAstNode createIntTerminal(ATermList contents, ATermAppl rhs) {
		IToken token = tokenizer.makeToken(offset, tokenManager.getTokenKind(contents, rhs), true);
		String sort = reader.getSort(rhs);
		int value = intAt(contents, 0);
		return factory.createIntTerminal(sort, token, value);
	}

	private AstNode createNonTerminalOrInjection(ATermList lhs, ATermAppl rhs, ATermAppl attrs,
			IToken prevToken, ArrayList<AstNode> children, boolean isList) {
		
		String constructor = reader.getConsAttribute(attrs);
		String sort = reader.getSort(rhs);
		
		if(constructor == null) {
			if (isList) {
				return createNonTerminal(sort, null, prevToken, children, true);
			}
			
			ATerm ast = reader.getAstAttribute(attrs);
			if (ast != null) {
				return createAstNonTerminal(rhs, prevToken, children, ast);
			} else if (children.size() == 0) {
				return createNonTerminal(sort, "None", prevToken, children, false);
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
			return createNonTerminal(sort, constructor, prevToken, children, isList);
		}
	}

	/** Implode a context-free node. */
	private AstNode createNonTerminal(String sort, String constructor, IToken prevToken,
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
			return factory.createStringTerminal(sort, left);
		} else {
			return factory.createNonTerminal(sort, constructor, left, right, children);
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
		boolean multipleNonAvoids = false;
		
	alts:
		for (int i = 0; i < ambs.getLength(); i++) {
			ATermAppl prod = resolveAmbiguities(termAt(ambs, i));
			ATermAppl appl = termAt(prod, APPL_PROD);
			ATermAppl attrs = termAt(appl, PROD_ATTRS);
			
			if ("attrs".equals(attrs.getName())) {
				ATermList attrList = termAt(attrs, 0);
				
				for (int j = 0; j < attrList.getLength(); j++) {
					ATerm attr = termAt(attrList, j);
					if (isAppl(attr) && "prefer".equals(asAppl(attr).getName())) {
						return resolveAmbiguities(prod);
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
			return lastNonAvoid != null ? lastNonAvoid : applAt(ambs, 0);
		} else {
			if (Debug.ENABLED && !lexicalContext) reportUnresolvedAmb(ambs);
			return resolveAmbiguities(ambs.getFirst());
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
	
	/** Implode any appl(_, _) that constructs a lex terminal. */
	protected void implodeLexical(ATermInt character) {
		assert tokenizer.getLexStream().getInputChars().length > offset
		    && character.getInt() == tokenizer.getLexStream().getCharValue(offset)
			: "Character from asfix stream (" + character.getInt()
			+ ") must be in lex stream ("
			+ (tokenizer.getLexStream().getInputChars().length > offset 
			   ? "???"
			   : (int) tokenizer.getLexStream().getCharValue(offset)) + ")";
		
		offset++;
	}
}
