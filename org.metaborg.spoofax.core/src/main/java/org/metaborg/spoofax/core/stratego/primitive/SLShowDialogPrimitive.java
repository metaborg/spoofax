package org.metaborg.spoofax.core.stratego.primitive;

import org.metaborg.core.context.IContext;
import org.metaborg.spoofax.core.stratego.primitive.generic.ASpoofaxContextPrimitive;
import org.metaborg.spoofax.core.dialogs.ISpoofaxDialogService;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.ITermFactory;
import org.spoofax.terms.util.TermUtils;

import jakarta.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * Displays a message or question dialog to the user.
 */
public final class SLShowDialogPrimitive extends ASpoofaxContextPrimitive {

    private final ISpoofaxDialogService dialogService;

    @jakarta.inject.Inject public SLShowDialogPrimitive(ISpoofaxDialogService dialogService) {
        super("SL_show_dialog", 0, 4);
        this.dialogService = dialogService;
    }

    @Override protected IStrategoTerm call(
            IStrategoTerm current,
            Strategy[] svars, IStrategoTerm[] tvars,
            ITermFactory factory, IContext context) {

        // @formatter:off
        final String message           = TermUtils.asJavaString(current).orElse("<empty>" /* TODO: Term to string */);
        @Nullable final String caption = TermUtils.asJavaString(tvars[0]).orElse(null);
        @Nullable final String kind    = TermUtils.asJavaString(tvars[1]).orElse(null);
        @Nullable List<String> options = TermUtils.asJavaList(tvars[2])
                .map(l -> l.stream().map(TermUtils::asJavaString).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList()))
                .orElse(null);
        final int defaultOption        = TermUtils.asJavaInt(tvars[3]).orElse(0);
        // @formatter:on

        @Nullable String result = invoke(message, caption, kind, options, defaultOption);

        return result != null ? factory.replaceTerm(factory.makeString(result), current) : null;
    }

    /**
     * Displays a message or question to the user.
     *
     * @param message the message to display
     * @param caption the caption of the message; or {@code null} (default)
     * @param kind the kind of message, one of: "Error", "Warning", "Info", "Question", {@code null} (default)
     * @param options the options given to the user, a list of "OK", "Cancel", "Yes", "No", "Retry", "Abort", "Ignore"; or {@code null} (default)
     * @param defaultOption the index of the default option to use
     * @return the name of the option chosen by the user; or {@code null} when the user dismissed the dialog or it could not be shown
     */
    @Nullable
    protected String invoke(String message, @Nullable String caption, @Nullable String kind, @Nullable List<String> options, int defaultOption) {
        @Nullable ISpoofaxDialogService.DialogKind dialogKind = safeEnumValueOf(ISpoofaxDialogService.DialogKind.class, kind);
        List<ISpoofaxDialogService.DialogOption> dialogOptions = options != null ? options.stream().map(ISpoofaxDialogService.DialogOption::new).collect(Collectors.toList()) : null;

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
        for (T member : cls.getEnumConstants()) {
            if (member.name().compareToIgnoreCase(name) == 0) {
                return member;
            }
        }
        return null;
    }
}
