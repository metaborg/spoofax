package org.strategoxt.imp.metatooling.stratego;

import java.io.IOException;

import org.eclipse.core.runtime.Platform;
import org.strategoxt.imp.runtime.Environment;
import org.strategoxt.stratego_xtc.xtc_command_1_0;
import org.strategoxt.strc.parse_stratego_file_0_0;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class MetaSPILibrary {
    public static void init() {
        try {
            nativeBundle.init();
        } catch(IOException e) {
            Environment.logException(
                "Could not determine the binary path for the native tool bundle (" + Platform.getOS() + "/"
                    + Platform.getOSArch() + ")", e);
        } catch(RuntimeException e) {
            Environment.logException("Failed to initialize the native tool bundle (" + Platform.getOS() + "/"
                + Platform.getOSArch() + ")", e);
        }
    }
}
