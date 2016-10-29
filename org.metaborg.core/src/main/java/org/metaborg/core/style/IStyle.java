package org.metaborg.core.style;

import java.awt.Color;
import java.io.Serializable;

import javax.annotation.Nullable;

/**
 * Interface for text styles.
 */
public interface IStyle extends Serializable {
    /**
     * @return Text color
     */
    @Nullable Color color();

    /**
     * @return Background color
     */
    @Nullable Color backgroundColor();

    /**
     * @return If text should be in bold.
     */
    boolean bold();

    /**
     * @return If text should be in italic.
     */
    boolean italic();

    /**
     * @return If text should be underscored.
     */
    boolean underscore();
    
    /**
     * @return If text should be strikeout.
     */
    boolean strikeout();
    
}
