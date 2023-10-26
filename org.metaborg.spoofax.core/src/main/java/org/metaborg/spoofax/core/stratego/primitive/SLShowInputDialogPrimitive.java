package org.metaborg.spoofax.core.stratego.primitive;

import org.metaborg.core.context.IContext;
import org.metaborg.spoofax.core.dialogs.ISpoofaxDialogService;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxContextPrimitive;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;

import jakarta.annotation.Nullable;
import java.util.function.Function;


/**
 * Displays an input dialog to the user.
 */
public final class SLShowInputDialogPrimitive extends ASpoofaxContextPrimitive {

    private final ISpoofaxDialogService dialogService;

    @jakarta.inject.Inject @javax.inject.Inject public SLShowInputDialogPrimitive(ISpoofaxDialogService dialogService) {
        super("SL_show_input_dialog", 0, 2);
        this.dialogService = dialogService;
    }

    @Override protected IStrategoTerm call(
            IStrategoTerm current,
            Strategy[] svars, IStrategoTerm[] tvars,
            ITermFactory factory, IContext context) {

        // @formatter:off
        final String message                    = TermUtils.asJavaString(current).orElse("<empty>" /* TODO: Term to string */);
        @Nullable final String caption          = TermUtils.asJavaString(tvars[0]).orElse(null);
        @Nullable final String initialValue     = TermUtils.asJavaString(tvars[1]).orElse(null);
        // @formatter:on

        // TODO: Support strategy as validator

        @Nullable String result = invoke(message, caption, initialValue, null);

        return result != null ? factory.replaceTerm(factory.makeString(result), current) : null;
    }

    /**
     * Displays an input dialog to the user.
     *
     * @param message the message to display
     * @param caption the caption of the message; or {@code null} (default)
     * @param initialValue the initial value; or {@code null} (default)
     * @param validator a function that returns {@code null} on success or an error message on failure; or {@code null} (default)
     * @return the input written by the user; or {@code null} when the user dismissed the dialog, canceled, or when it could not be shown
     */
    @Nullable
    protected String invoke(String message, @Nullable String caption, @Nullable String initialValue, @Nullable Function<String, String> validator) {
        return dialogService.showInputDialog(message, caption, initialValue, validator);
    }
}
