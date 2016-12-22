package org.metaborg.spoofax.core.language;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSelectInfo;
import org.apache.commons.vfs2.FileSelector;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgConstants;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.AnalyzerFacet;
import org.metaborg.core.config.ConfigRequest;
import org.metaborg.core.config.ILanguageComponentConfig;
import org.metaborg.core.config.ILanguageComponentConfigService;
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
import org.metaborg.core.language.ResourceExtensionFacet;
import org.metaborg.core.language.ResourceExtensionsIdentifier;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.syntax.ParseFacet;
import org.metaborg.spoofax.core.action.ActionFacet;
import org.metaborg.spoofax.core.action.ActionFacetFromESV;
import org.metaborg.spoofax.core.analysis.AnalysisFacet;
import org.metaborg.spoofax.core.analysis.AnalysisFacetFromESV;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzer;
import org.metaborg.spoofax.core.analysis.constraint.ConstraintMultiFileAnalyzer;
import org.metaborg.spoofax.core.analysis.constraint.ConstraintSingleFileAnalyzer;
import org.metaborg.spoofax.core.analysis.legacy.StrategoAnalyzer;
import org.metaborg.spoofax.core.analysis.taskengine.TaskEngineAnalyzer;
import org.metaborg.spoofax.core.context.ContextFacetFromESV;
import org.metaborg.spoofax.core.context.IndexTaskContextFactory;
import org.metaborg.spoofax.core.context.LegacyContextFactory;
import org.metaborg.spoofax.core.context.scopegraph.MultiFileScopeGraphContextFactory;
import org.metaborg.spoofax.core.context.scopegraph.SingleFileScopeGraphContextFactory;
import org.metaborg.spoofax.core.esv.ESVReader;
import org.metaborg.spoofax.core.outline.OutlineFacet;
import org.metaborg.spoofax.core.outline.OutlineFacetFromESV;
import org.metaborg.spoofax.core.shell.ShellFacet;
import org.metaborg.spoofax.core.shell.ShellFacetFromESV;
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
import org.metaborg.util.iterators.Iterables2;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.terms.ParseError;
import org.spoofax.terms.io.binary.TermReader;

import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;

public class LanguageDiscoveryService implements ILanguageDiscoveryService {

    private static final ILogger logger = LoggerUtils.logger(LanguageDiscoveryService.class);

    private final ILanguageService languageService;
    private final ILanguageComponentConfigService componentConfigService;
    private final ITermFactoryService termFactoryService;
    private final Map<String,IContextFactory> contextFactories;
    private final Map<String,IContextStrategy> contextStrategies;
    private final Map<String,ISpoofaxAnalyzer> analyzers;


    @Inject public LanguageDiscoveryService(ILanguageService languageService,
            ILanguageComponentConfigService componentConfigService, ITermFactoryService termFactoryService,
            Map<String,IContextFactory> contextFactories, Map<String,IContextStrategy> contextStrategies,
            Map<String,ISpoofaxAnalyzer> analyzers) {
        this.languageService = languageService;
        this.componentConfigService = componentConfigService;
        this.termFactoryService = termFactoryService;
        this.contextFactories = contextFactories;
        this.contextStrategies = contextStrategies;
        this.analyzers = analyzers;
    }


    @Override public Collection<ILanguageDiscoveryRequest> request(FileObject location) throws MetaborgException {
        final Collection<ILanguageDiscoveryRequest> requests = Lists.newLinkedList();
        final FileObject[] configFiles;
        try {
            configFiles = location.findFiles(new FileSelector() {

                @Override public boolean traverseDescendents(FileSelectInfo fileInfo) throws Exception {
                    final String baseName = fileInfo.getFile().getName().getBaseName();
                    return !baseName.equals("bin") && !baseName.equals("target");
                }

                @Override public boolean includeFile(FileSelectInfo fileInfo) throws Exception {
                    return fileInfo.getFile().getName().getBaseName().equals(MetaborgConstants.FILE_COMPONENT_CONFIG);
                }
            });
        } catch (FileSystemException e) {
            throw new MetaborgException("Searching for language components failed unexpectedly", e);
        }

        if (configFiles == null || configFiles.length == 0) {
            return requests;
        }

        final Multimap<FileName,FileName> locToConfigs = ArrayListMultimap.create();
        final Map<FileName,FileObject> nameToFile = Maps.newHashMap();
        for (FileObject configFile : configFiles) {
            try {
                final FileName configName = configFile.getName();
                final FileObject languageLoc = configFile.getParent().getParent();
                final FileName languageLocName = languageLoc.getName();
                locToConfigs.put(languageLocName, configName);
                nameToFile.put(languageLocName, languageLoc);
            } catch (FileSystemException e) {
                logger.error("Could not resolve parent directory of config file {}, skipping", e, configFile);
                continue;
            }
        }

        final Collection<FileObject> languageLocations = Lists.newArrayList();
        for (Entry<FileName,Collection<FileName>> configEntry : locToConfigs.asMap().entrySet()) {
            final FileName languageLocName = configEntry.getKey();
            final FileObject languageLoc = nameToFile.get(languageLocName);
            final Collection<FileName> configLocNames = configEntry.getValue();
            if (configLocNames.size() > 1) {
                final String message = logger.format("Found multiple config files at location {}: {}", languageLocName,
                        Joiner.on(", ").join(configLocNames));
                requests.add(new LanguageDiscoveryRequest(languageLoc, message));
                continue;
            } else {
                languageLocations.add(languageLoc);
            }
        }

        for (FileObject languageLocation : languageLocations) {
            final Collection<String> errors = Lists.newLinkedList();
            final Collection<Throwable> exceptions = Lists.newLinkedList();

            final ConfigRequest<ILanguageComponentConfig> configRequest = componentConfigService.get(languageLocation);
            if (!configRequest.valid()) {
                for (IMessage message : configRequest.errors()) {
                    errors.add(message.message());
                    final Throwable exception = message.exception();
                    if (exception != null) {
                        exceptions.add(exception);
                    }
                }
            }
            final ILanguageComponentConfig config = configRequest.config();
            if (config == null) {
                final String message = logger.format("Cannot retrieve language component configuration at {}",
                        languageLocation);
                errors.add(message);
                requests.add(new LanguageDiscoveryRequest(languageLocation, errors, exceptions));
                continue;
            }

            final IStrategoAppl esvTerm;
            try {
                final FileObject esvFile = languageLocation.resolveFile("target/metaborg/editor.esv.af");
                if (!esvFile.exists()) {
                    esvTerm = null;
                } else {
                    esvTerm = esvTerm(languageLocation, esvFile);
                }
            } catch (ParseError | IOException | MetaborgException e) {
                exceptions.add(e);
                requests.add(new LanguageDiscoveryRequest(languageLocation, errors, exceptions));
                continue;
            }

            SyntaxFacet syntaxFacet = null;
            StrategoRuntimeFacet strategoRuntimeFacet = null;
            if (esvTerm != null) {
                try {
                    syntaxFacet = SyntaxFacetFromESV.create(esvTerm, languageLocation);
                    if (syntaxFacet != null) {
                        Iterables.addAll(errors, syntaxFacet.available());
                    }
                } catch (FileSystemException e) {
                    exceptions.add(e);
                }

                try {
                    strategoRuntimeFacet = StrategoRuntimeFacetFromESV.create(esvTerm, languageLocation);
                    if (strategoRuntimeFacet != null) {
                        Iterables.addAll(errors, strategoRuntimeFacet.available());
                    }
                } catch (FileSystemException e) {
                    exceptions.add(e);
                }
            }

            final ILanguageDiscoveryRequest request;
            if (errors.isEmpty() && exceptions.isEmpty()) {
                request = new LanguageDiscoveryRequest(languageLocation, config, esvTerm, syntaxFacet,
                        strategoRuntimeFacet);
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

    @Override public Collection<ILanguageComponent> discover(Iterable<ILanguageDiscoveryRequest> requests)
            throws MetaborgException {
        final Collection<ILanguageComponent> components = Lists.newLinkedList();
        for (ILanguageDiscoveryRequest request : requests) {
            components.add(discover(request));
        }
        return components;
    }


    private IStrategoAppl esvTerm(FileObject location, FileObject esvFile)
            throws ParseError, IOException, MetaborgException {
        final TermReader reader = new TermReader(
                termFactoryService.getGeneric().getFactoryWithStorageType(IStrategoTerm.MUTABLE));
        final IStrategoTerm term = reader.parseFromStream(esvFile.getContent().getInputStream());
        if (term.getTermType() != IStrategoTerm.APPL) {
            final String message = logger.format(
                    "Cannot discover language at {}, ESV file at {} does not contain a valid ESV term", location,
                    esvFile);
            throw new MetaborgException(message);
        }
        return (IStrategoAppl) term;
    }

    private ILanguageComponent createComponent(LanguageDiscoveryRequest discoveryRequest) throws MetaborgException {
        final FileObject location = discoveryRequest.location();
        if (!discoveryRequest.available()) {
            throw new MetaborgException(discoveryRequest.toString());
        }

        final ILanguageComponentConfig config = discoveryRequest.config();

        logger.debug("Creating language component for {}", location);

        final LanguageIdentifier identifier = config.identifier();
        final Collection<LanguageContributionIdentifier> langContribs = discoveryRequest.config().langContribs();
        if (langContribs.isEmpty()) {
            langContribs.add(new LanguageContributionIdentifier(identifier, config.name()));
        }
        final LanguageCreationRequest request = languageService.create(identifier, location, langContribs, config);


        final SyntaxFacet syntaxFacet;
        if (config.sdfEnabled()) {
            syntaxFacet = discoveryRequest.syntaxFacet();
            if (syntaxFacet != null) {
                request.addFacet(syntaxFacet);
            }
        } else {
            syntaxFacet = null;
        }

        final StrategoRuntimeFacet strategoRuntimeFacet = discoveryRequest.strategoRuntimeFacet();
        if (strategoRuntimeFacet != null) {
            request.addFacet(strategoRuntimeFacet);
        }

        final IStrategoAppl esvTerm = discoveryRequest.esvTerm();
        if (esvTerm != null) {
            final String[] extensions = extensions(esvTerm);
            if (extensions.length != 0) {
                final Iterable<String> extensionsIterable = Iterables2.from(extensions);

                final IdentificationFacet identificationFacet = new IdentificationFacet(
                        new ResourceExtensionsIdentifier(extensionsIterable));
                request.addFacet(identificationFacet);

                final ResourceExtensionFacet resourceExtensionsFacet = new ResourceExtensionFacet(extensionsIterable);
                request.addFacet(resourceExtensionsFacet);
            }

            if (ParseFacetFromESV.hasParser(esvTerm)) {
                request.addFacet(ParseFacetFromESV.create(esvTerm));
            } else if (syntaxFacet != null) {
                request.addFacet(new ParseFacet("jsglr"));
            }

            final boolean hasContext = ContextFacetFromESV.hasContext(esvTerm);
            final boolean hasAnalysis = AnalysisFacetFromESV.hasAnalysis(esvTerm);

            final IContextFactory contextFactory;
            final ISpoofaxAnalyzer analyzer;
            final AnalysisFacet analysisFacet;

            if (hasAnalysis) {
                final String analysisType = AnalysisFacetFromESV.type(esvTerm);
                assert analysisType != null : "Analyzer type cannot be null because hasAnalysis is true, no null check is needed.";
                analyzer = analyzers.get(analysisType);
                analysisFacet = AnalysisFacetFromESV.create(esvTerm);
                final String contextType = hasContext ? ContextFacetFromESV.type(esvTerm) : null;
                if (hasContext && contextType == null) {
                    contextFactory = null;
                } else {
                    final String analysisContextType;
                    switch (analysisType) {
                    default:
                    case StrategoAnalyzer.name:
                        analysisContextType = LegacyContextFactory.name;
                        break;
                    case TaskEngineAnalyzer.name:
                        analysisContextType = IndexTaskContextFactory.name;
                        break;
                    case ConstraintSingleFileAnalyzer.name:
                        analysisContextType = SingleFileScopeGraphContextFactory.name;
                        break;
                    case ConstraintMultiFileAnalyzer.name:
                        analysisContextType = MultiFileScopeGraphContextFactory.name;
                        break;
                    }
                    if(hasContext && !contextType.equals(analysisContextType)) {
                        logger.warn("Ignoring explicit context type {}, because it is incompatible with analysis {}.",
                            contextType, analysisType);
                    }
                    contextFactory = contextFactory(analysisContextType);
                }
            } else if (hasContext) {
                final String type = ContextFacetFromESV.type(esvTerm);
                contextFactory = contextFactory(type);
                analyzer = null;
                analysisFacet = null;
            } else {
                contextFactory = contextFactory(LegacyContextFactory.name);
                analyzer = null;
                analysisFacet = null;
            }

            if (contextFactory != null) {
                final IContextStrategy contextStrategy = contextStrategy(ProjectContextStrategy.name);
                request.addFacet(new ContextFacet(contextFactory, contextStrategy));
            }
            if (analyzer != null) {
                request.addFacet(new AnalyzerFacet<>(analyzer));
            }
            if (analysisFacet != null) {
                request.addFacet(analysisFacet);
            }


            final ActionFacet menusFacet = ActionFacetFromESV.create(esvTerm);
            if (menusFacet != null) {
                request.addFacet(menusFacet);
            }

            final StylerFacet stylerFacet = StylerFacetFromESV.create(esvTerm);
            if (stylerFacet != null) {
                request.addFacet(stylerFacet);
            }

            final ResolverFacet resolverFacet = ResolverFacetFromESV.createResolver(esvTerm);
            if (resolverFacet != null) {
                request.addFacet(resolverFacet);
            }

            final HoverFacet hoverFacet = ResolverFacetFromESV.createHover(esvTerm);
            if (hoverFacet != null) {
                request.addFacet(hoverFacet);
            }

            final OutlineFacet outlineFacet = OutlineFacetFromESV.create(esvTerm);
            if (outlineFacet != null) {
                request.addFacet(outlineFacet);
            }

            final ShellFacet shellFacet = ShellFacetFromESV.create(esvTerm);
            if (shellFacet != null) {
                request.addFacet(shellFacet);
            }
        }

        return languageService.add(request);
    }

    private static String[] extensions(IStrategoAppl document) {
        final String extensionsStr = ESVReader.getProperty(document, "Extensions");
        if (extensionsStr == null) {
            return new String[0];
        }
        return extensionsStr.split(",");
    }

    private @Nullable IContextFactory contextFactory(@Nullable String name) throws MetaborgException {
        if (name == null) {
            return null;
        }
        final IContextFactory contextFactory = contextFactories.get(name);
        if (contextFactory == null) {
            final String message = logger.format("Could not get context factory with name {}", name);
            throw new MetaborgException(message);
        }
        return contextFactory;
    }

    private IContextStrategy contextStrategy(String name) throws MetaborgException {
        final IContextStrategy contextStrategy = contextStrategies.get(name);
        if (contextStrategy == null) {
            final String message = logger.format("Could not get context strategy with name {}", name);
            throw new MetaborgException(message);
        }
        return contextStrategy;
    }
}
