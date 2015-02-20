package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.sugarj.cleardep.CompilationUnit;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.SimpleMode;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.build.EmptyBuildInput;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class All extends Builder<SpoofaxBuildContext, EmptyBuildInput, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, EmptyBuildInput, SimpleCompilationUnit, All> factory = new BuilderFactory<SpoofaxBuildContext, EmptyBuildInput, SimpleCompilationUnit, All>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4707671129478582293L;

		@Override
		public All makeBuilder(SpoofaxBuildContext context) { return new All(context); }
	};
	
	private All(SpoofaxBuildContext context) {
		super(context, factory);
	}

	@Override
	protected String taskDescription(EmptyBuildInput input) {
		return "Build Spoofax project";
	}
	
	@Override
	protected Path persistentPath(EmptyBuildInput input) {
		return context.depPath("all.dep");
	}
	
	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, EmptyBuildInput input) throws IOException {
		RelativePath ppInput = context.basePath("${lib}/EditorService-pretty.pp");
		RelativePath ppTermOutput = context.basePath("${include}/EditorService-pretty.pp.af");
		CompilationUnit ppPack = context.ppPack.require(new PPPack.Input(ppInput, ppTermOutput), new SimpleMode());
		result.addModuleDependency(ppPack);
		
		CompilationUnit spoofaxDefault = context.spoofaxDefaultCtree.require(null, new SimpleMode());
		result.addModuleDependency(spoofaxDefault);
	}

}
