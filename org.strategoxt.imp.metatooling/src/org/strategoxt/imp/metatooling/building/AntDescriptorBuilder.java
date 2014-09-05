package org.strategoxt.imp.metatooling.building;

/**
 * Proxy class to keep compatibility with old Ant scripts.
 */
public class AntDescriptorBuilder {
    public static void main(String[] args) {
        org.metaborg.spoofax.eclipse.ant.AntDescriptorBuilder.main(args);
    }

    public static boolean isActive() {
        return org.metaborg.spoofax.eclipse.ant.AntDescriptorBuilder.isActive();
    }
}
