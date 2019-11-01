package org.metaborg.spoofax.meta.core.config;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.metaborg.core.config.IGenerateConfig;
import org.metaborg.core.config.JSGLRVersion;
import org.metaborg.core.config.Sdf2tableVersion;
import org.metaborg.core.config.IExportConfig;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageBuilder;
import org.metaborg.core.project.NameUtil;
import org.metaborg.meta.core.config.LanguageSpecConfig;
import org.metaborg.spoofax.core.config.SpoofaxProjectConfig;
import org.metaborg.util.cmd.Arguments;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.collect.Lists;

import mb.nabl2.config.NaBL2Config;

/**
 * An implementation of the {@link ISpoofaxLanguageSpecConfig} interface that is backed by an
 * {@link ImmutableConfiguration} object.
 */
public class SpoofaxLanguageSpecConfig extends LanguageSpecConfig implements ISpoofaxLanguageSpecConfig {
    private static final ILogger logger = LoggerUtils.logger(SpoofaxLanguageSpecConfig.class);

    private static final String PROP_SDF = "language.sdf";
    private static final String PROP_SDF_VERSION = PROP_SDF + ".version";


    private static final String PROP_SDF_MAIN_FILE = PROP_SDF + ".main-file";


    private static final String PROP_SDF_EXTERNAL_DEF = PROP_SDF + ".externalDef";
    private static final String PROP_SDF_ARGS = PROP_SDF + ".args";

    private static final String PROP_PRETTY_PRINT = PROP_SDF + ".pretty-print";

    private static final String PROP_PLACEHOLDER_PREFIX = PROP_SDF + ".placeholder.prefix";
    private static final String PROP_PLACEHOLDER_SUFFIX = PROP_SDF + ".placeholder.suffix";

    private static final String PROP_SDF_META = PROP_SDF + ".sdf-meta";

    private static final String PROP_STR = "language.stratego";
    private static final String PROP_STR_BUILD_SETTING = PROP_STR + ".build";
    private static final String PROP_STR_FORMAT = PROP_STR + ".format";
    private static final String PROP_STR_EXTERNAL_JAR = PROP_STR + ".externalJar.name";
    private static final String PROP_STR_EXTERNAL_JAR_FLAGS = PROP_STR + ".externalJar.flags";
    private static final String PROP_STR_ARGS = PROP_STR + ".args";

    private static final LanguageSpecBuildPhase defaultPhase = LanguageSpecBuildPhase.compile;
    private static final String PROP_BUILD = "build";
    private static final String PROP_BUILD_ANT = PROP_BUILD + ".ant";
    private static final String PROP_BUILD_STR = PROP_BUILD + ".stratego-cli";

    private final SpoofaxProjectConfig projectConfig;

    public SpoofaxLanguageSpecConfig(HierarchicalConfiguration<ImmutableNode> config,
        SpoofaxProjectConfig projectConfig) {
        super(config, projectConfig);
        this.projectConfig = projectConfig;
    }

    protected SpoofaxLanguageSpecConfig(final HierarchicalConfiguration<ImmutableNode> config,
        SpoofaxProjectConfig projectConfig, @Nullable LanguageIdentifier id, @Nullable String name,
        @Nullable Collection<LanguageContributionIdentifier> langContribs,
        @Nullable Collection<IGenerateConfig> generates, @Nullable Collection<IExportConfig> exports,
        @Nullable Collection<String> pardonedLanguages, @Nullable Boolean useBuildSystemSpec,
        @Nullable SdfVersion sdfVersion, @Nullable Boolean sdfEnabled, @Nullable Sdf2tableVersion sdf2tableVersion,
        @Nullable Boolean checkOverlap, @Nullable Boolean checkPriorities, @Nullable Boolean dataDependent,
        @Nullable String parseTable, @Nullable String completionsParseTable, @Nullable JSGLRVersion jsglrVersion,
        @Nullable String sdfMainFile, @Nullable PlaceholderCharacters placeholderCharacters,
        @Nullable String prettyPrint, @Nullable List<String> sdfMetaFile, @Nullable String externalDef,
        @Nullable Arguments sdfArgs, @Nullable StrategoBuildSetting buildSetting, @Nullable StrategoFormat format,
        @Nullable String externalJar, @Nullable String externalJarFlags, @Nullable Arguments strategoArgs,
        @Nullable Collection<IBuildStepConfig> buildSteps) {
        super(config, projectConfig, id, name, sdfEnabled, sdf2tableVersion, checkOverlap, checkPriorities,
            dataDependent, parseTable, completionsParseTable, jsglrVersion, langContribs, generates, exports,
            pardonedLanguages, useBuildSystemSpec);
        this.projectConfig = projectConfig;

        if(sdfVersion != null) {
            config.setProperty(PROP_SDF_VERSION, sdfVersion);
        }
        if(sdfMainFile != null) {
            config.setProperty(PROP_SDF_MAIN_FILE, sdfMainFile);
        }
        if(sdfMetaFile != null) {
            config.setProperty(PROP_SDF_META, sdfMetaFile);
        }
        if(placeholderCharacters != null) {
            config.setProperty(PROP_PLACEHOLDER_PREFIX, placeholderCharacters.prefix);
            config.setProperty(PROP_PLACEHOLDER_SUFFIX, placeholderCharacters.suffix);
        } else {
            config.setProperty(PROP_PLACEHOLDER_PREFIX, null);
            config.setProperty(PROP_PLACEHOLDER_SUFFIX, null);
        }
        if(prettyPrint != null) {
            config.setProperty(PROP_PRETTY_PRINT, prettyPrint);
        }
        if(externalDef != null) {
            config.setProperty(PROP_SDF_EXTERNAL_DEF, externalDef);
        }
        if(sdfArgs != null) {
            config.setProperty(PROP_SDF_ARGS, sdfArgs);
        }

        if(buildSetting != null) {
            config.setProperty(PROP_STR_BUILD_SETTING, buildSetting);
        }
        if(format != null) {
            config.setProperty(PROP_STR_FORMAT, format);
        }
        if(externalJar != null) {
            config.setProperty(PROP_STR_EXTERNAL_JAR, externalJar);
        }
        if(externalJarFlags != null) {
            config.setProperty(PROP_STR_EXTERNAL_JAR_FLAGS, externalJarFlags);
        }
        if(strategoArgs != null) {
            config.setProperty(PROP_STR_ARGS, strategoArgs);
        }

        if(buildSteps != null) {
            for(IBuildStepConfig buildStep : buildSteps) {
                buildStep.accept(new IBuildStepVisitor() {
                    @Override public void visit(AntBuildStepConfig buildStep) {
                        config.addProperty(PROP_BUILD_ANT, buildStep);
                    }

                    @Override public void visit(StrategoBuildStepConfig buildStep) {
                        config.addProperty(PROP_BUILD_STR, buildStep);
                    }
                });
            }

        }
    }


    @Override public boolean typesmart() {
        return projectConfig.typesmart();
    }

    @Override public NaBL2Config nabl2Config() {
        return projectConfig.nabl2Config();
    }


    @Override public SdfVersion sdfVersion() {
        final String value = this.config.getString(PROP_SDF_VERSION);
        return value != null ? SdfVersion.valueOf(value) : SdfVersion.sdf3;
    }



    @Override public String sdfMainFile() {
        final String value = this.config.getString(PROP_SDF_MAIN_FILE);
        return value;
    }

    @Override public String prettyPrintLanguage() {
        final String value = this.config.getString(PROP_PRETTY_PRINT);
        return value != null ? value : name();
    }

    @Override @Nullable public String sdfExternalDef() {
        return config.getString(PROP_SDF_EXTERNAL_DEF);
    }

    @Override public Arguments sdfArgs() {
        final List<String> values = config.getList(String.class, PROP_SDF_ARGS);
        final Arguments arguments = new Arguments();
        if(values != null) {
            for(String value : values) {
                arguments.add(value);
            }
        }
        return arguments;
    }

    @Override public StrategoBuildSetting strBuildSetting() {
        final String value = this.config.getString(PROP_STR_BUILD_SETTING);
        return value != null ? StrategoBuildSetting.valueOf(value) : StrategoBuildSetting.batch;
    }

    @Override public StrategoFormat strFormat() {
        if(strBuildSetting() == StrategoBuildSetting.incremental) {
            return StrategoFormat.jar;
        }
        final String value = this.config.getString(PROP_STR_FORMAT);
        return value != null ? StrategoFormat.valueOf(value) : StrategoFormat.ctree;
    }

    @Override public @Nullable String strExternalJar() {
        return config.getString(PROP_STR_EXTERNAL_JAR);
    }

    @Override public @Nullable String strExternalJarFlags() {
        return config.getString(PROP_STR_EXTERNAL_JAR_FLAGS);
    }

    @Override public Arguments strArgs() {
        @Nullable final List<String> values = config.getList(String.class, PROP_STR_ARGS);
        final Arguments arguments = new Arguments();
        if(values != null) {
            for(String value : values) {
                arguments.add(value);
            }
        }
        return arguments;
    }

    @Override public Collection<IBuildStepConfig> buildSteps() {
        final List<HierarchicalConfiguration<ImmutableNode>> antConfigs = config.configurationsAt(PROP_BUILD_ANT);
        final List<HierarchicalConfiguration<ImmutableNode>> strConfigs = config.configurationsAt(PROP_BUILD_STR);
        final List<IBuildStepConfig> buildSteps = Lists.newArrayListWithCapacity(antConfigs.size() + strConfigs.size());

        for(HierarchicalConfiguration<ImmutableNode> antConfig : antConfigs) {
            final LanguageSpecBuildPhase phase = phase(antConfig);
            final String file = antConfig.getString("file");
            final String target = antConfig.getString("target");
            if(file != null && target != null) {
                buildSteps.add(new AntBuildStepConfig(phase, file, target));
            }
        }
        for(HierarchicalConfiguration<ImmutableNode> strConfig : strConfigs) {
            final LanguageSpecBuildPhase phase = phase(strConfig);
            final String strategy = strConfig.getString("strategy");
            final List<String> args = strConfig.getList(String.class, "args", Lists.<String>newArrayList());
            if(strategy != null) {
                buildSteps.add(new StrategoBuildStepConfig(phase, strategy, args));
            }
        }

        return buildSteps;
    }

    private LanguageSpecBuildPhase phase(HierarchicalConfiguration<ImmutableNode> config) {
        final String phaseStr = config.getString("phase");
        try {
            return phaseStr != null ? LanguageSpecBuildPhase.valueOf(phaseStr) : defaultPhase;
        } catch(IllegalArgumentException e) {
            logger.warn("Language specification build phase with name {} does not exist, defaulting to {}", e, phaseStr,
                defaultPhase);
            return defaultPhase;
        }
    }


    @Override public String esvName() {
        return name();
    }

    @Override public String sdfName() {
        return name();
    }

    @Override public String metaSdfName() {
        return "Stratego-" + sdfName();
    }

    @Override public String strategoName() {
        return NameUtil.toJavaId(name().toLowerCase());
    }

    @Override public String packageName() {
        return NameUtil.toJavaId(identifier().id);
    }

    @Override public String javaName() {
        return NameUtil.toJavaId(name());
    }


    @Override public Collection<IMessage> validate(MessageBuilder mb) {
        final Collection<IMessage> messages = super.validate(mb);

        // TODO: validate sdf version
        // TODO: validate Stratego format
        // TODO: validate buildSteps

        return messages;
    }

    @Override public PlaceholderCharacters placeholderChars() {
        PlaceholderCharacters placeholderChars = null;
        String prefix = this.config.getString(PROP_PLACEHOLDER_PREFIX);
        String suffix = this.config.getString(PROP_PLACEHOLDER_SUFFIX);
        if(prefix == null && suffix == null) {
            placeholderChars = new PlaceholderCharacters("$", null);
        } else {
            try {
                placeholderChars = new PlaceholderCharacters(prefix, suffix);
            } catch(IllegalArgumentException e) {
                logger.warn(
                    "Placeholder suffix {} cannot be specified without a prefix, using \"[[\" and \"]]\" instead",
                    suffix);
                placeholderChars = new PlaceholderCharacters("$", null);
            }
        }
        return placeholderChars;
    }

    @Override public List<String> sdfMetaFiles() {
        final List<String> values = config.getList(String.class, PROP_SDF_META);
        if(values == null) {
            return Lists.newArrayList("Stratego-" + sdfName());
        }
        return values;
    }
}
