package org.metaborg.spoofax.core.syntax;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.config.JSGLRVersion;
import org.metaborg.core.config.Sdf2tableVersion;
import org.metaborg.core.language.ILanguageCache;
import org.metaborg.core.language.ILanguageComponent;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.sdf2table.parsetable.ParseTable;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.metaborg.spoofax.core.unit.ParseContrib;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.metaborg.util.task.ICancel;
import org.metaborg.util.task.IProgress;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.InvalidParseTableException;
import org.spoofax.jsglr2.parsetable.ParseTableReadException;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;

public class JSGLRParseService implements ISpoofaxParser, ILanguageCache {
    public static final String name = "jsglr";

    private static final ILogger logger = LoggerUtils.logger(JSGLRParseService.class);

    private final ISpoofaxUnitService unitService;
    private final ITermFactoryService termFactoryService;
    private final JSGLRParserConfiguration defaultParserConfig;

    private final Map<ILanguageImpl, IParserConfig> parserConfigs = Maps.newHashMap();
    private final Map<ILanguageImpl, IParserConfig> completionParserConfigs = Maps.newHashMap();

    private final Map<ILanguageImpl, ParseTable> referenceParseTables = Maps.newHashMap();
    private final Map<ILanguageImpl, ParseTable> referenceCompletionParseTables = Maps.newHashMap();

    @Inject public JSGLRParseService(ISpoofaxUnitService unitService, ITermFactoryService termFactoryService,
        JSGLRParserConfiguration defaultParserConfig) {
        this.unitService = unitService;
        this.termFactoryService = termFactoryService;
        this.defaultParserConfig = defaultParserConfig;
    }

    private JSGLRVersion jsglrVersion(ISpoofaxInputUnit input) {
    		JSGLRVersion version = JSGLRVersion.v1;
    		
        for(ILanguageComponent langComp : input.langImpl().components()) {
        		version = langComp.config().jsglrVersion();
        			break;
        }
        
        return version;
    }

    @Override public ISpoofaxParseUnit parse(ISpoofaxInputUnit input, IProgress progress, ICancel cancel)
        throws ParseException {
        final FileObject source = input.source();
        final ILanguageImpl langImpl;
        final ILanguageImpl base;

        if(input.dialect() != null) {
            langImpl = input.dialect();
            base = input.langImpl();
        } else {
            langImpl = input.langImpl();
            base = null;
        }
        final String text = input.text();

        final ITermFactory termFactory = termFactoryService.get(langImpl, null, false);
        final IParserConfig config;

        JSGLRParserConfiguration parserConfig = input.config();
        if(parserConfig == null) {
            parserConfig = defaultParserConfig;
        }

        if(parserConfig.completion) {
            config = getCompletionParserConfig(langImpl, input);
        } else {
            config = getParserConfig(langImpl, input);
        }

        try {
            logger.trace("Parsing {}", source);

            JSGLRVersion version = jsglrVersion(input);
            
            final JSGLRI<?> parser;
            
            boolean dataDependentParsing = false;
            for(ILanguageComponent component : langImpl.components()) {
                if(component.config().dataDependent()) {
                    dataDependentParsing = true;
                }
            }

            if (version == JSGLRVersion.v2) {
                if(dataDependentParsing) {
                    parser = new JSGLR2I(config, termFactory, langImpl, null, source, text, true, false);
                } else {
                    parser = new JSGLR2I(config, termFactory, langImpl, null, source, text, false, true);
                }                
            } else {
                if(base != null) {
                    parser = new JSGLR1I(config, termFactory, base, langImpl, source, text);
                } else {
                    parser = new JSGLR1I(config, termFactory, langImpl, null, source, text);
                }
            }

            final ParseContrib contrib = parser.parse(parserConfig);
            
            if (version == JSGLRVersion.v2) {
                if (contrib.valid)
                    logger.info("Valid JSGLR2 parse");
                else
                    logger.info("Invalid JSGLR2 parse");
            }
            
            final ISpoofaxParseUnit unit = unitService.parseUnit(input, contrib);
            return unit;
        } catch(IOException | InvalidParseTableException | ParseTableReadException e) {
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

    public IParserConfig getParserConfig(ILanguageImpl lang, ISpoofaxInputUnit input) throws ParseException {
        IParserConfig config = parserConfigs.get(lang);
        if(config == null) {
            final ITermFactory termFactory =
                termFactoryService.getGeneric().getFactoryWithStorageType(IStrategoTerm.MUTABLE);
            final SyntaxFacet facet = lang.facet(SyntaxFacet.class);

            FileObject parseTable = null;
            boolean incrementalPTGen = false;
            for(ILanguageComponent component : lang.components()) {
                if(component.config().sdfEnabled()
                    && component.config().sdf2tableVersion() == Sdf2tableVersion.incremental) {
                    incrementalPTGen = true;
                }
            }

            if(facet.parseTable == null) {
                try {
                    boolean multipleTables = false;
                    for(ILanguageComponent component : lang.components()) {
                        if(component.config().sdfEnabled()) {
                            if(component.config().parseTable() != null) {
                                if(multipleTables) {
                                    logger.error("Different components are specifying multiple parse tables.");
                                    throw new ParseException(input);
                                }

                                parseTable = component.location().resolveFile(component.config().parseTable());
                                multipleTables = true;
                            }
                        }
                    }
                } catch(FileSystemException e) {
                    logger.error("Parse table not found or sdf is not enabled for this language.");
                    throw new ParseException(input, e);
                }
            } else {
                parseTable = facet.parseTable;
            }

            try {
                if(parseTable == null || !parseTable.exists()) {
                    logger.error("Parse table not found or sdf is not enabled for this language.");
                    throw new ParseException(input);
                }
            } catch(FileSystemException e) {
                logger.error("Parse table not found or sdf is not enabled for this language.");
                throw new ParseException(input, e);
            }

            final IParseTableProvider provider;
            JSGLRVersion version = jsglrVersion(input);
            
            if (version == JSGLRVersion.v2) {
                provider = new JSGLR2FileParseTableProvider(parseTable, termFactory);
            } else {
	            final ParseTable referenceParseTable = referenceParseTables.get(lang);
	
	            if(referenceParseTable != null && incrementalPTGen) {
	                provider = new JSGLR1IncrementalParseTableProvider(parseTable, termFactory, referenceParseTable);
	            } else {
	                provider = new JSGLR1FileParseTableProvider(parseTable, termFactory);
	            }
            }

            config = new ParserConfig(Iterables.get(facet.startSymbols, 0), provider);
            parserConfigs.put(lang, config);


        }
        return config;

    }

    public IParserConfig getCompletionParserConfig(ILanguageImpl lang, ISpoofaxInputUnit input) throws ParseException {
        IParserConfig config = completionParserConfigs.get(lang);
        if(config == null) {
            final ITermFactory termFactory =
                termFactoryService.getGeneric().getFactoryWithStorageType(IStrategoTerm.MUTABLE);
            final SyntaxFacet facet = lang.facet(SyntaxFacet.class);

            FileObject completionParseTable = null;
            boolean incrementalPTGen = false;

            for(ILanguageComponent component : lang.components()) {
                if(component.config().sdfEnabled()
                    && component.config().sdf2tableVersion() == Sdf2tableVersion.incremental) {
                    incrementalPTGen = true;
                }
            }

            if(facet.completionParseTable == null) {
                try {
                    boolean multipleTables = false;

                    for(ILanguageComponent component : lang.components()) {
                        if(component.config().sdfEnabled()) {
                            if(component.config().completionsParseTable() != null) {
                                if(multipleTables) {
                                    logger
                                        .error("Different components are specifying multiple completion parse tables.");
                                    throw new ParseException(input);
                                }

                                completionParseTable =
                                    component.location().resolveFile(component.config().completionsParseTable());
                                multipleTables = true;
                            }
                        }

                    }
                } catch(FileSystemException e) {
                    logger.error("Completion parse table not found or sdf is not enabled for this language.");
                    throw new ParseException(input, e);
                }
            } else {
                completionParseTable = facet.completionParseTable;
            }

            try {
                if(completionParseTable == null || !completionParseTable.exists()) {
                    logger.error("Completion parse table not found or sdf is not enabled for this language.");
                    throw new ParseException(input);
                }
            } catch(FileSystemException e) {
                logger.error("Completion parse table not found or sdf is not enabled for this language.");
                throw new ParseException(input, e);
            }

            final IParseTableProvider provider;
            final ParseTable referenceParseTable = referenceCompletionParseTables.get(lang);

            if(referenceParseTable != null && incrementalPTGen) {
                provider = new JSGLR1IncrementalParseTableProvider(completionParseTable, termFactory, referenceParseTable);
            } else {
                provider = new JSGLR1FileParseTableProvider(completionParseTable, termFactory);
            }

            config = new ParserConfig(Iterables.get(facet.startSymbols, 0), provider);
            completionParserConfigs.put(lang, config);


        }
        return config;
    }


    @Override public void invalidateCache(ILanguageImpl impl) {

        boolean incrementalPTGen = false;
        org.spoofax.jsglr.client.ParseTable pt = null;

        for(ILanguageComponent component : impl.components()) {
            if(component.config().sdfEnabled()
                && component.config().sdf2tableVersion() == Sdf2tableVersion.incremental) {
                incrementalPTGen = true;
            }
        }

        logger.debug("Storing reference parse table for {}", impl);
        if(parserConfigs.get(impl) != null && incrementalPTGen) {
            try {
                pt = (org.spoofax.jsglr.client.ParseTable) parserConfigs.get(impl).getParseTableProvider().parseTable();
                if(pt != null && pt.getPTgenerator() != null && pt.getPTgenerator().getParseTable() != null) {
                    referenceParseTables.put(impl, pt.getPTgenerator().getParseTable());
                }
            } catch(IOException e) {
                logger.error("Could not save reference parse table for incremental parse table generation.");
            }
        }

        if(completionParserConfigs.get(impl) != null && incrementalPTGen) {
            try {
                pt = (org.spoofax.jsglr.client.ParseTable) completionParserConfigs.get(impl).getParseTableProvider().parseTable();
                if(pt != null && pt.getPTgenerator() != null && pt.getPTgenerator().getParseTable() != null) {
                    referenceCompletionParseTables.put(impl, pt.getPTgenerator().getParseTable());
                }
            } catch(IOException e) {
                logger.error("Could not save reference completion parse table for incremental parse table generation.");
            }
        }

        logger.debug("Removing cached parse table for {}", impl);
        parserConfigs.remove(impl);
        completionParserConfigs.remove(impl);
    }

    @Override public void invalidateCache(ILanguageComponent component) {

    }
}
