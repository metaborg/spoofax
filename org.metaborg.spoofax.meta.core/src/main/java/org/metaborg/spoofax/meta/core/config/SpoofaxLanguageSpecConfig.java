package org.metaborg.spoofax.meta.core.config;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.metaborg.core.config.Export;
import org.metaborg.core.config.Generate;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.project.NameUtil;
import org.metaborg.meta.core.config.LanguageSpecConfig;
import org.metaborg.spoofax.core.project.settings.StrategoFormat;
import org.metaborg.util.cmd.Arguments;

/**
 * An implementation of the {@link ISpoofaxLanguageSpecConfig} interface that is backed by an
 * {@link ImmutableConfiguration} object.
 */
public class SpoofaxLanguageSpecConfig extends LanguageSpecConfig implements ISpoofaxLanguageSpecConfig {
    private static final long serialVersionUID = -2143964605340506212L;

    private static final String PROP_SDF_ARGS = "language.sdf.args";
    private static final String PROP_EXTERNAL_DEF = "language.sdf.externalDef";

    private static final String PROP_FORMAT = "language.stratego.format";
    private static final String PROP_EXTERNAL_JAR = "language.stratego.externalJar.name";
    private static final String PROP_EXTERNAL_JAR_FLAGS = "language.stratego.externalJar.flags";
    private static final String PROP_STRATEGO_ARGS = "language.stratego.args";

    private static final String PROP_PARDONED_LANGUAGES = "pardonedLanguages";


    public SpoofaxLanguageSpecConfig(HierarchicalConfiguration<ImmutableNode> config) {
        super(config);
    }

    protected SpoofaxLanguageSpecConfig(HierarchicalConfiguration<ImmutableNode> config, LanguageIdentifier id,
        String name, Collection<LanguageIdentifier> compileDeps, Collection<LanguageIdentifier> sourceDeps,
        Collection<LanguageIdentifier> javaDeps, Collection<LanguageContributionIdentifier> langContribs,
        Collection<Generate> generates, Collection<Export> exports, String metaborgVersion,
        Collection<String> pardonedLanguages, StrategoFormat format, String externalDef, String externalJar,
        String externalJarFlags, Arguments sdfArgs, Arguments strategoArgs) {
        super(config, id, name, compileDeps, sourceDeps, javaDeps, langContribs, generates, exports, metaborgVersion);

        config.setProperty(PROP_PARDONED_LANGUAGES, pardonedLanguages);
        config.setProperty(PROP_FORMAT, format);
        config.setProperty(PROP_EXTERNAL_DEF, externalDef);
        config.setProperty(PROP_EXTERNAL_JAR, externalJar);
        config.setProperty(PROP_EXTERNAL_JAR_FLAGS, externalJarFlags);
        config.setProperty(PROP_SDF_ARGS, sdfArgs);
        config.setProperty(PROP_STRATEGO_ARGS, strategoArgs);
    }


    @Override public Collection<String> pardonedLanguages() {
        final List<String> value = this.config.getList(String.class, PROP_PARDONED_LANGUAGES);
        return value != null ? value : Collections.<String>emptyList();
    }

    public StrategoFormat format() {
        final String value = this.config.getString(PROP_FORMAT);
        return value != null ? StrategoFormat.valueOf(value) : StrategoFormat.ctree;
    }

    @Override public String sdfName() {
        return name();
    }

    @Override public String metaSdfName() {
        return sdfName() + "-Stratego";
    }

    public Arguments sdfArgs() {
        final List<String> values = this.config.getList(String.class, PROP_SDF_ARGS);
        final Arguments arguments = new Arguments();
        if(values != null) {
            for(String value : values) {
                arguments.add(value);
            }
        }
        return arguments;
    }

    @Nullable public String externalDef() {
        return this.config.getString(PROP_EXTERNAL_DEF);
    }

    @Override public Arguments strategoArgs() {
        @Nullable final List<String> values = this.config.getList(String.class, PROP_STRATEGO_ARGS);
        final Arguments arguments = new Arguments();
        if(values != null) {
            for(String value : values) {
                arguments.add(value);
            }
        }
        return arguments;
    }

    @Override public @Nullable String externalJar() {
        return this.config.getString(PROP_EXTERNAL_JAR);
    }

    @Override public @Nullable String externalJarFlags() {
        return this.config.getString(PROP_EXTERNAL_JAR_FLAGS);
    }

    @Override public String strategoName() {
        return NameUtil.toJavaId(this.name().toLowerCase());
    }

    @Override public String javaName() {
        return NameUtil.toJavaId(this.name());
    }

    @Override public String packageName() {
        return NameUtil.toJavaId(this.identifier().id);
    }

    @Override public String strategiesPackageName() {
        return packageName() + ".strategies";
    }

    @Override public String esvName() {
        return name();
    }
}
