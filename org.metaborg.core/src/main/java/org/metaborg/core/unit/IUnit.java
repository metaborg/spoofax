package org.metaborg.core.unit;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;

/**
 * Results of operations on a single source file. Extensible through unit contributions.
 */
public interface IUnit {
    /**
     * @return Source file that operations were executed on, or <code>null</code> if the source file is unknown. The
     *         source file can be unknown when executing operations on a string of text in memory for example. When the
     *         source is unknown, this unit is detached.
     */
    @Nullable FileObject source();

    /**
     * @return True if the unit is detached, i.e. the source is unknown. False otherwise.
     */
    boolean detached();

    /**
     * Returns a unit contribution with given identifier.
     * 
     * @param id
     *            Unit contribution identifier.
     * @return Unit contribution with given identifier, or null if it does not exist.
     */
    @Nullable IUnitContrib unitContrib(String id);

    /**
     * @return All unit contributions.
     */
    Iterable<IUnitContrib> unitContribs();
}
