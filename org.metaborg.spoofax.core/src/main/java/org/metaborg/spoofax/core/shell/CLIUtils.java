package org.metaborg.spoofax.core.shell;

import java.io.IOException;
import java.io.OutputStream;
import java.util.NoSuchElementException;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgConstants;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.action.CompileGoal;
import org.metaborg.core.action.EndNamedGoal;
import org.metaborg.core.action.ITransformGoal;
import org.metaborg.core.action.TransformActionContrib;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.IMessagePrinter;
import org.metaborg.core.messages.WithLocationStreamMessagePrinter;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.ISimpleProjectService;
import org.metaborg.core.transform.ITransformConfig;
import org.metaborg.core.transform.TransformConfig;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.core.unit.ISpoofaxAnalyzeUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;
import org.metaborg.util.concurrent.IClosableLock;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.collect.Iterables;

public class CLIUtils {
    private static final ILogger logger = LoggerUtils.logger(CLIUtils.class);

    public static final String SPOOFAXPATH = "SPOOFAXPATH";

    private final Spoofax spoofax;

    public CLIUtils(Spoofax spoofax) {
        this.spoofax = spoofax;
    }

    /** Load languages from the SPOOFAXPATH environment variable */
    public void loadLanguagesFromPath() throws MetaborgException {
        final String spoofaxPath = System.getenv(SPOOFAXPATH);
        if(spoofaxPath != null) {
            for(String spoofaxPathComponentName : spoofaxPath.split(":")) {
                if(spoofaxPathComponentName.isEmpty()) {
                    continue;
                }
                final FileObject spoofaxPathComponent;
                try {
                    spoofaxPathComponent = spoofax.resourceService.resolve(spoofaxPathComponentName);
                } catch(MetaborgRuntimeException ex) {
                    String message = logger.format("Invalid path {} in " + SPOOFAXPATH, spoofaxPathComponentName);
                    logger.warn(message, ex);
                    continue;
                }
                try {
                    if(spoofaxPathComponent.isFolder()) {
                        spoofax.languageDiscoveryService.languagesFromDirectory(spoofaxPathComponent);
                    } else if(spoofaxPathComponent.isFile()) {
                        spoofax.languageDiscoveryService.languagesFromArchive(spoofaxPathComponent);
                    }
                } catch(FileSystemException ex) {
                    // ignore unknown path component
                } catch(MetaborgException ex) {
                    // ignore non-language path component
                }
            }
        }
    }

    /** Load a language from the directory or archive indicated by location */
    public ILanguageImpl loadLanguage(FileObject location) throws MetaborgException {
        try {
            if(location.isFolder()) {
                return spoofax.languageDiscoveryService.languageFromDirectory(location);
            } else if(location.isFile()) {
                return spoofax.languageDiscoveryService.languageFromArchive(location);
            } else {
                throw new MetaborgException("Cannot load language from location with type " + location.getType());
            }
        } catch(FileSystemException ex) {
            throw new MetaborgException(ex);
        }
    }

    /** Get a already loaded language by language name */
    public ILanguageImpl getLanguage(String languageName) throws MetaborgException {
        final ILanguage lang = spoofax.languageService.getLanguage(languageName);
        if(lang == null) {
            throw new MetaborgException("Cannot find language " + languageName);
        }
        final ILanguageImpl langImpl = lang.activeImpl();
        if(langImpl == null) {
            throw new MetaborgException("Language " + languageName + " has no active implementation");
        }
        return langImpl;
    }

    /** Get the current working directory */
    public FileObject getCWD() {
        final String cwdPath = System.getProperty("user.dir");
        return spoofax.resolve(cwdPath != null ? cwdPath : ".");
    }

    /** Get or create the project in the current working directory */
    public IProject getOrCreateCWDProject() throws MetaborgException {
        final String cwdPath = System.getProperty("user.dir");
        final FileObject cwd = spoofax.resolve(cwdPath != null ? cwdPath : ".");
        return getOrCreateProject(cwd);
    }

    /** Get or create project at the given location */
    public IProject getOrCreateProject(FileObject location) throws MetaborgException {
        final ISimpleProjectService projectService = spoofax.injector.getInstance(ISimpleProjectService.class);
        final IProject project = projectService.get(location);
        if(project == null) {
            return projectService.create(location);
        }
        return project;
    }

    /** Get project containing the given resource */
    public IProject getProject(FileObject location) throws MetaborgException {
        final IProject project = spoofax.projectService.get(location);
        if(project == null) {
            throw new MetaborgException(
                    "File " + location + " is not part of a project. Missing " + MetaborgConstants.FILE_CONFIG + "?");
        }
        return project;
    }

    /** Read file content */
    public ISpoofaxInputUnit read(FileObject resource, ILanguageImpl lang) throws MetaborgException {
        if(!spoofax.languageIdentifierService.identify(resource, lang)) {
            throw new MetaborgException(resource + " is not a file of " + lang.belongsTo().name());
        }
        final String text;
        try {
            text = spoofax.sourceTextService.text(resource);
        } catch(IOException e) {
            throw new MetaborgException("Cannot read " + resource, e);
        }
        final ISpoofaxInputUnit inputUnit = spoofax.unitService.inputUnit(resource, text, lang, null);
        return inputUnit;
    }

    /** Parse input unit */
    public ISpoofaxParseUnit parse(ISpoofaxInputUnit inputUnit, ILanguageImpl lang) throws MetaborgException {
        if(!spoofax.syntaxService.available(lang)) {
            throw new MetaborgException("Parsing not available.");
        }
        final ISpoofaxParseUnit parseUnit = spoofax.syntaxService.parse(inputUnit);
        if(!parseUnit.valid()) {
            throw new MetaborgException("Parsing failed.");
        }
        return parseUnit;
    }

    /** Analyze parse unit */
    public ISpoofaxAnalyzeUnit analyze(ISpoofaxParseUnit parseUnit, IContext context) throws MetaborgException {
        final ILanguageImpl lang = context.language();
        if(!spoofax.analysisService.available(lang)) {
            throw new MetaborgException("Analysis not available.");
        }
        final ISpoofaxAnalyzeUnit analysisUnit;
        try(IClosableLock lock = context.write()) {
            analysisUnit = spoofax.analysisService.analyze(parseUnit, context).result();
        }
        if(!analysisUnit.valid()) {
            throw new MetaborgException("Analysis failed.");
        }
        return analysisUnit;
    }

    /** Get compile action */
    public TransformActionContrib getCompileAction(ILanguageImpl lang) throws MetaborgException {
        return getTransformAction(new CompileGoal(), lang);
    }

    /** Get named transform action */
    public TransformActionContrib getNamedTransformAction(String name, ILanguageImpl lang) throws MetaborgException {
        return getTransformAction(new EndNamedGoal(name), lang);
    }

    /** Get a transform action for goal */
    public TransformActionContrib getTransformAction(ITransformGoal goal, ILanguageImpl lang) throws MetaborgException {
        if(!spoofax.actionService.available(lang, goal)) {
            throw new MetaborgException("Cannot find transformation " + goal);
        }
        final TransformActionContrib action;
        try {
            action = Iterables.getOnlyElement(spoofax.actionService.actionContributions(lang, goal));
        } catch(NoSuchElementException ex) {
            throw new MetaborgException("Transformation " + goal + " not a singleton.");
        }
        return action;
    }

    /** Transform analyze unit */
    public IStrategoTerm transform(ISpoofaxAnalyzeUnit analysisUnit, TransformActionContrib action, IContext context)
            throws MetaborgException {
        final ITransformConfig config = new TransformConfig(true);
        final ISpoofaxTransformUnit<ISpoofaxAnalyzeUnit> transformUnit =
                spoofax.transformService.transform(analysisUnit, context, action, config);
        if(!transformUnit.valid()) {
            throw new MetaborgException("Failed to transform " + transformUnit.source());
        }
        return transformUnit.ast();
    }

    /** Print formatted messages to the given output stream */
    public void printMessages(OutputStream os, Iterable<IMessage> messages) {
        final IMessagePrinter printer =
                new WithLocationStreamMessagePrinter(spoofax.sourceTextService, spoofax.projectService, os);
        for(IMessage message : messages) {
            printer.print(message, false);
        }
    }

}