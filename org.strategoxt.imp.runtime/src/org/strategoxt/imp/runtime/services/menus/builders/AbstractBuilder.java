package org.strategoxt.imp.runtime.services.menus.builders;

/**
 * @author Oskar van Rest
 */
public abstract class AbstractBuilder implements IBuilder{

	@Override
	public int getContributionType() {
		return IMenuContribution.BUILDER;
	}
}
