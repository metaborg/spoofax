package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;
import java.util.List;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.metaborg.spoofax.build.cleardep.util.FileExtensionFilter;
import org.sugarj.cleardep.output.None;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class ForceOnSave extends SpoofaxBuilder<SpoofaxInput, None> {

	public static SpoofaxBuilderFactory<SpoofaxInput, None, ForceOnSave> factory = new SpoofaxBuilderFactory<SpoofaxInput, None, ForceOnSave>() {
		private static final long serialVersionUID = 4436143308769039647L;

		@Override
		public ForceOnSave makeBuilder(SpoofaxInput input) { return new ForceOnSave(input); }
	};
	
	public ForceOnSave(SpoofaxInput input) {
		super(input);
	}

	@Override
	protected String taskDescription() {
		return "Force on-save handlers for SDF3, NaBL, TS, etc.";
	}
	
	@Override
	protected Path persistentPath() {
		return context.depPath("forceOnSave.dep");
	}

	@Override
	public None build() throws IOException {
		// XXX really need to delete old sdf3 files? Or is it sufficient to remove them from `paths` below?
		List<RelativePath> oldSdf3Paths = FileCommands.listFilesRecursive(context.basePath("src-gen"), new FileExtensionFilter("sdf3"));
		for (Path p : oldSdf3Paths)
			FileCommands.delete(p);
		
		List<RelativePath> paths = FileCommands.listFilesRecursive(
				context.baseDir, 
				new FileExtensionFilter("tmpl", "sdf3", "nab", "ts"));
		for (RelativePath p : paths)
			require(ForceOnSaveFile.factory, new ForceOnSaveFile.Input(context, p));
		return None.val;
	}
}
