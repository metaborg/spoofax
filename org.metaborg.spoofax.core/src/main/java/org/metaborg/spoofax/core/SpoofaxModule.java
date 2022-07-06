package org.metaborg.spoofax.core;

import org.metaborg.core.MetaborgModule;
import org.metaborg.core.action.IActionService;
import org.metaborg.core.analysis.IAnalysisService;
import org.metaborg.core.analysis.IAnalyzer;
import org.metaborg.core.build.IBuildOutputInternal;
import org.metaborg.core.build.IBuilder;
import org.metaborg.core.completion.ICompletionService;
import org.metaborg.core.config.IProjectConfigBuilder;
import org.metaborg.core.config.IProjectConfigService;
import org.metaborg.core.config.IProjectConfigWriter;
import org.metaborg.core.config.ProjectConfigService;
import org.metaborg.core.context.IContextFactory;
import org.metaborg.core.language.ILanguageComponentFactory;
import org.metaborg.core.language.ILanguageDiscoveryService;
import org.metaborg.core.language.dialect.IDialectIdentifier;
import org.metaborg.core.language.dialect.IDialectProcessor;
import org.metaborg.core.language.dialect.IDialectService;
import org.metaborg.core.menu.IMenuService;
import org.metaborg.core.outline.IOutlineService;
import org.metaborg.core.processing.IProcessor;
import org.metaborg.core.processing.IProcessorRunner;
import org.metaborg.core.processing.analyze.IAnalysisResultProcessor;
import org.metaborg.core.processing.analyze.IAnalysisResultRequester;
import org.metaborg.core.processing.analyze.IAnalysisResultUpdater;
import org.metaborg.core.processing.parse.IParseResultProcessor;
import org.metaborg.core.processing.parse.IParseResultRequester;
import org.metaborg.core.processing.parse.IParseResultUpdater;
import org.metaborg.core.style.ICategorizerService;
import org.metaborg.core.style.IStylerService;
import org.metaborg.core.syntax.IParser;
import org.metaborg.core.syntax.ISyntaxService;
import org.metaborg.core.tracing.IHoverService;
import org.metaborg.core.tracing.IResolverService;
import org.metaborg.core.tracing.ITracingService;
import org.metaborg.core.transform.ITransformService;
import org.metaborg.core.transform.ITransformer;
import org.metaborg.core.unit.IInputUnitService;
import org.metaborg.core.unit.IUnitService;
import org.metaborg.runtime.task.primitives.TaskLibrary;
import org.metaborg.spoofax.core.action.ActionService;
import org.metaborg.spoofax.core.analysis.AnalysisCommon;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalysisService;
import org.metaborg.spoofax.core.analysis.ISpoofaxAnalyzer;
import org.metaborg.spoofax.core.analysis.SpoofaxAnalysisService;
import org.metaborg.spoofax.core.analysis.constraint.MultiFileConstraintAnalyzer;
import org.metaborg.spoofax.core.analysis.constraint.SingleFileConstraintAnalyzer;
import org.metaborg.spoofax.core.analysis.legacy.StrategoAnalyzer;
import org.metaborg.spoofax.core.analysis.taskengine.TaskEngineAnalyzer;
import org.metaborg.spoofax.core.build.ISpoofaxBuilder;
import org.metaborg.spoofax.core.build.SpoofaxBuildOutput;
import org.metaborg.spoofax.core.build.SpoofaxBuilder;
import org.metaborg.spoofax.core.completion.ISpoofaxCompletionService;
import org.metaborg.spoofax.core.completion.JSGLRCompletionService;
import org.metaborg.spoofax.core.config.ISpoofaxProjectConfigBuilder;
import org.metaborg.spoofax.core.config.ISpoofaxProjectConfigService;
import org.metaborg.spoofax.core.config.ISpoofaxProjectConfigWriter;
import org.metaborg.spoofax.core.config.SpoofaxProjectConfigBuilder;
import org.metaborg.spoofax.core.config.SpoofaxProjectConfigService;
import org.metaborg.spoofax.core.context.IndexTaskContextFactory;
import org.metaborg.spoofax.core.context.LegacyContextFactory;
import org.metaborg.spoofax.core.context.constraint.ConstraintContextFactory;
import org.metaborg.spoofax.core.dialogs.ISpoofaxDialogService;
import org.metaborg.spoofax.core.dialogs.NullSpoofaxDialogService;
import org.metaborg.spoofax.core.dynamicclassloading.DynamicClassLoadingService;
import org.metaborg.spoofax.core.dynamicclassloading.IDynamicClassLoadingService;
import org.metaborg.spoofax.core.language.LanguageComponentFactory;
import org.metaborg.spoofax.core.language.LanguageDiscoveryService;
import org.metaborg.spoofax.core.language.dialect.DialectIdentifier;
import org.metaborg.spoofax.core.language.dialect.DialectProcessor;
import org.metaborg.spoofax.core.language.dialect.DialectService;
import org.metaborg.spoofax.core.menu.MenuService;
import org.metaborg.spoofax.core.outline.ISpoofaxOutlineService;
import org.metaborg.spoofax.core.outline.OutlineService;
import org.metaborg.spoofax.core.processing.ISpoofaxProcessor;
import org.metaborg.spoofax.core.processing.ISpoofaxProcessorRunner;
import org.metaborg.spoofax.core.processing.SpoofaxBlockingProcessor;
import org.metaborg.spoofax.core.processing.SpoofaxProcessorRunner;
import org.metaborg.spoofax.core.processing.analyze.ISpoofaxAnalysisResultProcessor;
import org.metaborg.spoofax.core.processing.analyze.ISpoofaxAnalysisResultRequester;
import org.metaborg.spoofax.core.processing.analyze.ISpoofaxAnalysisResultUpdater;
import org.metaborg.spoofax.core.processing.analyze.SpoofaxAnalysisResultProcessor;
import org.metaborg.spoofax.core.processing.parse.ISpoofaxParseResultProcessor;
import org.metaborg.spoofax.core.processing.parse.ISpoofaxParseResultRequester;
import org.metaborg.spoofax.core.processing.parse.ISpoofaxParseResultUpdater;
import org.metaborg.spoofax.core.processing.parse.SpoofaxParseResultProcessor;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.stratego.StrategoCommon;
import org.metaborg.spoofax.core.stratego.StrategoRuntimeService;
import org.metaborg.spoofax.core.stratego.primitive.AbsolutePathPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.CallStrategyPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.DigestPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.ExplicateInjectionsPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.GetSortNamePrimitive;
import org.metaborg.spoofax.core.stratego.primitive.IsLanguageActivePrimitive;
import org.metaborg.spoofax.core.stratego.primitive.LanguageComponentsPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.LanguageImplementationPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.LanguageIncludeDirectoriesPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.LanguageIncludeFilesPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.LanguagePrimitive;
import org.metaborg.spoofax.core.stratego.primitive.LanguageResourcesPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.LanguageSourceDirectoriesPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.LanguageSourceFilesPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.LocalPathPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.LocalReplicatePrimitive;
import org.metaborg.spoofax.core.stratego.primitive.ParsePrimitive;
import org.metaborg.spoofax.core.stratego.primitive.ProjectPathPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.ProjectResourcesPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.RelativeSourceOrIncludePath;
import org.metaborg.spoofax.core.stratego.primitive.RelativeSourcePath;
import org.metaborg.spoofax.core.stratego.primitive.SLShowDialogPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.SLShowInputDialogPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.SpoofaxPrimitiveLibrary;
import org.metaborg.spoofax.core.stratego.primitive.constraint.C_get_project_analyses;
import org.metaborg.spoofax.core.stratego.primitive.constraint.C_get_project_analyzed_asts;
import org.metaborg.spoofax.core.stratego.primitive.constraint.C_get_resource_analysis;
import org.metaborg.spoofax.core.stratego.primitive.flowspec.FS_solve;
import org.metaborg.spoofax.core.stratego.primitive.flowspec.FlowSpecLibrary;
import org.metaborg.spoofax.core.stratego.primitive.generic.DummyPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.legacy.LegacyForeignCallPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.legacy.LegacyLanguageIncludeFilesPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.legacy.LegacyLanguageIncludeLocationsPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.legacy.LegacyLanguageIncludeLocationsPrimitive2;
import org.metaborg.spoofax.core.stratego.primitive.legacy.LegacyLanguageSourceFilesPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.legacy.LegacyLanguageSourceLocationsPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.legacy.LegacyLanguageSourceLocationsPrimitive2;
import org.metaborg.spoofax.core.stratego.primitive.legacy.LegacyProjectPathPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.legacy.LegacySpoofaxPrimitiveLibrary;
import org.metaborg.spoofax.core.stratego.primitive.legacy.parse.LegacyParseFilePrimitive;
import org.metaborg.spoofax.core.stratego.primitive.legacy.parse.LegacyParseFilePtPrimitive;
import org.metaborg.spoofax.core.stratego.primitive.legacy.parse.LegacySpoofaxJSGLRLibrary;
import org.metaborg.spoofax.core.stratego.primitive.nabl2.SG_is_debug_collection_enabled;
import org.metaborg.spoofax.core.stratego.primitive.nabl2.SG_is_debug_custom_enabled;
import org.metaborg.spoofax.core.stratego.primitive.nabl2.SG_is_debug_resolution_enabled;
import org.metaborg.spoofax.core.stratego.primitive.renaming.RenamingLibrary;
import org.metaborg.spoofax.core.stratego.primitive.statix.STX_is_concurrent_enabled;
import org.metaborg.spoofax.core.stratego.primitive.statix.STX_project_config;
import org.metaborg.spoofax.core.stratego.primitive.statix.STX_solver_mode;
import org.metaborg.spoofax.core.stratego.primitive.statix.StatixLibrary;
import org.metaborg.spoofax.core.stratego.primitive.nabl2.NaBL2Library;
import org.metaborg.spoofax.core.stratego.strategies.ParseFileStrategy;
import org.metaborg.spoofax.core.stratego.strategies.ParseStrategoFileStrategy;
import org.metaborg.spoofax.core.style.CategorizerService;
import org.metaborg.spoofax.core.style.ISpoofaxCategorizerService;
import org.metaborg.spoofax.core.style.ISpoofaxStylerService;
import org.metaborg.spoofax.core.style.StylerService;
import org.metaborg.spoofax.core.syntax.ISpoofaxParser;
import org.metaborg.spoofax.core.syntax.ISpoofaxSyntaxService;
import org.metaborg.spoofax.core.syntax.JSGLRParseService;
import org.metaborg.spoofax.core.syntax.JSGLRParserConfiguration;
import org.metaborg.spoofax.core.syntax.SpoofaxSyntaxService;
import org.metaborg.spoofax.core.tracing.HoverService;
import org.metaborg.spoofax.core.tracing.ISpoofaxHoverService;
import org.metaborg.spoofax.core.tracing.ISpoofaxResolverService;
import org.metaborg.spoofax.core.tracing.ISpoofaxTracingService;
import org.metaborg.spoofax.core.tracing.ResolverService;
import org.metaborg.spoofax.core.tracing.TracingCommon;
import org.metaborg.spoofax.core.tracing.TracingService;
import org.metaborg.spoofax.core.transform.ISpoofaxTransformService;
import org.metaborg.spoofax.core.transform.IStrategoTransformer;
import org.metaborg.spoofax.core.transform.SpoofaxTransformService;
import org.metaborg.spoofax.core.transform.StrategoTransformer;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnitUpdate;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnitService;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.metaborg.spoofax.core.unit.UnitService;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.IOperatorRegistry;
import org.spoofax.interpreter.library.index.primitives.legacy.LegacyIndexLibrary;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.jsglr.client.imploder.ImploderOriginTermFactory;
import org.spoofax.terms.TermFactory;

import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;

import mb.flowspec.primitives.FS_build_cfg;
import mb.flowspec.primitives.FS_create_cfg;
import mb.flowspec.primitives.FS_get_cfg_node;
import mb.flowspec.primitives.FS_get_cfg_pred;
import mb.flowspec.primitives.FS_get_cfg_succ;
import mb.flowspec.primitives.FS_get_property_post;
import mb.flowspec.primitives.FS_get_property_pre;
import mb.flowspec.primitives.FS_show_control_flow_graph;
import mb.nabl2.spoofax.primitives.SG_analysis_has_errors;
import mb.nabl2.spoofax.primitives.SG_debug_ast_properties;
import mb.nabl2.spoofax.primitives.SG_debug_constraints;
import mb.nabl2.spoofax.primitives.SG_debug_name_resolution;
import mb.nabl2.spoofax.primitives.SG_debug_scope_graph;
import mb.nabl2.spoofax.primitives.SG_debug_symbolic_constraints;
import mb.nabl2.spoofax.primitives.SG_debug_unifier;
import mb.nabl2.spoofax.primitives.SG_focus_term;
import mb.nabl2.spoofax.primitives.SG_get_all_decls;
import mb.nabl2.spoofax.primitives.SG_get_all_refs;
import mb.nabl2.spoofax.primitives.SG_get_all_scopes;
import mb.nabl2.spoofax.primitives.SG_get_ast_decls;
import mb.nabl2.spoofax.primitives.SG_get_ast_property;
import mb.nabl2.spoofax.primitives.SG_get_ast_refs;
import mb.nabl2.spoofax.primitives.SG_get_ast_resolution;
import mb.nabl2.spoofax.primitives.SG_get_custom_analysis;
import mb.nabl2.spoofax.primitives.SG_get_decl_property;
import mb.nabl2.spoofax.primitives.SG_get_decl_resolution;
import mb.nabl2.spoofax.primitives.SG_get_decl_scope;
import mb.nabl2.spoofax.primitives.SG_get_direct_edges;
import mb.nabl2.spoofax.primitives.SG_get_direct_edges_inv;
import mb.nabl2.spoofax.primitives.SG_get_export_edges;
import mb.nabl2.spoofax.primitives.SG_get_export_edges_inv;
import mb.nabl2.spoofax.primitives.SG_get_import_edges;
import mb.nabl2.spoofax.primitives.SG_get_import_edges_inv;
import mb.nabl2.spoofax.primitives.SG_get_reachable_decls;
import mb.nabl2.spoofax.primitives.SG_get_ref_resolution;
import mb.nabl2.spoofax.primitives.SG_get_ref_scope;
import mb.nabl2.spoofax.primitives.SG_get_scope_decls;
import mb.nabl2.spoofax.primitives.SG_get_scope_refs;
import mb.nabl2.spoofax.primitives.SG_get_symbolic_facts;
import mb.nabl2.spoofax.primitives.SG_get_symbolic_goals;
import mb.nabl2.spoofax.primitives.SG_get_visible_decls;
import mb.nabl2.spoofax.primitives.SG_set_custom_analysis;
import mb.nabl2.spoofax.primitives.SG_solve_multi_final_constraint;
import mb.nabl2.spoofax.primitives.SG_solve_multi_initial_constraint;
import mb.nabl2.spoofax.primitives.SG_solve_multi_unit_constraint;
import mb.nabl2.spoofax.primitives.SG_solve_single_constraint;
import mb.nabl2.terms.stratego.primitives.SG_erase_ast_indices;
import mb.nabl2.terms.stratego.primitives.SG_get_ast_index;
import mb.nabl2.terms.stratego.primitives.SG_get_ast_resource;
import mb.nabl2.terms.stratego.primitives.SG_index_ast;
import mb.nabl2.terms.stratego.primitives.SG_set_ast_index;
import mb.renaming.namegraph.FindAllRelatedOccurrencesPrimitive;
import mb.statix.spoofax.STX_analysis_has_errors;
import mb.statix.spoofax.STX_compare_patterns;
import mb.statix.spoofax.STX_debug_scopegraph;
import mb.statix.spoofax.STX_delays_as_errors;
import mb.statix.spoofax.STX_diff_scopegraphs;
import mb.statix.spoofax.STX_extract_messages;
import mb.statix.spoofax.STX_get_all_properties;
import mb.statix.spoofax.STX_get_ast_properties;
import mb.statix.spoofax.STX_get_ast_property;
import mb.statix.spoofax.STX_get_scopegraph;
import mb.statix.spoofax.STX_get_scopegraph_data;
import mb.statix.spoofax.STX_get_scopegraph_edges;
import mb.statix.spoofax.STX_incremental_diagnostics;
import mb.statix.spoofax.STX_is_analysis;
import mb.statix.spoofax.STX_labelord_lt;
import mb.statix.spoofax.STX_labelre_to_states;
import mb.statix.spoofax.STX_ords_to_relation;
import mb.statix.spoofax.STX_solve_constraint;
import mb.statix.spoofax.STX_solve_constraint_concurrent;
import mb.statix.spoofax.STX_solve_multi;
import mb.statix.spoofax.STX_solve_multi_file;
import mb.statix.spoofax.STX_solve_multi_project;
import mb.statix.spoofax.STX_test_log_level;

/**
 * Guice module that specifies which implementations to use for services and factories.
 */
public class SpoofaxModule extends MetaborgModule {

    private MapBinder<String, IParser<ISpoofaxInputUnit, ISpoofaxParseUnit>> parserBinder;
    private MapBinder<String, ISpoofaxParser> spoofaxParserBinder;
    private MapBinder<String, IAnalyzer<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate>> analyzerBinder;
    private MapBinder<String, ISpoofaxAnalyzer> spoofaxAnalyzerBinder;


    public SpoofaxModule() {
        this(SpoofaxModule.class.getClassLoader());
    }

    public SpoofaxModule(ClassLoader resourceClassLoader) {
        super(resourceClassLoader);
    }

    @Override protected void configure() {
        super.configure();

        parserBinder = MapBinder.newMapBinder(binder(), new TypeLiteral<String>() {},
                new TypeLiteral<IParser<ISpoofaxInputUnit, ISpoofaxParseUnit>>() {});
        spoofaxParserBinder = MapBinder.newMapBinder(binder(), String.class, ISpoofaxParser.class);
        analyzerBinder = MapBinder.newMapBinder(binder(), new TypeLiteral<String>() {},
                new TypeLiteral<IAnalyzer<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate>>() {});
        spoofaxAnalyzerBinder = MapBinder.newMapBinder(binder(), String.class, ISpoofaxAnalyzer.class);

        Multibinder.newSetBinder(binder(), ClassLoader.class).permitDuplicates();

        bindUnit();
        bindSyntax();
        bindParsers(parserBinder, spoofaxParserBinder);
        bindAnalyzers(analyzerBinder, spoofaxAnalyzerBinder);
        bindCompletion();
        bindAction();
        bindTransformer();
        bindCategorizer();
        bindStyler();
        bindTracing();
        bindOutline();
        bindMenu();
        bindDialog();
    }


    protected void bindUnit() {
        bind(UnitService.class).in(Singleton.class);
        bind(ISpoofaxUnitService.class).to(UnitService.class);
        bind(new TypeLiteral<IUnitService<ISpoofaxInputUnit, ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate, ISpoofaxTransformUnit<ISpoofaxParseUnit>, ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit>>>() {})
                .to(UnitService.class);
        bind(new TypeLiteral<IUnitService<?, ?, ?, ?, ?, ?>>() {}).to(UnitService.class);
        bind(IUnitService.class).to(UnitService.class);

        bind(ISpoofaxInputUnitService.class).to(UnitService.class);
        bind(new TypeLiteral<IInputUnitService<ISpoofaxInputUnit>>() {}).to(UnitService.class);
        bind(new TypeLiteral<IInputUnitService<?>>() {}).to(UnitService.class);
        bind(IInputUnitService.class).to(UnitService.class);
    }

    @Override protected void bindLanguage() {
        super.bindLanguage();

        bind(ILanguageComponentFactory.class).to(LanguageComponentFactory.class).in(Singleton.class);
        bind(ILanguageDiscoveryService.class).to(LanguageDiscoveryService.class).in(Singleton.class);

        bind(IDialectService.class).to(DialectService.class).in(Singleton.class);
        bind(IDialectIdentifier.class).to(DialectIdentifier.class).in(Singleton.class);
        bind(IDialectProcessor.class).to(DialectProcessor.class).in(Singleton.class);
    }

    @Override protected void bindProjectConfig() {
        bind(IProjectConfigWriter.class).to(ProjectConfigService.class).in(Singleton.class);

        bind(SpoofaxProjectConfigService.class).in(Singleton.class);
        bind(IProjectConfigService.class).to(SpoofaxProjectConfigService.class);
        bind(ISpoofaxProjectConfigService.class).to(SpoofaxProjectConfigService.class);
        bind(ISpoofaxProjectConfigWriter.class).to(SpoofaxProjectConfigService.class);

        bind(SpoofaxProjectConfigBuilder.class);
        bind(IProjectConfigBuilder.class).to(SpoofaxProjectConfigBuilder.class);
        bind(ISpoofaxProjectConfigBuilder.class).to(SpoofaxProjectConfigBuilder.class);
    }

    @Override protected void bindContextFactories(MapBinder<String, IContextFactory> binder) {
        super.bindContextFactories(binder);

        binder.addBinding(IndexTaskContextFactory.name).to(IndexTaskContextFactory.class).in(Singleton.class);
        binder.addBinding(LegacyContextFactory.name).to(LegacyContextFactory.class).in(Singleton.class);
        binder.addBinding(ConstraintContextFactory.name).to(ConstraintContextFactory.class)
                .in(Singleton.class);
    }

    protected void bindSyntax() {
        bind(SpoofaxSyntaxService.class).in(Singleton.class);
        bind(ISpoofaxSyntaxService.class).to(SpoofaxSyntaxService.class);
        bind(new TypeLiteral<ISyntaxService<ISpoofaxInputUnit, ISpoofaxParseUnit>>() {}).to(SpoofaxSyntaxService.class);
        bind(new TypeLiteral<ISyntaxService<?, ?>>() {}).to(SpoofaxSyntaxService.class);
        bind(ISyntaxService.class).to(SpoofaxSyntaxService.class);

        bind(ITermFactory.class).toInstance(new ImploderOriginTermFactory(new TermFactory()));
    }

    protected void bindParsers(MapBinder<String, IParser<ISpoofaxInputUnit, ISpoofaxParseUnit>> parserBinder,
            MapBinder<String, ISpoofaxParser> spoofaxParserBinder) {
        bind(JSGLRParseService.class).in(Singleton.class);
        parserBinder.addBinding(JSGLRParseService.name).to(JSGLRParseService.class);
        spoofaxParserBinder.addBinding(JSGLRParseService.name).to(JSGLRParseService.class);
        languageCacheBinder.addBinding().to(JSGLRParseService.class);
        autoClosableBinder.addBinding().to(JSGLRParseService.class);

        bind(JSGLRParserConfiguration.class).toInstance(new JSGLRParserConfiguration());
    }

    /**
     * Overrides {@link MetaborgModule#bindAnalysis()} to provide Spoofax-specific bindings with Spoofax interfaces, and
     * to provide analyzers.
     */
    @Override protected void bindAnalysis() {
        // Analysis service
        bind(SpoofaxAnalysisService.class).in(Singleton.class);
        bind(ISpoofaxAnalysisService.class).to(SpoofaxAnalysisService.class);
        bind(new TypeLiteral<IAnalysisService<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate>>() {})
                .to(SpoofaxAnalysisService.class);
        bind(new TypeLiteral<IAnalysisService<?, ?, ?>>() {}).to(SpoofaxAnalysisService.class);
        bind(IAnalysisService.class).to(SpoofaxAnalysisService.class);

        // Semantic provider
        bind(DynamicClassLoadingService.class).in(Singleton.class);
        bind(IDynamicClassLoadingService.class).to(DynamicClassLoadingService.class);
        languageCacheBinder.addBinding().to(DynamicClassLoadingService.class);

        // Stratego runtime
        bind(StrategoRuntimeService.class).in(Singleton.class);
        bind(IStrategoRuntimeService.class).to(StrategoRuntimeService.class);
        languageCacheBinder.addBinding().to(StrategoRuntimeService.class);
        autoClosableBinder.addBinding().to(StrategoRuntimeService.class);


        // Utilities
        bind(IStrategoCommon.class).to(StrategoCommon.class).in(Singleton.class);
        bind(AnalysisCommon.class).in(Singleton.class);

        // Stratego primitives
        bind(ParseFileStrategy.class).in(Singleton.class);
        bind(ParseStrategoFileStrategy.class).in(Singleton.class);

        final Multibinder<IOperatorRegistry> libraryBinder =
                Multibinder.newSetBinder(binder(), IOperatorRegistry.class);
        bindPrimitiveLibrary(libraryBinder, TaskLibrary.class);
        bindPrimitiveLibrary(libraryBinder, LegacyIndexLibrary.class);
        bindPrimitiveLibrary(libraryBinder, SpoofaxPrimitiveLibrary.class);
        bindPrimitiveLibrary(libraryBinder, NaBL2Library.class);
        bindPrimitiveLibrary(libraryBinder, StatixLibrary.class);
        bindPrimitiveLibrary(libraryBinder, FlowSpecLibrary.class);
        bindPrimitiveLibrary(libraryBinder, LegacySpoofaxPrimitiveLibrary.class);
        bindPrimitiveLibrary(libraryBinder, LegacySpoofaxJSGLRLibrary.class);
        bindPrimitiveLibrary(libraryBinder, RenamingLibrary.class);

        final Multibinder<AbstractPrimitive> spoofaxPrimitiveLibrary =
                Multibinder.newSetBinder(binder(), AbstractPrimitive.class, Names.named(SpoofaxPrimitiveLibrary.name));
        bindPrimitive(spoofaxPrimitiveLibrary, AbsolutePathPrimitive.class);
        bindPrimitive(spoofaxPrimitiveLibrary, CallStrategyPrimitive.class);
        bindPrimitive(spoofaxPrimitiveLibrary, DigestPrimitive.class);
        bindPrimitive(spoofaxPrimitiveLibrary, ExplicateInjectionsPrimitive.class);
        bindPrimitive(spoofaxPrimitiveLibrary, GetSortNamePrimitive.class);
        bindPrimitive(spoofaxPrimitiveLibrary, IsLanguageActivePrimitive.class);
        bindPrimitive(spoofaxPrimitiveLibrary, LanguageComponentsPrimitive.class);
        bindPrimitive(spoofaxPrimitiveLibrary, LanguageImplementationPrimitive.class);
        bindPrimitive(spoofaxPrimitiveLibrary, LanguageIncludeDirectoriesPrimitive.class);
        bindPrimitive(spoofaxPrimitiveLibrary, LanguageIncludeFilesPrimitive.class);
        bindPrimitive(spoofaxPrimitiveLibrary, LanguagePrimitive.class);
        bindPrimitive(spoofaxPrimitiveLibrary, LanguageResourcesPrimitive.class);
        bindPrimitive(spoofaxPrimitiveLibrary, LanguageSourceDirectoriesPrimitive.class);
        bindPrimitive(spoofaxPrimitiveLibrary, LanguageSourceFilesPrimitive.class);
        bindPrimitive(spoofaxPrimitiveLibrary, LocalPathPrimitive.class);
        bindPrimitive(spoofaxPrimitiveLibrary, LocalReplicatePrimitive.class);
        bindPrimitive(spoofaxPrimitiveLibrary, ParsePrimitive.class);
        bindPrimitive(spoofaxPrimitiveLibrary, ProjectPathPrimitive.class);
        bindPrimitive(spoofaxPrimitiveLibrary, ProjectResourcesPrimitive.class);
        bindPrimitive(spoofaxPrimitiveLibrary, RelativeSourceOrIncludePath.class);
        bindPrimitive(spoofaxPrimitiveLibrary, RelativeSourcePath.class);
        bindPrimitive(spoofaxPrimitiveLibrary, SLShowDialogPrimitive.class);
        bindPrimitive(spoofaxPrimitiveLibrary, SLShowInputDialogPrimitive.class);


        final Multibinder<AbstractPrimitive> spoofaxScopeGraphLibrary =
                Multibinder.newSetBinder(binder(), AbstractPrimitive.class, Names.named(NaBL2Library.name));
        // libspoofax
        bindPrimitive(spoofaxScopeGraphLibrary, C_get_project_analyses.class);
        bindPrimitive(spoofaxScopeGraphLibrary, C_get_project_analyzed_asts.class);
        bindPrimitive(spoofaxScopeGraphLibrary, C_get_resource_analysis.class);
        // nabl2.terms
        bindPrimitive(spoofaxScopeGraphLibrary, SG_erase_ast_indices.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_ast_index.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_ast_resource.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_index_ast.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_set_ast_index.class);
        // nabl2.solver
        bindPrimitive(spoofaxScopeGraphLibrary, SG_analysis_has_errors.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_debug_constraints.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_debug_name_resolution.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_debug_scope_graph.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_debug_symbolic_constraints.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_debug_ast_properties.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_debug_unifier.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_focus_term.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_all_decls.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_all_refs.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_all_scopes.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_ast_decls.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_ast_property.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_ast_refs.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_ast_resolution.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_custom_analysis.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_decl_property.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_decl_resolution.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_decl_scope.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_direct_edges_inv.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_direct_edges.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_export_edges_inv.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_export_edges.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_import_edges_inv.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_import_edges.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_reachable_decls.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_ref_resolution.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_ref_scope.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_scope_decls.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_scope_refs.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_symbolic_facts.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_symbolic_goals.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_get_visible_decls.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_is_debug_collection_enabled.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_is_debug_custom_enabled.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_is_debug_resolution_enabled.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_set_custom_analysis.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_solve_single_constraint.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_solve_multi_initial_constraint.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_solve_multi_unit_constraint.class);
        bindPrimitive(spoofaxScopeGraphLibrary, SG_solve_multi_final_constraint.class);

        final Multibinder<AbstractPrimitive> statixLibrary =
                Multibinder.newSetBinder(binder(), AbstractPrimitive.class, Names.named(StatixLibrary.name));
        // libspoofax
        bindPrimitive(statixLibrary, STX_is_concurrent_enabled.class);
        bindPrimitive(statixLibrary, STX_project_config.class);
        bindPrimitive(statixLibrary, STX_solver_mode.class);
        // statix.solver
        bindPrimitive(statixLibrary, STX_analysis_has_errors.class);
        bindPrimitive(statixLibrary, STX_compare_patterns.class);
        bindPrimitive(statixLibrary, STX_debug_scopegraph.class);
        bindPrimitive(statixLibrary, STX_delays_as_errors.class);
        bindPrimitive(statixLibrary, STX_diff_scopegraphs.class);
        bindPrimitive(statixLibrary, STX_extract_messages.class);
        bindPrimitive(statixLibrary, STX_get_ast_property.class);
        bindPrimitive(statixLibrary, STX_get_ast_properties.class);
        bindPrimitive(statixLibrary, STX_get_all_properties.class);
        bindPrimitive(statixLibrary, STX_get_scopegraph.class);
        bindPrimitive(statixLibrary, STX_get_scopegraph_data.class);
        bindPrimitive(statixLibrary, STX_get_scopegraph_edges.class);
        bindPrimitive(statixLibrary, STX_is_analysis.class);
        bindPrimitive(statixLibrary, STX_solve_constraint.class);
        bindPrimitive(statixLibrary, STX_solve_constraint_concurrent.class);
        bindPrimitive(statixLibrary, STX_solve_multi.class);
        bindPrimitive(statixLibrary, STX_solve_multi_file.class);
        bindPrimitive(statixLibrary, STX_solve_multi_project.class);
        bindPrimitive(statixLibrary, STX_test_log_level.class);
        bindPrimitive(statixLibrary, STX_incremental_diagnostics.class);

        bindPrimitive(statixLibrary, STX_labelre_to_states.class);
        bindPrimitive(statixLibrary, STX_ords_to_relation.class);
        bindPrimitive(statixLibrary, STX_labelord_lt.class);

        /*
         * Note that FS_solve first needs to be identified as a Singleton, so that afterwards it
         * can be used with bindPrimitive and languageCacheBinder without creating multiple
         * instances. Multiple instances would mess up the language cache invalidation.
         */
        bind(FS_solve.class).in(Singleton.class);
        final Multibinder<AbstractPrimitive> spoofaxFlowSpecLibrary =
                Multibinder.newSetBinder(binder(), AbstractPrimitive.class, Names.named(FlowSpecLibrary.name));
        bindPrimitive(spoofaxFlowSpecLibrary, FS_solve.class);
        bindPrimitive(spoofaxFlowSpecLibrary, FS_build_cfg.class);
        bindPrimitive(spoofaxFlowSpecLibrary, FS_create_cfg.class);
        bindPrimitive(spoofaxFlowSpecLibrary, FS_get_cfg_node.class);
        bindPrimitive(spoofaxFlowSpecLibrary, FS_get_cfg_pred.class);
        bindPrimitive(spoofaxFlowSpecLibrary, FS_get_cfg_succ.class);
        bindPrimitive(spoofaxFlowSpecLibrary, FS_get_property_pre.class);
        bindPrimitive(spoofaxFlowSpecLibrary, FS_get_property_post.class);
        bindPrimitive(spoofaxFlowSpecLibrary, FS_show_control_flow_graph.class);
        languageCacheBinder.addBinding().to(FS_solve.class);

        final Multibinder<AbstractPrimitive> legacySpoofaxLibrary = Multibinder.newSetBinder(binder(),
                AbstractPrimitive.class, Names.named(LegacySpoofaxPrimitiveLibrary.name));
        bindPrimitive(legacySpoofaxLibrary, LegacyProjectPathPrimitive.class);
        bindPrimitive(legacySpoofaxLibrary, LegacyLanguageSourceLocationsPrimitive.class);
        bindPrimitive(legacySpoofaxLibrary, LegacyLanguageSourceLocationsPrimitive2.class);
        bindPrimitive(legacySpoofaxLibrary, LegacyLanguageIncludeLocationsPrimitive.class);
        bindPrimitive(legacySpoofaxLibrary, LegacyLanguageIncludeLocationsPrimitive2.class);
        bindPrimitive(legacySpoofaxLibrary, LegacyLanguageSourceFilesPrimitive.class);
        bindPrimitive(legacySpoofaxLibrary, LegacyLanguageIncludeFilesPrimitive.class);
        bindPrimitive(legacySpoofaxLibrary, LegacyForeignCallPrimitive.class);
        bindPrimitive(legacySpoofaxLibrary, new DummyPrimitive("SSL_EXT_set_total_work_units", 0, 0));
        bindPrimitive(legacySpoofaxLibrary, new DummyPrimitive("SSL_EXT_set_markers", 0, 1));
        bindPrimitive(legacySpoofaxLibrary, new DummyPrimitive("SSL_EXT_refreshresource", 0, 1));
        bindPrimitive(legacySpoofaxLibrary, new DummyPrimitive("SSL_EXT_queue_strategy", 0, 2));
        bindPrimitive(legacySpoofaxLibrary, new DummyPrimitive("SSL_EXT_complete_work_unit", 0, 0));
        bindPrimitive(legacySpoofaxLibrary, new DummyPrimitive("SSL_EXT_pluginpath", 0, 0));

        final Multibinder<AbstractPrimitive> legacySpoofaxJSGLRLibrary = Multibinder.newSetBinder(binder(),
                AbstractPrimitive.class, Names.named(LegacySpoofaxJSGLRLibrary.injectionName));
        bindPrimitive(legacySpoofaxJSGLRLibrary, LegacyParseFilePrimitive.class);
        bindPrimitive(legacySpoofaxJSGLRLibrary, LegacyParseFilePtPrimitive.class);
        bindPrimitive(legacySpoofaxJSGLRLibrary, new DummyPrimitive("STRSGLR_open_parse_table", 0, 1));
        bindPrimitive(legacySpoofaxJSGLRLibrary, new DummyPrimitive("STRSGLR_close_parse_table", 0, 1));

        final Multibinder<AbstractPrimitive> renamingPrimitivesLibrary = Multibinder.newSetBinder(binder(),
                AbstractPrimitive.class, Names.named(RenamingLibrary.name));
        bindPrimitive(renamingPrimitivesLibrary, FindAllRelatedOccurrencesPrimitive.class);
    }

    private void bindAnalyzers(
            MapBinder<String, IAnalyzer<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate>> analyzerBinder,
            MapBinder<String, ISpoofaxAnalyzer> spoofaxAnalyzerBinder) {
        bind(StrategoAnalyzer.class).in(Singleton.class);
        bind(TaskEngineAnalyzer.class).in(Singleton.class);
        bind(SingleFileConstraintAnalyzer.class).in(Singleton.class);
        bind(MultiFileConstraintAnalyzer.class).in(Singleton.class);

        analyzerBinder.addBinding(StrategoAnalyzer.name).to(StrategoAnalyzer.class);
        spoofaxAnalyzerBinder.addBinding(StrategoAnalyzer.name).to(StrategoAnalyzer.class);
        analyzerBinder.addBinding(TaskEngineAnalyzer.name).to(TaskEngineAnalyzer.class);
        spoofaxAnalyzerBinder.addBinding(TaskEngineAnalyzer.name).to(TaskEngineAnalyzer.class);
        analyzerBinder.addBinding(SingleFileConstraintAnalyzer.name).to(SingleFileConstraintAnalyzer.class);
        spoofaxAnalyzerBinder.addBinding(SingleFileConstraintAnalyzer.name).to(SingleFileConstraintAnalyzer.class);
        //        languageCacheBinder.addBinding().to(MultiFileConstraintAnalyzer.class);
        analyzerBinder.addBinding(MultiFileConstraintAnalyzer.name).to(MultiFileConstraintAnalyzer.class);
        spoofaxAnalyzerBinder.addBinding(MultiFileConstraintAnalyzer.name).to(MultiFileConstraintAnalyzer.class);
        //        languageCacheBinder.addBinding().to(MultiFileConstraintAnalyzer.class);
    }

    protected void bindAction() {
        bind(IActionService.class).to(ActionService.class).in(Singleton.class);
    }

    protected void bindTransformer() {
        // Analysis service
        bind(SpoofaxTransformService.class).in(Singleton.class);
        bind(ISpoofaxTransformService.class).to(SpoofaxTransformService.class);
        bind(new TypeLiteral<ITransformService<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxTransformUnit<ISpoofaxParseUnit>, ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit>>>() {})
                .to(SpoofaxTransformService.class);
        bind(new TypeLiteral<ITransformService<?, ?, ?, ?>>() {}).to(SpoofaxTransformService.class);
        bind(ITransformService.class).to(SpoofaxTransformService.class);

        // Analyzers
        bind(StrategoTransformer.class).in(Singleton.class);
        bind(IStrategoTransformer.class).to(StrategoTransformer.class);
        bind(new TypeLiteral<ITransformer<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxTransformUnit<ISpoofaxParseUnit>, ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit>>>() {})
                .to(StrategoTransformer.class);
        bind(new TypeLiteral<ITransformer<?, ?, ?, ?>>() {}).to(StrategoTransformer.class);
        bind(ITransformer.class).to(StrategoTransformer.class);
    }

    /**
     * Overrides {@link MetaborgModule#bindBuilder()} to provide Spoofax-specific bindings with generics filled in as
     * {@link IStrategoTerm}.
     */
    @Override protected void bindBuilder() {
        bind(SpoofaxParseResultProcessor.class).in(Singleton.class);

        bind(ISpoofaxParseResultRequester.class).to(SpoofaxParseResultProcessor.class);
        bind(new TypeLiteral<IParseResultRequester<ISpoofaxInputUnit, ISpoofaxParseUnit>>() {})
                .to(SpoofaxParseResultProcessor.class);
        bind(new TypeLiteral<IParseResultRequester<?, ?>>() {}).to(SpoofaxParseResultProcessor.class);
        bind(IParseResultRequester.class).to(SpoofaxParseResultProcessor.class);

        bind(ISpoofaxParseResultUpdater.class).to(SpoofaxParseResultProcessor.class);
        bind(new TypeLiteral<IParseResultUpdater<ISpoofaxParseUnit>>() {}).to(SpoofaxParseResultProcessor.class);
        bind(new TypeLiteral<IParseResultUpdater<?>>() {}).to(SpoofaxParseResultProcessor.class);
        bind(IParseResultUpdater.class).to(SpoofaxParseResultProcessor.class);

        bind(ISpoofaxParseResultProcessor.class).to(SpoofaxParseResultProcessor.class);
        bind(new TypeLiteral<IParseResultProcessor<ISpoofaxInputUnit, ISpoofaxParseUnit>>() {})
                .to(SpoofaxParseResultProcessor.class);
        bind(new TypeLiteral<IParseResultProcessor<?, ?>>() {}).to(SpoofaxParseResultProcessor.class);
        bind(IParseResultProcessor.class).to(SpoofaxParseResultProcessor.class);


        bind(SpoofaxAnalysisResultProcessor.class).in(Singleton.class);

        bind(ISpoofaxAnalysisResultRequester.class).to(SpoofaxAnalysisResultProcessor.class);
        bind(new TypeLiteral<IAnalysisResultRequester<ISpoofaxInputUnit, ISpoofaxAnalyzeUnit>>() {})
                .to(SpoofaxAnalysisResultProcessor.class);
        bind(new TypeLiteral<IAnalysisResultRequester<?, ?>>() {}).to(SpoofaxAnalysisResultProcessor.class);
        bind(IAnalysisResultRequester.class).to(SpoofaxAnalysisResultProcessor.class);

        bind(ISpoofaxAnalysisResultUpdater.class).to(SpoofaxAnalysisResultProcessor.class);
        bind(new TypeLiteral<IAnalysisResultUpdater<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit>>() {})
                .to(SpoofaxAnalysisResultProcessor.class);
        bind(new TypeLiteral<IAnalysisResultUpdater<?, ?>>() {}).to(SpoofaxAnalysisResultProcessor.class);
        bind(IAnalysisResultUpdater.class).to(SpoofaxAnalysisResultProcessor.class);

        bind(ISpoofaxAnalysisResultProcessor.class).to(SpoofaxAnalysisResultProcessor.class);
        bind(new TypeLiteral<IAnalysisResultProcessor<ISpoofaxInputUnit, ISpoofaxParseUnit, ISpoofaxAnalyzeUnit>>() {})
                .to(SpoofaxAnalysisResultProcessor.class);
        bind(new TypeLiteral<IAnalysisResultProcessor<?, ?, ?>>() {}).to(SpoofaxAnalysisResultProcessor.class);
        bind(IAnalysisResultProcessor.class).to(SpoofaxAnalysisResultProcessor.class);


        bind(SpoofaxBuilder.class).in(Singleton.class);
        bind(ISpoofaxBuilder.class).to(SpoofaxBuilder.class);
        bind(new TypeLiteral<IBuilder<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate, ISpoofaxTransformUnit<?>>>() {})
                .to(SpoofaxBuilder.class);
        bind(new TypeLiteral<IBuilder<?, ?, ?, ?>>() {}).to(SpoofaxBuilder.class);
        bind(IBuilder.class).to(SpoofaxBuilder.class);

        // No scope for build output, new instance for every request.
        bind(new TypeLiteral<IBuildOutputInternal<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate, ISpoofaxTransformUnit<?>>>() {})
                .to(SpoofaxBuildOutput.class);
    }

    /**
     * Overrides {@link MetaborgModule#bindProcessorRunner()} to provide Spoofax-specific bindings with generics filled
     * in as {@link IStrategoTerm}.
     */
    @Override protected void bindProcessorRunner() {
        bind(SpoofaxProcessorRunner.class).in(Singleton.class);
        bind(ISpoofaxProcessorRunner.class).to(SpoofaxProcessorRunner.class);
        bind(new TypeLiteral<IProcessorRunner<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate, ISpoofaxTransformUnit<?>>>() {})
                .to(SpoofaxProcessorRunner.class);
        bind(new TypeLiteral<IProcessorRunner<?, ?, ?, ?>>() {}).to(SpoofaxProcessorRunner.class);
        bind(IProcessorRunner.class).to(SpoofaxProcessorRunner.class);
    }

    /**
     * Overrides {@link MetaborgModule#bindProcessor()} to provide Spoofax-specific bindings with generics filled in as
     * {@link IStrategoTerm}.
     */
    @Override protected void bindProcessor() {
        bind(SpoofaxBlockingProcessor.class).in(Singleton.class);
        bind(ISpoofaxProcessor.class).to(SpoofaxBlockingProcessor.class);
        bind(new TypeLiteral<IProcessor<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxAnalyzeUnitUpdate, ISpoofaxTransformUnit<?>>>() {})
                .to(SpoofaxBlockingProcessor.class);
        bind(new TypeLiteral<IProcessor<?, ?, ?, ?>>() {}).to(SpoofaxBlockingProcessor.class);
        bind(IProcessor.class).to(SpoofaxBlockingProcessor.class);
    }

    protected void bindCategorizer() {
        bind(CategorizerService.class).in(Singleton.class);
        bind(ISpoofaxCategorizerService.class).to(CategorizerService.class);
        bind(new TypeLiteral<ICategorizerService<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, IStrategoTerm>>() {})
                .to(CategorizerService.class);
        bind(new TypeLiteral<ICategorizerService<?, ?, ?>>() {}).to(CategorizerService.class);
        bind(ICategorizerService.class).to(CategorizerService.class);
    }

    protected void bindStyler() {
        bind(StylerService.class).in(Singleton.class);
        bind(ISpoofaxStylerService.class).to(StylerService.class);
        bind(new TypeLiteral<IStylerService<IStrategoTerm>>() {}).to(StylerService.class);
        bind(new TypeLiteral<IStylerService<?>>() {}).to(StylerService.class);
        bind(IStylerService.class).to(StylerService.class);
    }

    protected void bindTracing() {
        bind(TracingService.class).in(Singleton.class);
        bind(ISpoofaxTracingService.class).to(TracingService.class);
        bind(new TypeLiteral<ITracingService<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit, ISpoofaxTransformUnit<?>, IStrategoTerm>>() {})
                .to(TracingService.class);
        bind(new TypeLiteral<ITracingService<?, ?, ?, ?>>() {}).to(TracingService.class);
        bind(ITracingService.class).to(TracingService.class);

        bind(TracingCommon.class).in(Singleton.class);

        bind(ResolverService.class).in(Singleton.class);
        bind(ISpoofaxResolverService.class).to(ResolverService.class);
        bind(new TypeLiteral<IResolverService<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit>>() {}).to(ResolverService.class);
        bind(new TypeLiteral<IResolverService<?, ?>>() {}).to(ResolverService.class);
        bind(IResolverService.class).to(ResolverService.class);

        bind(HoverService.class).in(Singleton.class);
        bind(ISpoofaxHoverService.class).to(HoverService.class);
        bind(new TypeLiteral<IHoverService<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit>>() {}).to(HoverService.class);
        bind(new TypeLiteral<IHoverService<?, ?>>() {}).to(HoverService.class);
        bind(IHoverService.class).to(HoverService.class);
    }

    protected void bindCompletion() {
        bind(JSGLRCompletionService.class).in(Singleton.class);
        bind(ISpoofaxCompletionService.class).to(JSGLRCompletionService.class);
        bind(new TypeLiteral<ICompletionService<ISpoofaxParseUnit>>() {}).to(JSGLRCompletionService.class);
        bind(new TypeLiteral<ICompletionService<?>>() {}).to(JSGLRCompletionService.class);
        bind(ICompletionService.class).to(JSGLRCompletionService.class);
    }

    protected void bindOutline() {
        bind(OutlineService.class).in(Singleton.class);
        bind(ISpoofaxOutlineService.class).to(OutlineService.class);
        bind(new TypeLiteral<IOutlineService<ISpoofaxParseUnit, ISpoofaxAnalyzeUnit>>() {}).to(OutlineService.class);
        bind(new TypeLiteral<IOutlineService<?, ?>>() {}).to(OutlineService.class);
        bind(IOutlineService.class).to(OutlineService.class);
    }

    protected void bindMenu() {
        bind(IMenuService.class).to(MenuService.class).in(Singleton.class);
    }

    protected void bindDialog() {
        bind(ISpoofaxDialogService.class).to(NullSpoofaxDialogService.class).in(Singleton.class);
    }

    protected static void bindPrimitive(Multibinder<AbstractPrimitive> binder, AbstractPrimitive primitive) {
        binder.addBinding().toInstance(primitive);
    }

    protected static void bindPrimitive(Multibinder<AbstractPrimitive> binder,
            Class<? extends AbstractPrimitive> primitive) {
        binder.addBinding().to(primitive).in(Singleton.class);
    }

    protected static void bindPrimitiveLibrary(Multibinder<IOperatorRegistry> binder,
            Class<? extends IOperatorRegistry> primitiveLibrary) {
        binder.addBinding().to(primitiveLibrary).in(Singleton.class);
    }
}
