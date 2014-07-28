package org.strategoxt.imp.runtime.services.menus.model;

/**
 * @author Oskar van Rest
 */
public abstract class AbstractBuilder implements IBuilder{

	@Override
	public int getContributionType() {
		return IMenuContribution.BUILDER;
	}
}
