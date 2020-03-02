package org.metaborg.spoofax.core.stratego.primitive;

import java.nio.charset.StandardCharsets;

import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxPrimitive;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import com.google.common.hash.Hashing;
import org.spoofax.terms.util.TermUtils;

public class DigestPrimitive extends ASpoofaxPrimitive {
    public DigestPrimitive() {
        super("digest", 0, 0);
    }


    @Override protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
        ITermFactory factory, IContext context) {
        if(!(current instanceof IStrategoString)) {
            return null;
        }
        final String str = TermUtils.toJavaString(current);
        final String hash = Hashing.sha256().hashString(str, StandardCharsets.UTF_8).toString();
        return factory.makeString(hash);
    }
}
