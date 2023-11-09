package org.metaborg.core.context;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageIdentifierService;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.util.log.ILogger;
import org.metaborg.util.log.LoggerUtils;

public class ContextUtils {
    private static final ILogger logger = LoggerUtils.logger(ContextUtils.class);


    /**
     * Gets a set of all contexts for given resources.
     * 
     * @param resources
     *            Resources to get contexts for.
     * @param project
     *            The project the resources belong to.
     * @param languageIdentifier
     *            Language identifier service.
     * @param contextService
     *            Context service.
     * @return Set of all contexts.
     */
    public static Set<IContext> getAll(Iterable<FileObject> resources, IProject project,
        ILanguageIdentifierService languageIdentifier, IContextService contextService) {
        final Set<IContext> contexts = new HashSet<IContext>();
        for(FileObject resource : resources) {
            final ILanguageImpl language = languageIdentifier.identify(resource, project);
            if(language == null) {
                logger.error("Could not identify language for {}", resource);
                continue;
            }
            try {
                contexts.add(contextService.get(resource, project, language));
            } catch(ContextException e) {
                final String message = String.format("Could not retrieve context for %s", resource);
                logger.error(message, e);
            }
        }
        return contexts;
    }
}
