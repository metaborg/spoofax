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
import java.util.regex.Pattern;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.jface.text.Position;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.Tokenizer;
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
 * @author Tobi Vollebregt
 */
public class ContentProposerParser {

	public static final IStrategoConstructor COMPLETION_CONSTRUCTOR =
		getTermFactory().makeConstructor("COMPLETION", 1);

	public static final IStrategoConstructor COMPLETION_UNKNOWN =
		getTermFactory().makeConstructor("NOCONTEXT", 1);

	private static final long REINIT_PARSE_DELAY = 4000;

	private final Pattern identifierLexical;

	private SGLRParseController parser;

	private ContentProposerAstReuser astReuser;

	private IStrategoTerm completionAst;

	private IStrategoTerm completionNode;

	private String completionPrefix;

	public ContentProposerParser(Pattern identifierLexical) {
		this.identifierLexical = identifierLexical;
		this.astReuser = new ContentProposerAstReuser(identifierLexical);
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
		return completionAst == null;
	}

	public IStrategoTerm parse(IParseController icontroller, Position selection, String document, boolean avoidReparse) {
		final int offset = selection.getOffset();

		completionAst = null;
		completionNode = null;
		completionPrefix = null;

		SGLRParseController controller = this.parser = getParser(icontroller);

		// Try to reuse previous AST.
		completionAst = astReuser.tryReusePreviousAst(selection, document);

		if (completionAst != null) {
			// We're lucky => re-put completionNode with the new prefix and return.
			completionPrefix = astReuser.getCompletionPrefix();
			completionNode = astReuser.getCompletionNode();

			completionNode = putCompletionNode(completionNode, completionPrefix, false);
			completionAst = getRoot(completionNode);

			return completionAst;
		}

		// Build document with completion token.
		String documentWithToken = document.substring(0, offset) + COMPLETION_TOKEN + document.substring(offset + selection.getLength());

		// Parse.
		completionAst = parse(controller, documentWithToken, avoidReparse);
		if (completionAst != null) {
			completionPrefix = readPrefix(offset, document);
			completionNode = identifyAndPutCompletionNode(completionAst, completionPrefix, COMPLETION_TOKEN);

			if (completionNode == null) {
				// Can't find COMPLETION_TOKEN...
				completionNode = putNoContextCompletionNode(completionAst, completionPrefix, offset);
			}
			completionAst = getRoot(completionNode);

			astReuser.storeAstForReuse(completionAst, completionNode, completionPrefix);

			return completionAst;
		}

		return null;
	}

	private static IStrategoTerm parse(SGLRParseController controller, String document, boolean avoidReparse) {
		JSGLRI parser = controller.getParser();
		IStrategoTerm result = null;

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
			}
		} catch (SGLRException e) {
			Environment.logException("Could not reparse input for content completion", e);
		} catch (IOException e) {
			Environment.logException("Could not reparse input for content completion", e);
		} finally {
			controller.getParseLock().unlock();
		}
		// Replaces former forceUseOldAst
		if (result == null) result = controller.getCurrentAst();
		return result;
	}

	private static SGLRParseController getParser(IParseController controller) {
		if (controller instanceof DynamicParseController)
			controller = ((DynamicParseController) controller).getWrapped();
		return (SGLRParseController) controller;
	}

	private static IStrategoTerm identifyAndPutCompletionNode(final IStrategoTerm ast, final String prefix, final String completionToken) {
		class Visitor extends TermVisitor {
			IStrategoTerm newCompletionNode;

			public void preVisit(IStrategoTerm term) {
				if (isTermString(term)) {
					String value = ((IStrategoString) term).stringValue();
					if (value.indexOf(completionToken) > -1) {
						newCompletionNode = putCompletionNode(term, prefix, false);
					}
				}
			}

			@Override
			public void postVisit(IStrategoTerm term) {
				// Visit annotations; testing language puts ast nodes in there
				if (!term.getAnnotations().isEmpty())
					visit(term.getAnnotations());
			}
		};
		Visitor visitor = new Visitor();
		visitor.visit(ast);
		return visitor.newCompletionNode;
	}

	/**
	 * Creates a new abstract syntax tree with the given node
	 * replaced by a COMPLETION(prefix) term.
	 */
	private static IStrategoTerm putCompletionNode(IStrategoTerm node, final String prefix, final boolean noContext) {
		final ParentTermFactory factory = new ParentTermFactory(Environment.getTermFactory());

		final IStrategoTerm targetNode = tryGetCompletionNodeWrappingTerm(node);

		class Transformer extends TermTransformer {
			IStrategoTerm newCompletionNode;

			public Transformer() {
				super(factory, true);
			}

			@Override
			public IStrategoTerm preTransform(IStrategoTerm current) {
				if (current == targetNode) {
					IStrategoTerm prefixTerm = factory.makeString(prefix);
					IStrategoTerm completionTerm = factory.makeAppl(COMPLETION_CONSTRUCTOR, prefixTerm);
					newCompletionNode = noContext ? factory.makeAppl(COMPLETION_UNKNOWN, completionTerm) : completionTerm;
					factory.copyAttachments(current, newCompletionNode, true);
					factory.copyAttachments(current, completionTerm, true);
					factory.copyAttachments(current, prefixTerm, true);
					return newCompletionNode;
				} else {
					return current;
				}
			}
		}

		Transformer trans = new Transformer();
		trans.transform(getRoot(node));

		if (!noContext && getParent(trans.newCompletionNode) != null)
			trans.newCompletionNode = getParent(trans.newCompletionNode); // add a bit of context

		return trans.newCompletionNode;
	}

	private static IStrategoTerm tryGetCompletionNodeWrappingTerm(IStrategoTerm node) {
		if (tryGetConstructor(node) != COMPLETION_CONSTRUCTOR) {
			for (IStrategoTerm child : node.getAllSubterms()) {
				if (tryGetConstructor(child) == COMPLETION_CONSTRUCTOR)
					node = child;
			}
		}
		assert getParent(node) == null || tryGetConstructor(getParent(node)) != COMPLETION_CONSTRUCTOR;
		return node;
	}

	/**
	 * Add NOCONTEXT(COMPLETION(...)) node based on token positions.
	 * This is used if completionToken can't be found in the AST.
	 */
	private static IStrategoTerm putNoContextCompletionNode(final IStrategoTerm ast, final String prefix, final int offset) {
		class Visitor extends TermVisitor {
			IStrategoTerm targetNode, lastNode;

			public void preVisit(IStrategoTerm node) {
				if (getLeftToken(node).getStartOffset() <= offset
						&& (offset <= getRightToken(node).getEndOffset() || isPartOfListSuffixAt(node, offset))) {
					targetNode = node;
				}
				lastNode = node;
			}
		}
		Visitor visitor = new Visitor();
		visitor.visit(ast);
		if (visitor.targetNode != null) {
			return putCompletionNode(visitor.targetNode, prefix, true);
		} else {
			return putCompletionNode(visitor.lastNode, prefix, true);
		}
	}

	/**
	 * Tests if an end offset is part of a list suffix
	 * (considers the layout following the list also part of the list).
	 */
	protected static boolean isPartOfListSuffixAt(IStrategoTerm node, final int offset) {
		return node.isList() && offset <= Tokenizer.findRightMostLayoutToken(getRightToken(node)).getEndOffset();
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
