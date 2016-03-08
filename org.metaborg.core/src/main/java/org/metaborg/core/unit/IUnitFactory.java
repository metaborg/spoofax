package org.metaborg.core.unit;


public interface IUnitFactory {
    IUnit create();

    <T> IUnit create(IUnitContrib<T> contrib);

    IUnit create(IUnitContrib<?>... contribs);


    /*
     * TODO: serialization
     * 
     * OutputStream serialize(IUnit unit);
     * 
     * IUnit deserialize(InputStream stream);
     */
}
