package org.metaborg.spoofax.core.syntax;

import java.io.Serializable;

public class MultiLineCommentCharacters implements Serializable {
    private static final long serialVersionUID = 4321993787571991571L;

    public final String prefix;
    public final String postfix;


    public MultiLineCommentCharacters(String prefix, String postfix) {
        this.prefix = prefix;
        this.postfix = postfix;
    }
}
