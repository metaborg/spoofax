package org.metaborg.spoofax.core.language;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.AnalyzerFacet;
import org.metaborg.core.analysis.IAnalyzer;
import org.metaborg.core.build.dependency.DependencyFacet;
import org.metaborg.core.context.ContextFacet;
import org.metaborg.core.context.IContextFactory;
import org.metaborg.core.context.IContextStrategy;
import org.metaborg.core.context.ProjectContextStrategy;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageDiscoveryRequest;
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
import org.metaborg.core.syntax.ParseFacet;
import org.metaborg.spoofax.core.action.ActionFacet;
import org.metaborg.spoofax.core.action.ActionFacetFromESV;
import org.metaborg.spoofax.core.analysis.AnalysisFacet;
import org.metaborg.spoofax.core.analysis.AnalysisFacetFromESV;
import org.metaborg.spoofax.core.analysis.legacy.StrategoAnalyzer;
import org.metaborg.spoofax.core.analysis.taskengine.TaskEngineAnalyzer;
import org.metaborg.spoofax.core.context.ContextFacetFromESV;
import org.metaborg.spoofax.core.context.IndexTaskContextFactory;
import org.metaborg.spoofax.core.context.LegacyContextFactory;
import org.metaborg.spoofax.core.esv.ESVReader;
import org.metaborg.spoofax.core.outline.OutlineFacet;
import org.metaborg.spoofax.core.outline.OutlineFacetFromESV;
import org.metaborg.spoofax.core.stratego.StrategoRuntimeFacet;
import org.metaborg.spoofax.core.stratego.StrategoRuntimeFacetFromESV;
import org.metaborg.spoofax.core.style.StylerFacet;
import org.metaborg.spoofax.core.style.StylerFacetFromESV;
import org.metaborg.spoofax.core.syntax.ParseFacetFromESV;
import org.metaborg.spoofax.core.syntax.SyntaxFacet;
import org.metaborg.spoofax.core.syntax.SyntaxFacetFromESV;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.tracing.HoverFacet;
import org.metaborg.spoofax.core.tracing.ResolverFacet;
import org.metaborg.spoofax.core.tracing.ResolverFacetFromESV;
import org.metaborg.spoofax.core.transform.compile.CompilerFacet;
import org.metaborg.spoofax.core.transform.compile.CompilerFacetFromESV;
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.resource.FileSelectorUtils;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.ParseError;
import org.spoofax.terms.io.binary.TermReader;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

@Deprecated
public class LanguageDiscoveryService implements ILanguageDiscoveryService {
    private static final ILogger logger = LoggerUtils.logger(LanguageDiscoveryService.class);

    private final ILanguageService languageService;
    private final IProjectSettingsService projectSettingsService;
    private final ITermFactoryService termFactoryService;
    private final Map<String, IContextFactory> contextFactories;
    private final Map<String, IContextStrategy> contextStrategies;
    private final Map<String, IAnalyzer<IStrategoTerm, IStrategoTerm>> analyzers;


    @Inject public LanguageDiscoveryService(ILanguageService languageService,
        IProjectSettingsService projectSettingsService, ITermFactoryService termFactoryService,
        Map<String, IContextFactory> contextFactories, Map<String, IContextStrategy> contextStrategies,
        Map<String, IAnalyzer<IStrategoTerm, IStrategoTerm>> analyzers) {
        this.languageService = languageService;
        this.projectSettingsService = projectSettingsService;
        this.termFactoryService = termFactoryService;
        this.contextFactories = contextFactories;
        this.contextStrategies = contextStrategies;
        this.analyzers = analyzers;
    }


    @Override public Iterable<ILanguageDiscoveryRequest> request(FileObject location) throws MetaborgException {
        final Collection<ILanguageDiscoveryRequest> requests = Lists.newLinkedList();
        final FileObject[] esvFiles;
        try {
            esvFiles = location.findFiles(FileSelectorUtils.endsWith("packed.esv"));
        } catch(FileSystemException e) {
            throw new MetaborgException("Searching for language components failed unexpectedly", e);
        }

        if(esvFiles == null || esvFiles.length == 0) {
            return requests;
        }

        final Set<FileObject> parents = Sets.newHashSet();
        for(FileObject esvFile : esvFiles) {
            final Collection<String> errors = Lists.newLinkedList();
            final Collection<Throwable> exceptions = Lists.newLinkedList();

            final FileObject languageLocation;
            try {
                languageLocation = esvFile.getParent().getParent();
            } catch(FileSystemException e) {
                logger.error("Could not resolve parent directory of ESV file {}, skipping", e, esvFile);
                continue;
            }

            if(parents.contains(languageLocation)) {
                final String message =
                    logger.format("Found multiple packed ESV files at {}, skipping", languageLocation);
                errors.add(message);
                requests.add(new LanguageDiscoveryRequest(languageLocation, errors, exceptions));
                continue;
            }
            parents.add(languageLocation);

            final IStrategoAppl esvTerm;
            try {
                esvTerm = esvTerm(languageLocation, esvFile);
            } catch(ParseError | IOException | MetaborgException e) {
                exceptions.add(e);
                requests.add(new LanguageDiscoveryRequest(languageLocation, errors, exceptions));
                continue;
            }

            final IProjectSettings settings = projectSettingsService.get(languageLocation);
            if(settings == null) {
                final String message = logger.format("Cannot retrieve project settings at {}", languageLocation);
                errors.add(message);
                requests.add(new LanguageDiscoveryRequest(languageLocation, errors, exceptions));
                continue;
            }

            SyntaxFacet syntaxFacet = null;
            try {
                syntaxFacet = SyntaxFacetFromESV.create(esvTerm, languageLocation);
                if(syntaxFacet != null) {
                    Iterables.addAll(errors, syntaxFacet.available());
                }
            } catch(FileSystemException e) {
                exceptions.add(e);
            }

            StrategoRuntimeFacet strategoRuntimeFacet = null;
            try {
                strategoRuntimeFacet = StrategoRuntimeFacetFromESV.create(esvTerm, languageLocation);
                if(strategoRuntimeFacet != null) {
                    Iterables.addAll(errors, strategoRuntimeFacet.available());
                }
            } catch(FileSystemException e) {
                exceptions.add(e);
            }

            final ILanguageDiscoveryRequest request;
            if(errors.isEmpty() && exceptions.isEmpty()) {
                request =
                    new LanguageDiscoveryRequest(languageLocation, esvTerm, settings, syntaxFacet, strategoRuntimeFacet);
            } else {
                request = new LanguageDiscoveryRequest(languageLocation, errors, exceptions);
            }
            requests.add(request);
        }

        return requests;
    }

    @Override public ILanguageComponent discover(ILanguageDiscoveryRequest request) throws MetaborgException {
        return createComponent((LanguageDiscoveryRequest) request);
    }

    @Override public Iterable<ILanguageComponent> discover(Iterable<ILanguageDiscoveryRequest> requests)
        throws MetaborgException {
        final Collection<ILanguageComponent> components = Lists.newLinkedList();
        for(ILanguageDiscoveryRequest request : requests) {
            components.add(createComponent((LanguageDiscoveryRequest) request));
        }
        return components;
    }

    @Override public Iterable<ILanguageComponent> discover(FileObject location) throws MetaborgException {
        return discover(request(location));
    }


    private IStrategoAppl esvTerm(FileObject location, FileObject esvFile) throws ParseError, IOException,
        MetaborgException {
        final TermReader reader =
            new TermReader(termFactoryService.getGeneric().getFactoryWithStorageType(IStrategoTerm.MUTABLE));
        final IStrategoTerm term = reader.parseFromStream(esvFile.getContent().getInputStream());
        if(term.getTermType() != IStrategoTerm.APPL) {
            final String message =
                logger.format("Cannot discover language at {}, ESV file at {} does not contain a valid ESV term",
                    location, esvFile);
            throw new MetaborgException(message);
        }
        final IStrategoAppl esvTerm = (IStrategoAppl) term;
        return esvTerm;
    }

    private ILanguageComponent createComponent(LanguageDiscoveryRequest discoveryRequest) throws MetaborgException {
        final FileObject location = discoveryRequest.location;
        if(!discoveryRequest.available) {
            final String message = discoveryRequest.errorSummary();
            throw new MetaborgException(message);
        }

        final IStrategoAppl esvTerm = discoveryRequest.esvTerm;
        final IProjectSettings settings = discoveryRequest.settings;
        final SyntaxFacet syntaxFacet = discoveryRequest.syntaxFacet;
        final StrategoRuntimeFacet strategoRuntimeFacet = discoveryRequest.strategoRuntimeFacet;

        logger.debug("Creating language component for {}", location);

        final LanguageIdentifier identifier = settings.identifier();
        final Iterable<LanguageContributionIdentifier> languageContributions =
            Iterables2.from(new LanguageContributionIdentifier(identifier, languageName(esvTerm)));
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

        if(syntaxFacet != null) {
            request.addFacet(syntaxFacet);
        }

        if(ParseFacetFromESV.hasParser(esvTerm)) {
            request.addFacet(ParseFacetFromESV.create(esvTerm));
        } else {
            request.addFacet(new ParseFacet("jsglr"));
        }

        if(strategoRuntimeFacet != null) {
            request.addFacet(strategoRuntimeFacet);
        }

        final boolean hasContext = ContextFacetFromESV.hasContext(esvTerm);
        final boolean hasAnalysis = AnalysisFacetFromESV.hasAnalysis(esvTerm);

        final IContextFactory contextFactory;
        final IAnalyzer<IStrategoTerm, IStrategoTerm> analyzer;
        final AnalysisFacet analysisFacet;
        if(!hasContext && !hasAnalysis) {
            contextFactory = contextFactory(LegacyContextFactory.name);
            analyzer = null;
            analysisFacet = null;
        } else if(hasContext && !hasAnalysis) {
            final String type = ContextFacetFromESV.type(esvTerm);
            contextFactory = contextFactory(type);
            analyzer = null;
            analysisFacet = null;
        } else if(!hasContext && hasAnalysis) {
            final String type = AnalysisFacetFromESV.type(esvTerm);
            switch(type) {
                default:
                case StrategoAnalyzer.name:
                    contextFactory = contextFactory(LegacyContextFactory.name);
                    break;
                case TaskEngineAnalyzer.name:
                    contextFactory = contextFactory(IndexTaskContextFactory.name);
                    break;
            }
            // Analyzer type cannot be null because hasAnalysis is true, no null check is needed.
            analyzer = analyzers.get(type);
            analysisFacet = AnalysisFacetFromESV.create(esvTerm);
        } else { // Both context and analysis are specified.
            final String contextType = ContextFacetFromESV.type(esvTerm);
            contextFactory = contextFactory(contextType);
            final String analysisType = AnalysisFacetFromESV.type(esvTerm);
            // Analyzer type cannot be null because hasAnalysis is true, no null check is needed.
            analyzer = analyzers.get(analysisType);
            analysisFacet = AnalysisFacetFromESV.create(esvTerm);
        }

        if(contextFactory != null) {
            final IContextStrategy contextStrategy = contextStrategy(ProjectContextStrategy.name);
            request.addFacet(new ContextFacet(contextFactory, contextStrategy));
        }
        if(analyzer != null) {
            request.addFacet(new AnalyzerFacet<>(analyzer));
        }
        if(analysisFacet != null) {
            request.addFacet(analysisFacet);
        }


        final ActionFacet menusFacet = ActionFacetFromESV.create(esvTerm);
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

        final ResolverFacet resolverFacet = ResolverFacetFromESV.createResolver(esvTerm);
        if(resolverFacet != null) {
            request.addFacet(resolverFacet);
        }

        final HoverFacet hoverFacet = ResolverFacetFromESV.createHover(esvTerm);
        if(hoverFacet != null) {
            request.addFacet(hoverFacet);
        }

        final OutlineFacet outlineFacet = OutlineFacetFromESV.create(esvTerm);
        if(outlineFacet != null) {
            request.addFacet(outlineFacet);
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

    private static String[] extensions(IStrategoAppl document) {
        final String extensionsStr = ESVReader.getProperty(document, "Extensions");
        if(extensionsStr == null) {
            return new String[0];
        }
        return extensionsStr.split(",");
    }

    @SuppressWarnings("unused") private static boolean isBaseline(LanguageIdentifier identifier) {
        final LanguageVersion version = identifier.version;
        final String qualifier = version.qualifier();
        // BOOTSTRAPPING: check for older baseline, update to new baseline and use when necessary.
        return qualifier.contains("baseline-20150905-200051");
    }


    private @Nullable IContextFactory contextFactory(@Nullable String name) throws MetaborgException {
        if(name == null) {
            return null;
        }
        final IContextFactory contextFactory = contextFactories.get(name);
        if(contextFactory == null) {
            final String message = logger.format("Could not get context factory with name {}", name);
            throw new MetaborgException(message);
        }
        return contextFactory;
    }

    private IContextStrategy contextStrategy(String name) throws MetaborgException {
        final IContextStrategy contextStrategy = contextStrategies.get(name);
        if(contextStrategy == null) {
            final String message = logger.format("Could not get context strategy with name {}", name);
            throw new MetaborgException(message);
        }
        return contextStrategy;
    }
}
