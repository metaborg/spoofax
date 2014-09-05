package org.strategoxt.imp.metatooling.building;

/**
 * Proxy class to keep compatibility with old Ant scripts.
 */
public class AntForceRefreshScheduler {
    public static void main(String[] args) throws Exception {
        org.metaborg.spoofax.eclipse.ant.AntForceRefreshScheduler.main(args);
    }
}
