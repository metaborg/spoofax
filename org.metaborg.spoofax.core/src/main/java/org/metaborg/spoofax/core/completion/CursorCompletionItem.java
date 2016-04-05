package org.metaborg.spoofax.core.completion;

import org.metaborg.core.completion.ICursorCompletionItem;

public class CursorCompletionItem implements ICursorCompletionItem {
    @Override public String toString() {
        return "<cursor>";
    }
}