package org.metaborg.spoofax.core.language;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.analysis.AnalyzerFacet;
import org.metaborg.core.config.ConfigRequest;
import org.metaborg.core.config.ILanguageComponentConfig;
import org.metaborg.core.config.ILanguageComponentConfigService;
import org.metaborg.core.context.ContextFacet;
import org.metaborg.core.context.IContextFactory;
import org.metaborg.core.context.IContextStrategy;
import org.metaborg.core.context.ProjectContextStrategy;
import org.metaborg.core.language.ComponentCreationConfig;
import org.metaborg.core.language.IComponentCreationConfigRequest;
import org.metaborg.core.language.ILanguageComponentFactory;
import org.metaborg.core.language.IdentificationFacet;
import org.metaborg.core.language.LanguageContributionIdentifier;
import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.language.ResourceExtensionFacet;
import org.metaborg.core.language.ResourceExtensionsIdentifier;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.resource.ResourceUtils;
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

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class LanguageComponentFactory implements ILanguageComponentFactory {
    private static final ILogger logger = LoggerUtils.logger(LanguageComponentFactory.class);

    private final IResourceService resourceService;
    private final ILanguageComponentConfigService componentConfigService;
    private final ITermFactoryService termFactoryService;
    private final Map<String, IContextFactory> contextFactories;
    private final Map<String, IContextStrategy> contextStrategies;
    private final Map<String, ISpoofaxAnalyzer> analyzers;


    @Inject public LanguageComponentFactory(IResourceService resourceService,
        ILanguageComponentConfigService componentConfigService, ITermFactoryService termFactoryService,
        Map<String, IContextFactory> contextFactories, Map<String, IContextStrategy> contextStrategies,
        Map<String, ISpoofaxAnalyzer> analyzers) {
        this.resourceService = resourceService;
        this.componentConfigService = componentConfigService;
        this.termFactoryService = termFactoryService;
        this.contextFactories = contextFactories;
        this.contextStrategies = contextStrategies;
        this.analyzers = analyzers;
    }


    @Override public IComponentCreationConfigRequest requestFromDirectory(FileObject directory) throws MetaborgException {
        try {
            if(!directory.exists()) {
                throw new MetaborgException(
                    logger.format("Cannot request component creation from directory {}, it does not exist", directory));
            }
            if(!directory.isFolder()) {
                throw new MetaborgException(
                    logger.format("Cannot request component creation from {}, it is not a directory", directory));
            }

            if(!componentConfigService.available(directory)) {
                throw new MetaborgException(logger.format(
                    "Cannot request component creation from directory {}, there is no component config file inside the directory",
                    directory));
            }

            return request(directory);
        } catch(IOException e) {
            throw new MetaborgException(
                logger.format("Cannot request component creation from directory {}, unexpected I/O error", directory),
                e);
        }
    }

    @Override public IComponentCreationConfigRequest requestFromArchive(FileObject archiveFile) throws MetaborgException {
        try {
            if(!archiveFile.exists()) {
                throw new MetaborgException(logger
                    .format("Cannot request component creation from archive file {}, it does not exist", archiveFile));
            }
            if(!archiveFile.isFile()) {
                throw new MetaborgException(logger
                    .format("Cannot request component creation from archive file {}, it is not a file", archiveFile));
            }

            final String archiveFileUri = archiveFile.getName().getURI();
            final FileObject archiveContents = resourceService.resolve("zip:" + archiveFileUri + "!/");
            if(!archiveContents.exists() || !archiveContents.isFolder()) {
                throw new MetaborgException(logger.format(
                    "Cannot request component creation from archive file {}, it is not a zip archive", archiveFile));
            }

            if(!componentConfigService.available(archiveContents)) {
                throw new MetaborgException(logger.format(
                    "Cannot request component creation from archive file {}, there is no component config file inside the archive",
                    archiveFile));
            }

            return request(archiveContents);
        } catch(IOException e) {
            throw new MetaborgException(logger.format(
                "Cannot request component creation from archive file {}, unexpected I/O error", archiveFile), e);
        }
    }

    @Override public Collection<IComponentCreationConfigRequest> requestAllInDirectory(FileObject directory)
        throws MetaborgException {
        final Set<IComponentCreationConfigRequest> requests = Sets.newHashSet();
        try {
            if(!directory.exists()) {
                throw new MetaborgException("Cannot scan directory " + directory + ", it does not exist");
            }
            if(!directory.isFolder()) {
                throw new MetaborgException("Cannot scan " + directory + ", it is not a directory");
            }

            final Iterable<FileObject> files = ResourceUtils.find(directory, new LanguageFileScanSelector());
            for(FileObject file : files) {
                final IComponentCreationConfigRequest request;
                if(file.isFolder()) {
                    request = requestFromDirectory(file);
                } else {
                    request = requestFromArchive(file);
                }
                requests.add(request);
            }
        } catch(FileSystemException e) {
            throw new MetaborgException("Cannot scan " + directory + ", unexpected I/O error", e);
        }
        return requests;
    }


    private IComponentCreationConfigRequest request(FileObject root) throws MetaborgException {
        final Collection<String> errors = Lists.newLinkedList();
        final Collection<Throwable> exceptions = Lists.newLinkedList();

        final ConfigRequest<ILanguageComponentConfig> configRequest = componentConfigService.get(root);
        if(!configRequest.valid()) {
            for(IMessage message : configRequest.errors()) {
                errors.add(message.message());
                final Throwable exception = message.exception();
                if(exception != null) {
                    exceptions.add(exception);
                }
            }
        }

        final ILanguageComponentConfig config = configRequest.config();
        if(config == null) {
            final String message = logger.format("Cannot retrieve language component configuration at {}", root);
            errors.add(message);
            return new ComponentFactoryRequest(root, errors, exceptions);
        }

        final IStrategoAppl esvTerm;
        try {
            final FileObject esvFile = root.resolveFile("target/metaborg/editor.esv.af");
            if(!esvFile.exists()) {
                esvTerm = null;
            } else {
                esvTerm = esvTerm(root, esvFile);
            }
        } catch(ParseError | IOException | MetaborgException e) {
            exceptions.add(e);
            return new ComponentFactoryRequest(root, errors, exceptions);
        }

        SyntaxFacet syntaxFacet = null;
        StrategoRuntimeFacet strategoRuntimeFacet = null;
        if(esvTerm != null) {
            try {
                syntaxFacet = SyntaxFacetFromESV.create(esvTerm, root);
                if(syntaxFacet != null) {
                    Iterables.addAll(errors, syntaxFacet.available());
                }
            } catch(FileSystemException e) {
                exceptions.add(e);
            }

            try {
                strategoRuntimeFacet = StrategoRuntimeFacetFromESV.create(esvTerm, root);
                if(strategoRuntimeFacet != null) {
                    Iterables.addAll(errors, strategoRuntimeFacet.available(resourceService));
                }
            } catch(IOException e) {
                exceptions.add(e);
            }
        }

        final ComponentFactoryRequest request;
        if(errors.isEmpty() && exceptions.isEmpty()) {
            request = new ComponentFactoryRequest(root, config, esvTerm, syntaxFacet, strategoRuntimeFacet);
        } else {
            request = new ComponentFactoryRequest(root, errors, exceptions);
        }
        return request;
    }

    private IStrategoAppl esvTerm(FileObject location, FileObject esvFile)
        throws ParseError, IOException, MetaborgException {
        final TermReader reader =
            new TermReader(termFactoryService.getGeneric().getFactoryWithStorageType(IStrategoTerm.MUTABLE));
        final IStrategoTerm term = reader.parseFromStream(esvFile.getContent().getInputStream());
        if(term.getTermType() != IStrategoTerm.APPL) {
            final String message = logger.format(
                "Cannot discover language at {}, ESV file at {} does not contain a valid ESV term", location, esvFile);
            throw new MetaborgException(message);
        }
        return (IStrategoAppl) term;
    }


    @Override public ComponentCreationConfig createConfig(IComponentCreationConfigRequest configRequest) throws MetaborgException {
        final ComponentFactoryRequest request = (ComponentFactoryRequest) configRequest;

        final FileObject location = request.location();
        if(!request.valid()) {
            throw new MetaborgException(request.toString());
        }

        final ILanguageComponentConfig componentConfig = request.config();

        logger.debug("Creating language component for {}", location);

        final LanguageIdentifier identifier = componentConfig.identifier();
        final Collection<LanguageContributionIdentifier> langContribs = request.config().langContribs();
        if(langContribs.isEmpty()) {
            langContribs.add(new LanguageContributionIdentifier(identifier, componentConfig.name()));
        }
        final ComponentCreationConfig config = new ComponentCreationConfig(identifier, location, langContribs, componentConfig);


        final SyntaxFacet syntaxFacet;
        if(componentConfig.sdfEnabled()) {
            syntaxFacet = request.syntaxFacet();
            if(syntaxFacet != null) {
                config.addFacet(syntaxFacet);
            }
        } else {
            syntaxFacet = null;
        }

        final StrategoRuntimeFacet strategoRuntimeFacet = request.strategoRuntimeFacet();
        if(strategoRuntimeFacet != null) {
            config.addFacet(strategoRuntimeFacet);
        }

        final IStrategoAppl esvTerm = request.esvTerm();
        if(esvTerm != null) {
            final String[] extensions = extensions(esvTerm);
            if(extensions.length != 0) {
                final Iterable<String> extensionsIterable = Iterables2.from(extensions);

                final IdentificationFacet identificationFacet =
                    new IdentificationFacet(new ResourceExtensionsIdentifier(extensionsIterable));
                config.addFacet(identificationFacet);

                final ResourceExtensionFacet resourceExtensionsFacet = new ResourceExtensionFacet(extensionsIterable);
                config.addFacet(resourceExtensionsFacet);
            }

            if(ParseFacetFromESV.hasParser(esvTerm)) {
                config.addFacet(ParseFacetFromESV.create(esvTerm));
            } else if(syntaxFacet != null) {
                config.addFacet(new ParseFacet("jsglr"));
            }

            final boolean hasContext = ContextFacetFromESV.hasContext(esvTerm);
            final boolean hasAnalysis = AnalysisFacetFromESV.hasAnalysis(esvTerm);

            final IContextFactory contextFactory;
            final ISpoofaxAnalyzer analyzer;
            final AnalysisFacet analysisFacet;

            if(hasAnalysis) {
                final String analysisType = AnalysisFacetFromESV.type(esvTerm);
                assert analysisType != null : "Analyzer type cannot be null because hasAnalysis is true, no null check is needed.";
                analyzer = analyzers.get(analysisType);
                analysisFacet = AnalysisFacetFromESV.create(esvTerm);
                final String contextType = hasContext ? ContextFacetFromESV.type(esvTerm) : null;
                if(hasContext && contextType == null) {
                    contextFactory = null;
                } else {
                    final String analysisContextType;
                    switch(analysisType) {
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
                    if(hasContext && !analysisContextType.equals(contextType)) {
                        logger.warn("Ignoring explicit context type {}, because it is incompatible with analysis {}.",
                            contextType, analysisType);
                    }
                    contextFactory = contextFactory(analysisContextType);
                }
            } else if(hasContext) {
                final String type = ContextFacetFromESV.type(esvTerm);
                contextFactory = contextFactory(type);
                analyzer = null;
                analysisFacet = null;
            } else {
                contextFactory = contextFactory(LegacyContextFactory.name);
                analyzer = null;
                analysisFacet = null;
            }

            if(contextFactory != null) {
                final IContextStrategy contextStrategy = contextStrategy(ProjectContextStrategy.name);
                config.addFacet(new ContextFacet(contextFactory, contextStrategy));
            }
            if(analyzer != null) {
                config.addFacet(new AnalyzerFacet<>(analyzer));
            }
            if(analysisFacet != null) {
                config.addFacet(analysisFacet);
            }


            final ActionFacet menusFacet = ActionFacetFromESV.create(esvTerm);
            if(menusFacet != null) {
                config.addFacet(menusFacet);
            }

            final StylerFacet stylerFacet = StylerFacetFromESV.create(esvTerm);
            if(stylerFacet != null) {
                config.addFacet(stylerFacet);
            }

            final ResolverFacet resolverFacet = ResolverFacetFromESV.createResolver(esvTerm);
            if(resolverFacet != null) {
                config.addFacet(resolverFacet);
            }

            final HoverFacet hoverFacet = ResolverFacetFromESV.createHover(esvTerm);
            if(hoverFacet != null) {
                config.addFacet(hoverFacet);
            }

            final OutlineFacet outlineFacet = OutlineFacetFromESV.create(esvTerm);
            if(outlineFacet != null) {
                config.addFacet(outlineFacet);
            }

            final ShellFacet shellFacet = ShellFacetFromESV.create(esvTerm);
            if(shellFacet != null) {
                config.addFacet(shellFacet);
            }
        }

        return config;
    }

    @Override public Collection<ComponentCreationConfig> createConfigs(Iterable<IComponentCreationConfigRequest> requests)
        throws MetaborgException {
        final List<ComponentCreationConfig> configs = Lists.newArrayList();
        for(IComponentCreationConfigRequest request : requests) {
            final ComponentCreationConfig config = createConfig(request);
            configs.add(config);
        }
        return configs;
    }


    private static String[] extensions(IStrategoAppl document) {
        final String extensionsStr = ESVReader.getProperty(document, "Extensions");
        if(extensionsStr == null) {
            return new String[0];
        }
        return extensionsStr.split(",");
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
