package org.metaborg.spoofax.build.cleardep;

import java.io.Serializable;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.sugarj.cleardep.CompilationUnit;
import org.sugarj.cleardep.build.BuildManager;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;

abstract public class SpoofaxBuilder<T extends SpoofaxInput> extends Builder<T, CompilationUnit> {

	public static abstract class SpoofaxBuilderFactory<T extends SpoofaxInput, B extends SpoofaxBuilder<T>> implements BuilderFactory<T, CompilationUnit, B> {
		private static final long serialVersionUID = 8998843329413855827L;

		@Override
		public abstract B makeBuilder(T input, BuildManager manager);
	}
	
	public static class SpoofaxInput implements Serializable{
		private static final long serialVersionUID = -6362900996234737307L;
		public final SpoofaxContext context;
		public SpoofaxInput(SpoofaxContext context) {
			this.context = context;
		}
	}
	
	protected final SpoofaxContext context;
	
	public <B extends SpoofaxBuilder<T>> SpoofaxBuilder(T input, SpoofaxBuilderFactory<T, B> factory, BuildManager manager) {
		super(input, factory, manager);
		this.context = input.context;
	}

	@Override
	protected Class<CompilationUnit> resultClass() {
		return CompilationUnit.class;
	}

	@Override
	protected Stamper defaultStamper() {
		return LastModifiedStamper.instance;
	}
}
