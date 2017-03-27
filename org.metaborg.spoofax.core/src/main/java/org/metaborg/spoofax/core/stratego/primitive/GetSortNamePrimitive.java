package org.metaborg.spoofax.core.stratego.primitive;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;

public class GetSortNamePrimitive extends AbstractPrimitive {
    public GetSortNamePrimitive() {
        super("SSL_EXT_get_sort_imploder_attachment", 0, 1);
    }


    @Override public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
        final ImploderAttachment attachment = tvars[0].getAttachment(ImploderAttachment.TYPE);
        if(attachment == null) {
            return false;
        }
        final String sort = attachment.getElementSort();
        if(sort == null) {
            return false;
        }
        env.setCurrent(env.getFactory().makeString(sort));
        return true;
    }
}
