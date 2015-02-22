package org.metaborg.spoofax.build.cleardep;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;

abstract public class SpoofaxBuilder<T extends SpoofaxInput> extends Builder<T, SimpleCompilationUnit> {

	public static abstract class SpoofaxBuilderFactory<T extends SpoofaxInput, B extends SpoofaxBuilder<T>> implements BuilderFactory<T, SimpleCompilationUnit, B> {
		public abstract B makeBuilder(T input);
	}
	
	public static class SpoofaxInput {
		public final SpoofaxContext context;
		public SpoofaxInput(SpoofaxContext context) {
			this.context = context;
		}
	}
	
	protected final SpoofaxContext context;
	
	public SpoofaxBuilder(T input) {
		super(input);
		this.context = input.context;
	}

	@Override
	protected Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	protected Stamper defaultStamper() {
		return LastModifiedStamper.instance;
	}
}
