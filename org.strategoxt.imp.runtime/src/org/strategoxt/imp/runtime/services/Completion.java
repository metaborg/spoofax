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

	private static final int BLANK_LINE_REQUIRED = 1;
	private static final int LINK_PLACEHOLDERS = 2;
	private static final int KEYWORD = 4;
	private static final int TEMPLATE = 8;
	private static final int SEMANTIC = 16;

	public static Completion makeKeyword(String literal) {
		return new Completion(literal, null, null, KEYWORD, null, keywordColor);
	}

	// for testing
	static Completion makeTemplate(String prefix, String sort) {
		return new Completion(prefix, sort, null, TEMPLATE, null, keywordColor);
	}

	// for testing
	static Completion makeTemplate(String prefix, String sort, boolean blankLineRequired) {
		int flags = TEMPLATE;
		if (blankLineRequired) flags |= BLANK_LINE_REQUIRED;
		return new Completion(prefix, sort, null, flags, null, keywordColor);
	}

	public static Completion makeTemplate(String prefix, String sort, IStrategoList completionParts, boolean blankLineRequired, boolean linkPlaceholders) {
		int flags = TEMPLATE;
		if (blankLineRequired) flags |= BLANK_LINE_REQUIRED;
		if (linkPlaceholders)  flags |= LINK_PLACEHOLDERS;
		return new Completion(prefix, sort, completionParts, flags, null, keywordColor);
	}
	
	// for testing
	static Completion makeSemantic(String prefix, String description) {
		return new Completion(prefix, null, null, SEMANTIC, description, null);
	}

	public static Completion makeSemantic(IStrategoList completionParts, String description) {
		final LazyColor color = completionParts.size() == 1 ? identifierColor : null; // identifier proposal?
		return new Completion(null, null, completionParts, SEMANTIC, description, color);
	}

	private final String prefix;

	private final String sort;

	private final IStrategoList newTextParts;

	private final String newText;

	private final int flags;

	private final String description;

	private final String name;

	private final LazyColor color;

	// prefix overrides the default prefix (calculated from newTextParts)
	protected Completion(String prefix, String sort, IStrategoList newTextParts, int flags, String description, LazyColor color) {
		this.prefix = prefix != null ? prefix : getPrefix(newTextParts);
		this.sort = sort;
		this.newTextParts = newTextParts;
		this.newText = buildNewText();
		this.flags = flags;
		this.description = description;
		this.name = prefix != null && prefix.matches(".*[a-zA-Z].*") ? prefix : buildName();
		this.color = color;
	}

	private static String getPrefix(IStrategoList completionParts) {
		IStrategoTerm prefixTerm = completionParts.head();
		boolean noPrefix = prefixTerm.getTermType() != IStrategoTerm.STRING
				&& !"String".equals(cons(prefixTerm));
		return noPrefix ? "" : termContents(prefixTerm);
	}

	private String buildNewText() {
		if (newTextParts == null)
			return null;
		StringBuilder result = new StringBuilder();
		for (IStrategoTerm part : newTextParts.getAllSubterms()) {
			if ("Placeholder".equals(cons(part)) || "PlaceholderWithSort".equals(cons(part))) {
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

	private String buildName() {
		if (newText == null || newText.indexOf("\n") != -1 || newText.indexOf("\t") != -1) {
			return prefix.replace("\\n", "").replace("\\t", "  ");
		}
		else {
			return newText;
		}
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

	public final String getNewText() {
		return newText;
	}

	public final boolean isBlankLineRequired() {
		return (flags & BLANK_LINE_REQUIRED) != 0;
	}

	public final boolean shouldLinkPlaceholders() {
		return (flags & LINK_PLACEHOLDERS) != 0;
	}

	public final boolean isKeyword() {
		return (flags & KEYWORD) != 0;
	}

	public final boolean isTemplate() {
		return (flags & TEMPLATE) != 0;
	}

	public final boolean isSemantic() {
		return (flags & SEMANTIC) != 0;
	}

	public String getDescription() {
		return description != null ? description : getNewText();
	}

	public String getName() {
		return name;
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

}
