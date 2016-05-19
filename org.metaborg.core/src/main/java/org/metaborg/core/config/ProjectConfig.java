package org.metaborg.core.config;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.metaborg.core.MetaborgConstants;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.MessageBuilder;

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

    protected final HierarchicalConfiguration<ImmutableNode> config;


    public ProjectConfig(HierarchicalConfiguration<ImmutableNode> config) {
        this.config = config;
        
        // Set metaborgVersion to default if it was not set in the config.
        if(!config.containsKey(PROP_METABORG_VERSION)) {
            config.setProperty(PROP_METABORG_VERSION, MetaborgConstants.METABORG_VERSION);
        }
    }

    protected ProjectConfig(HierarchicalConfiguration<ImmutableNode> config, String metaborgVersion,
        Collection<LanguageIdentifier> compileDeps, Collection<LanguageIdentifier> sourceDeps,
        Collection<LanguageIdentifier> javaDeps) {
        this(config);

        config.setProperty(PROP_METABORG_VERSION, metaborgVersion);
        config.setProperty(PROP_COMPILE_DEPENDENCIES, compileDeps);
        config.setProperty(PROP_SOURCE_DEPENDENCIES, sourceDeps);
        config.setProperty(PROP_JAVA_DEPENDENCIES, javaDeps);
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


    public Collection<IMessage> validate(MessageBuilder mb) {
        final Collection<IMessage> messages = Lists.newArrayList();
        validateDeps(config, PROP_COMPILE_DEPENDENCIES, "compile", mb, messages);
        validateDeps(config, PROP_SOURCE_DEPENDENCIES, "source", mb, messages);
        validateDeps(config, PROP_JAVA_DEPENDENCIES, "java", mb, messages);
        return messages;
    }

    private void validateDeps(ImmutableConfiguration config, String key, String name, MessageBuilder mb,
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
