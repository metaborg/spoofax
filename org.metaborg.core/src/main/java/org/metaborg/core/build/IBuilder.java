package org.metaborg.core.build;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelector;
import org.metaborg.core.MetaborgRuntimeException;

/**
 * Incrementally parses, analyses, and compiles source files.
 * 
 * @param <P>
 *            Type of parsed fragments.
 * @param <A>
 *            Type of analyzed fragments.
 * @param <T>
 *            Type of transformed fragments.
 */
public interface IBuilder<P, A, T> {
    /**
     * Parses, analyses, and compiles changed resources.
     * 
     * @param input
     *            Build input.
     * @return Result of building.
     * @throws MetaborgRuntimeException
     *             When {@code input.throwOnErrors} is set to true and errors occur.
     */
    public abstract IBuildOutput<P, A, T> build(BuildInput input);

    /**
     * Cleans derived resources and contexts from given location.
     * 
     * @param location
     *            Location to clean.
     */
    public abstract void clean(FileObject location, FileSelector excludeSelector);
}