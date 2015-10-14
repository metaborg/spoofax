package org.strategoxt.imp.metatooling.stratego;

import org.strategoxt.lang.GlobalLibraries;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class MetaSPILibrary {
    public static void init() {
    	// Eclipse Library should be loaded in any stratego program now
        GlobalInitializers.registerLibraryClass(
        		org.strategoxt.imp.metatooling.stratego.LibraryInitializer.class);
    }
}
