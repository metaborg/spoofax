package org.strategoxt.imp.runtime.services;

import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getRightToken;
import static org.spoofax.jsglr.client.imploder.ImploderAttachment.getSort;
import static org.spoofax.terms.Term.tryGetConstructor;
import static org.spoofax.terms.Term.tryGetName;
import static org.spoofax.terms.attachments.ParentAttachment.getParent;

import java.util.List;

import org.eclipse.imp.parser.IParseController;
import org.eclipse.imp.services.ITokenColorer;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.interpreter.terms.IStrategoConstructor;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.IToken;
import org.spoofax.jsglr.client.imploder.Token;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.imp.runtime.dynamicloading.BadDescriptorException;
import org.strategoxt.imp.runtime.dynamicloading.DynamicTokenColorer;
import org.strategoxt.imp.runtime.parser.SGLRParseController;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class TokenColorer implements ITokenColorer {
	
	private static final int GRAY_COMPONENT = 110;
	
	private static volatile Color gray;
	
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
	
	public TokenColorer(
			SGLRParseController controller,
			List<TextAttributeMapping> envMappings,
			List<TextAttributeMapping> nodeMappings,
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
		IToken token = (IToken) oToken;
		IStrategoTerm node = (IStrategoTerm) token.getAstNode();
		TextAttribute nodeColor = null;
		int tokenKind = token.getKind();
		
		// Use the parent of string/int terminal nodes
		if (tryGetConstructor(node) == null && getParent(node) != null) {
			nodeColor = getColoring(nodeMappings, null, getSort(node), tokenKind);
			node = getParent(node);
		}
		
		String sort = node == null ? null : getSort(node);
		String constructor = tryGetName(node);
		
		if ((tokenKind == IToken.TK_LAYOUT || tokenKind == IToken.TK_ERROR_LAYOUT) && Token.isWhiteSpace(token)) {
			// Don't treat whitespace layout as comments, to avoid italics in text that
			// was just typed in
			tokenKind = IToken.TK_UNKNOWN;
		} else if (tokenKind == IToken.TK_ERROR_KEYWORD) {
			tokenKind = IToken.TK_KEYWORD;
		} else if (tokenKind == IToken.TK_ESCAPE_OPERATOR) {
			return new TextAttribute(getGrayColor());
		}
		 
		TextAttribute tokenColor = getColoring(tokenMappings, constructor, sort, tokenKind);
		if (nodeColor == null && tokenKind != IToken.TK_LAYOUT)
			nodeColor = getColoring(nodeMappings, constructor, sort, tokenKind);
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

	private TextAttribute addEnvColoring(TextAttribute attribute, ISimpleTerm node, int tokenKind) {
		TextAttribute envColor = null;
		
		// TODO: Optimize - don't traverse up the tree to color every node
		
		while (getParent(node) != null && (envColor == null || hasBlankFields(attribute))) {
			node = getParent(node);
			String sort = getSort(node);
			IStrategoConstructor termCons = tryGetConstructor((IStrategoTerm) node);
			String constructor = termCons == null ? null : termCons.getName();

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
		
		int style = master.getStyle() | slave.getStyle();

		if (fg == null || bg == null || style != master.getStyle()) {
			return new TextAttribute(fg == null ? slave.getForeground() : fg, bg == null ? slave
					.getBackground() : bg, style);
		} else {
			return master;
		}
	}

	private static TextAttribute noWhitespaceBackground(TextAttribute attribute, IToken token, int tokenKind) {
		// TODO: Prefer a white background for layout tokens next to another white token
		
		if (attribute.getBackground() == null) {
			// _lastBackground = WHITE;
			return attribute;
		} else if (tokenKind == IToken.TK_LAYOUT && Token.indexOf(token, '\n') != -1) {			
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
		// Right now, TokenColorerHelper.isParserBasedPresentation() depends on this property
		ISimpleTerm ast = (ISimpleTerm) parseController.getCurrentAst();
		return new Region(0, getRightToken(ast).getTokenizer().getInput().length() - 1);
		// return seed;
	}

	public static TextAttribute toGray(TextAttribute attribute) {
		return attribute == null
				? new TextAttribute(getGrayColor())
				: new TextAttribute(getGrayColor(), attribute.getBackground(), attribute.getStyle(), attribute.getFont());
	}
	
	public static Color getGrayColor() {
		if (gray == null) {
			synchronized (DynamicTokenColorer.class) {
				if (gray == null)
					gray = new Color(Display.getCurrent(), GRAY_COMPONENT, GRAY_COMPONENT, GRAY_COMPONENT);
			}
		}
		return gray;
	}
}
