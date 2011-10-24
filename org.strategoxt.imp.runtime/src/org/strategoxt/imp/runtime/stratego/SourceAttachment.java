package org.strategoxt.imp.runtime.stratego;

import java.io.File;
import java.io.FileNotFoundException;

import org.eclipse.core.resources.IResource;
import org.eclipse.imp.parser.IParseController;
import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.attachments.AbstractTermAttachment;
import org.spoofax.terms.attachments.ParentAttachment;
import org.spoofax.terms.attachments.TermAttachmentType;
import org.spoofax.terms.attachments.VolatileTermAttachmentType;

/** 
 * A tree-wide source resource and parse controller attachment.
 * 
 * Uses {@link ParentAttachment} to identify the root of a tree,
 * where this attachment is stored.
 * 
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class SourceAttachment extends AbstractTermAttachment {
	
	private static final long serialVersionUID = -8114392265614382463L;

	public static TermAttachmentType<SourceAttachment> TYPE =
		new VolatileTermAttachmentType<SourceAttachment>(SourceAttachment.class);
	
	private final IResource resource;
	
	private final IParseController controller;

	private SourceAttachment(IResource resource, IParseController controller) {
		this.resource = resource;
		this.controller = controller;
		assert resource != null;
	}
	
	public IResource getResource() {
		return resource;
	}
	
	public IParseController getParseController() {
		return controller;
	}

	public TermAttachmentType<SourceAttachment> getAttachmentType() {
		return TYPE;
	}

	public static IResource getResource(ISimpleTerm term) {
		SourceAttachment resource = ParentAttachment.getRoot(term).getAttachment(TYPE);
		if (resource == null) {
			while (term.getAttachment(ImploderAttachment.TYPE) == null && term.getSubtermCount() > 0)
				term = term.getSubterm(0);
			if (term.getAttachment(ImploderAttachment.TYPE) == null)
				return null;
			
			String file = ImploderAttachment.getFilename(term);
			try {
				return file == null ? null : EditorIOAgent.getResource(new File(file));
			} catch (FileNotFoundException e) {
				return null;
			}
		} else {
			return resource.resource;
		}
	}

	public static IParseController getParseController(ISimpleTerm term) {
		SourceAttachment resource = ParentAttachment.getRoot(term).getAttachment(TYPE);
		return resource == null ? null : resource.controller;
	}
	
	/**
	 * Sets the resource for a term tree.
	 * Should only be applied to the root of a tree.
	 */
	public static void putSource(ISimpleTerm term, IResource resource, IParseController controller) {
		ISimpleTerm root = ParentAttachment.getRoot(term);
		assert term == root;
		root.putAttachment(new SourceAttachment(resource, controller));
	}
}
