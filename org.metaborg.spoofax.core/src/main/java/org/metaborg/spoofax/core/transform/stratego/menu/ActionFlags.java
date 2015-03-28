package org.metaborg.spoofax.core.transform.stratego.menu;

public class ActionFlags {
    /**
     * Flag indicating if the strategy should be invoked on the parsed AST instead of the analyzed AST.
     */
    public boolean parsed;
    /**
     * Flag indicating if this is a meta-action, which is hidden from regular users.
     */
    public boolean meta;
    /**
     * Flag indicating if the result of this action should be shown in a new editor.
     */
    public boolean openEditor;
    /**
     * Flag indicating if this action should be updated in real time whenever the input changes.
     */
    public boolean realtime;


    public ActionFlags() {
        this(false, false, false, false);
    }

    public ActionFlags(boolean parsed, boolean meta, boolean openEditor, boolean realtime) {
        this.parsed = parsed;
        this.meta = meta;
        this.openEditor = openEditor;
        this.realtime = realtime;
    }


    public static ActionFlags merge(ActionFlags x, ActionFlags y) {
        return new ActionFlags(x.parsed || y.parsed, x.meta || y.meta, x.openEditor || y.openEditor, x.realtime
            || y.realtime);
    }
}
