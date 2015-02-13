package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;
import java.util.List;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.metaborg.spoofax.build.cleardep.util.FileExtensionFilter;
import org.sugarj.cleardep.CompilationUnit;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.SimpleMode;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class ForceOnSave extends Builder<SpoofaxBuildContext, Void, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, Void, SimpleCompilationUnit, ForceOnSave> factory = new BuilderFactory<SpoofaxBuildContext, Void, SimpleCompilationUnit, ForceOnSave>() {
		@Override
		public ForceOnSave makeBuilder(SpoofaxBuildContext context) { return new ForceOnSave(context); }
	};
	
	public ForceOnSave(SpoofaxBuildContext context) {
		super(context);
	}

	@Override
	protected String taskDescription(Void input) {
		return "Force on-save handlers for NaBL, TS, etc.";
	}
	
	@Override
	protected Path persistentPath(Void input) {
		return context.depPath("forceOnSave.dep");
	}

	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, Void input) throws IOException {
		// XXX really need to delete old sdf3 files? Or is it sufficient to remove them from `paths` below?
		List<RelativePath> oldSdf3Paths = FileCommands.listFilesRecursive(context.basePath("src-gen"), new FileExtensionFilter("sdf3"));
		for (Path p : oldSdf3Paths)
			FileCommands.delete(p);
		
		List<RelativePath> paths = FileCommands.listFilesRecursive(
				context.baseDir, 
				new FileExtensionFilter("tmpl", "sdf3", "nab", "ts"));
		for (RelativePath p : paths) {
			CompilationUnit forceOnSaveFile = context.forceOnSaveFile.require(p, new SimpleMode());
			result.addModuleDependency(forceOnSaveFile);
		}
	}
}
