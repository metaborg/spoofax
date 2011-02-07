package org.strategoxt.imp.runtime.services;

import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getLeftToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getRightToken;
import static org.spoofax.terms.Term.isTermString;
import static org.spoofax.terms.Term.tryGetConstructor;
import static org.spoofax.terms.attachments.ParentAttachment.getParent;
import static org.spoofax.terms.attachments.ParentAttachment.getRoot;
import static org.strategoxt.imp.runtime.Environment.getTermFactory;
import static org.strategoxt.imp.runtime.services.ContentProposer.COMPLETION_TOKEN;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.imp.parser.IParseController;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.shared.SGLRException;
import org.spoofax.terms.TermTransformer;
import org.spoofax.terms.TermVisitor;
import org.spoofax.terms.attachments.ParentTermFactory;
import org.strategoxt.imp.runtime.Debug;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.DynamicParseController;
import org.strategoxt.imp.runtime.parser.JSGLRI;
import org.strategoxt.imp.runtime.parser.SGLRParseController;

/**
 * Content completion parsing and tree construction
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class ContentProposerParser {
	
	protected static final IStrategoConstructor COMPLETION_CONSTRUCTOR =
		getTermFactory().makeConstructor("COMPLETION", 1);
	
	protected static final IStrategoConstructor COMPLETION_UNKNOWN =
		getTermFactory().makeConstructor("NOCONTEXT", 1);

	private static final long REINIT_PARSE_DELAY = 4000;
	
	private final Pattern identifierLexical;
	
	private SGLRParseController parser;
	
	private IStrategoTerm lastParserAst;
	
	private IStrategoTerm lastCompletionAst;
	
	private IStrategoTerm lastCompletionNode;
	
	private String lastCompletionPrefix;
	
	private String lastDocument;
	
	private int lastOffset;
	
	private IStrategoTerm completionNode;
	
	private String completionPrefix;
	
	public ContentProposerParser(Pattern identifierLexical) {
		this.identifierLexical = identifierLexical;
	}
	
	protected SGLRParseController getParser() {
		return parser;
	}
	
	public IStrategoTerm getCompletionNode() {
		return completionNode;
	}
	
	public String getCompletionPrefix() {
		return completionPrefix;
	}
	
	public boolean isFatalSyntaxError() {
		return lastCompletionAst == null && lastParserAst == null && completionNode == null;
	}

	public IStrategoTerm parse(IParseController icontroller, int offset, String document, boolean avoidReparse) {
		lastCompletionNode = completionNode;
		lastCompletionPrefix = completionPrefix;
		completionNode = null;
		
		SGLRParseController controller = this.parser = getParser(icontroller);
		IStrategoTerm ast = tryReusePreviousAst(offset, document);
		if (ast != null) return ast;

		String documentWithToken = document.substring(0, offset) + COMPLETION_TOKEN + document.substring(offset);
		ast = parse(controller, offset, documentWithToken, avoidReparse);
		if (ast == null) return null;
		
		IStrategoTerm result = identifyCompletionNode(ast, COMPLETION_TOKEN);
		if (completionNode == null) result = addNoContextNode(ast, offset, document);
		return result;
	}

	private IStrategoTerm parse(SGLRParseController controller, int offset, String document, boolean avoidReparse) {
		JSGLRI parser = controller.getParser();
		IStrategoTerm result;
		
		controller.scheduleParserUpdate(REINIT_PARSE_DELAY, true); // cancel current parse
		Debug.startTimer();
		controller.getParseLock().lock();
		Debug.stopTimer("Completion acquired parse lock");
		try {
			if (avoidReparse && controller.getCurrentAst() != null) {
				// Don't reparse for keyword completion
				result = controller.getCurrentAst();
			} else {
				Debug.startTimer();
				try {
					result = parser.parse(document, controller.getPath().toPortableString());
				} finally {
					Debug.stopTimer("Completion parsed");
				}
				lastParserAst = controller.getCurrentAst();
				lastCompletionAst = result;
			}
		} catch (SGLRException e) {
			Environment.logException("Could not reparse input for content completion", e);
			result = forceUseOldAst(controller, offset, document);
		} catch (IOException e) {
			Environment.logException("Could not reparse input for content completion", e);
			result = forceUseOldAst(controller, offset, document);
		} finally {
			controller.getParseLock().unlock();
		}
		return result;
	}

	/**
	 * Reuse the previous AST if the user just added or deleted a single character.
	 */
	private IStrategoTerm tryReusePreviousAst(int offset, String document) {
		if (offset == 0) return null;
		if (lastCompletionNode != null && lastDocument.length() == document.length() - 1 && lastOffset == offset - 1) {
			// Reuse document, ignoring latest typed character
			String newCharacter = document.substring(offset - 1, offset);
			String previousDocument = lastDocument.substring(0, offset - 1) + newCharacter + lastDocument.substring(offset - 1);
			if (documentsSufficientlyEqual(document, previousDocument, offset)) {
				return reusePreviousAst(offset, document, lastCompletionPrefix + newCharacter);
			}
		} else if (lastCompletionNode != null && lastCompletionPrefix.length() > 0
				&& lastDocument.length() == document.length() + 1 && lastOffset == offset + 1) {
			// Reuse document, ignoring previously typed character
			String oldCharacter = lastDocument.substring(offset, offset + 1);
			String currentDocument = document.substring(0, offset) + oldCharacter + document.substring(offset);
			if (documentsSufficientlyEqual(currentDocument, lastDocument, offset + 1)) {
				return reusePreviousAst(offset, document, lastCompletionPrefix.substring(0, lastCompletionPrefix.length() - 1));
			}
		} else if (lastCompletionNode != null && lastDocument.equals(document) && offset == lastOffset) {
			return reusePreviousAst(offset, document, lastCompletionPrefix);
		}
		lastDocument = document;
		lastOffset = offset;
		return null;
	}
	
	/**
	 * @return Whether doc1 and doc2 are equal disregarding the last
	 * identifierLexical immediately before offset. If there is no
	 * identifierLexical at that place in either document, false is returned.
	 */
	private boolean documentsSufficientlyEqual(String doc1, String doc2, int offset) {
		String s1 = removeLastOccurrenceOfPatternBeforeIndex(identifierLexical, doc1, offset);
		String s2 = removeLastOccurrenceOfPatternBeforeIndex(identifierLexical, doc2, offset);
		if (s1 == null || s2 == null) return false;
		return s1.equals(s2);
	}
	
	/**
	 * @return s with the occurrence of p immediately before endIndex removed,
	 * or null if p does not match before endIndex. Note: only examines the
	 * last 50 characters of s.
	 */
	private static String removeLastOccurrenceOfPatternBeforeIndex(Pattern p, String s, int endIndex) {
		int beginIndex = Math.max(0, endIndex - 50);
		Matcher m = p.matcher(s.substring(beginIndex, endIndex));
		while (m.find()) {
			if (m.end() == endIndex - beginIndex) {
				return s.substring(0, beginIndex + m.start()) + s.substring(endIndex);
			}
		}
		return null;
	}
	
	private IStrategoTerm reusePreviousAst(int offset, String document, String prefix) {
		completionPrefix = prefix;
		lastDocument = document;
		lastOffset = offset;
		String prefixInAst = sanitizePrefix(completionPrefix);
		if (prefixInAst == null)
			return null;
		completionNode = lastCompletionNode;
		putCompletionNode(completionNode, prefixInAst, false);
		return lastCompletionAst;
	}

	private String sanitizePrefix(String prefix) {
		Matcher matcher = identifierLexical.matcher(prefix);
		if (prefix.length() == 0) {
			return "";
		} else if (matcher.lookingAt()) {
			return prefix.substring(0, matcher.end());
		} else {
			return null;
		}
	}
	
	private IStrategoTerm forceUseOldAst(SGLRParseController parser, int offset, String document) {
		if (parser.getCurrentAst() != lastParserAst) { // parser has a more recent AST
			lastParserAst = parser.getCurrentAst();
			lastCompletionAst = parser.getCurrentAst();
			completionNode = null;
		} else {
			if (completionNode == null)
				return null;
			putCompletionNode(completionNode, readPrefix(offset, document), false);
		}
		return lastCompletionAst;
	}

	private static SGLRParseController getParser(IParseController controller) {
		if (controller instanceof DynamicParseController)
			controller = ((DynamicParseController) controller).getWrapped();
		return (SGLRParseController) controller;
	}
	
	private IStrategoTerm identifyCompletionNode(final IStrategoTerm ast, final String completionToken) {
		class Visitor extends TermVisitor {
			IStrategoTerm result = ast;
			public void preVisit(IStrategoTerm node) {
				if (isTermString(node)) {
					String value = ((IStrategoString) node).stringValue();
					if (value.indexOf(completionToken) > -1) {
						putCompletionNode(node, value.replace(completionToken, ""), false);
						result = getRoot(completionNode);
					}
				}
			}
		}
		Visitor visitor = new Visitor();
		visitor.visit(ast);
		return visitor.result;
	}
	
	/**
	 * Creates a new abstract syntax tree with the given node
	 * replaced by a COMPLETION(prefix) term,
	 * and assigns completionNode.
	 */
	private void putCompletionNode(IStrategoTerm node, final String prefix, final boolean noContext) {
		final ParentTermFactory factory = new ParentTermFactory(Environment.getTermFactory());
		
		final IStrategoTerm targetNode = tryGetCompletionNodeWrappingTerm(node);
		
		new TermTransformer(factory, true) {
			@Override
			public IStrategoTerm preTransform(IStrategoTerm current) {
				if (current == targetNode) {
					IStrategoTerm prefixTerm = factory.makeString(prefix);
					completionPrefix = prefix;
					IStrategoTerm completionTerm = factory.makeAppl(COMPLETION_CONSTRUCTOR, prefixTerm);
					completionNode = noContext ? factory.makeAppl(COMPLETION_UNKNOWN, completionTerm) : completionTerm;
					factory.copyAttachments(current, completionNode, true);
					factory.copyAttachments(current, completionTerm, true);
					factory.copyAttachments(current, prefixTerm, true);
					return completionNode;
				} else {
					return current;
				}
			}
		}.transform(getRoot(node));

		if (!noContext && getParent(completionNode) != null)
			completionNode = getParent(completionNode); // add a bit of context
	}

	private IStrategoTerm tryGetCompletionNodeWrappingTerm(IStrategoTerm node) {
		if (tryGetConstructor(node) != COMPLETION_CONSTRUCTOR) {
			for (IStrategoTerm child : node.getAllSubterms()) {
				if (tryGetConstructor(child) == COMPLETION_CONSTRUCTOR)
					node = child;
			}
		}
		assert getParent(node) == null || tryGetConstructor(getParent(node)) != COMPLETION_CONSTRUCTOR;
		return node;
	}
	
	private IStrategoTerm addNoContextNode(IStrategoTerm ast, final int offset, String document) {
		class Visitor extends TermVisitor {
			IStrategoTerm targetNode, lastNode;
			
			public void preVisit(IStrategoTerm node) {
				if (getLeftToken(node).getStartOffset() <= offset
						&& offset <= getRightToken(node).getEndOffset()) {
					targetNode = node;
				}
				lastNode = node;
			}
		}
		Visitor visitor = new Visitor();
		visitor.visit(ast);
		if (visitor.targetNode != null) {
			putCompletionNode(visitor.targetNode, readPrefix(offset, document), true);
		} else {
			putCompletionNode(visitor.lastNode, readPrefix(offset, document), true);
		}
		return ast;
	}
	
	/**
	 * Read the identifier at the offset location, using
	 * the identifier lexical regular expression.
	 */
	public String readPrefix(int offset, String document) {
		int prefixStart = offset;
		int lastGoodPrefixStart = offset;
		while (--prefixStart >= 0) {
			String prefix = document.substring(prefixStart, offset);
			if (identifierLexical.matcher(prefix).matches()) {
				lastGoodPrefixStart = prefixStart;
			} else if (prefix.charAt(0) == '\n') {
				return document.substring(lastGoodPrefixStart, offset);
			}
		}
		return document.substring(0, offset);
	}
	
}
