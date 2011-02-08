package org.strategoxt.imp.runtime.stratego;

import static java.util.Collections.synchronizedMap;
import static org.spoofax.interpreter.core.Tools.asJavaString;
import static org.spoofax.interpreter.core.Tools.isTermString;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getLeftToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getRightToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.hasImploderOrigin;
import static org.spoofax.jsglr.client.imploder.Token.isWhiteSpace;
import static org.spoofax.terms.attachments.OriginAttachment.tryGetOrigin;
import static org.spoofax.terms.attachments.ParentAttachment.getParent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

import org.eclipse.imp.language.Language;
import org.eclipse.imp.language.LanguageRegistry;
import org.eclipse.imp.language.ServiceFactory;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.ssl.SSLLibrary;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokenizer;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.Descriptor;
import org.strategoxt.imp.runtime.services.SyntaxProperties;


/**
 * Extracts all block comments between the previous sibling and the current node,
 * and all line comments between the current node and the next sibling.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class OriginSurroundingCommentsPrimitive extends AbstractPrimitive {
	
	private static final String NAME = "SSL_EXT_origin_surrounding_comments";
	
	private static final Map<String, Pattern> blockMiddlePatterns =
		synchronizedMap(new WeakHashMap<String, Pattern>());

	public OriginSurroundingCommentsPrimitive() {
		super(NAME, 0, 2);
	}
	
	@Override
	public final boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
		if (!isTermString(tvars[0]) || !hasImploderOrigin(tvars[1]))
			return false;
		
		Language language = LanguageRegistry.findLanguage(asJavaString(tvars[0]));
		if (language == null) {
			SSLLibrary.instance(env).getIOAgent().printError(NAME
					+ ": no definition for language " + language);
			return false;
		}
		
		IStrategoList result = getSurroundingComments(language, tryGetOrigin(tvars[1]), true);
		if (result == null) return false;
		env.setCurrent(result);
		return true;
	}
	
	/**
	 * Extracts all block comments between the previous sibling and the current node,
	 * and all line comments between the current node and the next sibling.
	 * Any other comments in these regions are also returned.
	 * 
	 * @param filter	Sets whether to filter out comment starting/continuation/ending characters.
	 * 
	 * @return A list with all comments surrounding the node, or null if none found.
	 */
	public static IStrategoList getSurroundingComments(Language language, ISimpleTerm node, boolean filter) {
		final ISimpleTerm parent = getParent(node);
		final ISimpleTerm container = getNonListContainer(parent);
		final IToken leftToken = getLeftToken(node);
		final IToken rightToken = getRightToken(node);
		final ITokenizer tokens = leftToken.getTokenizer();
		final int layoutKind = IToken.TK_LAYOUT;
		final int leftIndex = leftToken.getIndex();
		final int rightIndex = rightToken.getIndex();
		final int prefixIndex = getPrefixIndex(tokens, leftIndex, parent, container);
		
		// Lazily allocate candidates
		IToken candidate = null;
		List<IToken> candidates = null;
		
		// Collect all tokens between the previous and the current node,
		// and those between the current and the next sibling node
		for (int i = prefixIndex, end = tokens.getTokenCount(); i < end; i++) {
			IToken current = tokens.getTokenAt(i);
			if (i > rightIndex && !belongsToEither(current, parent, container))
				break;
			// TODO: maybe all layout should be returned if filter == false?
			if (current.getKind() == layoutKind && !isWhiteSpace(current)) {
				if (candidate == null) {
					candidate = current;
				} else if (candidates == null) {
					candidates = new ArrayList<IToken>();
					candidates.add(candidate);
					candidates.add(current);
				} else {
					candidates.add(current);
				}
			}
			if (i == leftIndex)
				i = rightIndex;
		}
		
		return extractComments(language, leftIndex, rightIndex, candidate, candidates, filter);
	}

	private static ISimpleTerm getNonListContainer(ISimpleTerm node) {
		while (node != null && node.isList())
			node = getParent(node);
		return node;
	}

	/**
	 * Extract comments either from a single candidate token (singleToken)
	 * or a list of candidate tokens (tokenList, if not null).
	 */
	private static IStrategoList extractComments(Language language, int leftIndex, int rightIndex,
			IToken singleToken, List<IToken> tokenList, boolean filter) {
		
		if (singleToken == null) return null;
		
		ILanguageSyntaxProperties syntax = getSyntaxProperties(language);
		ITermFactory factory = Environment.getTermFactory();
		
		if (tokenList == null) { // only 1 result
			String comment = extractComment(singleToken, leftIndex, rightIndex, syntax, filter);
			if (comment == null) {
				return null;
			} else {
				return factory.makeList(factory.makeString(comment));
			}
		} else {
			IStrategoList comments = factory.makeList();
			for (int i = tokenList.size() - 1; i >= 0; i--) {
				String comment = extractComment(tokenList.get(i), leftIndex, rightIndex, syntax, filter);
				if (comment != null)
					comments = factory.makeListCons(factory.makeString(comment), comments);
			}
			return comments.isEmpty() ? null : comments;
		}
	}

	private static String extractComment(IToken token, int leftIndex, int rightIndex,
			ILanguageSyntaxProperties syntax, boolean filter) {

		String text = token.toString();
		String linePrefix = syntax.getSingleLineCommentPrefix();
		String blockStart = syntax.getBlockCommentStart();
		String blockMiddle = syntax.getBlockCommentContinuation();
		String blockEnd = syntax.getBlockCommentEnd();
		
		if (text.startsWith(linePrefix)) {
			if (token.getIndex() < leftIndex)
				return null;
			if (!filter) return text;
			return text.substring(linePrefix.length()).trim();
		} else if (text.startsWith(blockStart) && text.endsWith(blockEnd)) {
			if (token.getIndex() > rightIndex)
				return null;
			if (!filter) return text;
			return extractBlockComment(text, blockStart, blockMiddle, blockEnd);
		} else {
			return text;
		}
	}

	private static String extractBlockComment(String text, String blockStart, String blockMiddle, String blockEnd) {
		text = text.substring(blockStart.length(), text.length() - blockEnd.length());
		Pattern middlePattern = getBlockMiddlePattern(blockMiddle);
		return middlePattern.matcher(text).replaceAll("\n");
	}

	/**
	 * Get a regular expression pattern to remove those pesky '   * ' prefixes
	 * from each block comment line.
	 */
	private static Pattern getBlockMiddlePattern(String blockMiddle) {
		Pattern result = blockMiddlePatterns.get(blockMiddle);
		if (result == null) {
			result = Pattern.compile("\n\\s*\\Q"
					+ blockMiddle.replace("\\E", "\\E\\\\E\\Q")
					+ "\\E\\s?");
			blockMiddlePatterns.put(blockMiddle, result);
		}
		return result;
	}

	/**
	 * Gets the index of the first token after the previous sibling node.
	 */
	private static int getPrefixIndex(ITokenizer tokens, int tokenIndex, ISimpleTerm parent, ISimpleTerm container) {
		for (;;) {
			if (tokenIndex == 0) return tokenIndex;
			IToken prevToken = tokens.getTokenAt(tokenIndex - 1);
			if (!belongsToEither(prevToken, parent, container))
				return tokenIndex;
			tokenIndex--;
		}
	}

	private static ILanguageSyntaxProperties getSyntaxProperties(Language language) {
		ILanguageSyntaxProperties result = null;
		Descriptor descriptor = Environment.getDescriptor(language);
		if (descriptor != null) {
			try {
				result = descriptor.createService(ILanguageSyntaxProperties.class, null);
			} catch (BadDescriptorException e) {
				Environment.logException("Could not read syntax properties", e);
			}
		} else {
			result = ServiceFactory.getInstance().getSyntaxProperties(language);
		}
		return result != null ? result : new SyntaxProperties(null, null, null, null, null, null, null);
	}

	private static boolean belongsToEither(IToken current, ISimpleTerm parent, ISimpleTerm container) {
		ISimpleTerm tokenNode = current.getAstNode();
		return tokenNode == parent || tokenNode == container;
	}

}
