package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.strategoxt.imp.metatooling.building.AntForceRefreshScheduler;
import org.sugarj.cleardep.CompilationUnit;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.SimpleMode;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.Log;
import org.sugarj.common.path.Path;

public class SpoofaxDefaultCtree extends Builder<SpoofaxBuildContext, Void, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, Void, SimpleCompilationUnit, SpoofaxDefaultCtree> factory = new BuilderFactory<SpoofaxBuildContext, Void, SimpleCompilationUnit, SpoofaxDefaultCtree>() {
		@Override
		public SpoofaxDefaultCtree makeBuilder(SpoofaxBuildContext context) { return new SpoofaxDefaultCtree(context); }
	};
	
	public SpoofaxDefaultCtree(SpoofaxBuildContext context) {
		super(context);
	}
	
	@Override
	protected String taskDescription(Void input) {
		return null;
	}

	@Override
	protected Path persistentPath(Void input) {
		return context.basePath("${include}/build.spoofaxDefault.dep");
	}
	
	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, Void input) throws IOException {
		checkClassPath();
		
		CompilationUnit forceOnSave = context.forceOnSave.require(null, new SimpleMode());
		result.addModuleDependency(forceOnSave);
		
		forceWorkspaceRefresh();
		
		String sdfmodule = context.props.get("sdfmodule");
		String sdfImports = context.props.get("build.sdf.imports");
		CompilationUnit sdf2Table = context.sdf2Table.require(new Sdf2Table.Input(sdfmodule, sdfImports), new SimpleMode());
		result.addModuleDependency(sdf2Table);
		
		CompilationUnit metaSdf2Table = context.metaSdf2Table.require(null, new SimpleMode());
		result.addModuleDependency(metaSdf2Table);
		
		CompilationUnit ppGen = context.ppGen.require(null, new SimpleMode());
		result.addModuleDependency(ppGen);
	}

	private void checkClassPath() {
		@SuppressWarnings("unused")
		org.strategoxt.imp.generator.sdf2imp c;
	}

	protected void forceWorkspaceRefresh() {
		try {
			AntForceRefreshScheduler.main(new String[] {context.basePath("${include}").getAbsolutePath()});
		} catch (Exception e) {
			Log.log.logErr(e.getMessage(), Log.CORE);
		}
	}
}
