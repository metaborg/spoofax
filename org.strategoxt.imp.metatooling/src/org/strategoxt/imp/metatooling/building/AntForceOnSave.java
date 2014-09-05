package org.strategoxt.imp.metatooling.building;

/**
 * Proxy class to keep compatibility with old Ant scripts.
 */
public class AntForceOnSave {
    public static void main(String[] args) {
        org.metaborg.spoofax.eclipse.ant.AntForceOnSave.main(args);
    }
}
