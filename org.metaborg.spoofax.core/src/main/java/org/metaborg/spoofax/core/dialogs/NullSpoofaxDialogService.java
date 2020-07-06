package org.metaborg.spoofax.core.dialogs;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Function;


/**
 * A no-op implementation of the dialog service.
 * This implementation always dismisses the dialog.
 */
public final class NullSpoofaxDialogService implements ISpoofaxDialogService {

    @Override
    @Nullable
    public DialogOption showDialog(String message, @Nullable String caption, @Nullable DialogKind kind, @Nullable List<DialogOption> options, int defaultOption) {
        return null;
    }

    @Override
    @Nullable
    public String showInputDialog(String message, @Nullable String caption, @Nullable String initialValue, @Nullable Function<String, String> validator) {
        return null;
    }

}
