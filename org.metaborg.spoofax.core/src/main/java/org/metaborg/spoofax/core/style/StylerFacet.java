package org.metaborg.spoofax.core.style;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import org.metaborg.core.language.IFacet;
import org.metaborg.core.style.IStyle;

public class StylerFacet implements IFacet {
    private final Map<SortConsCategory, IStyle> sortConsToStyle = new HashMap<>();
    private final Map<String, IStyle> consToStyle = new HashMap<>();
    private final Map<String, IStyle> sortToStyle = new HashMap<>();
    private final Map<String, IStyle> tokenToStyle = new HashMap<>();


    public boolean hasSortConsStyle(String sort, String cons) {
        return sortConsToStyle.containsKey(new SortConsCategory(sort, cons));
    }

    public boolean hasConsStyle(String cons) {
        return consToStyle.containsKey(cons);
    }

    public boolean hasSortStyle(String sort) {
        return sortToStyle.containsKey(sort);
    }

    public boolean hasTokenStyle(String builtin) {
        return tokenToStyle.containsKey(builtin);
    }


    public @Nullable IStyle sortConsStyle(String sort, String cons) {
        return sortConsToStyle.get(new SortConsCategory(sort, cons));
    }

    public @Nullable IStyle consStyle(String cons) {
        return consToStyle.get(cons);
    }

    public @Nullable IStyle sortStyle(String sort) {
        return sortToStyle.get(sort);
    }

    public @Nullable IStyle tokenStyle(String builtin) {
        return tokenToStyle.get(builtin);
    }


    public void mapSortConsToStyle(String sort, String cons, IStyle style) {
        sortConsToStyle.put(new SortConsCategory(sort, cons), style);
    }

    public void mapConsToStyle(String cons, IStyle style) {
        consToStyle.put(cons, style);
    }

    public void mapSortToStyle(String sort, IStyle style) {
        sortToStyle.put(sort, style);
    }

    public void mapTokenToStyle(String builtin, IStyle style) {
        tokenToStyle.put(builtin, style);
    }
}
