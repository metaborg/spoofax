package org.metaborg.spoofax.build.cleardep;

import java.io.Serializable;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.output.BuildOutput;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;

abstract public class SpoofaxBuilder<In extends SpoofaxInput, Out extends BuildOutput> extends Builder<In, Out> {

	public static abstract class SpoofaxBuilderFactory<In extends SpoofaxInput, Out extends BuildOutput, B extends Builder<In, Out>>
		implements BuilderFactory<In, Out, B> {
		private static final long serialVersionUID = 8998843329413855827L;

		@Override
		public abstract B makeBuilder(In input);
	}
	
	public static class SpoofaxInput implements Serializable{
		private static final long serialVersionUID = -6362900996234737307L;
		public final SpoofaxContext context;
		public SpoofaxInput(SpoofaxContext context) {
			this.context = context;
		}
	}
	
	protected final SpoofaxContext context;
	
	public SpoofaxBuilder(In input) {
		super(input);
		this.context = input.context;
	}

	@Override
	protected Stamper defaultStamper() {
		return LastModifiedStamper.instance;
	}
}
