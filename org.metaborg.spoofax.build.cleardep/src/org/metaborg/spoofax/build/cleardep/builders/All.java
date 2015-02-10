package org.metaborg.spoofax.build.cleardep.builders;


import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.Main;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.sugarj.cleardep.CompilationUnit;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.SimpleMode;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.Log;
import org.sugarj.common.path.RelativePath;

public class All extends Builder<SpoofaxBuildContext, Void, SimpleCompilationUnit> {

	public All(SpoofaxBuildContext context) {
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
		Log.log.beginTask("Spoofax build all", Log.CORE);
		
		RelativePath ppInput = context.basePath("${lib}/EditorService-pretty.pp");
		RelativePath ppTermOutput = context.basePath("${include}/EditorService-pretty.pp.af");
		RelativePath ppDep = FileCommands.addExtension(ppTermOutput, "dep");
		CompilationUnit ppPack = Main.ppPack.require(new PPPack.Input(ppInput, ppTermOutput), ppDep, new SimpleMode());
		result.addModuleDependency(ppPack);
		
		CompilationUnit spoofaxDefault = Main.spoofaxDefaultCtree.require(null, context.basePath("${include}/build.spoofaxDefault.dep"), new SimpleMode());
		result.addModuleDependency(spoofaxDefault);
		
		Log.log.endTask();
	}

}
