package org.metaborg.spoofax.meta.core.stratego.primitives;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.jsglr.client.imploder.ImploderAttachment;
import org.spoofax.terms.TermFactory;

public class GetSortNamePrimitive extends AbstractPrimitive {
    public GetSortNamePrimitive() {
        super("SSL_EXT_get_sort_imploder_attachment", 0, 1);
    }

    @Override public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
        ImploderAttachment ia = tvars[0].getAttachment(ImploderAttachment.TYPE);
        
        if(ia == null)
            return false;

        String sort = ia.getElementSort();
        
        if(sort == null)
            return false;
        
        TermFactory tf = new TermFactory();
        
        env.setCurrent(tf.makeString(sort));
        return true;
    }
}
