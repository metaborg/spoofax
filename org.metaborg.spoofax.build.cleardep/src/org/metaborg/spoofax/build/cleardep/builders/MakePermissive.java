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
import org.sugarj.common.Log;

public class MakePermissive extends Builder<SpoofaxBuildContext, Void, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, Void, SimpleCompilationUnit, MakePermissive> factory = new BuilderFactory<SpoofaxBuildContext, Void, SimpleCompilationUnit, MakePermissive>() {
		@Override
		public MakePermissive makeBuilder(SpoofaxBuildContext context) { return new MakePermissive(context); }
	};
	
	public MakePermissive(SpoofaxBuildContext context) {
		super(context);
	}

	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, Void input) throws IOException {
		Log.log.beginInlineTask("Make grammar permissive for error-recovery parsing.", Log.CORE); 
		
		CompilationUnit packSdf = context.packSdf.require(null, context.basePath("${include}/build.packSdf.dep"), new SimpleMode());
		result.addModuleDependency(packSdf);
		
		Log.log.endTask();
	}

}
