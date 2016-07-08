package org.metaborg.spoofax.core.stratego.primitives;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;

import com.google.common.hash.Hashing;

public class DigestPrimitive extends AbstractPrimitive {
    private final MessageDigest digest;


    public DigestPrimitive() throws NoSuchAlgorithmException {
        super("digest", 0, 0);
        this.digest = MessageDigest.getInstance("SHA-256");
    }


    @Override public boolean call(IContext env, Strategy[] strategies, IStrategoTerm[] terms)
        throws InterpreterException {
        final IStrategoTerm strTerm = env.current();
        if(!(strTerm instanceof IStrategoString)) {
            return false;
        }
        final String str = Tools.asJavaString(strTerm);

        final String hash = Hashing.sha256().hashString(str, StandardCharsets.UTF_8).toString();

        env.setCurrent(env.getFactory().makeString(hash));
        return true;
    }
}
