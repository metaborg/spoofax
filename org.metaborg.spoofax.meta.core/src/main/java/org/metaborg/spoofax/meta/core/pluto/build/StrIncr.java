package org.metaborg.spoofax.meta.core.pluto.build;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilder;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxBuilderFactoryFactory;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxContext;
import org.metaborg.spoofax.meta.core.pluto.SpoofaxInput;
import org.metaborg.spoofax.meta.core.pluto.build.StrIncrFrontEnd.Import;
import org.metaborg.spoofax.meta.core.pluto.util.OutputIgnoreOriginBuilder;
import org.metaborg.util.cmd.Arguments;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.sugarj.common.FileCommands;

import build.pluto.dependency.Origin;
import build.pluto.dependency.Origin.Builder;
import build.pluto.output.None;

public class StrIncr extends SpoofaxBuilder<StrIncr.Input, None> {
    private static final ILogger logger = LoggerUtils.logger(StrIncr.class);

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

        @Override public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((cacheDir == null) ? 0 : cacheDir.hashCode());
            result = prime * result + ((extraArgs == null) ? 0 : extraArgs.hashCode());
            result = prime * result + ((includeDirs == null) ? 0 : includeDirs.hashCode());
            result = prime * result + ((includeFiles == null) ? 0 : includeFiles.hashCode());
            result = prime * result + ((inputFile == null) ? 0 : inputFile.hashCode());
            result = prime * result + ((javaPackageName == null) ? 0 : javaPackageName.hashCode());
            result = prime * result + ((origin == null) ? 0 : origin.hashCode());
            result = prime * result + ((outputPath == null) ? 0 : outputPath.hashCode());
            return result;
        }

        @Override public boolean equals(Object obj) {
            if(this == obj)
                return true;
            if(obj == null)
                return false;
            if(getClass() != obj.getClass())
                return false;
            Input other = (Input) obj;
            if(cacheDir == null) {
                if(other.cacheDir != null)
                    return false;
            } else if(!cacheDir.equals(other.cacheDir))
                return false;
            if(extraArgs == null) {
                if(other.extraArgs != null)
                    return false;
            } else if(!extraArgs.equals(other.extraArgs))
                return false;
            if(includeDirs == null) {
                if(other.includeDirs != null)
                    return false;
            } else if(!includeDirs.equals(other.includeDirs))
                return false;
            if(includeFiles == null) {
                if(other.includeFiles != null)
                    return false;
            } else if(!includeFiles.equals(other.includeFiles))
                return false;
            if(inputFile == null) {
                if(other.inputFile != null)
                    return false;
            } else if(!inputFile.equals(other.inputFile))
                return false;
            if(javaPackageName == null) {
                if(other.javaPackageName != null)
                    return false;
            } else if(!javaPackageName.equals(other.javaPackageName))
                return false;
            if(origin == null) {
                if(other.origin != null)
                    return false;
            } else if(!origin.equals(other.origin))
                return false;
            if(outputPath == null) {
                if(other.outputPath != null)
                    return false;
            } else if(!outputPath.equals(other.outputPath))
                return false;
            return true;
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

        logger.debug("Starting time measurement");
        long startTime = System.nanoTime();

        final Origin ignoreBuilderOrigin = OutputIgnoreOriginBuilder.ignoreOutputs(input.origin);

        // FRONTEND
        final Set<File> seen = new HashSet<>();
        final Deque<File> workList = new ArrayDeque<>();
        workList.add(input.inputFile);
        seen.add(input.inputFile);

        final List<File> boilerplateFiles = new ArrayList<>();
        final Origin.Builder allFrontEndTasks = new OutputIgnoreOriginBuilder();
        final Map<String, Set<File>> strategyFiles = new HashMap<>();
        final Map<String, Set<String>> strategyConstrFiles = new HashMap<>();
        final Map<String, Set<File>> overlayFiles = new HashMap<>();
        final Map<String, Origin.Builder> strategyOrigins = new HashMap<>();
        final Map<String, Origin.Builder> overlayOrigins = new HashMap<>();

        long frontEndStartTime;
        long frontEndTime = 0;
        long shuffleStartTime;
        long shuffleTime = 0;
        do {
            frontEndStartTime = System.nanoTime();
            final File strFile = workList.remove();
            final String projectName = projectName(strFile);
            final StrIncrFrontEnd.Input frontEndInput =
                new StrIncrFrontEnd.Input(context, strFile, projectName, ignoreBuilderOrigin);
            final StrIncrFrontEnd.BuildRequest request = StrIncrFrontEnd.request(frontEndInput);
            final StrIncrFrontEnd.Output frontEndOutput = requireBuild(request);
            shuffleStartTime = System.nanoTime();
            frontEndTime += shuffleStartTime - frontEndStartTime;

            // shuffling output for backend
            allFrontEndTasks.add(request);
            boilerplateFiles
                .add(context.toFile(paths.strSepCompBoilerplateFile(projectName, frontEndOutput.moduleName)));
            for(Entry<String, File> gen : frontEndOutput.strategyFiles.entrySet()) {
                String strategyName = gen.getKey();
                getOrInitialize(strategyFiles, strategyName, HashSet::new).add(gen.getValue());
                getOrInitialize(strategyConstrFiles, strategyName, HashSet::new)
                    .addAll(frontEndOutput.strategyConstrFiles.get(strategyName));
                getOrInitialize(strategyOrigins, strategyName, OutputIgnoreOriginBuilder::new).add(request);
            }
            for(Entry<String, File> gen : frontEndOutput.overlayFiles.entrySet()) {
                final String overlayName = gen.getKey();
                getOrInitialize(overlayFiles, overlayName, HashSet::new).add(gen.getValue());
                getOrInitialize(overlayOrigins, overlayName, OutputIgnoreOriginBuilder::new).add(request);
            }

            // resolving imports
            for(Import i : frontEndOutput.imports) {
                final Set<File> resolvedImport = i.resolveImport(input.includeDirs);
                resolvedImport.removeAll(seen);
                workList.addAll(resolvedImport);
                seen.addAll(resolvedImport);
            }

            shuffleTime += System.nanoTime() - shuffleStartTime;
        } while(!workList.isEmpty());

        long betweenFrontAndBack = System.nanoTime();
        logger.debug("Frontends overall took: {} ns", betweenFrontAndBack - startTime);
        logger.debug("Purely frontend tasks took: {} ns", frontEndTime);
        logger.debug("While shuffling information and tracking imports took: {} ns", shuffleTime);

        // BACKEND
        for(String strategyName : strategyFiles.keySet()) {
            Origin.Builder backEndOrigin = Origin.Builder();
            backEndOrigin.add(strategyOrigins.get(strategyName).get());
            File strategyDir = context.toFile(paths.strSepCompStrategyDir(strategyName));
            List<File> strategyOverlayFiles = new ArrayList<>();
            for(String overlayName : strategyConstrFiles.get(strategyName)) {
                final Set<File> theOverlayFiles = overlayFiles.get(overlayName);
                if(theOverlayFiles != null) {
                    strategyOverlayFiles.addAll(theOverlayFiles);
                }
                final Builder overlayOriginBuilder = overlayOrigins.get(overlayName);
                if(overlayOriginBuilder != null) {
                    backEndOrigin.add(overlayOriginBuilder.get());
                }
            }
            StrIncrBackEnd.Input backEndInput = new StrIncrBackEnd.Input(context, backEndOrigin.get(), strategyName,
                strategyDir, Arrays.asList(strategyFiles.get(strategyName).toArray(new File[0])),
                strategyOverlayFiles, input.javaPackageName,
                input.outputPath, input.cacheDir, input.extraArgs, false);
            requireBuild(StrIncrBackEnd.request(backEndInput));
        }
        // boilerplate task
        File strSrcGenDir = context.toFile(paths.strSepCompSrcGenDir());
        StrIncrBackEnd.Input backEndInput = new StrIncrBackEnd.Input(context, allFrontEndTasks.get(), null,
            strSrcGenDir, boilerplateFiles, Collections.emptyList(), input.javaPackageName, input.outputPath,
            input.cacheDir, input.extraArgs, true);
        requireBuild(StrIncrBackEnd.request(backEndInput));

        long finishTime = System.nanoTime();
        logger.debug("Backends overall took: {} ns", finishTime - betweenFrontAndBack);

        logger.debug("Full Stratego incremental build took: {} ns", finishTime - startTime);
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

    public static final <K, V> V getOrInitialize(Map<K, V> map, K key, Supplier<V> initialize) {
        map.computeIfAbsent(key, ignore -> initialize.get());
        return map.get(key);
    }

}
