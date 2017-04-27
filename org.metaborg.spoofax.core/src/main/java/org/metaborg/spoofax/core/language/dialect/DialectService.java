package org.metaborg.spoofax.core.language.dialect;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.config.ILanguageComponentConfig;
import org.metaborg.core.language.IFacet;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.IdentificationFacet;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.ComponentCreationConfig;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.language.ResourceExtensionFacet;
import org.metaborg.core.language.dialect.IDialectService;
import org.metaborg.spoofax.core.syntax.SyntaxFacet;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;

/**
 * Default implementation for the dialect service. It is mostly generic, but contains some logic for .meta files, which
 * are Spoofax-specific.
 * 
 * A dialect is simply a language implementation, with a single component, that mimics its base language, but has a
 * different name and a few different facets. Dialects are created by copying over all facets from a language
 * implementation into a new language implementation, except that:
 * <ul>
 * <li>{@link ResourceExtensionFacet}: removed to prevent dialects from overriding an extension of the base language.
 * </li>
 * <li>{@link IdentificationFacet}: wrapped by {@link MetaFileIdentifier} such that files without a corresponding .meta
 * file do not identify to the dialect.</li>
 * <li>{@link SyntaxFacet}: replaced by the dialect's syntax facet that uses a different parse table.</li>
 * </ul>
 */
public class DialectService implements IDialectService {
    private static final ILogger logger = LoggerUtils.logger(DialectService.class);

    private final ILanguageService languageService;

    // private final Class<SyntaxFacet> syntaxFacetClass = SyntaxFacet.class;
    // private final Class<IdentificationFacet> identificationFacetClass = IdentificationFacet.class;
    // private final Class<ResourceExtensionFacet> resourceExtensionFacetClass = ResourceExtensionFacet.class;

    private final Map<String, ILanguageImpl> nameToDialect = Maps.newHashMap();
    private final Map<ILanguageImpl, ILanguageImpl> dialectToBase = Maps.newHashMap();
    private final Multimap<ILanguageImpl, ILanguageImpl> baseLanguageToDialects = HashMultimap.create();


    @Inject public DialectService(ILanguageService languageService) {
        this.languageService = languageService;
    }


    @Override public boolean hasDialect(String name) {
        return nameToDialect.containsKey(name);
    }

    @Override public @Nullable ILanguageImpl getDialect(String name) {
        return nameToDialect.get(name);
    }

    @Override public Iterable<ILanguageImpl> getDialects(ILanguageImpl base) {
        return baseLanguageToDialects.get(base);
    }

    @Override public ILanguageImpl getBase(ILanguageImpl dialect) {
        return dialectToBase.get(dialect);
    }

    @Override public ILanguageImpl add(String name, FileObject location, ILanguageImpl base, IFacet syntaxFacet) {
        if(nameToDialect.containsKey(name)) {
            final String message = String.format("Dialect with name %s already exists", name);
            logger.error(message);
            throw new MetaborgRuntimeException(message);
        }
        logger.debug("Adding dialect {} from {} with {} as base", name, location, base);

        final ILanguageImpl dialect = createDialect(name, location, base, syntaxFacet, true, true);

        nameToDialect.put(name, dialect);
        dialectToBase.put(dialect, base);
        baseLanguageToDialects.put(base, dialect);
        return dialect;
    }


    @Override public ILanguageImpl update(String name, IFacet syntaxFacet) {
        final ILanguageImpl dialect = nameToDialect.get(name);
        if(dialect == null) {
            final String message = String.format("Dialect with name %s does not exist", name);
            logger.error(message);
            throw new MetaborgRuntimeException(message);
        }
        logger.debug("Updating syntax facet for dialect {}", name);

        final FileObject location = Iterables.get(dialect.components(), 0).location();
        final ILanguageImpl newDialect = createDialect(name, location, dialect, syntaxFacet, false, false);

        nameToDialect.put(name, newDialect);

        return newDialect;
    }

    @Override public Iterable<ILanguageImpl> update(ILanguageImpl base) {
        final Collection<ILanguageImpl> dialects = baseLanguageToDialects.get(base);
        if(dialects.isEmpty()) {
            return dialects;
        }
        logger.debug("Updating base language for {} dialects", dialects.size());
        final Collection<ILanguageImpl> newDialects = Lists.newArrayListWithCapacity(dialects.size());
        for(ILanguageImpl dialect : dialects) {
            final String name = dialect.belongsTo().name();
            final FileObject location = Iterables.get(dialect.components(), 0).location();
            // HACK: assuming single syntax facet
            final IFacet syntaxFacet = dialect.facet(SyntaxFacet.class);

            final ILanguageImpl newDialect;
            try {
                // Add dialect before updating maps, adding can cause an exception; maps should not be updated.
                // Adding reloads the dialect because location is the same, no need to remove old dialect.
                // GTODO: what if id's or version change?
                newDialect = createDialect(name, location, base, syntaxFacet, true, true);
            } catch(IllegalStateException e) {
                final String message = String.format("Error updating dialect %s", name);
                logger.error(message, e);
                continue;
            }

            nameToDialect.put(name, newDialect);
            newDialects.add(newDialect);
        }
        return newDialects;
    }

    @Override public ILanguageImpl remove(String name) {
        final ILanguageImpl dialect = nameToDialect.remove(name);
        if(dialect == null) {
            final String message = String.format("Dialect with name %s does not exist", name);
            logger.error(message);
            throw new MetaborgRuntimeException(message);
        }
        logger.debug("Removing dialect {}", name);

        final ILanguageImpl base = dialectToBase.remove(dialect);
        baseLanguageToDialects.remove(base, dialect);
        try {
            // Remove dialect after updating maps, exception indicates that dialect has already been removed.
            final ILanguageComponent dialectComponent = Iterables.get(dialect.components(), 0);
            languageService.remove(dialectComponent);
        } catch(IllegalStateException e) {
            final String message = String.format("Error removing dialect %s", name);
            logger.error(message, e);
        }

        return dialect;
    }

    @Override public Iterable<ILanguageImpl> remove(ILanguageImpl base) {
        final Collection<ILanguageImpl> dialects = baseLanguageToDialects.get(base);
        if(dialects.isEmpty()) {
            return dialects;
        }
        logger.debug("Removing {} dialects for base language {}", dialects.size(), base);

        final Collection<ILanguageImpl> removedDialects = Lists.newArrayListWithCapacity(dialects.size());
        for(ILanguageImpl dialect : dialects) {
            final String name = dialect.belongsTo().name();
            nameToDialect.remove(name);
            dialectToBase.remove(dialect);
            baseLanguageToDialects.remove(base, dialect);
            try {
                // Remove dialect after updating maps, exception indicates that dialect has already been removed.
                final ILanguageComponent dialectComponent = Iterables.get(dialect.components(), 0);
                languageService.remove(dialectComponent);
            } catch(IllegalStateException e) {
                final String message = String.format("Error removing dialect %s", name);
                logger.error(message, e);
                continue;
            }
            removedDialects.add(dialect);
        }

        return removedDialects;
    }


    private ILanguageImpl createDialect(String name, FileObject location, ILanguageImpl base, IFacet syntaxFacet,
        boolean replaceIdentification, boolean appendDialectName) {
        final LanguageIdentifier baseId = base.id();
        final String dialectId;
        if(appendDialectName) {
            dialectId = baseId.id + "-Dialect-" + name;
        } else {
            dialectId = baseId.id;
        }
        final LanguageIdentifier id = new LanguageIdentifier(baseId.groupId, dialectId, baseId.version);
        // HACK: use config of first component.
        final ILanguageComponentConfig config = Iterables.get(base.components(), 0).config();
        final ComponentCreationConfig creationConfig = languageService.create(id, location,
            Iterables2.singleton(new LanguageContributionIdentifier(id, name)), config);

        for(IFacet facet : base.facets()) {
            if(facet instanceof IdentificationFacet && replaceIdentification) {
                creationConfig.addFacet(new IdentificationFacet(new MetaFileIdentifier((IdentificationFacet) facet)));
            } else if(facet instanceof SyntaxFacet || facet instanceof ResourceExtensionFacet) {
                // Ignore
            } else {
                creationConfig.addFacet(facet);
            }
        }
        creationConfig.addFacet(syntaxFacet);

        final ILanguageComponent dialectComponent = languageService.add(creationConfig);
        final ILanguageImpl dialect = Iterables.get(dialectComponent.contributesTo(), 0);

        return dialect;
    }
}
