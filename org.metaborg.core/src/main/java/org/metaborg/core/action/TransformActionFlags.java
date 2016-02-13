package org.metaborg.core.action;

public class TransformActionFlags {
    /**
     * Flag indicating if the strategy should be invoked on the parsed AST instead of the analyzed AST.
     */
    public boolean parsed;

    /**
     * Flag indicating if the result of this action should be shown in a new editor.
     */
    public boolean openEditor;

    /**
     * Flag indicating if this action should be updated in real time whenever the input changes.
     */
    public boolean realtime;


    public TransformActionFlags() {
        this(false, false, false);
    }

    public TransformActionFlags(boolean parsed, boolean openEditor, boolean realtime) {
        this.parsed = parsed;
        this.openEditor = openEditor;
        this.realtime = realtime;
    }


    public static TransformActionFlags merge(TransformActionFlags x, TransformActionFlags y) {
        return new TransformActionFlags(x.parsed || y.parsed, x.openEditor || y.openEditor, x.realtime || y.realtime);
    }
}
