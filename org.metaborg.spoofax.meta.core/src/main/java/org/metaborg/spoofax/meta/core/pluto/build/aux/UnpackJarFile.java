package org.metaborg.spoofax.meta.core.pluto.build.aux;

import java.io.File;
import java.nio.file.Path;

import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.sugarj.common.FileCommands;

import build.pluto.output.OutputPersisted;
import build.pluto.stamp.LastModifiedStamper;

public class UnpackJarFile extends SpoofaxBuilder<UnpackJarFile.Input, OutputPersisted<File>> {

	public static SpoofaxBuilderFactory<Input, OutputPersisted<File>, UnpackJarFile> factory = SpoofaxBuilderFactoryFactory.of(UnpackJarFile.class, Input.class);
	

	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 12331766781256062L;

		public final File jarfile;
		public final File outdir;

		public Input(SpoofaxContext context, File jarfile, File outdir) {
			super(context);
			this.jarfile = jarfile;
			this.outdir = outdir;
		}
	}
	
	public UnpackJarFile(Input input) {
		super(input);
	}

	@Override
	protected String description(Input input) {
		return "Unpack jarfile " + FileCommands.fileName(input.jarfile);
	}
	
	@Override
	public File persistentPath(Input input) {
		return context.depPath("unpack.jar." + FileCommands.fileName(input.jarfile) + ".dep");
	}

	@Override
	public OutputPersisted<File> build(Input input) throws Exception {
		require(input.jarfile, LastModifiedStamper.instance);
		File dir = input.outdir != null ? input.outdir : FileCommands.newTempDir();
		FileCommands.unpackJarfile(dir, input.jarfile);
		for (Path p : FileCommands.listFilesRecursive(dir.toPath()))
			provide(p.toFile(), LastModifiedStamper.instance);
		return OutputPersisted.of(dir);
		
	}
}
