package org.metaborg.core.config;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.metaborg.core.language.LanguageIdentifier;

/**
 * An implementation of the {@link ILanguageComponentConfig} interface that is backed by an
 * {@link ImmutableConfiguration} object.
 */
public class ProjectConfig implements IProjectConfig, IConfig {
    private static final String PROP_COMPILE_DEPENDENCIES = "dependencies.compile";
    private static final String PROP_SOURCE_DEPENDENCIES = "dependencies.source";
    private static final String PROP_JAVA_DEPENDENCIES = "dependencies.java";

    protected final HierarchicalConfiguration<ImmutableNode> config;


    public ProjectConfig(HierarchicalConfiguration<ImmutableNode> config) {
        this.config = config;
    }

    protected ProjectConfig(HierarchicalConfiguration<ImmutableNode> config, Collection<LanguageIdentifier> compileDeps,
        Collection<LanguageIdentifier> sourceDeps, Collection<LanguageIdentifier> javaDeps) {
        this(config);
        config.setProperty(PROP_COMPILE_DEPENDENCIES, compileDeps);
        config.setProperty(PROP_SOURCE_DEPENDENCIES, sourceDeps);
        config.setProperty(PROP_JAVA_DEPENDENCIES, javaDeps);
    }


    @Override public HierarchicalConfiguration<ImmutableNode> getConfig() {
        return this.config;
    }

    @Override public Collection<LanguageIdentifier> compileDeps() {
        final List<LanguageIdentifier> deps = config.getList(LanguageIdentifier.class, PROP_COMPILE_DEPENDENCIES);
        return deps != null ? deps : Collections.<LanguageIdentifier>emptyList();
    }

    @Override public Collection<LanguageIdentifier> sourceDeps() {
        final List<LanguageIdentifier> deps = config.getList(LanguageIdentifier.class, PROP_SOURCE_DEPENDENCIES);
        return deps != null ? deps : Collections.<LanguageIdentifier>emptyList();
    }

    @Override public Collection<LanguageIdentifier> javaDeps() {
        final List<LanguageIdentifier> deps = config.getList(LanguageIdentifier.class, PROP_JAVA_DEPENDENCIES);
        return deps != null ? deps : Collections.<LanguageIdentifier>emptyList();
    }
}
