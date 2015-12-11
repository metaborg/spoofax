package org.metaborg.core;

/**
 * Builder of objects.
 */
@Deprecated
public interface IObjectBuilder<T> {

    /**
     * Builds the object.
     *
     * @return The built object.
     * @throws IllegalStateException The builder state is not valid,
     * i.e. {@link #isValid()} returned <code>false</code>.
     */
    T build() throws IllegalStateException;

    /**
     * Determines whether the builder's state is valid.
     *
     * @return <code>true</code> when the builder's state is valid;
     * otherwise, <code>false</code>.
     */
    boolean isValid();

    /**
     * Resets the values of this builder.
     *
     * @return This builder.
     */
    void reset();

    /**
     * Copies the values from the specified object.
     *
     * @param obj The object to copy values from.
     */
    void copyFrom(T obj);
}
