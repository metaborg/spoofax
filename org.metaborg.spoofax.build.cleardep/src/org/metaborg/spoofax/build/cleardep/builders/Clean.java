package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.metaborg.spoofax.build.cleardep.util.FileExtensionFilter;
import org.metaborg.spoofax.build.cleardep.util.FileNameFilter;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.Log;
import org.sugarj.common.path.Path;

public class Clean extends Builder<SpoofaxBuildContext, Void, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, Void, SimpleCompilationUnit, Clean> factory = new BuilderFactory<SpoofaxBuildContext, Void, SimpleCompilationUnit, Clean>() {
		@Override
		public Clean makeBuilder(SpoofaxBuildContext context) { return new Clean(context); }
	};
	
	public Clean(SpoofaxBuildContext context) {
		super(context);
	}

	@Override
	protected String taskDescription(Void input) {
		return "Clean";
	}
	
	@Override
	protected Path persistentPath(Void input) {
		return context.basePath("${include}/build.clean.dep");
	}

	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}
	
	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, Void input) throws IOException {
		String[] paths = {
				".cache",
				"${include}/${sdfmodule}.def",
				"${include}/${sdfmodule}-parenthesize.str",
				"${include}/${sdfmodule}-Permissive.def",
				"${include}/${sdfmodule}.generated.pp",
				"${include}/${sdfmodule}.generated.pp.af",
				"${include}/${sdfmodule}.generated.pp.af.dep",
				"${include}/${sdfmodule}.packed.esv",
				"${include}/${sdfmodule}.pp.af",
				"${include}/${sdfmodule}.rtg",
				"${lib-gen}/${ppmodule}.jar",
				"${lib-gen}/${ppmodule}.rtree",
				"${lib-gen}/${sigmodule}.str",
				"${lib-gen}/${sigmodule}.ctree",
				"${lib-gen}/${sigmodule}.rtree",
				"${lib-gen}/${sigmodule}.ctree.dep",
				"${include}/${sdfmodule}.str",
				"${include}/${sdfmodule}.tbl",
				"${include}/${strmodule}.rtree",
				"${include}/${strmodule}.ctree",
				"${include}/${strmodule}.ctree.dep",
				"${include}/${strmodule}.jar",
				"${src-gen}/trans",
				"${src-gen}/templatelang/pplib",
				"${src-gen}/templatelang/siglib",
				"${syntax}/${sdfmodule}.generated.esv",
				"${syntax}/${sdfmodule}.generated.pp",
				"${include}/${metasdfmodule}-Permissive.def",
				"${include}/${metasdfmodule}.def",
				"${include}/${metasdfmodule}.tbl",
				"utils"};
		
		for (String p : paths) {
			Path path = context.basePath(p);
			Log.log.log("Delete " + path, Log.DETAIL); 
			FileCommands.delete(path); 
			result.addGeneratedFile(path);
		}
		
		for (Path p : FileCommands.listFiles(context.basePath("${build}"))) {
			Log.log.log("Delete " + p, Log.DETAIL); 
			FileCommands.delete(p); 
			result.addGeneratedFile(p);
		}
		
		for (Path p : FileCommands.listFiles(context.basePath("${lib}"), new FileNameFilter(".*\\.generated\\.str"))) {
			Log.log.log("Delete " + p, Log.DETAIL); 
			FileCommands.delete(p); 
			result.addGeneratedFile(p);
		}
		
		for (Path p : FileCommands.listFiles(context.basePath("${include}"), new FileExtensionFilter("dep"))) {
			Log.log.log("Delete " + p, Log.DETAIL); 
			FileCommands.delete(p); 
			result.addGeneratedFile(p);
		}
	}

}
