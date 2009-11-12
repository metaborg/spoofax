package org.strategoxt.imp.metatooling.stratego;

import org.strategoxt.imp.nativebundle.SDFBundleCommand;
import org.strategoxt.stratego_xtc.xtc_command_1_0;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class MetaIMPLibrary {
	public static void init() {
		// It's a small library, okay?
		xtc_command_1_0.instance = new SDFBundleCommand();
	}
}
