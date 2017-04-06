package org.metaborg.core.config;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.metaborg.core.MetaborgConstants;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageBuilder;
import org.metaborg.util.config.NaBL2Config;

import com.google.common.collect.Lists;

/**
 * An implementation of the {@link ILanguageComponentConfig} interface that is backed by an
 * {@link ImmutableConfiguration} object.
 */
public class ProjectConfig implements IProjectConfig, IConfig {
    private static final String PROP_METABORG_VERSION = "metaborgVersion";
    private static final String PROP_COMPILE_DEPENDENCIES = "dependencies.compile";
    private static final String PROP_SOURCE_DEPENDENCIES = "dependencies.source";
    private static final String PROP_JAVA_DEPENDENCIES = "dependencies.java";

    private static final String PROP_STR_TYPESMART = "debug.typesmart";

    private static final String PROP_RUNTIME = "runtime";
    private static final String PROP_NABL2 = PROP_RUNTIME + ".nabl2";


    protected final HierarchicalConfiguration<ImmutableNode> config;


    public ProjectConfig(HierarchicalConfiguration<ImmutableNode> config) {
        this.config = config;

        // Set metaborgVersion to default if it was not set in the config.
        if(!config.containsKey(PROP_METABORG_VERSION)) {
            config.setProperty(PROP_METABORG_VERSION, MetaborgConstants.METABORG_VERSION);
        }
    }

    protected ProjectConfig(HierarchicalConfiguration<ImmutableNode> config, @Nullable String metaborgVersion,
            @Nullable Collection<LanguageIdentifier> compileDeps, @Nullable Collection<LanguageIdentifier> sourceDeps,
            @Nullable Collection<LanguageIdentifier> javaDeps, @Nullable Boolean typesmart,
            @Nullable NaBL2Config nabl2Config) {
        this(config);

        if(metaborgVersion != null) {
            config.setProperty(PROP_METABORG_VERSION, metaborgVersion);
        }
        if(compileDeps != null) {
            config.setProperty(PROP_COMPILE_DEPENDENCIES, compileDeps);
        }
        if(sourceDeps != null) {
            config.setProperty(PROP_SOURCE_DEPENDENCIES, sourceDeps);
        }
        if(javaDeps != null) {
            config.setProperty(PROP_JAVA_DEPENDENCIES, javaDeps);
        }
        if(typesmart != null) {
            config.setProperty(PROP_STR_TYPESMART, typesmart);
        }
        if(nabl2Config != null) {
            NaBL2ConfigReaderWriter.write(nabl2Config, config.configurationAt(PROP_NABL2, true));
        }
    }


    @Override public HierarchicalConfiguration<ImmutableNode> getConfig() {
        return this.config;
    }


    @Override public String metaborgVersion() {
        return config.getString(PROP_METABORG_VERSION, MetaborgConstants.METABORG_VERSION);
    }

    @Override public Collection<LanguageIdentifier> compileDeps() {
        return config.getList(LanguageIdentifier.class, PROP_COMPILE_DEPENDENCIES,
                Collections.<LanguageIdentifier>emptyList());
    }

    @Override public Collection<LanguageIdentifier> sourceDeps() {
        return config.getList(LanguageIdentifier.class, PROP_SOURCE_DEPENDENCIES,
                Collections.<LanguageIdentifier>emptyList());
    }

    @Override public Collection<LanguageIdentifier> javaDeps() {
        return config.getList(LanguageIdentifier.class, PROP_JAVA_DEPENDENCIES,
                Collections.<LanguageIdentifier>emptyList());
    }

    @Override public boolean typesmart() {
        return config.getBoolean(PROP_STR_TYPESMART, false);
    }

    @Override public NaBL2Config nabl2Config() {
        return config.containsKey(PROP_NABL2) ? NaBL2ConfigReaderWriter.read(config.configurationAt(PROP_NABL2))
                : NaBL2Config.DEFAULT;
    }


    public Collection<IMessage> validate(MessageBuilder mb) {
        final Collection<IMessage> messages = Lists.newArrayList();
        validateDeps(config, PROP_COMPILE_DEPENDENCIES, "compile", mb, messages);
        validateDeps(config, PROP_SOURCE_DEPENDENCIES, "source", mb, messages);
        validateDeps(config, PROP_JAVA_DEPENDENCIES, "java", mb, messages);
        if(config.containsKey(PROP_NABL2)) {
            messages.addAll(NaBL2ConfigReaderWriter.validate(config.immutableSubset(PROP_NABL2), mb));
        }
        return messages;
    }

    private static void validateDeps(ImmutableConfiguration config, String key, String name, MessageBuilder mb,
            Collection<IMessage> messages) {
        final List<String> depStrs = config.getList(String.class, key, Lists.<String>newArrayList());
        for(String depStr : depStrs) {
            try {
                LanguageIdentifier.parse(depStr);
            } catch(IllegalArgumentException e) {
                messages.add(mb.withMessage("Invalid " + name + " dependency. " + e.getMessage()).build());
            }
        }
    }
}
