package org.strategoxt.imp.runtime.services;

import java.util.regex.Pattern;

import org.eclipse.imp.services.ILanguageSyntaxProperties;
import org.spoofax.jsglr.client.incremental.CommentDamageExpander;

public class SyntaxProperties implements ILanguageSyntaxProperties {
	
	private final String singleLineCommentPrefix;
	
	private final String blockCommentStart, blockCommentContinuation, blockCommentEnd;
	
	private final String[][] fences;
	
	private final String[][] allFences;

	private final Pattern identifierLexical;
	
	public SyntaxProperties(String singleLineCommentPrefix, String blockCommentStart,
			String blockCommentContinuation, String blockCommentEnd, String[][] fences,
			String[][] allFences, Pattern identifierLexical) {
		
		this.singleLineCommentPrefix = singleLineCommentPrefix;
		this.blockCommentStart = blockCommentStart;
		this.blockCommentContinuation = blockCommentContinuation;
		this.blockCommentEnd = blockCommentEnd;
		this.fences = fences;
		this.allFences = allFences;
		this.identifierLexical = identifierLexical;
	}

	public String getSingleLineCommentPrefix() {
		return singleLineCommentPrefix;
	}

	public String[][] getFences() {
		return fences;
	}
	
	/**
	 * Return all fences, including unbalanced ones and fences
	 * with more than one character.
	 */
	public String[][] getAllFences() {
		return allFences;
	}
	
	public Pattern getIdentifierLexical() {
		return identifierLexical;
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
		// Unused (could be sort of derived from getIdentifierLexical() by testing all chars)
		return null;
	}
	
	public CommentDamageExpander toCommentDamageExpander() {
		return new CommentDamageExpander(getBlockCommentStart(), getBlockCommentEnd());
	}
	
}