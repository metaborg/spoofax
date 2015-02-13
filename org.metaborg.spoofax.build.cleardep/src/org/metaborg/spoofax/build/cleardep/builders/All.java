package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.sugarj.cleardep.CompilationUnit;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.SimpleMode;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class All extends Builder<SpoofaxBuildContext, Void, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, Void, SimpleCompilationUnit, All> factory = new BuilderFactory<SpoofaxBuildContext, Void, SimpleCompilationUnit, All>() {
		@Override
		public All makeBuilder(SpoofaxBuildContext context) { return new All(context); }
	};
	
	public All(SpoofaxBuildContext context) {
		super(context);
	}

	@Override
	protected String taskDescription(Void input) {
		return "Build Spoofax project";
	}
	
	@Override
	protected Path persistentPath(Void input) {
		return context.depPath("all.dep");
	}
	
	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, Void input) throws IOException {
		RelativePath ppInput = context.basePath("${lib}/EditorService-pretty.pp");
		RelativePath ppTermOutput = context.basePath("${include}/EditorService-pretty.pp.af");
		CompilationUnit ppPack = context.ppPack.require(new PPPack.Input(ppInput, ppTermOutput), new SimpleMode());
		result.addModuleDependency(ppPack);
		
		CompilationUnit spoofaxDefault = context.spoofaxDefaultCtree.require(null, new SimpleMode());
		result.addModuleDependency(spoofaxDefault);
	}

}
