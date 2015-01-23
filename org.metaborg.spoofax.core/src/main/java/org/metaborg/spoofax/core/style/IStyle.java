package org.metaborg.spoofax.core.style;

import java.awt.Color;

import javax.annotation.Nullable;

/**
 * Interface for text styles.
 */
public interface IStyle {
    /**
     * @return Text color
     */
    public abstract @Nullable Color color();

    /**
     * @return Background color
     */
    public abstract @Nullable Color backgroundColor();

    /**
     * @return If text should be in bold.
     */
    public abstract boolean bold();

    /**
     * @return If text should be in italic.
     */
    public abstract boolean italic();

    /**
     * @return If text should be underscored.
     */
    public abstract boolean underscore();
}
