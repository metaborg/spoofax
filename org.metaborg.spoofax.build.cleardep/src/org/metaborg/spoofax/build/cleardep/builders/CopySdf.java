package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;
import java.nio.file.StandardCopyOption;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxContext;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.sugarj.cleardep.SimpleCompilationUnit;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.cleardep.stamp.Stamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;

public class CopySdf extends SpoofaxBuilder<CopySdf.Input> {

	public static SpoofaxBuilderFactory<Input, CopySdf> factory = new SpoofaxBuilderFactory<Input, CopySdf>() {
		@Override
		public CopySdf makeBuilder(Input input) { return new CopySdf(input); }
	};
	
	public static class Input extends SpoofaxInput {
		public final String sdfmodule;
		public final Path externaldef;
		public Input(SpoofaxContext context, String sdfmodule, Path externaldef) {
			super(context);
			this.sdfmodule = sdfmodule;
			this.externaldef = externaldef;
		}
	}
	
	public CopySdf(Input input) {
		super(input);
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
	public Class<SimpleCompilationUnit> resultClass() {
		return SimpleCompilationUnit.class;
	}

	@Override
	public Stamper defaultStamper() { return LastModifiedStamper.instance; }

	@Override
	public void build(SimpleCompilationUnit result) throws IOException {
		if (input.externaldef != null) {
			Path target = context.basePath("${include}/" + input.sdfmodule + ".def");
			result.addExternalFileDependency(input.externaldef);
			FileCommands.copyFile(input.externaldef, target, StandardCopyOption.COPY_ATTRIBUTES);
			result.addGeneratedFile(target);
		}
	}
}
