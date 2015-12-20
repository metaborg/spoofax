package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.StrategoExecutor;
import org.metaborg.spoofax.meta.core.pluto.StrategoExecutor.ExecutionResult;
import org.metaborg.spoofax.meta.core.pluto.build.misc.ParseSdf;
import org.metaborg.spoofax.meta.core.pluto.stamp.Sdf2RtgStamper;
import org.metaborg.spoofax.meta.core.pluto.util.LoggingFilteringIOAgent;
import org.metaborg.util.file.FileUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.strategoxt.tools.main_sdf2rtg_0_0;

import build.pluto.BuildUnit.State;
import build.pluto.builder.BuildRequest;
import build.pluto.dependency.Origin;
import build.pluto.output.None;
import build.pluto.output.OutputPersisted;

import com.google.common.base.Joiner;

public class Sdf2Rtg extends SpoofaxBuilder<Sdf2Rtg.Input, None> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -4487049822305558202L;

        public final String sdfModule;

        public Input(SpoofaxContext context, String sdfModule) {
            super(context);
            this.sdfModule = sdfModule;
        }
    }


    public static SpoofaxBuilderFactory<Input, None, Sdf2Rtg> factory = SpoofaxBuilderFactoryFactory.of(Sdf2Rtg.class,
        Input.class);


    public Sdf2Rtg(Input input) {
        super(input);
    }


    public static BuildRequest<Input, None, Sdf2Rtg, SpoofaxBuilderFactory<Input, None, Sdf2Rtg>> request(Input input) {
        return new BuildRequest<>(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }


    @Override protected String description(Input input) {
        return "Extract constructor signatures from grammar";
    }

    @Override public File persistentPath(Input input) {
        return context.depPath("sdf2rtg." + input.sdfModule + ".dep");
    }

    @Override public None build(Input input) throws IOException {
        final String sdfArgs = Joiner.on(' ').join(context.settings.sdfArgs());
        final Origin packSdf = PackSdf.origin(new PackSdf.Input(context, input.sdfModule, sdfArgs));
        requireBuild(packSdf);

        final File inputPath = FileUtils.toFile(context.settings.getSdfCompiledDefFile(input.sdfModule));
        final File outputPath = FileUtils.toFile(context.settings.getRtgFile(input.sdfModule));

        if(SpoofaxContext.BETTER_STAMPERS) {
            final BuildRequest<?, OutputPersisted<IStrategoTerm>, ?, ?> parseSdf =
                ParseSdf.request(new ParseSdf.Input(context, inputPath, packSdf));
            require(inputPath, new Sdf2RtgStamper(parseSdf));
        } else {
            require(inputPath);
        }

        // TODO: avoid redundant call to sdf2table
        // TODO: set nativepath to the native bundle, so that sdf2table can be found
        final ExecutionResult result =
            StrategoExecutor.runStrategoCLI(StrategoExecutor.toolsContext(), main_sdf2rtg_0_0.instance, "sdf2rtg",
                new LoggingFilteringIOAgent(Pattern.quote("Invoking native tool") + ".*"), "-i", inputPath, "-m",
                input.sdfModule, "-o", outputPath, "--ignore-missing-cons");
        // , "-Xnativepath", context.basePath("${nativepath}/")

        provide(outputPath);
        setState(State.finished(result.success));

        return None.val;
    }

}
