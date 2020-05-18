package org.metaborg.spoofax.core.syntax;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.config.JSGLR2Logging;
import org.metaborg.core.config.JSGLRVersion;
import org.metaborg.core.config.Sdf2tableVersion;
import org.metaborg.core.language.ILanguageCache;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.metaborg.spoofax.core.unit.ParseContrib;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr2.JSGLR2;
import org.strategoxt.lang.Context;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

public class JSGLRParseService implements ISpoofaxParser, ILanguageCache, AutoCloseable {
    public static final String name = "jsglr";

    private static final ILogger logger = LoggerUtils.logger(JSGLRParseService.class);

    private final ISpoofaxUnitService unitService;
    private final ITermFactoryService termFactoryService;
    private final IStrategoRuntimeService strategoRuntimeService;
    private final JSGLRParserConfiguration defaultParserConfig;

    private final Map<ILanguageImpl, IParserConfig> parserConfigs = Maps.newHashMap();
    private final Map<ILanguageImpl, IParserConfig> completionParserConfigs = Maps.newHashMap();

    private final Map<ILanguageImpl, ParseTable> referenceParseTables = Maps.newHashMap();
    private final Map<ILanguageImpl, ParseTable> referenceCompletionParseTables = Maps.newHashMap();

    private final Map<ILanguageImpl, JSGLRI<?>> parsers = Maps.newHashMap();
    private final Map<ILanguageImpl, JSGLRI<?>> completionParsers = Maps.newHashMap();

    @Inject public JSGLRParseService(ISpoofaxUnitService unitService, ITermFactoryService termFactoryService,
        IStrategoRuntimeService strategoRuntimeService, JSGLRParserConfiguration defaultParserConfig) {
        this.unitService = unitService;
        this.termFactoryService = termFactoryService;
        this.strategoRuntimeService = strategoRuntimeService;
        this.defaultParserConfig = defaultParserConfig;
    }

    @Override public ISpoofaxParseUnit parse(ISpoofaxInputUnit input, IProgress progress, ICancel cancel)
        throws ParseException {
        return parse(input, progress, cancel, null, null);
    }

    @Override public ISpoofaxParseUnit parse(ISpoofaxInputUnit input, IProgress progress, ICancel cancel,
        @Nullable JSGLRVersion overrideJSGLRVersion, @Nullable ImploderImplementation overrideImploder) throws ParseException {
        final FileObject source = input.source();
        final String text = input.text();

        final JSGLRParserConfiguration parserConfig;
        if(input.config() == null) {
            parserConfig = defaultParserConfig;
        } else {
            parserConfig = input.config();
        }

        try {
            logger.trace("Parsing {}", source);

            final JSGLRI<?> parser = getParser(input, parserConfig, overrideJSGLRVersion, overrideImploder);

            final ParseContrib contrib = parser.parse(parserConfig, source, text);

            return unitService.parseUnit(input, contrib);
        } catch(IOException e) {
            throw new ParseException(input, e);
        }
    }

    @Override public Collection<ISpoofaxParseUnit> parseAll(Iterable<ISpoofaxInputUnit> inputs, IProgress progress,
        ICancel cancel) throws ParseException {
        final Collection<ISpoofaxParseUnit> parseUnits = Lists.newArrayList();
        for(ISpoofaxInputUnit input : inputs) {
            parseUnits.add(parse(input, progress, cancel));
        }
        return parseUnits;
    }

    @Override public void invalidateCache(ILanguageImpl impl) {
        if(hasIncrementalPTGen(impl)) {
            logger.debug("Storing reference parse table for {}", impl);
            updateReferenceParseTables(impl, false, parserConfigs, referenceParseTables);
            updateReferenceParseTables(impl, true, completionParserConfigs, referenceCompletionParseTables);
        }

        logger.debug("Removing cached parse table for {}", impl);
        parserConfigs.remove(impl);
        completionParserConfigs.remove(impl);
        parsers.remove(impl);
        completionParsers.remove(impl);
    }

    @Override public void invalidateCache(ILanguageComponent component) {

    }

    @Override public void close() {
        parserConfigs.clear();
        completionParserConfigs.clear();
        referenceParseTables.clear();
        referenceCompletionParseTables.clear();
        parsers.clear();
        completionParsers.clear();
    }


    private JSGLRI<?> getParser(ISpoofaxInputUnit input, JSGLRParserConfiguration parserConfig,
        @Nullable JSGLRVersion overrideJSGLRVersion, @Nullable ImploderImplementation overrideImploder) throws IOException, ParseException {

        final ILanguageImpl langImpl;
        final ILanguageImpl base;
        if(input.dialect() != null) {
            langImpl = input.dialect();
            base = input.langImpl();
        } else {
            langImpl = input.langImpl();
            base = null;
        }

        final Map<ILanguageImpl, JSGLRI<?>> parserMap = parserConfig.completion ? completionParsers : parsers;

        if(!parserMap.containsKey(langImpl) || overrideImploder != null || overrideJSGLRVersion != null) {
            final IParserConfig config = getParserConfig(langImpl, input, parserConfig.completion, overrideJSGLRVersion, overrideImploder);
            final ITermFactory termFactory = termFactoryService.getGeneric();
            final JSGLRVersion version = jsglrVersion(input, overrideJSGLRVersion);

            final JSGLRI<?> parser;
            if(version == JSGLRVersion.v1) {
                final Context context = strategoRuntimeService.genericRuntime().getCompiledContext();
                if(base != null) {
                    parser = new JSGLR1I(config, termFactory, context, base, langImpl);
                } else {
                    parser = new JSGLR1I(config, termFactory, context, langImpl, null);
                }
            } else {
                final JSGLR2Logging jsglr2Logging = jsglr2Logging(input);

                parser = new JSGLR2I(config, termFactory, langImpl, null, version, jsglr2Logging);
            }

            // Don't cache an overridden configuration
            if(overrideImploder != null || overrideJSGLRVersion != null) {
                return parser;
            }
            parserMap.put(langImpl, parser);
        }
        return parserMap.get(langImpl);
    }

    private IParserConfig getParserConfig(ILanguageImpl lang, ISpoofaxInputUnit input, boolean completion,
        @Nullable JSGLRVersion overrideJSGLRVersion, @Nullable ImploderImplementation overrideImploder) throws ParseException {
        final Map<ILanguageImpl, IParserConfig> parserConfigMap;
        if(completion) {
            parserConfigMap = this.completionParserConfigs;
        } else {
            parserConfigMap = this.parserConfigs;
        }

        IParserConfig parserConfig = null;
        if(!parserConfigMap.containsKey(lang) || overrideJSGLRVersion != null) {
            final ITermFactory termFactory = termFactoryService.getGeneric();
            final SyntaxFacet facet = lang.facet(SyntaxFacet.class);

            final String errorNotFound;
            final String errorMultiple;
            final FileObject parseTableFile;
            if(completion) {
                errorNotFound = "Completion parse table not found or sdf is not enabled for this language.";
                errorMultiple = "Different components are specifying multiple completion parse tables.";
                parseTableFile = facet.completionParseTable;
            } else {
                errorNotFound = "Parse table not found or sdf is not enabled for this language.";
                errorMultiple = "Different components are specifying multiple parse tables.";
                parseTableFile = facet.parseTable;
            }

            FileObject parseTable = null;
            if(parseTableFile == null) {
                try {
                    boolean multipleTables = false;
                    for(ILanguageComponent component : lang.components()) {
                        if(component.config().sdfEnabled()) {
                            String parseTableLocation;
                            if(completion) {
                                parseTableLocation = component.config().completionsParseTable();
                            } else {
                                parseTableLocation = component.config().parseTable();
                            }
                            if(parseTableLocation != null) {
                                if(multipleTables) {
                                    logger.error(errorMultiple);
                                    throw new ParseException(input);
                                }

                                parseTable = component.location().resolveFile(parseTableLocation);
                                multipleTables = true;
                            }
                        }
                    }
                } catch(FileSystemException e) {
                    logger.error(errorNotFound);
                    throw new ParseException(input, e);
                }
            } else {
                parseTable = parseTableFile;
            }

            try {
                if(parseTable == null || !parseTable.exists()) {
                    logger.error(errorNotFound);
                    throw new ParseException(input);
                }
            } catch(FileSystemException e) {
                logger.error(errorNotFound);
                throw new ParseException(input, e);
            }

            final IParseTableProvider provider;
            final JSGLRVersion version = jsglrVersion(input, overrideJSGLRVersion);

            if(version == JSGLRVersion.v1) {
                final ParseTable referenceParseTable = referenceParseTables.get(lang);

                if(referenceParseTable != null && hasIncrementalPTGen(lang)) {
                    provider = new JSGLR1IncrementalParseTableProvider(parseTable, termFactory, referenceParseTable);
                } else {
                    provider = new JSGLR1FileParseTableProvider(parseTable, termFactory);
                }
            } else {
                provider = new JSGLR2FileParseTableProvider(parseTable, termFactory);
            }

            parserConfig = new ParserConfig(facet.startSymbols != null ? Iterables.get(facet.startSymbols, 0) : null, provider, facet.imploder);
            if(overrideJSGLRVersion == null) {
                parserConfigMap.put(lang, parserConfig);
            }
        }
        if(parserConfig == null) {
            parserConfig = parserConfigMap.get(lang);
        }
        if(overrideImploder != null) {
            parserConfig = new ParserConfig(parserConfig.getStartSymbol(), parserConfig.getParseTableProvider(), overrideImploder);
        }
        return parserConfig;
    }


    private JSGLRVersion jsglrVersion(ISpoofaxInputUnit input, @Nullable JSGLRVersion overrideJSGLRVersion) {
        if(overrideJSGLRVersion != null) {
            return overrideJSGLRVersion;
        }
        ILanguageComponent langComp = Iterables.getFirst(input.langImpl().components(), null);
        if(langComp == null)
            return JSGLRVersion.v1;
        else
            return langComp.config().jsglrVersion();
    }

    private JSGLR2Logging jsglr2Logging(ISpoofaxInputUnit input) {
        ILanguageComponent langComp = Iterables.getFirst(input.langImpl().components(), null);
        if(langComp == null)
            return JSGLR2Logging.none;
        else
            return langComp.config().jsglr2Logging();
    }

    private boolean hasIncrementalPTGen(ILanguageImpl impl) {
        for(ILanguageComponent component : impl.components()) {
            if(component.config().sdfEnabled()
                && component.config().sdf2tableVersion() == Sdf2tableVersion.incremental) {
                return true;
            }
        }
        return false;
    }

    private void updateReferenceParseTables(ILanguageImpl impl, boolean completion,
        Map<ILanguageImpl, IParserConfig> parserConfigs, Map<ILanguageImpl, ParseTable> referenceParseTables) {
        if(parserConfigs.get(impl) != null) {
            try {
                org.spoofax.jsglr.client.ParseTable pt;
                pt = (org.spoofax.jsglr.client.ParseTable) parserConfigs.get(impl).getParseTableProvider().parseTable();
                if(pt != null && pt.getPTgenerator() != null && pt.getPTgenerator().getParseTable() != null) {
                    referenceParseTables.put(impl, (ParseTable) pt.getPTgenerator().getParseTable());
                }
            } catch(IOException e) {
                String c = completion ? "completion " : "";
                logger.error("Could not save reference " + c + "parse table for incremental parse table generation.");
            }
        }
    }
}
