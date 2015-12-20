package org.metaborg.spoofax.meta.core.pluto;

import java.io.Serializable;

public class SpoofaxInput implements Serializable {
    private static final long serialVersionUID = -6362900996234737307L;

    public final SpoofaxContext context;


    public SpoofaxInput(SpoofaxContext context) {
        this.context = context;
    }
}
