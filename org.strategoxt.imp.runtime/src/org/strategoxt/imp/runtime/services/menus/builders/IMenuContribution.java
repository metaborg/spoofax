package org.strategoxt.imp.runtime.services.menus.builders;

public interface IMenuContribution {

	public static final int MENU = 1;
	public static final int BUILDER = 2;
	public static final int SEPARATOR = 3;
	
	String getCaption();
	
	int getContributionType();
}
