package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.sugarj.cleardep.output.None;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;


public class All extends SpoofaxBuilder<SpoofaxInput, None> {

	public static SpoofaxBuilderFactory<SpoofaxInput, None, All> factory = new SpoofaxBuilderFactory<SpoofaxInput, None, All>() {
		private static final long serialVersionUID = 1747202833519981639L;

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
	public None build() throws IOException {
		RelativePath ppInput = context.basePath("${lib}/EditorService-pretty.pp");
		RelativePath ppTermOutput = context.basePath("${include}/EditorService-pretty.pp.af");
		requireBuild(PPPack.factory, new PPPack.Input(context, ppInput, ppTermOutput));
		
		requireBuild(SpoofaxDefaultCtree.factory, input);
		return None.val;
	}

}
