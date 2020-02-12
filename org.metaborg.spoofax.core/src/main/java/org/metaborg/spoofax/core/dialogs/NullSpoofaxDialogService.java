package org.metaborg.spoofax.core.dialogs;

import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;


/**
 * A no-op implementation of the dialog service.
 * This implementation always returns the default option.
 */
public final class NullSpoofaxDialogService implements ISpoofaxDialogService {

    @Override
    public @Nullable DialogOption showDialog(String message, @Nullable String caption, @Nullable DialogKind kind, @Nullable List<DialogOption> options, int defaultOption) {
        if (options == null) return DialogOption.OK;
        List<DialogOption> validOptions = options.stream().filter(o -> o != null && !o.getName().isEmpty()).collect(Collectors.toList());
        if (validOptions.size() == 0) return DialogOption.OK;
        return defaultOption >= 0 && defaultOption < validOptions.size() ? validOptions.get(defaultOption) : validOptions.get(0);
    }

}
