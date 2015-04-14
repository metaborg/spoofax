package org.metaborg.spoofax.core.language;

import static org.metaborg.spoofax.core.esv.ESVReader.attachedFiles;
import static org.metaborg.spoofax.core.esv.ESVReader.completionStrategy;
import static org.metaborg.spoofax.core.esv.ESVReader.extensions;
import static org.metaborg.spoofax.core.esv.ESVReader.hoverStrategy;
import static org.metaborg.spoofax.core.esv.ESVReader.languageName;
import static org.metaborg.spoofax.core.esv.ESVReader.observerFunction;
import static org.metaborg.spoofax.core.esv.ESVReader.onSaveFunction;
import static org.metaborg.spoofax.core.esv.ESVReader.parseTableName;
import static org.metaborg.spoofax.core.esv.ESVReader.resolverStrategy;
import static org.metaborg.spoofax.core.esv.ESVReader.startSymbol;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.analysis.stratego.StrategoFacet;
import org.metaborg.spoofax.core.context.ContextFacet;
import org.metaborg.spoofax.core.context.IContextStrategy;
import org.metaborg.spoofax.core.style.StylerFacet;
import org.metaborg.spoofax.core.style.StylerFacetFromESV;
import org.metaborg.spoofax.core.syntax.SyntaxFacet;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.transform.stratego.MenusFacet;
import org.metaborg.spoofax.core.transform.stratego.MenusFacetFromESV;
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
import com.google.inject.name.Named;

public class LanguageDiscoveryService implements ILanguageDiscoveryService {
    private static final Logger logger = LoggerFactory.getLogger(LanguageDiscoveryService.class);

    private final ILanguageService languageService;
    private final ITermFactoryService termFactoryService;
    private final Map<String, IContextStrategy> contextStrategies;
    @Inject(optional = true) @Named("LanguageDiscoveryAnalysisOverride") private String analysisStrategyOverride;


    @Inject public LanguageDiscoveryService(ILanguageService languageService, ITermFactoryService termFactoryService,
        Map<String, IContextStrategy> contextStrategies) {
        this.languageService = languageService;
        this.termFactoryService = termFactoryService;
        this.contextStrategies = contextStrategies;
    }


    @Override public Iterable<ILanguage> discover(FileObject location) throws Exception {
        final FileObject[] esvFiles = location.findFiles(new ContainsFileSelector("packed.esv"));
        final Set<FileObject> parents = Sets.newHashSet();
        final Collection<ILanguage> languages = Lists.newLinkedList();
        for(FileObject esvFile : esvFiles) {
            final FileObject languageLocation = esvFile.getParent().getParent();
            if(parents.contains(languageLocation)) {
                logger.error("Found multiple packed ESV files in language directory: " + languageLocation
                    + ", skipping.");
                continue;
            }
            parents.add(languageLocation);
            // GTODO: get language version from ESV?
            languages.add(languageFromESV(languageLocation, esvFile, new LanguageVersion(1, 0, 0, 0)));
        }
        return languages;
    }

    private ILanguage languageFromESV(FileObject location, FileObject esvFile, LanguageVersion version)
        throws Exception {
        logger.debug("Discovering language at {}", location);

        final TermReader reader =
            new TermReader(termFactoryService.getGeneric().getFactoryWithStorageType(IStrategoTerm.MUTABLE));
        final IStrategoTerm term = reader.parseFromStream(esvFile.getContent().getInputStream());
        if(term.getTermType() != IStrategoTerm.APPL) {
            throw new IllegalStateException("Packed ESV file does not contain a valid ESV term.");
        }
        final IStrategoAppl esvTerm = (IStrategoAppl) term;

        final String name = languageName(esvTerm);
        final Iterable<String> extensions = Iterables2.from(extensions(esvTerm));
        final ILanguage language = languageService.create(name, version, location);

        final IdentificationFacet identificationFacet =
            new IdentificationFacet(new ResourceExtensionsIdentifier(extensions));
        language.addFacet(identificationFacet);

        final ResourceExtensionFacet resourceExtensionsFacet = new ResourceExtensionFacet(extensions);
        language.addFacet(resourceExtensionsFacet);

        // TODO: get facet strategy from language specification. Currently there is no specification yet so always
        // choose 'project' as the context strategy.
        final IContextStrategy contextStrategy = contextStrategies.get("project");
        final ContextFacet contextFacet = new ContextFacet(contextStrategy);
        language.addFacet(contextFacet);

        final FileObject parseTable = location.resolveFile(parseTableName(esvTerm));
        final String startSymbol = startSymbol(esvTerm); // GTODO: what about multiple start symbols?
        final SyntaxFacet syntaxFacet = new SyntaxFacet(parseTable, Sets.newHashSet(startSymbol));
        language.addFacet(syntaxFacet);

        final Set<FileObject> strategoFiles = attachedFiles(esvTerm, location);
        final Set<FileObject> ctreeFiles = Sets.newLinkedHashSet();
        final Set<FileObject> jarFiles = Sets.newLinkedHashSet();
        for(FileObject strategoFile : strategoFiles) {
            final String extension = strategoFile.getName().getExtension();
            if(extension.equals("jar")) {
                jarFiles.add(strategoFile);
            } else if(extension.equals("ctree")) {
                ctreeFiles.add(strategoFile);
            } else {
                logger.warn("Stratego provider file " + strategoFile + " has unknown extension " + extension
                    + ", ignoring.");
            }
        }
        final String analysisStrategy =
            analysisStrategyOverride == null ? observerFunction(esvTerm) : analysisStrategyOverride;
        final String onSaveStrategy = onSaveFunction(esvTerm);
        final String resolverStrategy = resolverStrategy(esvTerm);
        final String hoverStrategy = hoverStrategy(esvTerm);
        final String completionStrategy = completionStrategy(esvTerm);
        final StrategoFacet strategoFacet =
            new StrategoFacet(ctreeFiles, jarFiles, analysisStrategy, onSaveStrategy, resolverStrategy, hoverStrategy,
                completionStrategy);
        language.addFacet(strategoFacet);

        final MenusFacet menusFacet = MenusFacetFromESV.create(esvTerm, language);
        language.addFacet(menusFacet);

        final StylerFacet stylerFacet = StylerFacetFromESV.create(esvTerm);
        language.addFacet(stylerFacet);

        languageService.add(language);

        return language;
    }
}
