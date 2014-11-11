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

	private static final boolean IGNORE_TEMPLATE_PREFIX_CASE = true;

	private static LazyColor keywordColor = new LazyColor(127, 0, 85);

	private static LazyColor identifierColor = new LazyColor(64, 64, 255);

	private static final int BLANK_LINE_REQUIRED = 1;
	private static final int LINK_PLACEHOLDERS = 2;
	private static final int KEYWORD = 4;
	private static final int TEMPLATE = 8;
	private static final int SEMANTIC = 16;

	public static Completion makeKeyword(String literal) {
		return new Completion(literal, null, false, null, KEYWORD, null, keywordColor);
	}

	// for testing
	static Completion makeTemplate(String prefix, String sort) {
		return new Completion(prefix, sort, false, null, TEMPLATE, null, keywordColor);
	}

	// for testing
	static Completion makeTemplate(String prefix, String sort, boolean blankLineRequired) {
		int flags = TEMPLATE;
		if (blankLineRequired) flags |= BLANK_LINE_REQUIRED;
		return new Completion(prefix, sort, false, null, flags, null, keywordColor);
	}

	public static Completion makeTemplate(String prefix, String sort, boolean isListSort, IStrategoList completionParts, boolean blankLineRequired, boolean linkPlaceholders) {
		int flags = TEMPLATE;
		if (blankLineRequired) flags |= BLANK_LINE_REQUIRED;
		if (linkPlaceholders)  flags |= LINK_PLACEHOLDERS;
		return new Completion(prefix, sort, isListSort, completionParts, flags, null, keywordColor);
	}
	
	// for testing
	static Completion makeSemantic(String prefix, String description) {
		return new Completion(prefix, null, false, null, SEMANTIC, description, null);
	}

	public static Completion makeSemantic(IStrategoList completionParts, String description) {
		final LazyColor color = completionParts.size() == 1 ? identifierColor : null; // identifier proposal?
		return new Completion(null, null, false, completionParts, SEMANTIC, description, color);
	}

	private final String prefix;

	private final String sort;

	private final boolean isListSort;

	private final IStrategoList newTextParts;

	private final String newText;

	private final int flags;

	private final String description;

	private final String name;

	private final LazyColor color;

	// prefix overrides the default prefix (calculated from newTextParts)
	protected Completion(String prefix, String sort, boolean isListSort, IStrategoList newTextParts, int flags, String description, LazyColor color) {
		this.prefix = prefix != null ? prefix : getPrefix(newTextParts);
		this.sort = sort;
		this.isListSort = isListSort;
		this.newTextParts = newTextParts;
		this.flags = flags;
		this.description = description;
		this.color = color;
		this.newText = buildNewText();
		this.name = prefix != null && prefix.matches(".*[a-zA-Z].*") ? prefix : buildName();
	}

	private static String getPrefix(IStrategoList completionParts) {
		IStrategoTerm prefixTerm = completionParts.head();
		boolean noPrefix = prefixTerm.getTermType() != IStrategoTerm.STRING
				&& !"String".equals(cons(prefixTerm));
		return noPrefix ? "" : termContents(prefixTerm);
	}

	private String buildNewText() {
		if (newTextParts == null){
			if(this.isKeyword()){
				return this.prefix;
			}
			return null;
		}
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

	public boolean isListSort() {
		return this.getSort() != null && this.isListSort;
	}

	public boolean extendsPrefix(String completionPrefix){
		return getPrefix().regionMatches(IGNORE_TEMPLATE_PREFIX_CASE, 0, completionPrefix, 0, completionPrefix.length());		
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
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((newText == null) ? 0 : newText.hashCode());
		result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
		result = prime * result + ((sort == null) ? 0 : sort.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Completion other = (Completion) obj;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (newText == null) {
			if (other.newText != null)
				return false;
		} else if (!newText.equals(other.newText))
			return false;
		if (prefix == null) {
			if (other.prefix != null)
				return false;
		} else if (!prefix.equals(other.prefix))
			return false;
		if (sort == null) {
			if (other.sort != null)
				return false;
		} else if (!sort.equals(other.sort))
			return false;
		return true;
	}
    
    @Override
    public String toString() {
    	String listSortMarker = isListSort? "*" : ""; 
    	return sort + listSortMarker + ": \"" + prefix + "\" = "  + newText;
    }
}
