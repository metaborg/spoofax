package org.metaborg.spoofax.build.cleardep;


import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.PPPack.Input;
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
		Log.log.beginTask("All", Log.CORE);
		
		RelativePath ppInput = context.relPath("${lib}/EditorService-pretty.pp");
		RelativePath ppTermOutput = context.relPath("${include}/EditorService-pretty.pp.af");
		RelativePath ppDep = FileCommands.replaceExtension(ppTermOutput, "dep");
		Main.ppPack.require(new Input(ppInput, ppTermOutput), ppDep, new SimpleMode());
		
		Log.log.endTask();
	}

}
