package org.metaborg.spoofax.build.cleardep.builders;

import java.io.IOException;
import java.nio.file.StandardCopyOption;

import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder;
import org.metaborg.spoofax.build.cleardep.SpoofaxBuilder.SpoofaxInput;
import org.metaborg.spoofax.build.cleardep.SpoofaxContext;
import org.sugarj.cleardep.output.None;
import org.sugarj.cleardep.stamp.LastModifiedStamper;
import org.sugarj.common.FileCommands;
import org.sugarj.common.path.Path;

public class CopySdf extends SpoofaxBuilder<CopySdf.Input, None> {

	public static SpoofaxBuilderFactory<Input, None, CopySdf> factory = new SpoofaxBuilderFactory<Input, None, CopySdf>() {
		private static final long serialVersionUID = -2175787090501831305L;

		@Override
		public CopySdf makeBuilder(Input input) { return new CopySdf(input); }
	};
	
	public static class Input extends SpoofaxInput {
		private static final long serialVersionUID = 6298820503718314523L;

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
	public None build() throws IOException {
		if (input.externaldef != null) {
			Path target = context.basePath("${include}/" + input.sdfmodule + ".def");
			requires(input.externaldef, LastModifiedStamper.instance);
			FileCommands.copyFile(input.externaldef, target, StandardCopyOption.COPY_ATTRIBUTES);
			generates(target);
		}
		return None.val;
	}
}
