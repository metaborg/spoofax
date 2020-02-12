package org.metaborg.spoofax.core.stratego.primitive;

import com.google.inject.Inject;
import org.metaborg.core.context.IContext;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxContextPrimitive;
import org.metaborg.spoofax.core.dialogs.ISpoofaxDialogService;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Displays a message or question to the user.
 */
public final class ShowDialogPrimitive extends ASpoofaxContextPrimitive {

    private final ISpoofaxDialogService dialogService;

    @Inject public ShowDialogPrimitive(ISpoofaxDialogService dialogService) {
        super("show_dialog", 0, 4);
        this.dialogService = dialogService;
    }

    @Override protected IStrategoTerm call(
            IStrategoTerm current,
            Strategy[] svars, IStrategoTerm[] tvars,
            ITermFactory factory, IContext context) {

        // @formatter:off
        final String message           = Tools.isTermString(current) ? Tools.asJavaString(current) : "<empty>"; // TODO: Term to string
        @Nullable final String caption = (0 < tvars.length && Tools.isTermString(tvars[0])) ? Tools.asJavaString(tvars[0]) : null;
        @Nullable final String kind    = (1 < tvars.length && Tools.isTermString(tvars[1])) ? Tools.asJavaString(tvars[1]) : null;
        @Nullable final String options = (2 < tvars.length && Tools.isTermString(tvars[2])) ? Tools.asJavaString(tvars[2]) : null;
        final int defaultOption        = (3 < tvars.length && Tools.isTermInt(tvars[3]))    ? Tools.asJavaInt(tvars[3])    : 0;
        // @formatter:on

        @Nullable String result = invoke(message, caption, kind, options, defaultOption);

        return result != null ? factory.replaceTerm(current, factory.makeString(result)) : null;
    }

    /**
     * Displays a message or question to the user.
     *
     * @param message the message to display
     * @param caption the caption of the message; or {@code null} (default)
     * @param kind the kind of message, one of: "Error", "Warning", "Info", "Question", {@code null} (default)
     * @param options the options given to the user, one of: "Ok" (default), "Ok+Cancel", "Yes+No", "Yes+No+Cancel", "Retry+Cancel", "Abort+Retry+Ignore", {@code null} (default)
     * @param defaultOption the index of the default option to use
     * @return the name of the option chosen by the user; or {@code null} when the user dismissed the dialog or it could not be shown
     */
    @Nullable
    protected String invoke(String message, @Nullable String caption, @Nullable String kind, @Nullable String options, int defaultOption) {
        @Nullable ISpoofaxDialogService.DialogKind dialogKind = safeEnumValueOf(ISpoofaxDialogService.DialogKind.class, kind);
        List<ISpoofaxDialogService.DialogOption> dialogOptions = Arrays.stream(options.split("\\+")).map(ISpoofaxDialogService.DialogOption::new).collect(Collectors.toList());

        @Nullable ISpoofaxDialogService.DialogOption resultOption = dialogService.showDialog(
                message, caption, dialogKind, dialogOptions, defaultOption);

        return resultOption != null ? resultOption.getName() : null;
    }

    /**
     * Returns the enum value with the specified name;
     * or {@code null} when it fails.
     *
     * @param cls the class of enum
     * @param name the name to find
     * @param <T> the type of enum
     * @return the enum member; or {@code null} if not found
     */
    @Nullable
    private static <T extends Enum<T>> T safeEnumValueOf(Class<T> cls, String name) {
        if (name == null) return null;
        try {
            return Enum.valueOf(cls, name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
