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

public class Sdf2Table extends Builder<SpoofaxBuildContext, Void, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, Void, SimpleCompilationUnit, Sdf2Table> factory = new BuilderFactory<SpoofaxBuildContext, Void, SimpleCompilationUnit, Sdf2Table>() {
		@Override
		public Sdf2Table makeBuilder(SpoofaxBuildContext context) { return new Sdf2Table(context); }
	};
	
	public Sdf2Table(SpoofaxBuildContext context) {
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
		Log.log.beginInlineTask("Compile grammar to parse table", Log.CORE); 

		CompilationUnit makePermissive = context.makePermissive.require(null, context.basePath("${include}/build.makePermissive.dep"), new SimpleMode());
		result.addModuleDependency(makePermissive);
		
		Log.log.endTask();
	}

}
