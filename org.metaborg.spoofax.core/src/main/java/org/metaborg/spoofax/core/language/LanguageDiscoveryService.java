package org.metaborg.spoofax.core.language;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.context.ContextFacet;
import org.metaborg.core.context.IContextStrategy;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageDiscoveryService;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.IdentificationFacet;
import org.metaborg.core.language.LanguageCreationRequest;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.language.LanguageImplIdentifier;
import org.metaborg.core.language.LanguagePathFacet;
import org.metaborg.core.language.LanguageVersion;
import org.metaborg.core.language.ResourceExtensionFacet;
import org.metaborg.core.language.ResourceExtensionsIdentifier;
import org.metaborg.spoofax.core.completion.CompletionFacet;
import org.metaborg.spoofax.core.completion.CompletionFacetFromItemSets;
import org.metaborg.spoofax.core.esv.ESVReader;
import org.metaborg.spoofax.core.stratego.StrategoFacet;
import org.metaborg.spoofax.core.stratego.StrategoFacetFromESV;
import org.metaborg.spoofax.core.style.StylerFacet;
import org.metaborg.spoofax.core.style.StylerFacetFromESV;
import org.metaborg.spoofax.core.syntax.SyntaxFacet;
import org.metaborg.spoofax.core.syntax.SyntaxFacetFromESV;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.transform.compile.CompilerFacet;
import org.metaborg.spoofax.core.transform.compile.CompilerFacetFromESV;
import org.metaborg.spoofax.core.transform.menu.MenusFacet;
import org.metaborg.spoofax.core.transform.menu.MenusFacetFromESV;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.resource.ContainsFileSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.io.binary.TermReader;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class LanguageDiscoveryService implements ILanguageDiscoveryService {
    private static final Logger logger = LoggerFactory.getLogger(LanguageDiscoveryService.class);

    private final ILanguageService languageService;
    private final ITermFactoryService termFactoryService;
    private final Map<String, IContextStrategy> contextStrategies;


    @Inject public LanguageDiscoveryService(ILanguageService languageService, ITermFactoryService termFactoryService,
        Map<String, IContextStrategy> contextStrategies) {
        this.languageService = languageService;
        this.termFactoryService = termFactoryService;
        this.contextStrategies = contextStrategies;
    }


    @Override public Iterable<ILanguageComponent> discover(FileObject location) throws Exception {
        final Collection<ILanguageComponent> components = Lists.newLinkedList();
        final FileObject[] esvFiles = location.findFiles(new ContainsFileSelector("packed.esv"));
        if(esvFiles == null) {
            return components;
        }
        final Set<FileObject> parents = Sets.newHashSet();
        for(FileObject esvFile : esvFiles) {
            final FileObject languageLocation = esvFile.getParent().getParent();
            if(parents.contains(languageLocation)) {
                logger.error("Found multiple packed ESV files in language directory: " + languageLocation
                    + ", skipping.");
                continue;
            }
            parents.add(languageLocation);
            components.add(componentFromESV(languageLocation, esvFile));
        }
        return components;
    }

    private ILanguageComponent componentFromESV(FileObject location, FileObject esvFile) throws Exception {
        logger.debug("Discovering language at {}", location);

        final TermReader reader =
            new TermReader(termFactoryService.getGeneric().getFactoryWithStorageType(IStrategoTerm.MUTABLE));
        final IStrategoTerm term = reader.parseFromStream(esvFile.getContent().getInputStream());
        if(term.getTermType() != IStrategoTerm.APPL) {
            throw new IllegalStateException("Packed ESV file does not contain a valid ESV term.");
        }
        final IStrategoAppl esvTerm = (IStrategoAppl) term;

        // GTODO: fetch this from ESV.
        final String groupId = "org.metaborg";
        final String id = languageId(esvTerm);
        final LanguageVersion version = LanguageVersion.parse(languageVersion(esvTerm));
        final String name = languageName(esvTerm);
        final LanguageIdentifier identifier = new LanguageIdentifier(groupId, id, version);
        // GTODO: read contributing component from ESV or POM file
        final LanguageCreationRequest request =
            languageService.create(identifier, location,
                Iterables2.singleton(new LanguageImplIdentifier(identifier, name)));

        final Iterable<String> extensions = Iterables2.from(extensions(esvTerm));
        final IdentificationFacet identificationFacet =
            new IdentificationFacet(new ResourceExtensionsIdentifier(extensions));
        request.addFacet(identificationFacet);

        final ResourceExtensionFacet resourceExtensionsFacet = new ResourceExtensionFacet(extensions);
        request.addFacet(resourceExtensionsFacet);

        // TODO: get facet strategy from language specification. Currently there is no specification yet so always
        // choose 'project' as the context strategy.
        final IContextStrategy contextStrategy = contextStrategies.get("project");
        final ContextFacet contextFacet = new ContextFacet(contextStrategy);
        request.addFacet(contextFacet);

        final SyntaxFacet syntaxFacet = SyntaxFacetFromESV.create(esvTerm, location);
        request.addFacet(syntaxFacet);

        final FileObject itemSetsFile = esvFile.getParent().resolveFile("item-sets.aterm");
        if(itemSetsFile.exists()) {
            final IStrategoTerm itemSetsTerm = reader.parseFromStream(itemSetsFile.getContent().getInputStream());
            final CompletionFacet completionFacet = CompletionFacetFromItemSets.create((IStrategoAppl) itemSetsTerm);
            request.addFacet(completionFacet);
        }

        final StrategoFacet strategoFacet = StrategoFacetFromESV.create(esvTerm, location);
        request.addFacet(strategoFacet);

        final MenusFacet menusFacet = MenusFacetFromESV.create(esvTerm, identifier);
        request.addFacet(menusFacet);

        final CompilerFacet compilerFacet = CompilerFacetFromESV.create(esvTerm, identifier);
        request.addFacet(compilerFacet);

        final StylerFacet stylerFacet = StylerFacetFromESV.create(esvTerm);
        request.addFacet(stylerFacet);

        final LanguagePathFacet languageComponentsFacet = LanguagePathFacetFromESV.create(esvTerm);
        request.addFacet(languageComponentsFacet);

        final ILanguageComponent component = languageService.add(request);
        return component;
    }

    private static String languageName(IStrategoAppl document) {
        return ESVReader.getProperty(document, "LanguageName");
    }

    private static String languageId(IStrategoAppl document) {
        return ESVReader.getProperty(document, "LanguageId");
    }

    private static String languageVersion(IStrategoAppl document) {
        return ESVReader.getProperty(document, "LanguageVersion", "");
    }

    private static String[] extensions(IStrategoAppl document) {
        return ESVReader.getProperty(document, "Extensions").split(",");
    }
}
