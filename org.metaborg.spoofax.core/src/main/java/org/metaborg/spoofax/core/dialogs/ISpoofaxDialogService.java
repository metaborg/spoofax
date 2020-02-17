package org.metaborg.spoofax.core.dialogs;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;


/**
 * Interface for dialogs.
 */
public interface ISpoofaxDialogService {

    /**
     * A dialog option.
     *
     * You can implement this class for your own custom options.
     */
    class DialogOption {

        // @formatter:off
        /** OK option. */
        public static DialogOption OK     = new DialogOption("OK");
        /** Cancel option. */
        public static DialogOption CANCEL = new DialogOption("Cancel");
        /** Yes option. */
        public static DialogOption YES    = new DialogOption("Yes");
        /** No option. */
        public static DialogOption NO     = new DialogOption("No");
        /** Retry option. */
        public static DialogOption RETRY  = new DialogOption("Retry");
        /** Abort option. */
        public static DialogOption ABORT  = new DialogOption("Abort");
        /** Ignore option. */
        public static DialogOption IGNORE = new DialogOption("Ignore");
        // @formatter:on

        private final String name;

        /**
         * Initializes a new instance of the {@link DialogOption} class.
         *
         * @param name the name of the option.
         */
        public DialogOption(String name) {
            this.name = name;
        }

        /**
         * Gets the name of the option.
         *
         * @return the name of the option
         */
        public String getName() {
            return this.name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DialogOption)) return false;
            DialogOption that = (DialogOption)o;
            return Objects.equals(this.name, that.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.name);
        }

        @Override
        public String toString() {
            return this.name;
        }

    }


    /**
     * Specifies the kind of dialog.
     */
    enum DialogKind {
        // NOTE: These names are used as constants in the ShowDialogPrimitive class.
        /** An unspecified dialog. */
        None,
        /** An informational dialog. */
        Info,
        /** A warning dialog. */
        Warning,
        /** An error dialog. */
        Error,
        /** A question dialog. */
        Question
    }


    /**
     * Shows a dialog to the user.
     *
     * @param message the text of the dialog
     * @param caption the caption of the dialog; or {@code null} to use the default
     * @param kind the kind of dialog; or {@code null} to use the default
     * @param options a list of options for the dialog; or {@code null} to use the default
     * @param defaultOption the index of the default option for the dialog
     * @return the option picked by the user; or {@code null} when the user dismissed the dialog
     * or when the dialog could not be displayed
     */
    @Nullable DialogOption showDialog(String message, @Nullable String caption, @Nullable DialogKind kind, @Nullable List<DialogOption> options, int defaultOption);

    /**
     * Shows an input dialog to the user.
     *
     * @param message the text of the dialog
     * @param caption the caption of the dialog; or {@code null} to use the default
     * @param initialValue the initial value; or {@code null} to use the default
     * @param validator the validator function, which either returns {@code null} on success or an error message on failure; or {@code null} to use no validator
     * @return the string input by the user; or {@code null} when the user cancelled or dismissed the dialog;
     * or when the dialog could not be displayed
     */
    @Nullable String showInputDialog(String message, @Nullable String caption, @Nullable String initialValue, @Nullable Function<String, String> validator);
}
