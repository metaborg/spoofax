package org.metaborg.spoofax.build.cleardep.builders;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuildContext;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.build.Builder;
import org.sugarj.cleardep.build.BuilderFactory;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class CopyJar extends Builder<SpoofaxBuildContext, CopyJar.Input, SimpleCompilationUnit> {

	public static BuilderFactory<SpoofaxBuildContext, Input, SimpleCompilationUnit, CopyJar> factory = new BuilderFactory<SpoofaxBuildContext, Input, SimpleCompilationUnit, CopyJar>() {
		@Override
		public CopyJar makeBuilder(SpoofaxBuildContext context) { return new CopyJar(context); }
	};
	
	public static class Input {
		public final Path externaljar;
		public Input(Path externaljar) {
			this.externaljar = externaljar;
		}
	}
	
	public CopyJar(SpoofaxBuildContext context) {
		super(context);
	}

	@Override
	protected String taskDescription(Input input) {
		return "Copy external Jar.";
	}
	
	@Override
	public Path persistentPath(Input input) {
		if (input.externaljar != null) {
			RelativePath rel = FileCommands.getRelativePath(context.baseDir, input.externaljar);
			String relname = rel.getRelativePath().replace(File.separatorChar, '_');
			return context.depPath("copyJar." + relname + ".dep");
		}
		return context.depPath("copyJar.dep");
	}

	@Override
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result, Input input) throws IOException {
		if (input.externaljar != null) {
			Path target = context.basePath("${include}/" + FileCommands.dropDirectory(input.externaljar));
			result.addExternalFileDependency(input.externaljar);
			FileCommands.copyFile(input.externaljar, target, StandardCopyOption.COPY_ATTRIBUTES);
			result.addGeneratedFile(target);
		}
	}
}
