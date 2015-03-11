package org.metaborg.spoofax.eclipse.editor;

import java.util.Collection;

import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.custom.StyleRange;
import org.metaborg.spoofax.eclipse.util.StyleUtils;

public class PresentationMerger implements ITextPresentationListener {
    private volatile TextPresentation sourcePresentation;
    private volatile Collection<StyleRange> styleRanges;


    public void set(TextPresentation presentation) {
        sourcePresentation = presentation;
        // Make a deep copy of style ranges to prevent sharing with other ITextPresentationListeners.
        styleRanges = StyleUtils.deepCopies(presentation);
    }

    public void invalidate() {
        sourcePresentation = null;
        styleRanges = null;
    }


    @Override public void applyTextPresentation(TextPresentation targetPresentation) {
        // No need to apply text presentation if source and target presentation are the same object.
        if(sourcePresentation == null || targetPresentation == sourcePresentation) {
            return;
        }

        // Merge style ranges inside the default style range from the source presentation to the target presentation.
        final StyleRange defaultStyleRange = targetPresentation.getDefaultStyleRange();
        final int defaultStyleEnd = defaultStyleRange.start + defaultStyleRange.length;
        for(StyleRange styleRange : styleRanges) {
            final int styleRangeEnd = styleRange.start + styleRange.length;
            // It is not allowed to change style ranges outside of the default range. Safe to skip since they will not
            // be redrawn.
            if(styleRange.start < defaultStyleRange.start || styleRangeEnd > defaultStyleEnd) {
                continue;
            }
            targetPresentation.mergeStyleRange(styleRange);
        }
    }
}
