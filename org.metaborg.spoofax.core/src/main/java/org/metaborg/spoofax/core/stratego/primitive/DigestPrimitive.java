package org.metaborg.spoofax.core.stratego.primitive;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxPrimitive;
import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;

public class DigestPrimitive extends ASpoofaxPrimitive {
    public DigestPrimitive() {
        super("digest", 0, 0);
    }


    @Override protected IStrategoTerm call(IStrategoTerm current, Strategy[] svars, IStrategoTerm[] tvars,
        ITermFactory factory, IContext context) {
        if(!(TermUtils.isString(current))) {
            return null;
        }
        final String str = TermUtils.toJavaString(current);
        final String hash;
        try {
            hash = sha256(str);
        } catch(NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
        return factory.makeString(hash);
    }

    private static String sha256(String str) throws NoSuchAlgorithmException {
        byte[] hash =
            MessageDigest.getInstance("SHA-256").digest(str.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            final int unsignedByte = 0xff & hash[i];
            final String hex = Integer.toHexString(unsignedByte);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
