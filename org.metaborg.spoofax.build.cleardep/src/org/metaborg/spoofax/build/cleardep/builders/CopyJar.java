package org.metaborg.spoofax.build.cleardep.builders;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.metaborg.spoofax.build.cleardep.SpoofaxContext;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.build.BuildManager;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;
import org.sugarj.common.path.RelativePath;

public class CopyJar extends SpoofaxBuilder<CopyJar.Input> {

	public static SpoofaxBuilderFactory<Input, CopyJar> factory = new SpoofaxBuilderFactory<Input, CopyJar>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8387363389037442076L;

		@Override
		public CopyJar makeBuilder(Input input, BuildManager manager) { return new CopyJar(input, manager); }
	};
	

	public static class Input extends SpoofaxInput {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8710048518971598430L;
		public final Path externaljar;
		public Input(SpoofaxContext context, Path externaljar) {
			super(context);
			this.externaljar = externaljar;
		}
	}
	
	public CopyJar(Input input, BuildManager manager) {
		super(input, factory, manager);
	}

	@Override
	protected String taskDescription() {
		return "Copy external Jar";
	}
	
	@Override
	public Path persistentPath() {
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
	public void build(SimpleCompilationUnit result) throws IOException {
		if (input.externaljar != null) {
			Path target = context.basePath("${include}/" + FileCommands.dropDirectory(input.externaljar));
			result.addExternalFileDependency(input.externaljar);
			FileCommands.copyFile(input.externaljar, target, StandardCopyOption.COPY_ATTRIBUTES);
			result.addGeneratedFile(target);
		}
	}
}
