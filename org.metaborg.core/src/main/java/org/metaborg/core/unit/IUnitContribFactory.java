package org.metaborg.core.unit;


public interface IUnitContribFactory<T> {
    IUnitContrib<T> create();

    IUnitContrib<T> create(T value);

    /*
     * TODO: composition
     * 
     * IUnitContrib<C> compose(Iterable<IUnitContrib<T>> contribs);
     * 
     * Iterable<IUnitContrib<T>> decompose(IUnitContrib<C> composed);
     */

    /*
     * TODO: serialization
     * 
     * OutputStream serialize(IUnitContrib<T> contrib);
     * 
     * IUnitContrib<T> deserialize(InputStream stream);
     */
}
