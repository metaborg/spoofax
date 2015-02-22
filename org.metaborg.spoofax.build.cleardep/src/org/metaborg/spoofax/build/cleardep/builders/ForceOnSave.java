package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;
import java.util.List;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.metaborg.spoofax.build.cleardep.util.FileExtensionFilter;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.SimpleMode;
import org.sugarj.cleardep.build.BuildManager;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class ForceOnSave extends SpoofaxBuilder<SpoofaxInput> {

	public static SpoofaxBuilderFactory<SpoofaxInput, ForceOnSave> factory = new SpoofaxBuilderFactory<SpoofaxInput, ForceOnSave>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4436143308769039647L;

		@Override
		public ForceOnSave makeBuilder(SpoofaxInput input, BuildManager manager) { return new ForceOnSave(input, manager); }
	};
	
	public ForceOnSave(SpoofaxInput input, BuildManager manager) {
		super(input, factory, manager);
	}

	@Override
	protected String taskDescription() {
		return "Force on-save handlers for NaBL, TS, etc.";
	}
	
	@Override
	protected Path persistentPath() {
		return context.depPath("forceOnSave.dep");
	}

	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result) throws IOException {
		// XXX really need to delete old sdf3 files? Or is it sufficient to remove them from `paths` below?
		List<RelativePath> oldSdf3Paths = FileCommands.listFilesRecursive(context.basePath("src-gen"), new FileExtensionFilter("sdf3"));
		for (Path p : oldSdf3Paths)
			FileCommands.delete(p);
		
		List<RelativePath> paths = FileCommands.listFilesRecursive(
				context.baseDir, 
				new FileExtensionFilter("tmpl", "sdf3", "nab", "ts"));
		for (RelativePath p : paths)
			require(ForceOnSaveFile.factory, new ForceOnSaveFile.Input(context, p), new SimpleMode());
	}
}
