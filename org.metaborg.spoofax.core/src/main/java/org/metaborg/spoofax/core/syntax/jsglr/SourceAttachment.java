package org.metaborg.spoofax.core.syntax.jsglr;

import static org.spoofax.jsglr.client.imploder.ImploderAttachment.hasImploderOrigin;
import static org.spoofax.terms.attachments.OriginAttachment.getOrigin;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.attachments.AbstractTermAttachment;
import org.spoofax.terms.attachments.ParentAttachment;
import org.spoofax.terms.attachments.TermAttachmentType;
import org.spoofax.terms.attachments.VolatileTermAttachmentType;

/**
 * A tree-wide source resource and parse controller attachment.
 * 
 * Uses {@link ParentAttachment} to identify the root of a tree, where this attachment is stored.
 */
public class SourceAttachment extends AbstractTermAttachment {
    private static final long serialVersionUID = -8114392265614382463L;

    public static TermAttachmentType<SourceAttachment> TYPE = new VolatileTermAttachmentType<SourceAttachment>(
        SourceAttachment.class);

    private final FileObject resource;
    private final IParserConfig controller;


    private SourceAttachment(FileObject resource, IParserConfig parserConfig) {
        this.resource = resource;
        this.controller = parserConfig;
    }


    public FileObject getFile() {
        return resource;
    }

    public IParserConfig getParseController() {
        return controller;
    }

    public TermAttachmentType<SourceAttachment> getAttachmentType() {
        return TYPE;
    }

    public static FileObject getResource(ISimpleTerm term, IResourceService resourceService) {
        final SourceAttachment resource = ParentAttachment.getRoot(term).getAttachment(TYPE);
        if(resource != null) {
            return resource.resource;
        }

        while(!hasImploderOrigin(term) && term.getSubtermCount() > 0) {
            term = term.getSubterm(0);
        }

        if(term.getAttachment(ImploderAttachment.TYPE) == null) {
            term = getOrigin(term);
        }
        if(term == null || term.getAttachment(ImploderAttachment.TYPE) == null) {
            return null;
        }

        final String fileName = ImploderAttachment.getFilename(term);
        return resourceService.resolve(fileName);
    }

    public static IParserConfig getParserConfig(ISimpleTerm term) {
        SourceAttachment resource = ParentAttachment.getRoot(term).getAttachment(TYPE);
        return resource == null ? null : resource.controller;
    }

    /**
     * Sets the resource for a term tree. Should only be applied to the root of a tree.
     */
    public static void putSource(ISimpleTerm term, FileObject resource, IParserConfig controller) {
        ISimpleTerm root = ParentAttachment.getRoot(term);
        assert term == root;
        root.putAttachment(new SourceAttachment(resource, controller));
    }
}
