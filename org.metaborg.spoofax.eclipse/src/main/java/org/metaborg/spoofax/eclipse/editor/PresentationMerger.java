package org.metaborg.spoofax.eclipse.editor;

import java.util.Collection;

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextPresentationListener;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.custom.StyleRange;
import org.metaborg.spoofax.eclipse.util.StyleUtils;

public class PresentationMerger implements ITextPresentationListener {
    private volatile TextPresentation sourcePresentation;
    private volatile Collection<StyleRange> styleRanges;


    public void set(TextPresentation presentation) {
        // Make a deep copy of style ranges to prevent sharing with other ITextPresentationListeners.
        styleRanges = StyleUtils.deepCopies(presentation);
        sourcePresentation = presentation;
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

        final IRegion extent = targetPresentation.getExtent();
        final int min = extent.getOffset();
        final int max = min + extent.getLength();
        for(StyleRange styleRange : styleRanges) {
            final int styleRangeEnd = styleRange.start + styleRange.length;
            // Not allowed to change style ranges outside of extent. Safe to skip since they will not be redrawn.
            if(styleRange.start < min || styleRangeEnd > max) {
                continue;
            }
            targetPresentation.mergeStyleRange(styleRange);
        }
    }
}
