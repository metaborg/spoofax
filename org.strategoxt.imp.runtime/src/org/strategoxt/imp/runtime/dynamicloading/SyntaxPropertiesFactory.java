package org.strategoxt.imp.runtime.dynamicloading;

import java.util.ArrayList;

import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.spoofax.interpreter.terms.IStrategoAppl;

import static org.strategoxt.imp.runtime.dynamicloading.TermReader.*;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
class SyntaxPropertiesFactory {
	/**
	 * @see Descriptor#getService(Class)
	 */
	public static ILanguageSyntaxProperties create(IStrategoAppl descriptor) throws BadDescriptorException {
		SyntaxProperties result = new SyntaxProperties();

		IStrategoAppl blockComment = findTerm(descriptor, "BlockCommentDef");
		result.singleLineCommentPrefix = termContents(findTerm(descriptor, "LineCommentPrefix"));
		result.blockCommentStart = termContents(termAt(blockComment, 0));
		result.blockCommentContinuation = termContents(termAt(blockComment, 1));
		result.blockCommentEnd = termContents(termAt(blockComment, 2));
		result.fences = readFences(descriptor);
		
		return result;
	}

	private static String[][] readFences(IStrategoAppl descriptor) {
		ArrayList<String[]> fences = new ArrayList<String[]>();
		
		for (IStrategoAppl fence : collectTerms(descriptor, "FenceDef")) {
			String[] array = { termContents(termAt(fence, 0)), termContents(termAt(fence, 1)) };
			fences.add(array);
		}
		
		return fences.toArray(new String[][] {});
	}
	
	private static class SyntaxProperties implements ILanguageSyntaxProperties {
		String singleLineCommentPrefix;
		
		String blockCommentStart, blockCommentContinuation, blockCommentEnd;
		
		String[][] fences;

		public String getSingleLineCommentPrefix() {
			return singleLineCommentPrefix;
		}

		public String[][] getFences() {
			return fences;
		}
		
		public String getBlockCommentContinuation() {
			return blockCommentContinuation;
		}

		public String getBlockCommentEnd() {
			return blockCommentEnd;
		}

		public String getBlockCommentStart() {
			return blockCommentStart;
		}

		public int[] getIdentifierComponents(String ident) {
			return null; // ?
		}

		public String getIdentifierConstituentChars() {
			return null; // ?
		}
		
	}
}
