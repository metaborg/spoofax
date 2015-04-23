package org.metaborg.spoofax.generator;

import java.io.File;
import java.util.List;

public class Options {
    
    /** Set SDF module name */
    public final String sdfMainModule;

    public String transModuleName() {
        return sdfMainModule.toLowerCase();
    }

    /** Set input file (*.def)*/
    private File inputFile;

    /** Set editor file extensions */
    public List<String> editorExtensions;

    public String editorExtension() {
        return editorExtensions.get(0);
    }

    /** Set parse table (*.tbl) */
    private File parseTable;

    /** Set base package name */
    private String basePackage;

    public String packageName() {
        return basePackage != null ?
                basePackage :
                sdfMainModule.toLowerCase();
    }

    public String packagePath() {
        return packageName().replace(".", "/")+"/";
    }

    /** Set project name */
    private String projectName;

    /** Set start symbol */
    private String startSymbol = "Start";

    /** Set jar files to include */
    private List<File> jarLocations;

    /** Reset all generated files to their defaults */
    public boolean resetFiles;

    /** Enable generation of ignores for version control systems */
    public boolean generateVCIgnores;

    /** Generate only minimal project */
    public boolean generateMinimal;

    public Options(String sdfMainModule) {
        this.sdfMainModule = sdfMainModule;
    }

}
