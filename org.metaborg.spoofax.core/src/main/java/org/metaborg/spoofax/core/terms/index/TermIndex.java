package org.metaborg.spoofax.core.terms.index;

import javax.annotation.Nullable;

import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.terms.ISimpleTerm;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.attachments.AbstractTermAttachment;
import org.spoofax.terms.attachments.TermAttachmentType;

public class TermIndex extends AbstractTermAttachment implements ITermIndex {

    private static final long serialVersionUID = 5958528158971840392L;

    public static final TermAttachmentType<TermIndex> TYPE =
            new TermAttachmentType<TermIndex>(TermIndex.class, "TermIndex", 2) {
                @Override protected IStrategoTerm[] toSubterms(ITermFactory factory, TermIndex attachment) {
                    return new IStrategoTerm[] {
                        factory.makeString(attachment.resource),
                        factory.makeInt(attachment.nodeId),
                    };
                }
                @Override protected TermIndex fromSubterms(IStrategoTerm[] subterms) {
                    return new TermIndex(
                            Tools.asJavaString(subterms[0]),
                            Tools.asJavaInt(subterms[1]));
                }
            };

    private final String resource;
    private final int nodeId;
 
    private TermIndex(String resource, int nodeId) {
        this.resource = resource;
        this.nodeId = nodeId;
    }


    @Override
    public TermAttachmentType<TermIndex> getAttachmentType() {
        return TYPE;
    }

    public IStrategoTerm toTerm(ITermFactory factory) {
        return TYPE.toTerm(factory, this);
    }


	public String resource() {
        return resource;
    }

    public int nodeId() {
        return nodeId;
    }

    
    public static void put(IStrategoTerm term, String resource, int nodeId) {
        term.putAttachment(new TermIndex(resource, nodeId));
    }

    public static void put(IStrategoTerm term, IStrategoAppl index) {
        term.putAttachment(TermIndex.TYPE.fromTerm(index));
    }

    public static @Nullable TermIndex get(ISimpleTerm term) {
		return term.getAttachment(TYPE);
	}
	
}
