package org.metaborg.spoofax.core.processing;

import org.apache.commons.vfs2.FileObject;

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
     */
    public abstract BuildOutput<P, A, T> build(BuildInput input);

    /**
     * Cleans derived resources and contexts from given location.
     * 
     * @param location
     *            Location to clean.
     */
    public abstract void clean(FileObject location);
}