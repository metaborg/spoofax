package org.metaborg.spoofax.generator.common;

import java.io.File;
import java.io.IOException;
import org.metaborg.spoofax.generator.BaseGenerator;

public class CommonGenerator extends BaseGenerator {

    public CommonGenerator(File root, String sdfMainModule) {
        super(root, sdfMainModule);
    }
    
    public void generateAll() throws IOException {
        generateCommonLibrary();
        generateRuntimeLibrary();
    }

    private void generateCommonLibrary() throws IOException {
        writer.write("lib/editor-common.generated.str", true);
        writer.write("lib/refactor-common.generated.str", true);
    }

    private void generateRuntimeLibrary() throws IOException {
        unpack("lib/runtime.zip");
    }

}
