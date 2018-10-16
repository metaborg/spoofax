package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.util.cmd.Arguments;
import org.sugarj.common.FileCommands;

import build.pluto.builder.Builder;
import build.pluto.builder.factory.BuilderFactory;
import build.pluto.dependency.Origin;
import build.pluto.output.IgnoreOutputStamper;
import build.pluto.output.None;
import build.pluto.output.Output;
import io.usethesource.capsule.BinaryRelation;

public class StrIncr extends SpoofaxBuilder<StrIncr.Input, None> {
    public static class Input extends SpoofaxInput {
        private static final long serialVersionUID = -1010675361672399734L;

        public final File inputFile;
        public final String javaPackageName;
        public final List<File> includeDirs;
        public final List<File> includeFiles;
        public final File cacheDir;
        public final Arguments extraArgs;
        public final File outputPath;
        public final Origin origin;

        public Input(SpoofaxContext context, File inputFile, String javaPackageName, List<File> includeDirs,
            List<File> includeFiles, File cacheDir, Arguments extraArgs, File outputPath, Origin origin) {
            super(context);

            this.inputFile = inputFile;
            this.javaPackageName = javaPackageName;
            this.includeDirs = includeDirs;
            this.includeFiles = includeFiles;
            this.cacheDir = cacheDir;
            this.extraArgs = extraArgs;
            this.outputPath = outputPath;
            this.origin = origin;
        }

    }

    // Just a type alias
    public static class BuildRequest
        extends build.pluto.builder.BuildRequest<Input, None, StrIncr, SpoofaxBuilderFactory<Input, None, StrIncr>> {
        private static final long serialVersionUID = -1299552527869341531L;

        public BuildRequest(SpoofaxBuilderFactory<Input, None, StrIncr> factory, Input input) {
            super(factory, input);
        }
    }

    public static SpoofaxBuilderFactory<Input, None, StrIncr> factory =
        SpoofaxBuilderFactoryFactory.of(StrIncr.class, Input.class);

    public static BuildRequest request(Input input) {
        return new BuildRequest(factory, input);
    }

    public static Origin origin(Input input) {
        return Origin.from(request(input));
    }

    public static String projectName(File inputFile) {
        // TODO: *can* we get the project name somehow?
        return Integer.toString(inputFile.toString().hashCode());
    }

    public StrIncr(Input input) {
        super(input);
    }

    @Override protected None build(Input input) throws Throwable {
        /*
         * Note that we require the sdf tasks here to force it to generated needed str files. We then discover those in
         * this method with a directory search, and start a front-end task for each. Every front-end task also depends
         * on the sdf tasks so there is no hidden dep. To make sure that front-end tasks only run when their input
         * _files_ change, we need the front-end to depend on the sdf tasks with a simple stamper that allows the
         * execution of the sdf task to be ignored.
         */
        requireBuild(input.origin);

        Origin.Builder oBuilder = Origin.Builder();
        for(@SuppressWarnings("rawtypes") build.pluto.builder.BuildRequest req : input.origin.getReqs()) {
            @SuppressWarnings("unchecked") BuilderFactory<Serializable, Output, Builder<Serializable, Output>> factory =
                req.factory;
            oBuilder.add(factory, req.input, IgnoreOutputStamper.instance);
        }
        Origin ignoreBuilderOrigin = oBuilder.get();


        // TODO: use mainFile once we track imports
        @SuppressWarnings("unused") File mainFile = input.inputFile;

        // FRONTEND
        List<File> boilerplateFiles = new ArrayList<>();
        Origin.Builder allFrontEndTasks = Origin.Builder();
        BinaryRelation.Transient<String, File> generatedFiles = BinaryRelation.Transient.of();
        Map<String, Origin.Builder> strategyOrigins = new HashMap<>();
        Set<File> strFiles = new HashSet<>(FileUtils.listFiles(context.baseDir, new String[] { "str" }, true));
        strFiles.addAll(input.includeFiles);
        for(File dir : input.includeDirs) {
            strFiles.addAll(FileUtils.listFiles(dir, new String[] { "str" }, true));
        }
        for(File strFile : strFiles) {
            // TODO: *can* we get the project name somehow?
            String projectName = Integer.toString(strFile.toString().hashCode());
            StrIncrFrontEnd.Input frontEndInput =
                new StrIncrFrontEnd.Input(context, strFile, projectName, ignoreBuilderOrigin);
            StrIncrFrontEnd.Output frontEndOutput = requireBuild(StrIncrFrontEnd.request(frontEndInput));

            // shuffling output for backend
            StrIncrFrontEnd.BuildRequest request = StrIncrFrontEnd.request(frontEndInput);
            allFrontEndTasks.add(request);
            boilerplateFiles
                .add(context.toFile(paths.strSepCompBoilerplateFile(projectName, frontEndOutput.moduleName)));
            for(Entry<String, File> gen : frontEndOutput.generatedFiles.entrySet()) {
                String strategyName = gen.getKey();
                generatedFiles.__insert(strategyName, gen.getValue());
                strategyOrigins.computeIfAbsent(strategyName, k -> new Origin.Builder());
                strategyOrigins.get(strategyName).add(request);
            }
        }

        // BACKEND
        for(String strategyName : generatedFiles.keySet()) {
            Origin strategyOrigin = strategyOrigins.get(strategyName).get();
            File strategyDir = context.toFile(paths.strSepCompStrategyDir(strategyName));
            StrIncrBackEnd.Input backEndInput = new StrIncrBackEnd.Input(context, strategyOrigin, strategyName,
                strategyDir, Arrays.asList(generatedFiles.get(strategyName).toArray(new File[0])),
                input.javaPackageName, input.outputPath, input.cacheDir, input.extraArgs, false);
            requireBuild(StrIncrBackEnd.request(backEndInput));
        }
        // boilerplate task
        File strSrcGenDir = context.toFile(paths.strSepCompSrcGenDir());
        StrIncrBackEnd.Input backEndInput =
            new StrIncrBackEnd.Input(context, allFrontEndTasks.get(), null, strSrcGenDir, boilerplateFiles,
                input.javaPackageName, input.outputPath, input.cacheDir, input.extraArgs, true);
        requireBuild(StrIncrBackEnd.request(backEndInput));

        return None.val;
    }

    @Override protected String description(Input input) {
        return "Stratego incremental compilation";
    }

    @Override public File persistentPath(Input input) {
        final Path rel = FileCommands.getRelativePath(context.baseDir, input.inputFile);
        final String relname = rel.toString().replace(File.separatorChar, '_');
        return context.depPath("str_incr." + relname + ".dep");
    }

}
