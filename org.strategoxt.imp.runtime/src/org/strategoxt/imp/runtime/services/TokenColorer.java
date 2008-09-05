package org.strategoxt.imp.runtime.services;

import java.util.List;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.base.TokenColorerBase;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.graphics.Color;

import org.strategoxt.imp.runtime.parser.ast.AstNode;
import org.strategoxt.imp.runtime.parser.tokens.TokenKind;
import org.strategoxt.imp.runtime.parser.tokens.SGLRToken;

import lpg.runtime.IAst;
import lpg.runtime.IToken;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class TokenColorer extends TokenColorerBase {
	private /*final*/ IParseController parseController;
	
	private final List<TextAttributeMapping> envMappings, nodeMappings, tokenMappings;
	
	public List<TextAttributeMapping> getTokenMappings() {
		return tokenMappings;
	}
	
	public List<TextAttributeMapping> getNodeMappings() {
		return nodeMappings;
	}
	
	public List<TextAttributeMapping> getEnvMappings() {
		return envMappings;
	}
	
	public TokenColorer(List<TextAttributeMapping> envMappings, List<TextAttributeMapping> nodeMappings,
			List<TextAttributeMapping> tokenMappings) {
		
		this.tokenMappings = tokenMappings;
		this.nodeMappings = nodeMappings;
		this.envMappings = envMappings;
	}

	@Override
	public TextAttribute getColoring(IParseController controller, Object oToken) {
		SGLRToken token = (SGLRToken) oToken;
		AstNode node = token.getAstNode();
		
		parseController = controller;
		
		// Use the parent of string/int terminal nodes
		if (node != null && node.getConstructor() == null && node.getParent() != null)
			node = node.getParent();
		
		int tokenKind = token.getKind();
		String sort = node == null ? null : node.getSort();
		String constructor = node == null ? null : node.getConstructor();
		
		TextAttribute tokenColor = getColoring(tokenMappings, constructor, sort, tokenKind);
		TextAttribute nodeColor = getColoring(nodeMappings, constructor, sort, tokenKind);
		TextAttribute result = mergeStyles(nodeColor, tokenColor);
		
		if (node != null) {
			return addEnvColoring(result, node, tokenKind); // TODO: noWhitespaceBackground?
		} else if (nodeColor == null) {
			result = getColoring(envMappings, constructor, sort, tokenKind);
		}
		
		if (result == null) return null;
		else return noWhitespaceBackground(result, token, tokenKind);
	}

	private TextAttribute getColoring(List<TextAttributeMapping> mappings, String constructor, String sort, int tokenKind) {
		for (TextAttributeMapping mapping : mappings) {
			TextAttribute result = mapping.getAttribute(constructor, sort, tokenKind).get();
			if (result != null) return result;
		}
		
		return null;
	}

	private TextAttribute addEnvColoring(TextAttribute attribute, AstNode node, int tokenKind) {
		TextAttribute envColor = null;
		
		// TODO: Optimize - don't traverse up the tree to color every node
		
		while (node.getParent() != null && (envColor == null || hasBlankFields(attribute))) {
			node = node.getParent();
			String sort = node.getSort();
			String constructor = node.getConstructor();

			envColor = getColoring(envMappings, constructor, sort, tokenKind);
			attribute = mergeStyles(envColor, attribute);
		}
		
		return attribute;
	}
	
	private static final boolean hasBlankFields(TextAttribute attribute) {
		return attribute == null || attribute.getBackground() == null
				|| attribute.getForeground() == null || attribute.getFont() == null;
	}

	private static TextAttribute mergeStyles(TextAttribute master, TextAttribute slave) {
		if (slave == null) return master;
		if (master == null) return slave;

		Color fg = master.getForeground();
		Color bg = master.getBackground();

		if (fg == null || bg == null) {
			return new TextAttribute(fg == null ? slave.getForeground() : fg, bg == null ? slave
					.getBackground() : bg, master.getStyle());
		} else {
			return master;
		}
	}

	private static TextAttribute noWhitespaceBackground(TextAttribute attribute, IToken token, int tokenKind) {
		// FIXME: Don't use toString() on tokens
		// TODO: Prefer a white background for layout tokens next to another white token
		
		if (attribute.getBackground() == null) {
			// _lastBackground = WHITE;
			return attribute;
		} else if (tokenKind == TokenKind.TK_LAYOUT.ordinal() && token.toString().contains("\n")) {			
			attribute = new TextAttribute(attribute.getForeground(), null, attribute.getStyle());
		}

		// _lastBackground = attribute.getBackground();
		return attribute;
	}

	@Override
	public IRegion calculateDamageExtent(IRegion seed) {
		if (parseController.getCurrentAst() == null) return seed;
		
		// Always damage the complete source
		IAst ast = (IAst) parseController.getCurrentAst();
		return new Region(0, ast.getRightIToken().getEndOffset());
	}
}
