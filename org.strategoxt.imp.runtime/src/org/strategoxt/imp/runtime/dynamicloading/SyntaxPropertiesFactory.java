package org.strategoxt.imp.runtime.dynamicloading;

import static org.spoofax.interpreter.core.Tools.termAt;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.collectTerms;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.findTerm;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.termContents;

import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.services.ContentProposer;
import org.strategoxt.imp.runtime.services.SyntaxProperties;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class SyntaxPropertiesFactory extends AbstractServiceFactory<ILanguageSyntaxProperties> {

	private static final String DEFAULT_LEXICAL = "[A-Za-z_0-9]+";
	
	public SyntaxPropertiesFactory() {
		super(ILanguageSyntaxProperties.class, true);
	}

	@Override
	public ILanguageSyntaxProperties create(Descriptor descriptor, SGLRParseController controller) throws BadDescriptorException {
		IStrategoAppl doc = descriptor.getDocument();

		IStrategoAppl blockComment = findTerm(doc, "BlockCommentDef");
		String singleLineCommentPrefix = termContents(findTerm(doc, "LineCommentPrefix"));
		Pattern identifierLexical = readIdentifierLexical(descriptor, false);
		String[][] fences = readFences(doc, identifierLexical, true);
		String[][] allFences = readFences(doc, identifierLexical, false);
		
		if (blockComment != null) {
			String blockCommentStart = termContents(termAt(blockComment, 0));
			String blockCommentContinuation = termContents(termAt(blockComment, 1));
			String blockCommentEnd = termContents(termAt(blockComment, 2));
			return new SyntaxProperties(singleLineCommentPrefix, blockCommentStart, blockCommentContinuation, blockCommentEnd, fences, allFences, identifierLexical);
		} else {
			return new SyntaxProperties(singleLineCommentPrefix, null, null, null, fences, allFences, identifierLexical);
		}
	}

	/**
	 * @param strict Only consider single-character fences that are unequal to each other.
	 */
	private static String[][] readFences(IStrategoAppl descriptor, Pattern identifierLexical, boolean strict) {
		ArrayList<String[]> results = new ArrayList<String[]>();
		
		for (IStrategoAppl fence : collectTerms(descriptor, "FenceDef")) {
			String openFence = termContents(termAt(fence, 0));
			String closeFence = termContents(termAt(fence, 1));
			readFence(results, openFence, closeFence, identifierLexical, strict);
		}
		
		if (!strict) {
			for (IStrategoAppl fence : collectTerms(descriptor, "IndentDef")) {
				String openFence = termContents(termAt(fence, 0));
				readFence(results, openFence, "", identifierLexical, strict);
			}
		}
		
		return results.toArray(new String[][] {});
	}

	private static void readFence(ArrayList<String[]> results, String openFence, String closeFence,
			Pattern identifierLexical, boolean strict) {
		
		if (!strict || (openFence.length() == 1 && closeFence.length() == 1 && !openFence.equals(closeFence))) {
			String[] array = { openFence, closeFence };
			results.add(array);
		}
	}

	protected static Pattern readIdentifierLexical(Descriptor descriptor, boolean mustMatchCompletionLexical) throws BadDescriptorException {
		try {
			String completionLexical = descriptor.getProperty("IdentifierLexical",
					descriptor.getProperty("CompletionLexical", DEFAULT_LEXICAL));
			Pattern result = Pattern.compile(completionLexical);
			if (result.matcher("").matches())
				throw new PatternSyntaxException("Identifier lexical matches the empty string", completionLexical, 0);
			if (mustMatchCompletionLexical && !result.matcher(ContentProposer.COMPLETION_TOKEN).matches())
				throw new PatternSyntaxException("Identifier lexical must allow letters and numbers (e.g., "
						+ ContentProposer.COMPLETION_TOKEN + ")", completionLexical, 0);
			return result;
		} catch (PatternSyntaxException e) {
			throw new BadDescriptorException("Illegal completion lexical in editor descriptor", e);
		}
	}
}
