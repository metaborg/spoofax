package org.strategoxt.imp.runtime.services;

import java.util.List;

import lpg.runtime.IAst;
import lpg.runtime.IToken;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.ITokenColorer;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.graphics.Color;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.parser.SGLRParseController;
import org.strategoxt.imp.runtime.parser.tokens.SGLRToken;
import org.strategoxt.imp.runtime.parser.tokens.TokenKind;
import org.strategoxt.imp.runtime.stratego.adapter.IStrategoAstNode;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class TokenColorer implements ITokenColorer {
	
	private final List<TextAttributeMapping> envMappings, nodeMappings, tokenMappings;
	
	private boolean isLazyColorsInited;
	
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

	/**
	 * Initializes lazily loaded Color objects (avoiding a potential deadlock when used before PresentationController acquires its lock).
	 */
	public static void initLazyColors(SGLRParseController controller) {
		try {
			ITokenColorer colorer = Environment.getDescriptor(controller.getLanguage()).createService(ITokenColorer.class, controller);
			if (colorer instanceof TokenColorer)
				((TokenColorer) colorer).initLazyColors();
		} catch (BadDescriptorException e) {
			Environment.logException("Could not initialize colorer", e);
		}
	}
	
	public void initLazyColors() {
		if (isLazyColorsInited) return;
		initLazyColors(envMappings);
		initLazyColors(nodeMappings);
		initLazyColors(tokenMappings);
		isLazyColorsInited = true;
	}

	private void initLazyColors(List<TextAttributeMapping> mappings) {
		for (TextAttributeMapping mapping : mappings) {
			TextAttribute attribute = mapping.getAttribute().get();
			attribute.getBackground();
			attribute.getForeground();
		}
	}

	public TextAttribute getColoring(IParseController controller, Object oToken) {
		SGLRToken token = (SGLRToken) oToken;
		IStrategoAstNode node = token.getAstNode();
		TextAttribute nodeColor = null;
		int tokenKind = token.getKind();
		
		// Use the parent of string/int terminal nodes
		if (node != null && node.getConstructor() == null && node.getParent() != null) {
			nodeColor = getColoring(nodeMappings, null, node.getSort(), tokenKind);
			node = node.getParent();
		}
		
		String sort = node == null ? null : node.getSort();
		String constructor = node == null ? null : node.getConstructor();
		
		if (tokenKind == TokenKind.TK_LAYOUT.ordinal() && SGLRToken.isWhiteSpace(token)) {
			// Don't treat whitespace layout as comments, to avoid italics in text that
			// was just typed in
			tokenKind = TokenKind.TK_UNKNOWN.ordinal();
		}
		 
		TextAttribute tokenColor = getColoring(tokenMappings, constructor, sort, tokenKind);
		if (nodeColor == null) nodeColor = getColoring(nodeMappings, constructor, sort, tokenKind);
		TextAttribute result = mergeStyles(nodeColor, tokenColor);
		
		if (node != null) {
			return addEnvColoring(result, node, tokenKind); // TODO: noWhitespaceBackground?
		} else if (nodeColor == null) {
			result = getColoring(envMappings, constructor, sort, tokenKind);
			if (result == null)
				result = tokenColor;
		}
		
		if (result == null) return null;
		else return noWhitespaceBackground(result, token, tokenKind);
	}

	private TextAttribute getColoring(List<TextAttributeMapping> mappings, String constructor, String sort, int tokenKind) {
		for (TextAttributeMapping mapping : mappings) {
			TextAttributeReference result = mapping.getAttribute(constructor, sort, tokenKind);
			if (result != null) return result.get();
		}
		
		return null;
	}

	private TextAttribute addEnvColoring(TextAttribute attribute, IStrategoAstNode node, int tokenKind) {
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
		// TODO: Prefer a white background for layout tokens next to another white token
		
		if (attribute.getBackground() == null) {
			// _lastBackground = WHITE;
			return attribute;
		} else if (tokenKind == TokenKind.TK_LAYOUT.ordinal() && SGLRToken.indexOf(token, '\n') != -1) {			
			attribute = new TextAttribute(attribute.getForeground(), null, attribute.getStyle());
		}

		// _lastBackground = attribute.getBackground();
		return attribute;
	}

	public IRegion calculateDamageExtent(IRegion seed, IParseController parseController) {
		if (parseController.getCurrentAst() == null)
			return seed;
		
		// Always damage the complete source
		// TODO: Is always damaging the complete source still necessary??
		IAst ast = (IAst) parseController.getCurrentAst();
		return new Region(0, ast.getRightIToken().getIPrsStream().getILexStream().getStreamLength() - 1);
		// return seed;
	}
}
