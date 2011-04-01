package org.strategoxt.imp.runtime.services;

import static org.spoofax.interpreter.core.Tools.termAt;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.cons;
import static org.strategoxt.imp.runtime.dynamicloading.TermReader.termContents;

import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.TextStyle;
import org.spoofax.interpreter.terms.IStrategoList;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Objects of this class are blueprints for different proposal types.
 *
 * @author Tobi Vollebregt
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class Completion {

	private static LazyColor keywordColor = new LazyColor(127, 0, 85);

	private static LazyColor identifierColor = new LazyColor(64, 64, 255);

	public static Completion makeKeyword(String literal) {
		return new Completion(literal, null, null, false, null, keywordColor);
	}

	public static Completion makeTemplate(String prefix, String sort, IStrategoList completionParts, boolean blankLineRequired) {
		return new Completion(prefix, sort, completionParts, blankLineRequired, null, keywordColor);
	}

	public static Completion makeSemantic(IStrategoList completionParts, String description) {
		final String prefix = ((IStrategoString) completionParts.head()).stringValue();
		final LazyColor color = completionParts.size() == 1 ? identifierColor : null; // identifier proposal?
		return new Completion(prefix, null, completionParts, false, description, color);
	}

	private final String prefix;

	private final String sort;

	private final IStrategoList newTextParts;

	private final boolean blankLineRequired;

	private final String description;

	private final LazyColor color;

	protected Completion(String prefix, String sort, IStrategoList newTextParts, boolean blankLineRequired, String description, LazyColor color) {
		this.prefix = prefix;
		this.sort = sort;
		this.newTextParts = newTextParts;
		this.blankLineRequired = blankLineRequired;
		this.description = description;
		this.color = color;
	}

	public final String getPrefix() {
		return prefix;
	}

	public final String getSort() {
		return sort;
	}

	public final IStrategoList getNewTextParts() {
		return newTextParts;
	}

	public final boolean isBlankLineRequired() {
		return blankLineRequired;
	}

	public String getDescription() {
		return description != null ? description : getNewText();
	}

	public String getName() {
		final String newText = getNewText();
		if (newText == null || newText.indexOf("\n") != -1 || newText.indexOf("\t") != -1) {
			return getPrefix().replace("\\n", "").replace("\\t", "  ");
		}
		return newText;
	}

	public StyledString getStyledName() {
		final String name = getName();
		return new StyledString(name, new StyledString.Styler() {
			@Override
			public void applyStyles(TextStyle textStyle) {
				if (color != null) {
					textStyle.foreground = color.get();
				}
			}
		});
	}

	public final String getNewText() {
		if (newTextParts == null)
			return null;
		StringBuilder result = new StringBuilder();
		for (IStrategoTerm part : newTextParts.getAllSubterms()) {
			if ("Placeholder".equals(cons(part))) {
				IStrategoString placeholder = termAt(part, 0);
				String contents = placeholder.stringValue();
				contents = contents.substring(1, contents.length() - 1); // strip < >
				result.append(contents);
			}
			else if ("Cursor".equals(cons(part))) {
				// do nothing
			}
			else {
				result.append(termContents(part));
			}
		}
		return AutoEditStrategy.formatInsertedText(result.toString(), "");
	}

}
