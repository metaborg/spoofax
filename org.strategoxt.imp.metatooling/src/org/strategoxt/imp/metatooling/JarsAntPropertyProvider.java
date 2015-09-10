package org.strategoxt.imp.metatooling;

import java.io.File;
import java.nio.file.Paths;

import org.eclipse.ant.core.IAntPropertyValueProvider;
import org.strategoxt.imp.generator.sdf2imp.sdf2imp;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 */
public class JarsAntPropertyProvider implements IAntPropertyValueProvider {

    public String getAntPropertyValue(String antPropertyName) {
        String result;
        result = Paths
                .get(new File(sdf2imp.class.getProtectionDomain().getCodeSource().getLocation().getFile())
                        .getAbsolutePath(),
                        "dist").toFile().getAbsolutePath();
        return result;
    }
}
