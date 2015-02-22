package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.SimpleMode;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class All extends SpoofaxBuilder<SpoofaxInput> {

	public static SpoofaxBuilderFactory<SpoofaxInput, All> factory = new SpoofaxBuilderFactory<SpoofaxInput, All>() {
		@Override
		public All makeBuilder(SpoofaxInput input) { return new All(input); }
	};
	
	public All(SpoofaxInput input) {
		super(input);
	}

	@Override
	protected String taskDescription() {
		return "Build Spoofax project";
	}
	
	@Override
	protected Path persistentPath() {
		return context.depPath("all.dep");
	}
	
	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result) throws IOException {
		RelativePath ppInput = context.basePath("${lib}/EditorService-pretty.pp");
		RelativePath ppTermOutput = context.basePath("${include}/EditorService-pretty.pp.af");
		require(PPPack.factory, new PPPack.Input(context, ppInput, ppTermOutput), new SimpleMode());
		
		require(SpoofaxDefaultCtree.factory, input, new SimpleMode());
	}

}
