package org.metaborg.spoofax.eclipse.util;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.text.TextPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.metaborg.spoofax.core.messages.ISourceRegion;
import org.metaborg.spoofax.core.style.IRegionStyle;
import org.metaborg.spoofax.core.style.IStyle;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.Lists;

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

    /**
     * Creates a deep copy of given style range.
     * 
     * @param styleRangeRef
     *            Style range to copy.
     * @return Deep copy of given style range.
     */
    public static StyleRange deepCopy(StyleRange styleRangeRef) {
        final StyleRange styleRange = new StyleRange(styleRangeRef);
        styleRange.start = styleRangeRef.start;
        styleRange.length = styleRangeRef.length;
        styleRange.fontStyle = styleRangeRef.fontStyle;
        return styleRange;
    }

    /**
     * Creates deep copies of style ranges in given text presentation.
     * 
     * @param presentation
     *            Text presentation to copy style ranges of.
     * @return Collection of deep style range copies.
     */
    public static Collection<StyleRange> deepCopies(TextPresentation presentation) {
        final Collection<StyleRange> styleRanges = Lists.newLinkedList();
        for(@SuppressWarnings("unchecked") Iterator<StyleRange> iter = presentation.getNonDefaultStyleRangeIterator(); iter
            .hasNext();) {
            final StyleRange styleRange = iter.next();
            styleRanges.add(deepCopy(styleRange));
        }
        return styleRanges;
    }

    /**
     * Converts given style range to a string.
     * 
     * @param range
     *            Style range to convert.
     * @return String representation of style range.
     */
    public static String styleRangeToString(StyleRange range) {
        final StringBuilder sb = new StringBuilder();
        sb.append("StyleRange[");
        sb.append("start = " + range.start);
        sb.append("length = " + range.length);
        sb.append("underline = " + range.underline);
        sb.append("underlineStyle  = " + range.underlineStyle);
        sb.append("foreground = " + range.foreground);
        sb.append("]");
        return sb.toString();
    }
}
