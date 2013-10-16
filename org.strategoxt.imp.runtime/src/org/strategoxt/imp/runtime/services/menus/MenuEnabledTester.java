package org.strategoxt.imp.runtime.services.menus;

import org.eclipse.core.expressions.PropertyTester;

public class MenuEnabledTester extends PropertyTester {

	private boolean again;
	private Object oldEditor;
	private int startup;
	
	
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

		if (property.equals("menuEnabledStartup")) {
			if (startup < MenusServiceConstants.NO_OF_TOOLBAR_MENUS) {
				startup ++;
				return true;
			}
			return false;
		}
		
		if (oldEditor == null || oldEditor != receiver || again) {
			oldEditor = receiver;
			
			again = !again;
			
			return false;
		}
		
		return true;
	}
}
