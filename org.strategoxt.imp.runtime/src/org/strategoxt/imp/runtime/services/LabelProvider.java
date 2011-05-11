package org.strategoxt.imp.runtime.services;

import static org.spoofax.jsglr.client.imploder.IToken.TK_IDENTIFIER;
import static org.spoofax.jsglr.client.imploder.IToken.TK_STRING;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getLeftToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getRightToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getSort;
import static org.spoofax.terms.Term.tryGetName;

import org.eclipse.imp.services.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.ITokenizer;
import org.spoofax.terms.attachments.ParentAttachment;
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
				getSort(node) + "." + tryGetName(node)
			);
			caption = tryGetName(node);
		}
		return caption;
		
	}

	private String getCaption(IStrategoTerm node) {
		// TODO: add user-defined outline captions, perhaps just using Stratego
		// HACK: Hardcoded outlining, until we have support for patterns
		String constructor = node == null ? null : tryGetName(node);
		
		if ("MethodDec".equals(constructor)
				&& node.getSubtermCount() > 0 && node.getSubterm(0).getSubtermCount() > 3) {
			return getIdentifier(node.getSubterm(0).getSubterm(3));
		} else if ("ClassDec".equals(constructor)
				&& node.getSubtermCount() > 0 && node.getSubterm(0).getSubtermCount() > 1) {
			return getIdentifier(node.getSubterm(0).getSubterm(1));
		} else if (node.getSubtermCount() == 1
				&& node.getSubterm(0).isList()) {
			return getLeftToken(node).toString(); // e.g., "rules", "strategies"
		} else {
			return getIdentifier(node);
		}
	}
	
	private String getIdentifier(IStrategoTerm node) {
		ITokenizer stream = getLeftToken(node).getTokenizer();
		int i = getLeftToken(node).getIndex();
		int end = getRightToken(node).getIndex();
		
		do {
			IToken token = stream.getTokenAt(i);
			int kind = token.getKind();
			
			if (kind == TK_IDENTIFIER || kind == TK_STRING) {
				
				/*
				 * heuristics: optional tokens are not representative labels
				 *  => Figure out whether the token occurs in a 'Some' node.
				 */
				if (!(token.getAstNode() instanceof IStrategoTerm))
					return token.toString();
				IStrategoTerm term = (IStrategoTerm) token.getAstNode();
				
				boolean isOptional = false;
				while (term != null && !term.equals(node)) {
					if ("Some".equals(tryGetName(term))) {
						isOptional = true;
						break;
					}
					term = ParentAttachment.getParent(term);
				}
				
				if (!isOptional)
					return token.toString();
			}
			
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
