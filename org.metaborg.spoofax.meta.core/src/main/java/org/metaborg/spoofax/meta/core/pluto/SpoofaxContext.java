package org.metaborg.spoofax.meta.core.pluto;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URI;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.build.paths.ILanguagePathService;
import org.metaborg.core.config.ConfigException;
import org.metaborg.core.context.IContextService;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.language.ILanguageService;
import org.metaborg.core.language.dialect.IDialectService;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.IProjectService;
import org.metaborg.core.resource.IResourceService;
import org.metaborg.core.source.ISourceTextService;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.stratego.ResourceAgent;
import org.metaborg.spoofax.core.syntax.ISpoofaxSyntaxService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.metaborg.spoofax.core.transform.ISpoofaxTransformService;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxUnitService;
import org.metaborg.spoofax.meta.core.pluto.build.main.IPieProvider;
import org.metaborg.spoofax.meta.core.pluto.util.ResourceAgentTracker;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpec;
import org.metaborg.spoofax.meta.core.project.ISpoofaxLanguageSpecService;
import org.metaborg.util.file.FileUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.inject.Injector;

import mb.stratego.build.strincr.StrIncr;

public class SpoofaxContext implements Serializable {
    private static final long serialVersionUID = -1973461199459693455L;

    public final static boolean BETTER_STAMPERS = true;

    // NOTE: This class should only contain static or transient fields.
    // If non-transient fields are required, they must be Serializable,
    // (so FileObject is out of the question).

    private static final ThreadLocal<Injector> injector = new ThreadLocal<>();
    private static final ThreadLocal<IResourceService> resourceService = new ThreadLocal<>();
    private static final ThreadLocal<ILanguageService> languageService = new ThreadLocal<>();
    private static final ThreadLocal<ILanguageIdentifierService> languageIdentifierService = new ThreadLocal<>();
    private static final ThreadLocal<ILanguagePathService> languagePathService = new ThreadLocal<>();
    private static final ThreadLocal<IProjectService> projectService = new ThreadLocal<>();
    private static final ThreadLocal<ISpoofaxLanguageSpecService> languageSpecService = new ThreadLocal<>();
    private static final ThreadLocal<ISourceTextService> sourceTextService = new ThreadLocal<>();
    private static final ThreadLocal<ISpoofaxUnitService> unitService = new ThreadLocal<>();
    private static final ThreadLocal<ISpoofaxSyntaxService> syntaxService = new ThreadLocal<>();
    private static final ThreadLocal<ITermFactoryService> termFactoryService = new ThreadLocal<>();
    private static final ThreadLocal<IStrategoCommon> strategoCommon = new ThreadLocal<>();
    private static final ThreadLocal<ISpoofaxTransformService> transformService = new ThreadLocal<>();
    private static final ThreadLocal<IContextService> contextService = new ThreadLocal<>();
    private static final ThreadLocal<IDialectService> dialectService = new ThreadLocal<>();
    private static final ThreadLocal<IPieProvider> pieProvider = new ThreadLocal<>();
    private static final ThreadLocal<StrIncr> strIncr = new ThreadLocal<>();

    public final File baseDir;
    public final URI baseURI;
    public final File depDir;

    public transient FileObject base;
    private transient @Nullable IProject project;
    public transient @Nullable ISpoofaxLanguageSpec languageSpec;


    public static void init(Injector newInjector) {
        if(injector.get() != null) {
            return;
        }

        injector.set(newInjector);
        resourceService.set(newInjector.getInstance(IResourceService.class));
        languageService.set(newInjector.getInstance(ILanguageService.class));
        languageIdentifierService.set(newInjector.getInstance(ILanguageIdentifierService.class));
        languagePathService.set(newInjector.getInstance(ILanguagePathService.class));
        projectService.set(newInjector.getInstance(IProjectService.class));
        languageSpecService.set(newInjector.getInstance(ISpoofaxLanguageSpecService.class));
        sourceTextService.set(newInjector.getInstance(ISourceTextService.class));
        unitService.set(newInjector.getInstance(ISpoofaxUnitService.class));
        syntaxService.set(newInjector.getInstance(ISpoofaxSyntaxService.class));
        termFactoryService.set(newInjector.getInstance(ITermFactoryService.class));
        strategoCommon.set(newInjector.getInstance(IStrategoCommon.class));
        transformService.set(newInjector.getInstance(ISpoofaxTransformService.class));
        contextService.set(newInjector.getInstance(IContextService.class));
        dialectService.set(newInjector.getInstance(IDialectService.class));
        pieProvider.set(newInjector.getInstance(IPieProvider.class));
        strIncr.set(newInjector.getInstance(StrIncr.class));
    }

    public static void deinit() {
        injector.set(null);
        resourceService.set(null);
        languageService.set(null);
        languageIdentifierService.set(null);
        languagePathService.set(null);
        projectService.set(null);
        languageSpecService.set(null);
        sourceTextService.set(null);
        unitService.set(null);
        syntaxService.set(null);
        termFactoryService.set(null);
        strategoCommon.set(null);
        transformService.set(null);
        contextService.set(null);
        dialectService.set(null);
        pieProvider.set(null);
        strIncr.set(null);
    }


    public SpoofaxContext(FileObject baseDir, FileObject depDir) {
        if(injector == null) {
            throw new MetaborgRuntimeException("Creating context while injector has not been set");
        }

        this.baseDir = toFile(baseDir);
        this.baseURI = FileUtils.toURI(baseDir);
        this.depDir = toFile(depDir);

        init();
    }

    public void init() {
        this.base = this.resourceService().resolve(baseURI);
        this.project = projectService.get().get(base);
        if(this.project == null) {
            this.languageSpec = null;
            return;
        }

        try {
            this.languageSpec = languageSpecService.get().get(project);
        } catch(ConfigException e) {
            throw new MetaborgRuntimeException(
                "Cannot convert project " + project + " into a language specification project", e);
        }
    }


    public IResourceService resourceService() {
        return resourceService.get();
    }

    public File toFile(FileObject fileObject) {
        return resourceService.get().localPath(fileObject);
    }

    public File toFileReplicate(FileObject fileObject) {
        return resourceService.get().localFile(fileObject);
    }

    public File basePath(String relative) {
        return new File(baseDir, relative);
    }

    public File depPath(String relative) {
        return new File(depDir, relative);
    }

    public ResourceAgentTracker newResourceTracker(String... excludePatterns) {
        final ResourceAgentTracker tracker = new ResourceAgentTracker(resourceService.get(), base, excludePatterns);
        final ResourceAgent agent = tracker.agent();
        agent.setAbsoluteWorkingDir(base);
        agent.setAbsoluteDefinitionDir(base);
        return tracker;
    }

    public IPieProvider pieProvider() {
        return pieProvider.get();
    }

    public @Nullable IStrategoTerm parse(File file) throws IOException, ParseException {
        final FileObject resource = resourceService.get().resolve(file);
        final ILanguageImpl language = languageIdentifierService.get().identify(resource);
        if(language == null) {
            return null;
        }
        final String text = sourceTextService.get().text(resource);
        final ISpoofaxInputUnit inputUnit = unitService.get().inputUnit(resource, text, language, null);
        final ISpoofaxParseUnit result = syntaxService.get().parse(inputUnit);
        if(!result.valid() || !result.success()) {
            return null;
        }
        final IStrategoTerm term = result.ast();
        return term;
    }

    public ILanguageService languageService() {
        return languageService.get();
    }

    public ILanguageIdentifierService languageIdentifierService() {
        return languageIdentifierService.get();
    }

    public ILanguagePathService languagePathService() {
        return languagePathService.get();
    }

    public ISourceTextService sourceTextService() {
        return sourceTextService.get();
    }

    public ISpoofaxUnitService unitService() {
        return unitService.get();
    }

    public ISpoofaxSyntaxService syntaxService() {
        return syntaxService.get();
    }

    public ITermFactoryService termFactoryService() {
        return termFactoryService.get();
    }

    public ITermFactory termFactory() {
        return termFactoryService.get().getGeneric();
    }


    public IStrategoCommon strategoCommon() {
        return strategoCommon.get();
    }

    public ISpoofaxTransformService transformService() {
        return transformService.get();
    }

    public IContextService contextService() {
        return contextService.get();
    }

    public IDialectService dialectService() {
        return dialectService.get();
    }

    public IProject project() {
        return project;
    }


    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();
        init();
    }

    public StrIncr getStrIncrTask() {
        return strIncr.get();
    }
}
