package org.strategoxt.imp.metatooling.loading;

/**
 * Proxy class to keep compatibility with old Ant scripts.
 */
public class AntDescriptorLoader {
    public static void main(String[] args) {
        org.metaborg.spoofax.eclipse.ant.AntDescriptorLoader.main(args);
    }
}
