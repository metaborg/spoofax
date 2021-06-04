package org.metaborg.spoofax.core.stratego.primitive.generic;

import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.AbstractStrategoOperatorRegistry;

public class GenericPrimitiveLibrary extends AbstractStrategoOperatorRegistry {
    private final String name;


    public GenericPrimitiveLibrary(Iterable<AbstractPrimitive> primitives, String name) {
        this.name = name;

        for(AbstractPrimitive primitive : primitives) {
            add(primitive);
        }
    }


    protected void onDuplicatePrimitiveAddition(AbstractPrimitive first, AbstractPrimitive second) {
        throw new RuntimeException("Attempted to add second primitives with name "
            + first.getName() + " to OperatorRegistry " + name + ". \n" + "First:\n" + first + "\nSecond:\n" + second);
    }

    @Override public String getOperatorRegistryName() {
        return name;
    }
}
