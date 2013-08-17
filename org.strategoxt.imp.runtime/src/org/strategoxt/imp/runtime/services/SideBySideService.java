package org.strategoxt.imp.runtime.services;

import org.stratego.imp.runtime.services.sidebyside.latest.LatestSidePaneEditorHelper;
import org.stratego.imp.runtime.services.sidebyside.main.SidePaneEditorHelper;


public class SideBySideService {
	
	private static SidePaneEditorHelper helper;
	
	public static SidePaneEditorHelper helperInstance() {
		helper = new LatestSidePaneEditorHelper();
		return helper;
	}
	
}
