package org.metaborg.spoofax.core.language.dialect;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.IFacet;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.IdentificationFacet;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.language.ResourceExtensionFacet;
import org.metaborg.core.language.dialect.IDialectService;
import org.metaborg.spoofax.core.syntax.SyntaxFacet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;

public class DialectService implements IDialectService {
    private static final Logger logger = LoggerFactory.getLogger(DialectService.class);

    private final ILanguageService languageService;

    private final Class<SyntaxFacet> syntaxFacetClass = SyntaxFacet.class;
    private final Class<IdentificationFacet> identificationFacetClass = IdentificationFacet.class;
    private final Class<ResourceExtensionFacet> resourceExtensionFacetClass = ResourceExtensionFacet.class;

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
        final LanguageIdentifier baseId = base.id();
        final ILanguageImpl dialect =
            languageService.create(new LanguageIdentifier(baseId.groupId, baseId.id + "-" + name, baseId.version),
                location, name);
        for(IFacet facet : base.facets()) {
            if(ignoreFacet(facet.getClass())) {
                continue;
            }
            dialect.addFacet(facet);
        }
        dialect.addFacet(syntaxFacet);
        dialect.addFacet(new IdentificationFacet(new MetaFileIdentifier(base.facets(identificationFacetClass))));
        // Add dialect before updating maps, adding can cause an exception; maps should not be updated.
        languageService.add(dialect);
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
        dialect.removeFacet(syntaxFacet.getClass());
        dialect.addFacet(syntaxFacet);
        return dialect;
    }

    @Override public Iterable<ILanguageImpl> update(ILanguageImpl oldBase, ILanguageImpl newBase) {
        final Collection<ILanguageImpl> dialects = baseLanguageToDialects.get(oldBase);
        if(dialects.isEmpty()) {
            return dialects;
        }
        logger.debug("Updating base language for {} dialects", dialects.size());
        final Collection<ILanguageImpl> newDialects = Lists.newArrayListWithCapacity(dialects.size());
        for(ILanguageImpl dialect : dialects) {
            final String name = dialect.name();
            final IFacet parserFacet = dialect.facets(syntaxFacetClass);
            final ILanguageImpl newDialect = languageService.create(dialect.id(), dialect.location(), name);
            for(IFacet facet : newBase.facets()) {
                if(ignoreFacet(facet.getClass())) {
                    continue;
                }
                newDialect.addFacet(facet);
            }
            newDialect.addFacet(parserFacet);
            dialect.addFacet(new IdentificationFacet(new MetaFileIdentifier(newBase.facets(identificationFacetClass))));
            try {
                // Add dialect before updating maps, adding can cause an exception; maps should not be updated.
                // Adding reloads the dialect because location is the same, no need to remove old dialect.
                languageService.add(newDialect);
            } catch(IllegalStateException e) {
                final String message = String.format("Error updating dialect %s", name);
                logger.error(message, e);
                continue;
            }
            nameToDialect.put(name, newDialect);
            dialectToBase.remove(dialect);
            dialectToBase.put(newDialect, newBase);
            baseLanguageToDialects.remove(oldBase, dialect);
            baseLanguageToDialects.put(newBase, newDialect);
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
            languageService.remove(dialect);
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
            final String name = dialect.name();
            nameToDialect.remove(name);
            dialectToBase.remove(dialect);
            baseLanguageToDialects.remove(base, dialect);
            try {
                // Remove dialect after updating maps, exception indicates that dialect has already been removed.
                languageService.remove(dialect);
            } catch(IllegalStateException e) {
                final String message = String.format("Error removing dialect %s", name);
                logger.error(message, e);
                continue;
            }
            removedDialects.add(dialect);
        }
        return removedDialects;
    }


    private boolean ignoreFacet(Class<? extends IFacet> facetClass) {
        return facetClass.equals(syntaxFacetClass) || facetClass.equals(identificationFacetClass)
            || facetClass.equals(resourceExtensionFacetClass);
    }
}
