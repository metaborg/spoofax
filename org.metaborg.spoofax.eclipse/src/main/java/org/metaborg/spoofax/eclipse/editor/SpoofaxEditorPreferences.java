package org.metaborg.spoofax.eclipse.editor;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

public class SpoofaxEditorPreferences {
    public static final String id = SpoofaxEditor.id + ".prefs";

    private static final String bracketMatching = id + ".bracketmatching";
    public static final String bracketMatchingEnabled = bracketMatching + ".enabled";
    public static final String bracketMatchingColor = bracketMatching + ".color";
    public static final String bracketMatchingHighlightAtCaret = bracketMatching + ".highlight-at-caret";
    public static final String bracketMatchingHighlightPeers = bracketMatching + ".highlight-peers";


    public static void setDefaults(IPreferenceStore prefs) {
        prefs.setDefault(bracketMatchingEnabled, true);
        prefs.setDefault(bracketMatchingColor, "128, 128, 128");
        prefs.setDefault(bracketMatchingHighlightAtCaret, true);
        prefs.setDefault(bracketMatchingHighlightPeers, false);
    }


    protected static void setPairMatcherKeys(SourceViewerDecorationSupport support) {
        support.setMatchingCharacterPainterPreferenceKeys(bracketMatchingEnabled, bracketMatchingColor,
            bracketMatchingHighlightAtCaret, bracketMatchingHighlightPeers);
    }
}
