package org.strategoxt.imp.runtime.dynamicloading;

import static org.strategoxt.imp.runtime.dynamicloading.TermReader.*;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.imp.services.ITokenColorer;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.strategoxt.imp.runtime.services.TextAttributeMapping;
import org.strategoxt.imp.runtime.services.TextAttributeReference;
import org.strategoxt.imp.runtime.services.TextAttributeReferenceMap;
import org.strategoxt.imp.runtime.services.TokenColorer;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
class TokenColorerFactory extends AbstractServiceFactory<ITokenColorer> {
	
	@Override
	public Class<ITokenColorer> getCreatedType() {
		return ITokenColorer.class;
	}
	
	@Override
	public ITokenColorer create(Descriptor descriptor) throws BadDescriptorException {
		IStrategoAppl doc = descriptor.getDocument();
		
		List<TextAttributeMapping> tokenMappings = new ArrayList<TextAttributeMapping>();
		List<TextAttributeMapping> nodeMappings = new ArrayList<TextAttributeMapping>();
		List<TextAttributeMapping> envMappings = new ArrayList<TextAttributeMapping>();
		
		TextAttributeReferenceMap colors = readColorList(doc);
		
		for (IStrategoAppl rule : collectTerms(doc, "ColorRuleAll", "ColorRuleAllNamed")) {
			addMapping(rule, envMappings, colors);
		}
		
		for (IStrategoAppl rule : collectTerms(doc, "ColorRule", "ColorRuleNamed")) {
			IStrategoAppl pattern = termAt(rule, 0);
			if (cons(pattern).equals("Token")) {
				addMapping(rule, tokenMappings, colors);
			} else {
				addMapping(rule, nodeMappings, colors);
			}
		}
		
		return new TokenColorer(envMappings, nodeMappings, tokenMappings);
	}

	private static void addMapping(IStrategoAppl rule, List<TextAttributeMapping> mappings,
			TextAttributeReferenceMap colors) throws BadDescriptorException {
		
		IStrategoAppl attribute = termAt(rule, rule.getSubtermCount() - 1);
		
		mappings.add(TextAttributeMapping.create(rule, readAttribute(colors, attribute)));
	}

	private static TextAttributeReference readAttribute(TextAttributeReferenceMap colors, IStrategoAppl attribute) {
		if (cons(attribute).equals("AttributeRef")) {
			return new TextAttributeReference(colors, termContents(attribute));
		} else {
			assert cons(attribute).equals("Attribute");
			IStrategoAppl foreground = termAt(attribute, 0);
			IStrategoAppl background = termAt(attribute, 1);
			IStrategoAppl font = termAt(attribute, 2);
			TextAttribute result = new TextAttribute(readColor(foreground), readColor(background), readFont(font));
			return new TextAttributeReference(colors, result);
		}
	}

	private static TextAttributeReferenceMap readColorList(IStrategoAppl descriptor) throws BadDescriptorException {
		TextAttributeReferenceMap results = new TextAttributeReferenceMap();
		
		for (IStrategoAppl rule : collectTerms(descriptor, "ColorDef")) {
			String name = termContents(termAt(rule, 0));
			IStrategoAppl attribute = termAt(rule, 1);
			results.register(name, readAttribute(results, attribute));
		}
		
		for (IStrategoAppl rule : collectTerms(descriptor, "ColorRuleNamed", "ColorRuleAllNamed")) {
			String name = termContents(termAt(rule, 1));
			IStrategoAppl attribute = termAt(rule, 2);
			results.register(name, readAttribute(results, attribute));
		}
		
		results.checkAllColors();
		
		return results;
	}
	
	private static int readFont(IStrategoAppl font) {
		if (cons(font).equals("BOLD")) return SWT.BOLD;
		if (cons(font).equals("ITALIC")) return SWT.ITALIC;
		if (cons(font).equals("BOLD_ITALIC")) return SWT.BOLD | SWT.ITALIC;
		return 0;
	}

	private static Color readColor(IStrategoAppl color) {
		if (cons(color).equals("ColorDefault") || cons(color).equals("NoColor")) {
			return null;
		} else if (cons(color).equals("ColorRGB")) {
			return new Color(Display.getCurrent(), 
					intAt(color, 0), intAt(color, 1), intAt(color, 2));
		} else {
			throw new IllegalArgumentException("Unknown color of type " + cons(color));
		}
	}
}
