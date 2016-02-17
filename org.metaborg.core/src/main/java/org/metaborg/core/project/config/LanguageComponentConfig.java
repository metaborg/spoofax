package org.metaborg.core.project.config;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ImmutableConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;

import com.google.common.collect.Lists;

/**
 * An implementation of the {@link ILanguageComponentConfig} interface that is backed by an
 * {@link ImmutableConfiguration} object.
 */
public class LanguageComponentConfig extends ProjectConfig implements ILanguageComponentConfig, IConfig {
    protected static final String PROP_IDENTIFIER = "id";
    protected static final String PROP_NAME = "name";
    protected static final String PROP_LANGUAGE_CONTRIBUTIONS_IDX_NAME = "contributions(%d).name";
    protected static final String PROP_LANGUAGE_CONTRIBUTIONS_IDX_ID = "contributions(%d).id";
    protected static final String PROP_LANGUAGE_CONTRIBUTIONS_LAST_NAME = "contributions.name";
    protected static final String PROP_LANGUAGE_CONTRIBUTIONS_LAST_ID = "contributions.id";
    protected static final String PROP_GENERATES = "generates";
    protected static final String PROP_EXPORTS = "exports";


    public LanguageComponentConfig(HierarchicalConfiguration<ImmutableNode> config) {
        super(config);
    }

    protected LanguageComponentConfig(HierarchicalConfiguration<ImmutableNode> config, LanguageIdentifier identifier,
        String name, Collection<LanguageIdentifier> compileDeps, Collection<LanguageIdentifier> sourceDeps,
        Collection<LanguageIdentifier> javaDeps, Collection<LanguageContributionIdentifier> langContribs,
        Collection<Generate> generates, Collection<Export> exports) {
        super(config, compileDeps, sourceDeps, javaDeps);

        config.setProperty(PROP_NAME, name);
        config.setProperty(PROP_IDENTIFIER, identifier);
        for(LanguageContributionIdentifier lcid : langContribs) {
            config.addProperty(String.format(PROP_LANGUAGE_CONTRIBUTIONS_IDX_ID, -1), lcid.identifier);
            config.addProperty(PROP_LANGUAGE_CONTRIBUTIONS_LAST_NAME, lcid.name);
        }
        config.setProperty(PROP_GENERATES, generates);
        config.setProperty(PROP_EXPORTS, exports);
    }


    @Override public LanguageIdentifier identifier() {
        final LanguageIdentifier value = config.get(LanguageIdentifier.class, PROP_IDENTIFIER);
        return value != null ? value : LanguageIdentifier.EMPTY;
    }

    @Override public String name() {
        final String value = config.getString(PROP_NAME);
        return value != null ? value : "";
    }

    @Override public Collection<LanguageContributionIdentifier> langContribs() {
        final List<LanguageIdentifier> ids =
            config.getList(LanguageIdentifier.class, PROP_LANGUAGE_CONTRIBUTIONS_LAST_ID);
        if(ids == null) {
            return Lists.newArrayList();
        }

        final List<LanguageContributionIdentifier> lcids = Lists.newArrayListWithCapacity(ids.size());
        for(int i = 0; i < ids.size(); i++) {
            LanguageIdentifier identifier = ids.get(i);
            String name = config.getString(String.format(PROP_LANGUAGE_CONTRIBUTIONS_IDX_NAME, i));
            lcids.add(new LanguageContributionIdentifier(identifier, name));
        }
        return lcids;
    }

    @Override public Collection<Generate> generates() {
        final List<Generate> generates = config.getList(Generate.class, PROP_GENERATES);
        return generates != null ? generates : Collections.<Generate>emptyList();
    }

    @Override public Collection<Export> exports() {
        final List<Export> exports = config.getList(Export.class, PROP_EXPORTS);
        return exports != null ? exports : Collections.<Export>emptyList();
    }
}
