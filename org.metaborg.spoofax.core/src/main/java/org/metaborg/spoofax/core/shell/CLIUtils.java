package org.metaborg.spoofax.core.shell;

import java.io.OutputStream;

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.metaborg.core.MetaborgConstants;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.MetaborgRuntimeException;
import org.metaborg.core.language.ILanguage;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.messages.IMessage;
import org.metaborg.core.messages.IMessagePrinter;
import org.metaborg.core.messages.StreamMessagePrinter;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.ISimpleProjectService;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

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

    /** Get or create project for the given resource */
    public IProject getOrCreateProject(FileObject resource) throws MetaborgException {
        final ISimpleProjectService projectService = spoofax.injector.getInstance(ISimpleProjectService.class);
        final IProject project = projectService.get(resource);
        if(project == null) {
            return projectService.create(resource);
        }
        return project;
    }

    /** Get project for the given resource */
    public IProject getProject(FileObject location) throws MetaborgException {
        final IProject project = spoofax.projectService.get(location);
        if(project == null) {
            throw new MetaborgException(
                    "File " + location + " is not part of a project. Missing " + MetaborgConstants.FILE_CONFIG + "?");
        }
        return project;
    }

    /** Print formatted messages to the given output stream */
    public void printMessages(OutputStream os, Iterable<IMessage> messages) {
        IMessagePrinter printer = new StreamMessagePrinter(spoofax.sourceTextService, true, false, os, os, os);
        for(IMessage message : messages) {
            printer.print(message, false);
        }
    }

}