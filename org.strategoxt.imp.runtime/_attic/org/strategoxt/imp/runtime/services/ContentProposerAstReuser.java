package org.strategoxt.imp.runtime.services;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.text.Position;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class ContentProposerAstReuser {

	private final Pattern identifierLexical;

	private IStrategoTerm lastCompletionAst;

	private IStrategoTerm lastCompletionNode;

	private String lastCompletionPrefix;

	private String lastDocument;

	private Position lastSelection;

	private IStrategoTerm completionNode;

	private String completionPrefix;

	public ContentProposerAstReuser(Pattern identifierLexical) {
		this.identifierLexical = identifierLexical;
	}

	public IStrategoTerm getCompletionNode() {
		return completionNode;
	}

	public String getCompletionPrefix() {
		return completionPrefix;
	}

	public void storeAstForReuse(IStrategoTerm lastCompletionAst, IStrategoTerm lastCompletionNode, String lastCompletionPrefix) {
		this.lastCompletionAst = lastCompletionAst;
		this.lastCompletionNode = lastCompletionNode;
		this.lastCompletionPrefix = lastCompletionPrefix;
	}

	/**
	 * Reuse the previous AST if the user just added or deleted a single character.
	 */
	public IStrategoTerm tryReusePreviousAst(Position selection, String document) {
		final int offset = selection.getOffset();
		if (offset != 0 && lastCompletionNode != null) {
			final int lastOffset = lastSelection.getOffset();
			if (lastSelection.getLength() == 0 && selection.getLength() == 0) {
				// No selection present.
				if (lastDocument.length() == document.length() - 1 && lastOffset == offset - 1) {
					// Reuse document, ignoring latest typed character
					String newCharacter = document.substring(offset - 1, offset);
					String previousDocument = lastDocument.substring(0, offset - 1) + newCharacter + lastDocument.substring(offset - 1);
					if (documentsSufficientlyEqual(document, previousDocument, offset)) {
						return reusePreviousAst(selection, document, lastCompletionPrefix + newCharacter);
					}
				} else if (lastCompletionPrefix.length() > 0
						&& lastDocument.length() == document.length() + 1 && lastOffset == offset + 1) {
					// Reuse document, ignoring previously typed character
					String oldCharacter = lastDocument.substring(offset, offset + 1);
					String currentDocument = document.substring(0, offset) + oldCharacter + document.substring(offset);
					if (documentsSufficientlyEqual(currentDocument, lastDocument, offset + 1)) {
						return reusePreviousAst(selection, document, lastCompletionPrefix.substring(0, lastCompletionPrefix.length() - 1));
					}
				} else if (lastDocument.equals(document) && offset == lastOffset) {
					return reusePreviousAst(selection, document, lastCompletionPrefix);
				}
			}
			else {
				// Selection present.
				// Probably not worth bothering with reuse here.
			}
		}
		return dontReusePreviousAst(selection, document);
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

	private IStrategoTerm reusePreviousAst(Position selection, String document, String prefix) {
		lastDocument = document;
		lastSelection = selection;
		lastCompletionPrefix = prefix;
		completionNode = lastCompletionNode;
		completionPrefix = prefix;
		return lastCompletionAst;
	}

	private IStrategoTerm dontReusePreviousAst(Position selection, String document) {
		lastDocument = document;
		lastSelection = selection;
		lastCompletionAst = null;
		lastCompletionNode = null;
		lastCompletionPrefix = null;
		return null;
	}
}
