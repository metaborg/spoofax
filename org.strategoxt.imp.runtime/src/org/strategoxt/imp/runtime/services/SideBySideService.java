package org.strategoxt.imp.runtime.services;

import org.stratego.imp.runtime.services.sidebyside.latest.LatestSidePaneEditorHelper;
import org.stratego.imp.runtime.services.sidebyside.legacy.LegacySidePaneEditorHelper;
import org.stratego.imp.runtime.services.sidebyside.main.SidePaneEditorHelper;
import org.strategoxt.imp.runtime.Environment;


public class SideBySideService {
	
	private static SidePaneEditorHelper helper;
	
	public static SidePaneEditorHelper helperInstance() {
		if(helper == null){
			try{
				helper = new LegacySidePaneEditorHelper();
			} catch(Throwable t) {
				Environment.logWarning("Legacy side-by-side not supported. Loading dummy.");
				helper = new LatestSidePaneEditorHelper();
			}
		}
		return helper;
	}
	
}
