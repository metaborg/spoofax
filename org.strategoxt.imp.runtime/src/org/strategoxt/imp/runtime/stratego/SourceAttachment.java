package org.strategoxt.imp.runtime.stratego;

import org.eclipse.core.resources.IResource;
import org.eclipse.imp.parser.IParseController;
import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.terms.attachments.AbstractTermAttachment;
import org.spoofax.terms.attachments.ParentAttachment;
import org.spoofax.terms.attachments.TermAttachmentType;

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
		TermAttachmentType.create(SourceAttachment.class);
	
	private final IResource resource;
	
	private final IParseController controller;

	private SourceAttachment(IResource resource, IParseController controller) {
		this.resource = resource;
		this.controller = controller;
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
		return resource == null ? null : resource.resource;
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
