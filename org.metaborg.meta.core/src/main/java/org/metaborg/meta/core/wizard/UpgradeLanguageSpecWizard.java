package org.metaborg.meta.core.wizard;

import java.util.List;

import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.language.LanguageVersion;

import com.google.common.collect.Lists;

/**
 * Helps with the validation and UI of an 'upgrade language specification' wizard.
 */
public abstract class UpgradeLanguageSpecWizard {
    public static class ValidationResult {
        public final boolean complete;
        public final List<String> errors;


        public ValidationResult(boolean complete, List<String> errors) {
            this.complete = complete;
            this.errors = errors;
        }
    }


    public String languageName() {
        return inputName();
    }

    public LanguageIdentifier languageIdentifier() {
        final LanguageVersion version = LanguageVersion.parse(inputVersion());
        return new LanguageIdentifier(inputGroupId(), inputId(), version);
    }

    public String id() {
        return inputId();
    }

    public String groupId() {
        return inputGroupId();
    }

    public String version() {
        return inputVersion();
    }

    protected abstract boolean inputNameModified();

    protected abstract String inputName();

    protected abstract boolean inputIdModified();

    protected abstract String inputId();

    protected abstract boolean inputGroupIdModified();

    protected abstract String inputGroupId();

    protected abstract boolean inputVersionModified();

    protected abstract String inputVersion();


    protected abstract void setGroupId(String groupId);

    protected abstract void setVersion(String version);



    public void setDefaults() {
        if(!inputGroupIdModified()) {
            setGroupId("org.example");
        }
        if(!inputVersionModified()) {
            setVersion("0.1.0-SNAPSHOT");
        }
    }

    public ValidationResult validate() {
        boolean complete = true;
        final List<String> errors = Lists.newArrayList();

        final String name = inputName();
        if(inputNameModified()) {
            if(name.isEmpty()) {
                errors.add("Language name must be specified");
            } else if(!LanguageIdentifier.validId(name)) {
                errors.add("Language name is invalid; " + LanguageIdentifier.errorDescription);
            }
        } else if(name.isEmpty()) {
            complete = false;
        }

        final String id = inputId();
        if(inputIdModified()) {
            if(id.isEmpty()) {
                errors.add("Identifier must be specified");
            } else if(!LanguageIdentifier.validId(id)) {
                errors.add("Identifier is invalid; " + LanguageIdentifier.errorDescription);
            }
        } else if(id.isEmpty()) {
            complete = false;
        }

        final String groupId = inputGroupId();
        if(inputGroupIdModified()) {
            if(groupId.isEmpty()) {
                errors.add("Group identifier must be specified");
            } else if(!LanguageIdentifier.validId(groupId)) {
                errors.add("Group identifier is invalid; " + LanguageIdentifier.errorDescription);
            }
        } else if(groupId.isEmpty()) {
            complete = false;
        }

        final String version = inputVersion();
        if(inputVersionModified()) {
            if(version.isEmpty()) {
                errors.add("Version must be specified");
            }
        } else if(version.isEmpty()) {
            complete = false;
        }

        complete = complete && errors.isEmpty();

        return new ValidationResult(complete, errors);
    }
}
