package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;
import java.nio.file.StandardCopyOption;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.metaborg.spoofax.build.cleardep.SpoofaxContext;
import org.sugarj.cleardep.CompilationUnit;
import org.sugarj.cleardep.build.BuildManager;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;

public class CopySdf extends SpoofaxBuilder<CopySdf.Input> {

	public static SpoofaxBuilderFactory<Input, CopySdf> factory = new SpoofaxBuilderFactory<Input, CopySdf>() {
		private static final long serialVersionUID = -2175787090501831305L;

		@Override
		public CopySdf makeBuilder(Input input, BuildManager manager) { return new CopySdf(input, manager); }
	};
	
	public static class Input extends SpoofaxInput {
		/**
		 * 
		 */
		private static final long serialVersionUID = 6298820503718314523L;
		public final String sdfmodule;
		public final Path externaldef;
		public Input(SpoofaxContext context, String sdfmodule, Path externaldef) {
			super(context);
			this.sdfmodule = sdfmodule;
			this.externaldef = externaldef;
		}
	}
	
	public CopySdf(Input input, BuildManager manager) {
		super(input, factory, manager);
	}

	@Override
	protected String taskDescription() {
		return "Copy external grammar definition.";
	}
	
	@Override
	public Path persistentPath() {
		if (input.externaldef != null)
			return context.depPath("copySdf." + input.externaldef + "." + input.sdfmodule + ".dep");
		return context.depPath("copySdf." + input.sdfmodule + ".dep");
	}

	@Override
	public void build(CompilationUnit result) throws IOException {
		if (input.externaldef != null) {
			Path target = context.basePath("${include}/" + input.sdfmodule + ".def");
			result.addExternalFileDependency(input.externaldef);
			FileCommands.copyFile(input.externaldef, target, StandardCopyOption.COPY_ATTRIBUTES);
			result.addGeneratedFile(target);
		}
	}
}
