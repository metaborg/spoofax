package org.strategoxt.imp.runtime.services;

import static org.strategoxt.imp.runtime.parser.tokens.TokenKind.*;
import lpg.runtime.IPrsStream;
import lpg.runtime.IToken;

import org.eclipse.imp.services.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.parser.ast.AstNode;
import org.strategoxt.imp.runtime.parser.ast.AstNodeLocator;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class LabelProvider implements ILabelProvider {

	public Image getImage(Object element) {
		return null;
	}

	public String getText(Object element) {
		AstNode node = AstNodeLocator.impObjectToAstNode(element);
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

	private String getCaption(AstNode node) {
		// TODO: add user-defined outline captions, perhaps just using Stratego
		// HACK: Hardcoded outlining, until we have support for patterns
		String constructor = node == null ? null : node.getConstructor();
		
		if ("MethodDec".equals(constructor)) {
			return node.getChildren().get(0).getChildren().get(3).toString();
		} else if ("ClassDec".equals(constructor)) {
			return node.getChildren().get(0).getChildren().get(1).toString();
		} else if ("Rules".equals(constructor)) {
			return "rules";
		} else if ("Strategies".equals(constructor)) {
			return "strategies";
		} else {
			return getIdentifier(node);
		}
	}
	
	private String getIdentifier(AstNode node) {
		IPrsStream stream = node.getLeftIToken().getIPrsStream();
		int i = node.getLeftIToken().getTokenIndex();
		int end = node.getRightIToken().getTokenIndex();
		
		do {
			IToken token = stream.getTokenAt(i);
			int kind = token.getKind();

			if (kind == TK_IDENTIFIER.ordinal() || kind == TK_STRING.ordinal())
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
