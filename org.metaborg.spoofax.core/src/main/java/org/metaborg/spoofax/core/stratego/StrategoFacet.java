package org.metaborg.spoofax.core.stratego;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.language.IFacet;
import org.metaborg.core.resource.ResourceService;
import org.metaborg.spoofax.core.analysis.StrategoAnalysisMode;

import com.google.common.collect.Lists;

/**
 * Represents the Stratego runtime facet of a language.
 */
public class StrategoFacet implements IFacet {
    private static final long serialVersionUID = -5993564430060643452L;

    private transient Iterable<FileObject> ctreeFiles;
    private transient Iterable<FileObject> jarFiles;
    private final @Nullable String analysisStrategy;
    private final @Nullable StrategoAnalysisMode analysisMode;
    private final @Nullable String onSaveStrategy;
    private final @Nullable String resolverStrategy;
    private final @Nullable String hoverStrategy;
    private final @Nullable String completionStrategy;


    /**
     * Creates a Stratego facet from Stratego provider files, and strategy names.
     * 
     * @param ctreeFile
     *            CTree provider files.
     * @param jarFiles
     *            JAR provider files.
     * @param analysisStrategy
     *            Name of the analysis strategy, or null if none.
     * @param analysisMode
     *            Analysis mode
     * @param onSaveStrategy
     *            Name of the on-save strategy, or null if none.
     * @param resolverStrategy
     *            Name of the reference resolution strategy, or null if none.
     * @param hoverStrategy
     *            Name of the hover strategy, or null if none.
     * @param completionStrategy
     *            Name of the semantic completions strategy, or null if none.
     */
    public StrategoFacet(Iterable<FileObject> ctreeFile, Iterable<FileObject> jarFiles,
        @Nullable String analysisStrategy, StrategoAnalysisMode analysisMode, @Nullable String onSaveStrategy,
        @Nullable String resolverStrategy, @Nullable String hoverStrategy, @Nullable String completionStrategy) {
        this.ctreeFiles = ctreeFile;
        this.jarFiles = jarFiles;
        this.analysisStrategy = analysisStrategy;
        this.analysisMode = analysisMode;
        this.onSaveStrategy = onSaveStrategy;
        this.resolverStrategy = resolverStrategy;
        this.hoverStrategy = hoverStrategy;
        this.completionStrategy = completionStrategy;
    }


    /**
     * @return Iterable over the ctree provider files.
     */
    public Iterable<FileObject> ctreeFiles() {
        return ctreeFiles;
    }

    /**
     * @return Iterable over the JAR provider files.
     */
    public Iterable<FileObject> jarFiles() {
        return jarFiles;
    }

    /**
     * @return Name of the analysis strategy, or null if none.
     */
    public @Nullable String analysisStrategy() {
        return analysisStrategy;
    }

    /**
     * @return Analysis mode, or null if none.
     */
    public @Nullable StrategoAnalysisMode analysisMode() {
        return analysisMode;
    }

    /**
     * @return Name of the on-save strategy, or null if none.
     */
    public @Nullable String onSaveStrategy() {
        return onSaveStrategy;
    }

    /**
     * @return Name of the reference resolver strategy, or null if none.
     */
    public @Nullable String resolverStrategy() {
        return resolverStrategy;
    }

    /**
     * @return Name of the hover strategy, or null if none.
     */
    public @Nullable String hoverStrategy() {
        return hoverStrategy;
    }

    /**
     * @return Name of the semantic completions strategy, or null if none.
     */
    public @Nullable String completionStrategy() {
        return completionStrategy;
    }


    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();

        List<FileObject> ctreeList = Lists.newArrayList(ctreeFiles);
        out.writeInt(ctreeList.size());
        for(FileObject fo : ctreeList)
            ResourceService.writeFileObject(fo, out);

        List<FileObject> jarList = Lists.newArrayList(jarFiles);
        out.writeInt(jarList.size());
        for(FileObject fo : jarList)
            ResourceService.writeFileObject(fo, out);
    }

    private void readObject(ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();

        List<FileObject> ctreeList = Lists.newArrayList();
        int ctreeCount = in.readInt();
        for(int i = 0; i < ctreeCount; i++)
            ctreeList.add(ResourceService.readFileObject(in));
        this.ctreeFiles = ctreeList;

        List<FileObject> jarList = Lists.newArrayList();
        int jarCount = in.readInt();
        for(int i = 0; i < jarCount; i++)
            jarList.add(ResourceService.readFileObject(in));
        this.jarFiles = jarList;
    }
}
