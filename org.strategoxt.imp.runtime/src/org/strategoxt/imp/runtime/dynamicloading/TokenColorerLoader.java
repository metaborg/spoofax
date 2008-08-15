package org.strategoxt.imp.runtime.dynamicloading;

import static org.strategoxt.imp.runtime.dynamicloading.TermReader.*;

import java.util.List;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.strategoxt.imp.runtime.parser.tokens.TokenKind;
import org.strategoxt.imp.runtime.services.ColorMapping;
import org.strategoxt.imp.runtime.services.TextAttributeReference;
import org.strategoxt.imp.runtime.services.TextAttributeReferenceMap;
import org.strategoxt.imp.runtime.services.TokenColorer;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
class TokenColorerLoader {
	private final IStrategoAppl descriptor;
	
	public TokenColorerLoader(IStrategoAppl descriptor) {
		this.descriptor = descriptor;
	}
	
	public void configureColorer(TokenColorer colorer) throws BadDescriptorException {
		TextAttributeReferenceMap colors = getColorList();
		
		for (IStrategoAppl rule : collectTerms(descriptor, "ColorRuleAll", "ColorRuleAllNamed")) {
			addMapping(rule, colorer.getEnvMappings(), colors);
		}
		
		for (IStrategoAppl rule : collectTerms(descriptor, "ColorRule", "ColorRuleNamed")) {
			IStrategoAppl pattern = termAt(rule, 0);
			if (cons(pattern).equals("Token")) {
				addMapping(rule, colorer.getTokenMappings(), colors);
			} else {
				addMapping(rule, colorer.getNodeMappings(), colors);
			}
		}
	}

	private void addMapping(IStrategoAppl rule, List<ColorMapping> mappings,
			TextAttributeReferenceMap colors) throws BadDescriptorException {
		
		// FIXME
		
		IStrategoAppl pattern = termAt(rule, 0);
		IStrategoAppl attribute = termAt(rule, 1);
		
		IStrategoAppl foreground = termAt(attribute, 0);
		IStrategoAppl background = termAt(attribute, 1);
		IStrategoAppl font = termAt(attribute, 2);
		
		String constructor = termContents(findTerm(pattern, "Constructor"));
		String sort = termContents(findTerm(pattern, "Sort"));
		String tokenKind = termContents(findTerm(pattern, "Token"));
		String listSort = termContents(findTerm(pattern, "Sort"));
		if (listSort != null) sort = listSort + "*";
		
		TextAttribute a = new TextAttribute(getAttribute(colors, foreground).get(), getAttribute(colors, background).get(), getFont(font));
		
		mappings.add(new ColorMapping(a, constructor, sort, getTokenKind(tokenKind)));
	}
	
	private TokenKind getTokenKind(String tokenKind) throws BadDescriptorException {
		try {
			return tokenKind == null ? null : TokenKind.valueOf(tokenKind);
		} catch (IllegalArgumentException e) {
			throw new BadDescriptorException("Could not set the coloring rule for token kind: " + tokenKind, e);
		}
	}

	private TextAttributeReferenceMap getColorList() {
		TextAttributeReferenceMap result = new TextAttributeReferenceMap();
		
		for (IStrategoAppl rule : collectTerms(descriptor, "ColorDef")) {
			String name = termContents(termAt(rule, 0));
			IStrategoAppl attribute = termAt(rule, 1);
			result.register(name, getAttribute(result, attribute));
		}
		
		for (IStrategoAppl rule : collectTerms(descriptor, "ColorRuleNamed", "ColorRuleAllNamed")) {
			String name = termContents(termAt(rule, 1));
			IStrategoAppl attribute = termAt(rule, 2);
			result.register(name, getAttribute(result, attribute));
		}
		
		return result;
	}
	
	private int getFont(IStrategoAppl font) {
		if (cons(font).equals("BOLD")) return SWT.BOLD;
		if (cons(font).equals("ITALIC")) return SWT.BOLD;
		if (cons(font).equals("BOLD_ITALIC")) return SWT.BOLD | SWT.ITALIC;
		return 0;
	}

	private TextAttributeReference getAttribute(TextAttributeReferenceMap colors, IStrategoAppl color) {
		TextAttributeReference result = null;
		
		if (cons(color).equals("ColorDefault")) {
			result = new TextAttributeReference(colors);
		} else if (cons(color).equals("ColorName")) {
			result = new TextAttributeReference(colors, termContents(color));
		} else if (cons(color).equals("ColorRGB")) {
			result = new TextAttributeReference(colors, new Color(Display.getCurrent(),
					intAt(color, 0), intAt(color, 1), intAt(color, 2)));
		}
		
		return result;
	}
}
