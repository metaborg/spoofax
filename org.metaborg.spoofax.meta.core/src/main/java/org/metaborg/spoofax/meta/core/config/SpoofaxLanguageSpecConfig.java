package org.metaborg.spoofax.meta.core.config;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.metaborg.core.config.IExportConfig;
import org.metaborg.core.config.IGenerateConfig;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageBuilder;
import org.metaborg.core.project.NameUtil;
import org.metaborg.meta.core.config.LanguageSpecConfig;
import org.metaborg.util.cmd.Arguments;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.collect.Lists;

/**
 * An implementation of the {@link ISpoofaxLanguageSpecConfig} interface that is backed by an
 * {@link ImmutableConfiguration} object.
 */
public class SpoofaxLanguageSpecConfig extends LanguageSpecConfig implements ISpoofaxLanguageSpecConfig {
    private static final ILogger logger = LoggerUtils.logger(SpoofaxLanguageSpecConfig.class);

    private static final String PROP_SDF = "language.sdf";
    private static final String PROP_SDF_VERSION = PROP_SDF + ".version";


    private static final String PROP_SDF_MAIN_FILE = PROP_SDF + ".main-file";


    private static final String PROP_SDF2TABLE_VERSION = PROP_SDF + ".sdf2table";
    private static final String PROP_SDF_EXTERNAL_DEF = PROP_SDF + ".externalDef";
    private static final String PROP_SDF_ARGS = PROP_SDF + ".args";

    private static final String PROP_PRETTY_PRINT = "pretty-print";

    private static final String PROP_PLACEHOLDER_PREFIX = "placeholder.prefix";
    private static final String PROP_PLACEHOLDER_SUFFIX = "placeholder.suffix";


    private static final String PROP_STR = "language.stratego";
    private static final String PROP_STR_FORMAT = PROP_STR + ".format";
    private static final String PROP_STR_EXTERNAL_JAR = PROP_STR + ".externalJar.name";
    private static final String PROP_STR_EXTERNAL_JAR_FLAGS = PROP_STR + ".externalJar.flags";
    private static final String PROP_STR_ARGS = PROP_STR + ".args";

    private static final LanguageSpecBuildPhase defaultPhase = LanguageSpecBuildPhase.compile;
    private static final String PROP_BUILD = "build";
    private static final String PROP_BUILD_ANT = PROP_BUILD + ".ant";
    private static final String PROP_BUILD_STR = PROP_BUILD + ".stratego-cli";


    public SpoofaxLanguageSpecConfig(HierarchicalConfiguration<ImmutableNode> config) {
        super(config);
    }

    protected SpoofaxLanguageSpecConfig(final HierarchicalConfiguration<ImmutableNode> config,
        @Nullable LanguageIdentifier id, @Nullable String name, @Nullable Collection<LanguageIdentifier> compileDeps,
        @Nullable Collection<LanguageIdentifier> sourceDeps, @Nullable Collection<LanguageIdentifier> javaDeps,
        @Nullable Boolean typesmart, @Nullable Collection<LanguageContributionIdentifier> langContribs,
        @Nullable Collection<IGenerateConfig> generates, @Nullable Collection<IExportConfig> exports,
        @Nullable String metaborgVersion, @Nullable Collection<String> pardonedLanguages,
        @Nullable Boolean useBuildSystemSpec, @Nullable SdfVersion sdfVersion, @Nullable Boolean sdfEnabled,
        @Nullable String parseTable, @Nullable String completionsParseTable, @Nullable String sdfMainFile,
        @Nullable Sdf2tableVersion sdf2tableVersion, @Nullable PlaceholderCharacters placeholderCharacters,
        @Nullable String prettyPrint, @Nullable String externalDef, @Nullable Arguments sdfArgs,
        @Nullable StrategoFormat format, @Nullable String externalJar, @Nullable String externalJarFlags,
        @Nullable Arguments strategoArgs, @Nullable Collection<IBuildStepConfig> buildSteps) {
        super(config, metaborgVersion, id, name, compileDeps, sourceDeps, javaDeps, sdfEnabled, parseTable,
            completionsParseTable, typesmart, langContribs, generates, exports, pardonedLanguages, useBuildSystemSpec);

        if(sdfVersion != null) {
            config.setProperty(PROP_SDF_VERSION, sdfVersion);
        }
        if(sdf2tableVersion != null) {
            config.setProperty(PROP_SDF2TABLE_VERSION, sdf2tableVersion);
        }

        if(sdfMainFile != null) {
            config.setProperty(PROP_SDF_MAIN_FILE, sdfMainFile);
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


    @Override public SdfVersion sdfVersion() {
        final String value = this.config.getString(PROP_SDF_VERSION);
        return value != null ? SdfVersion.valueOf(value) : SdfVersion.sdf3;
    }

    @Override public Sdf2tableVersion sdf2tableVersion() {
        final String value = this.config.getString(PROP_SDF2TABLE_VERSION);
        return value != null ? Sdf2tableVersion.valueOf(value) : Sdf2tableVersion.c;
    }

    @Override public String sdfMainFile() {
        final String value = this.config.getString(PROP_SDF_MAIN_FILE);
        return value;
    }

    @Override public String prettyPrintLanguage() {
        final String value = this.config.getString(PROP_PRETTY_PRINT);
        return value != null ? value : name();
    }

    @Nullable public String sdfExternalDef() {
        return config.getString(PROP_SDF_EXTERNAL_DEF);
    }

    public Arguments sdfArgs() {
        final List<String> values = config.getList(String.class, PROP_SDF_ARGS);
        final Arguments arguments = new Arguments();
        if(values != null) {
            for(String value : values) {
                arguments.add(value);
            }
        }
        return arguments;
    }


    public StrategoFormat strFormat() {
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


    public Collection<IMessage> validate(MessageBuilder mb) {
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
            placeholderChars = new PlaceholderCharacters("[[", "]]");
        } else {
            try {
                placeholderChars = new PlaceholderCharacters(prefix, suffix);
            } catch(IllegalArgumentException e) {
                logger.warn(
                    "Placeholder suffix {} cannot be specified without a prefix, using \"[[\" and \"]]\" instead",
                    suffix);
                placeholderChars = new PlaceholderCharacters("[[", "]]");
            }
        }
        return placeholderChars;
    }
}
