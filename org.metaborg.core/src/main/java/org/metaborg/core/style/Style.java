package org.metaborg.core.style;

import java.awt.Color;

import javax.annotation.Nullable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Style implements IStyle {
    private static final long serialVersionUID = -8435127070824031921L;

    private final @Nullable Color color;
    private final @Nullable Color backgroundColor;
    private final boolean bold;
    private final boolean italic;
    private final boolean underscore;
    private final boolean strikeout;


    public Style(@Nullable Color color, @Nullable Color backgroundColor, boolean bold, boolean italic,
        boolean underscore, boolean strikeout) {
        this.color = color;
        this.backgroundColor = backgroundColor;
        this.bold = bold;
        this.italic = italic;
        this.underscore = underscore;
        this.strikeout = strikeout;
    }


    @Override public @Nullable Color color() {
        return color;
    }

    @Override public @Nullable Color backgroundColor() {
        return backgroundColor;
    }

    @Override public boolean bold() {
        return bold;
    }

    @Override public boolean italic() {
        return italic;
    }

    @Override public boolean underscore() {
        return underscore;
    }

    @Override public boolean strikeout() {
        return strikeout;
    }


    @Override public int hashCode() {
        // @formatter:off
        return new HashCodeBuilder(17, 37)
            .append(this.color)
            .append(this.backgroundColor)
            .append(this.bold)
            .append(this.italic)
            .append(this.underscore)
            .append(this.strikeout)
            .toHashCode();
        // @formatter:on
    }

    @Override public boolean equals(Object obj) {
        if(obj == this)
            return true;
        if(!(obj instanceof Style))
            return false;

        final Style other = (Style) obj;
        // @formatter:off
        return new EqualsBuilder()
            .append(this.color, other.color)
            .append(this.backgroundColor, other.backgroundColor)
            .append(this.bold, other.bold)
            .append(this.italic, other.italic)
            .append(this.underscore, other.underscore)
            .append(this.strikeout, other.strikeout)
            .isEquals();
        // @formatter:on
    }

    @Override public String toString() {
        return String.format("Style [color=%s, backgroundColor=%s, bold=%s, italic=%s, underscore=%s, strikeout=%s]", color,
            backgroundColor, bold, italic, underscore, strikeout);
    }
}
