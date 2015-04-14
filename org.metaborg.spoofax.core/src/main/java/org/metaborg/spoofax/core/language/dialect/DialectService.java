package org.metaborg.spoofax.core.language.dialect;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.SpoofaxRuntimeException;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageFacet;
import org.metaborg.spoofax.core.language.ILanguageService;
import org.metaborg.spoofax.core.language.IdentificationFacet;
import org.metaborg.spoofax.core.language.ResourceExtensionFacet;
import org.metaborg.spoofax.core.syntax.SyntaxFacet;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
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
    private final ITermFactoryService termFactoryService;

    private final Class<? extends ILanguageFacet> syntaxFacetClass = SyntaxFacet.class;
    private final Class<? extends ILanguageFacet> identifierFacetClass = IdentificationFacet.class;
    private final Class<? extends ILanguageFacet> resourceExtensionFacetClass = ResourceExtensionFacet.class;

    private final Map<String, ILanguage> nameToDialect = Maps.newHashMap();
    private final Map<ILanguage, ILanguage> dialectToBase = Maps.newHashMap();
    private final Multimap<ILanguage, ILanguage> baseLanguageToDialects = HashMultimap.create();


    @Inject public DialectService(ILanguageService languageService, ITermFactoryService termFactoryService) {
        this.languageService = languageService;
        this.termFactoryService = termFactoryService;
    }


    @Override public boolean hasDialect(String name) {
        return nameToDialect.containsKey(name);
    }

    @Override public @Nullable ILanguage getDialect(String name) {
        return nameToDialect.get(name);
    }

    @Override public Iterable<ILanguage> getDialects(ILanguage base) {
        return baseLanguageToDialects.get(base);
    }

    @Override public ILanguage getBase(ILanguage dialect) {
        return dialectToBase.get(dialect);
    }

    @Override public ILanguage add(String name, FileObject location, ILanguage base, ILanguageFacet syntaxFacet) {
        if(nameToDialect.containsKey(name)) {
            final String message = String.format("Dialect with name %s already exists", name);
            logger.error(message);
            throw new SpoofaxRuntimeException(message);
        }
        logger.debug("Adding dialect {} from {} with {} as base", name, location, base);
        final ILanguage dialect = languageService.create(name, base.version(), location);
        for(ILanguageFacet facet : base.facets()) {
            if(ignoreFacet(facet.getClass())) {
                continue;
            }
            dialect.addFacet(facet);
        }
        dialect.addFacet(syntaxFacet);
        dialect.addFacet(new IdentificationFacet(new ResourceDialectIdentifier(termFactoryService, name)));
        // Add dialect before updating maps, adding can cause an exception; maps should not be updated.
        languageService.add(dialect);
        nameToDialect.put(name, dialect);
        dialectToBase.put(dialect, base);
        baseLanguageToDialects.put(base, dialect);
        return dialect;
    }

    @Override public ILanguage update(String name, ILanguageFacet syntaxFacet) {
        final ILanguage dialect = nameToDialect.get(name);
        if(dialect == null) {
            final String message = String.format("Dialect with name %s does not exist", name);
            logger.error(message);
            throw new SpoofaxRuntimeException(message);
        }
        logger.debug("Updating syntax facet for dialect {}", name);
        dialect.removeFacet(syntaxFacet.getClass());
        dialect.addFacet(syntaxFacet);
        return dialect;
    }

    @Override public Iterable<ILanguage> update(ILanguage oldBase, ILanguage newBase) {
        final Collection<ILanguage> dialects = baseLanguageToDialects.get(oldBase);
        if(dialects.isEmpty()) {
            return dialects;
        }
        logger.debug("Updating base language for {} dialects", dialects.size());
        final Collection<ILanguage> newDialects = Lists.newArrayListWithCapacity(dialects.size());
        for(ILanguage dialect : dialects) {
            final String name = dialect.name();
            final ILanguageFacet parserFacet = dialect.facet(syntaxFacetClass);
            final ILanguage newDialect = languageService.create(name, newBase.version(), dialect.location());
            for(ILanguageFacet facet : newBase.facets()) {
                if(ignoreFacet(facet.getClass())) {
                    continue;
                }
                newDialect.addFacet(facet);
            }
            newDialect.addFacet(parserFacet);
            newDialect.addFacet(new IdentificationFacet(new ResourceDialectIdentifier(termFactoryService, name)));
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

    @Override public ILanguage remove(String name) {
        final ILanguage dialect = nameToDialect.remove(name);
        if(dialect == null) {
            final String message = String.format("Dialect with name %s does not exist", name);
            logger.error(message);
            throw new SpoofaxRuntimeException(message);
        }
        logger.debug("Removing dialect {}", name);
        final ILanguage base = dialectToBase.remove(dialect);
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

    @Override public Iterable<ILanguage> remove(ILanguage base) {
        final Collection<ILanguage> dialects = baseLanguageToDialects.get(base);
        if(dialects.isEmpty()) {
            return dialects;
        }
        logger.debug("Removing {} dialects for base language {}", dialects.size(), base);
        final Collection<ILanguage> removedDialects = Lists.newArrayListWithCapacity(dialects.size());
        for(ILanguage dialect : dialects) {
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


    private boolean ignoreFacet(Class<? extends ILanguageFacet> facetClass) {
        return facetClass.equals(syntaxFacetClass) || facetClass.equals(identifierFacetClass)
            || facetClass.equals(resourceExtensionFacetClass);
    }
}
