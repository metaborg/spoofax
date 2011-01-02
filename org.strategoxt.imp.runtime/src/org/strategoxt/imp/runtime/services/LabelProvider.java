package org.strategoxt.imp.runtime.services;

import static org.spoofax.jsglr.client.imploder.IToken.TK_IDENTIFIER;
import static org.spoofax.jsglr.client.imploder.IToken.TK_STRING;

import org.eclipse.imp.services.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokenizer;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.ast.AstNodeLocator;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class LabelProvider implements ILabelProvider {

	public Image getImage(Object element) {
		return null;
	}

	public String getText(Object element) {
		IStrategoTerm node = AstNodeLocator.impObjectToAstNode(element);
		String caption = getCaption(node);
		
		if (caption == null) {
			Environment.logException(
				"Unable to infer the caption of this AST node: " +
				node.getSort() + "." + node.getConstructor()
			);
			caption = node.getConstructor();
		}
		return caption;
		
	}

	private String getCaption(IStrategoTerm node) {
		// TODO: add user-defined outline captions, perhaps just using Stratego
		// HACK: Hardcoded outlining, until we have support for patterns
		String constructor = node == null ? null : node.getConstructor();
		
		if ("MethodDec".equals(constructor)
				&& node.getSubtermCount() > 0 && node.getSubterm(0).getSubtermCount() > 3) {
			return node.getSubterm(0).getSubterm(3).toString();
		} else if ("ClassDec".equals(constructor)
				&& node.getSubtermCount() > 0 && node.getSubterm(0).getSubtermCount() > 1) {
			return node.getSubterm(0).getSubterm(1).toString();
		} else if (node.getSubtermCount() == 1
				&& node.getSubtermCount() > 0 && node.getSubterm(0).isList()) {
			return node.getLeftToken().toString(); // e.g., "rules", "strategies"
		} else {
			return getIdentifier(node);
		}
	}
	
	private String getIdentifier(IStrategoTerm node) {
		ITokenizer stream = node.getLeftToken().getTokenizer();
		int i = node.getLeftToken().getIndex();
		int end = node.getRightToken().getIndex();
		
		do {
			IToken token = stream.getTokenAt(i);
			int kind = token.getKind();

			if (kind == TK_IDENTIFIER || kind == TK_STRING)
				return token.toString();
			
		} while (i++ < end);
		
		return null;
	}

	public boolean isLabelProperty(Object element, String property) {
		return true; // TODO: Optimize LabelProvider.isLabelProperty?
	}

	public void addListener(ILabelProviderListener listener) {
		// Do nothing
	}

	public void removeListener(ILabelProviderListener listener) {
		// Do nothing
	}

	public void dispose() {
		// Do nothing
	}

}
