package org.strategoxt.imp.runtime.services;

import static org.spoofax.jsglr.client.imploder.IToken.TK_LAYOUT;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getLeftToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getRightToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getSort;
import static org.spoofax.terms.Term.tryGetConstructor;

import java.util.List;

import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.imp.services.base.FolderBase;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokenizer;
import org.spoofax.terms.TermVisitor;
import org.strategoxt.imp.runtime.Environment;

/**
 * Folding service. Includes special logic to deal with layout tokens in SGLR
 * token streams.
 * 
 * @author Lennart Kats <L.C.L.Kats add tudelft.nl>
 */
public class FoldingUpdater extends FolderBase {
	private ITokenizer parseStream;

	private final List<NodeMapping> folded;

	private final List<NodeMapping> defaultFolded;

	public FoldingUpdater(List<NodeMapping> folded, List<NodeMapping> defaultFolded) {
		this.folded = folded;
		this.defaultFolded = defaultFolded;
	}

	private class FoldingVisitor extends TermVisitor {
		public void preVisit(IStrategoTerm node) {
			IStrategoConstructor termCons = tryGetConstructor(node);
			String constructor = termCons == null ? null : termCons.getName();
			String sort = getSort(node);

			if (NodeMapping.hasAttribute(folded, constructor, sort, 0))
				makeCompleteAnnotation(node);

			for (NodeMapping folding : defaultFolded) {
				if (folding.getAttribute(constructor, sort, 0) != null) {
					makeCompleteAnnotation(node);
					// TODO: Fold node by default
					Environment.logWarning("Folding annotation (folded) not implemented");
					break;
				}
			}
		}
	}

	@Override
	public void sendVisitorToAST(java.util.HashMap newAnnotations, java.util.List annotations,
			java.lang.Object node) {

		IStrategoTerm astNode = (IStrategoTerm) node;
		parseStream = getLeftToken(astNode).getTokenizer();

		new FoldingVisitor().visit(astNode);
	}

	public void makeCompleteAnnotation(IStrategoTerm node) {
		makeCompleteAnnotation(getLeftToken(node), getRightToken(node));
	}

	public void makeCompleteAnnotation(IToken firstToken, IToken lastToken) {
		final int start = firstToken.getEndOffset();
		int end = -1;
		if (start == -1)
			return; // empty starting token

		if (firstToken.getLine() != lastToken.getLine()) {
			// Consume any layout tokens at the end of our AST node until the
			// next EOL
			while (parseStream.getTokenCount() >= lastToken.getIndex()) {
				IToken next = parseStream.getTokenAt(lastToken.getIndex() + 1);

				if (next.getKind() == TK_LAYOUT) {
					lastToken = next;
					if ((end = getEndOfLinePosition(next)) != -1)
						break;
				} else {
					// Next AST node starts at the same line!
					break;
				}
			}

			if (end == -1)
				end = lastToken.getEndOffset();

			try {
				makeAnnotation(start, end - start + 1);
			} catch (AssertionFailedException e) {
				Environment.logException("Could not create a folding annotation at (" + start + ","
						+ (end - start + 1));
			}
		}
	}

	private int getEndOfLinePosition(IToken token) {
		int end = token.getEndOffset();

		String lexStream = parseStream.getInput();

		for (int i = token.getStartOffset(); i <= end; i++) {
			char c = lexStream.charAt(i);
			if (c == '\n' || c == '\r')
				return i;
		}

		return -1;
	}
}
