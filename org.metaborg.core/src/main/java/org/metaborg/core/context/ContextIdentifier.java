package org.metaborg.core.context;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;

public class ContextIdentifier {
    public final IProject project;
    public final FileObject location;
    public final ILanguageImpl language;


    /**
     *
     * @param resource The resource.
     * @param project The project to which the resource belongs.
     * @param language
     */
    public ContextIdentifier(FileObject resource, IProject project, ILanguageImpl language) {
        this.project = project;
        this.location = resource;
        this.language = language;
    }


    @Override public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + location.hashCode();
        result = prime * result + language.hashCode();
        return result;
    }

    @Override public boolean equals(Object obj) {
        if(this == obj)
            return true;
        if(obj == null)
            return false;
        if(getClass() != obj.getClass())
            return false;
        final ContextIdentifier other = (ContextIdentifier) obj;
        if(!language.equals(other.language))
            return false;
        if(!location.equals(other.location))
            return false;
        return true;
    }
}
