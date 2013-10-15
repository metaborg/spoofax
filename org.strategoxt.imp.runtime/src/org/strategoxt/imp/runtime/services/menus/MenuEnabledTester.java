package org.strategoxt.imp.runtime.services.menus;

import org.eclipse.core.expressions.PropertyTester;

public class MenuEnabledTester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		return true;
	}
}
