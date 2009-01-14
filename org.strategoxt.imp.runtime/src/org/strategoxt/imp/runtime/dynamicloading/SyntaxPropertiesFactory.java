package org.strategoxt.imp.runtime.dynamicloading;

import java.util.ArrayList;

import org.eclipse.imp.editor.LanguageServiceManager;
import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.spoofax.interpreter.terms.IStrategoAppl;

import static org.strategoxt.imp.runtime.dynamicloading.TermReader.*;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
class SyntaxPropertiesFactory extends AbstractServiceFactory<ILanguageSyntaxProperties> {
	
	@Override
	public Class<ILanguageSyntaxProperties> getCreatedType() {
		return ILanguageSyntaxProperties.class;
	}

	@Override
	public ILanguageSyntaxProperties create(Descriptor descriptor) throws BadDescriptorException {
		SyntaxProperties result = new SyntaxProperties();
		
		IStrategoAppl doc = descriptor.getDocument();

		IStrategoAppl blockComment = findTerm(doc, "BlockCommentDef");
		result.singleLineCommentPrefix = termContents(findTerm(doc, "LineCommentPrefix"));
		result.fences = readFences(doc);
		
		if (blockComment != null) {
			result.blockCommentStart = termContents(termAt(blockComment, 0));
			result.blockCommentContinuation = termContents(termAt(blockComment, 1));
			result.blockCommentEnd = termContents(termAt(blockComment, 2));
		}
		
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
		
		public void initialize(LanguageServiceManager manager) {
			// Not used here
		}

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
			return null; // described in PrefspecsSyntaxProperties.
		}

		public String getIdentifierConstituentChars() {
			return null; // all characters in identifiers. Unused.
		}
		
	}
}
