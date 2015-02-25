package org.metaborg.spoofax.eclipse.util;

import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.metaborg.spoofax.core.messages.ISourceRegion;
import org.metaborg.spoofax.core.style.IRegionStyle;
import org.metaborg.spoofax.core.style.IStyle;
import org.spoofax.interpreter.terms.IStrategoTerm;

/**
 * Utility functions for creating Eclipse text styles.
 */
public final class StyleUtils {
    /**
     * Creates an Eclipse text presentation from given Spoofax styles.
     * 
     * @param styles
     *            Stream of Spoofax styles.
     * @param display
     *            Display to create the Eclipse text presentation on.
     * @return Eclipse text presentation.
     */
    public static TextPresentation
        createTextPresentation(Iterable<IRegionStyle<IStrategoTerm>> styles, Display display) {
        final TextPresentation presentation = new TextPresentation();
        for(IRegionStyle<IStrategoTerm> regionStyle : styles) {
            presentation.addStyleRange(createStyleRange(regionStyle, display));
        }
        return presentation;
    }

    /**
     * Creates an Eclipse style range from given Spoofax style region.
     * 
     * @param regionStyle
     *            Spoofax style region.
     * @param display
     *            Display to create the Eclipse style range on.
     * @return Eclipse style range.
     */
    public static StyleRange createStyleRange(IRegionStyle<IStrategoTerm> regionStyle, Display display) {
        final IStyle style = regionStyle.style();
        final ISourceRegion region = regionStyle.region();

        final StyleRange styleRange = new StyleRange();
        final java.awt.Color foreground = style.color();
        if(foreground != null) {
            styleRange.foreground = createColor(foreground, display);
        }
        final java.awt.Color background = style.backgroundColor();
        if(background != null) {
            styleRange.background = createColor(background, display);
        }
        if(style.bold()) {
            styleRange.fontStyle |= SWT.BOLD;
        }
        if(style.italic()) {
            styleRange.fontStyle |= SWT.ITALIC;
        }
        if(style.underscore()) {
            styleRange.underline = true;
        }

        styleRange.start = region.startOffset();
        styleRange.length = region.endOffset() - region.startOffset() + 1;

        return styleRange;
    }

    /**
     * Creates an Eclipse color from given Java color.
     * 
     * @param color
     *            Java color.
     * @param display
     *            Display to create the color on.
     * @return Eclipse color.
     */
    public static Color createColor(java.awt.Color color, Display display) {
        // GTODO: this color object needs to be disposed manually!
        return new Color(display, color.getRed(), color.getGreen(), color.getBlue());
    }
}
