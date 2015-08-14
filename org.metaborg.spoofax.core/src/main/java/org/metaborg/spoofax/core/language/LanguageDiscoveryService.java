package org.metaborg.spoofax.core.language;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.build.dependency.DependencyFacet;
import org.metaborg.core.context.ContextFacet;
import org.metaborg.core.context.IContextStrategy;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageDiscoveryService;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.IdentificationFacet;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageCreationRequest;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.language.LanguagePathFacet;
import org.metaborg.core.language.LanguageVersion;
import org.metaborg.core.language.ResourceExtensionFacet;
import org.metaborg.core.language.ResourceExtensionsIdentifier;
import org.metaborg.core.project.settings.IProjectSettings;
import org.metaborg.core.project.settings.IProjectSettingsService;
import org.metaborg.spoofax.core.analysis.AnalysisFacet;
import org.metaborg.spoofax.core.analysis.AnalysisFacetFromESV;
import org.metaborg.spoofax.core.completion.SemanticCompletionFacet;
import org.metaborg.spoofax.core.completion.SemanticCompletionFacetFromESV;
import org.metaborg.spoofax.core.completion.SyntacticCompletionFacet;
import org.metaborg.spoofax.core.completion.SyntacticCompletionFacetFromItemSets;
import org.metaborg.spoofax.core.esv.ESVReader;
import org.metaborg.spoofax.core.menu.MenuFacet;
import org.metaborg.spoofax.core.menu.MenusFacetFromESV;
import org.metaborg.spoofax.core.stratego.StrategoRuntimeFacet;
import org.metaborg.spoofax.core.stratego.StrategoRuntimeFacetFromESV;
import org.metaborg.spoofax.core.style.StylerFacet;
import org.metaborg.spoofax.core.style.StylerFacetFromESV;
import org.metaborg.spoofax.core.syntax.SyntaxFacet;
import org.metaborg.spoofax.core.syntax.SyntaxFacetFromESV;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.tracing.HoverFacet;
import org.metaborg.spoofax.core.tracing.ReferencesFacetsFromESV;
import org.metaborg.spoofax.core.tracing.ResolverFacet;
import org.metaborg.spoofax.core.transform.compile.CompilerFacet;
import org.metaborg.spoofax.core.transform.compile.CompilerFacetFromESV;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.resource.ContainsFileSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.ParseError;
import org.spoofax.terms.io.binary.TermReader;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class LanguageDiscoveryService implements ILanguageDiscoveryService {
    private static final Logger logger = LoggerFactory.getLogger(LanguageDiscoveryService.class);

    private final ILanguageService languageService;
    private final IProjectSettingsService projectSettingsService;
    private final ITermFactoryService termFactoryService;
    private final Map<String, IContextStrategy> contextStrategies;


    @Inject public LanguageDiscoveryService(ILanguageService languageService,
        IProjectSettingsService projectSettingsService, ITermFactoryService termFactoryService,
        Map<String, IContextStrategy> contextStrategies) {
        this.languageService = languageService;
        this.projectSettingsService = projectSettingsService;
        this.termFactoryService = termFactoryService;
        this.contextStrategies = contextStrategies;
    }


    @Override public Iterable<ILanguageComponent> discover(FileObject location) throws MetaborgException {
        try {
            final Collection<ILanguageComponent> components = Lists.newLinkedList();
            final FileObject[] esvFiles = location.findFiles(new ContainsFileSelector("packed.esv"));
            if(esvFiles == null || esvFiles.length == 0) {
                logger.error("No packed.esv files found at {}, no languages were discovered", location);
                return components;
            }
            final Set<FileObject> parents = Sets.newHashSet();
            for(FileObject esvFile : esvFiles) {
                final FileObject languageLocation = esvFile.getParent().getParent();
                if(parents.contains(languageLocation)) {
                    logger.error("Found multiple packed ESV files at: " + languageLocation + ", skipping");
                    continue;
                }
                parents.add(languageLocation);
                components.add(componentFromESV(languageLocation, esvFile));
            }
            return components;
        } catch(ParseError | IOException e) {
            final String message = String.format("Discovering language at %s failed unexpectedly", location);
            throw new MetaborgException(message, e);
        }
    }

    private ILanguageComponent componentFromESV(FileObject location, FileObject esvFile) throws MetaborgException,
        ParseError, IOException {
        logger.debug("Discovering language at {}", location);

        final TermReader reader =
            new TermReader(termFactoryService.getGeneric().getFactoryWithStorageType(IStrategoTerm.MUTABLE));
        final IStrategoTerm term = reader.parseFromStream(esvFile.getContent().getInputStream());
        if(term.getTermType() != IStrategoTerm.APPL) {
            throw new MetaborgException("Packed ESV file does not contain a valid ESV term");
        }
        final IStrategoAppl esvTerm = (IStrategoAppl) term;

        final LanguageIdentifier identifier;
        Iterable<LanguageContributionIdentifier> languageContributions;
        final IProjectSettings settings = projectSettingsService.get(location);
        if(settings != null) {
            identifier = settings.identifier();
            languageContributions = settings.languageContributions();
        } else {
            identifier =
                new LanguageIdentifier("org.metaborg", languageId(esvTerm),
                    LanguageVersion.parse(languageVersion(esvTerm)));
            languageContributions = Iterables2.<LanguageContributionIdentifier>empty();
        }
        if(Iterables.isEmpty(languageContributions)) {
            languageContributions =
                Iterables2.from(new LanguageContributionIdentifier(identifier, languageName(esvTerm)));
        }
        final LanguageCreationRequest request = languageService.create(identifier, location, languageContributions);

        final String[] extensions = extensions(esvTerm);
        if(extensions.length != 0) {
            final Iterable<String> extensionsIterable = Iterables2.from(extensions);

            final IdentificationFacet identificationFacet =
                new IdentificationFacet(new ResourceExtensionsIdentifier(extensionsIterable));
            request.addFacet(identificationFacet);

            final ResourceExtensionFacet resourceExtensionsFacet = new ResourceExtensionFacet(extensionsIterable);
            request.addFacet(resourceExtensionsFacet);
        }

        final SyntaxFacet syntaxFacet = SyntaxFacetFromESV.create(esvTerm, location);
        if(syntaxFacet != null) {
            request.addFacet(syntaxFacet);
        }

        final FileObject itemSetsFile = esvFile.getParent().resolveFile("item-sets.aterm");
        if(itemSetsFile.exists()) {
            final IStrategoTerm itemSetsTerm = reader.parseFromStream(itemSetsFile.getContent().getInputStream());
            final SyntacticCompletionFacet completionFacet =
                SyntacticCompletionFacetFromItemSets.create((IStrategoAppl) itemSetsTerm);
            request.addFacet(completionFacet);
        }

        final SemanticCompletionFacet semanticCompletionFacet = SemanticCompletionFacetFromESV.create(esvTerm);
        if(semanticCompletionFacet != null) {
            request.addFacet(semanticCompletionFacet);
        }

        final StrategoRuntimeFacet strategoRuntimeFacet = StrategoRuntimeFacetFromESV.create(esvTerm, location);
        if(strategoRuntimeFacet != null) {
            request.addFacet(strategoRuntimeFacet);
        }

        final AnalysisFacet analysisFacet = AnalysisFacetFromESV.create(esvTerm);
        if(analysisFacet != null) {
            request.addFacet(analysisFacet);

            // TODO: get facet strategy from language specification. Currently there is no specification yet so always
            // choose 'project' as the context strategy.
            final IContextStrategy contextStrategy = contextStrategies.get("project");
            final ContextFacet contextFacet = new ContextFacet(contextStrategy);
            request.addFacet(contextFacet);
        }

        final MenuFacet menusFacet = MenusFacetFromESV.create(esvTerm, identifier);
        if(menusFacet != null) {
            request.addFacet(menusFacet);
        }

        final CompilerFacet compilerFacet = CompilerFacetFromESV.create(esvTerm, identifier);
        if(compilerFacet != null) {
            request.addFacet(compilerFacet);
        }

        final StylerFacet stylerFacet = StylerFacetFromESV.create(esvTerm);
        if(stylerFacet != null) {
            request.addFacet(stylerFacet);
        }

        final ResolverFacet resolverFacet = ReferencesFacetsFromESV.createResolver(esvTerm);
        if(resolverFacet != null) {
            request.addFacet(resolverFacet);
        }

        final HoverFacet hoverFacet = ReferencesFacetsFromESV.createHover(esvTerm);
        if(hoverFacet != null) {
            request.addFacet(hoverFacet);
        }

        final LanguagePathFacet languageComponentsFacet = LanguagePathFacetFromESV.create(esvTerm);
        request.addFacet(languageComponentsFacet);

        if(settings != null) {
            final DependencyFacet dependencyFacet =
                new DependencyFacet(settings.compileDependencies(), settings.runtimeDependencies());
            request.addFacet(dependencyFacet);
        }

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
        final String extensionsStr = ESVReader.getProperty(document, "Extensions");
        if(extensionsStr == null) {
            return new String[0];
        }
        return extensionsStr.split(",");
    }
}
