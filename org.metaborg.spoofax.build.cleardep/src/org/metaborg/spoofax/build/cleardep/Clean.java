package org.metaborg.spoofax.build.cleardep;

import java.io.IOException;

import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.Log;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class Clean extends Builder<SpoofaxBuildContext, Void, SimpleCompilationUnit> {

	public Clean(SpoofaxBuildContext context) {
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
		Log.log.beginTask("Clean", Log.ALWAYS);
		
		String[] paths = {
				"${build}",
				".cache",
				"${include}/${sdfmodule}.def",
				"${include}/${sdfmodule}-parenthesize.str",
				"${include}/${sdfmodule}-Permissive.def",
				"${include}/${sdfmodule}.generated.pp",
				"${include}/${sdfmodule}.generated.pp.af",
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
			Path path = new RelativePath(context.baseDir, context.props.substitute(p));
			Log.log.log("Delete " + path, Log.DETAIL); 
			FileCommands.delete(path); 
			result.addGeneratedFile(path);
		}
			
		for (Path p : FileCommands.listFiles(new RelativePath(context.baseDir, context.props.substitute("${lib}"))))
			if (FileCommands.fileName(p).matches(".*\\.generated\\.str")) {
				Log.log.log("Delete " + p, Log.DETAIL); 
				FileCommands.delete(p); 
				result.addGeneratedFile(p);
			}
		
		Log.log.endTask();
	}

}
