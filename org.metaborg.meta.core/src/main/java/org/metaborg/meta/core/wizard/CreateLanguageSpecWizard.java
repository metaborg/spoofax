package org.metaborg.meta.core.wizard;

import java.util.Collection;
import java.util.List;

import org.metaborg.core.language.LanguageIdentifier;
import org.metaborg.core.language.LanguageVersion;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

/**
 * Helps with the validation and UI of a 'create language specification' wizard.
 */
public abstract class CreateLanguageSpecWizard {
    public static class ValidationResult {
        public final boolean complete;
        public final List<String> errors;


        public ValidationResult(boolean complete, List<String> errors) {
            this.complete = complete;
            this.errors = errors;
        }
    }

    private final boolean upgrade;


    public CreateLanguageSpecWizard(boolean upgrade) {
        this.upgrade = upgrade;
    }


    public String projectName() {
        return inputProjectName();
    }

    public String languageName() {
        return inputName();
    }

    public LanguageIdentifier languageIdentifier() {
        final LanguageVersion version = LanguageVersion.parse(inputVersion());
        return new LanguageIdentifier(inputGroupId(), inputId(), version);
    }

    public Collection<String> extensions() {
        return Splitter.on(',').trimResults().omitEmptyStrings().splitToList(inputExtensions());
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


    protected abstract boolean inputProjectNameModified();

    protected abstract String inputProjectName();

    protected abstract boolean inputNameModified();

    protected abstract String inputName();

    protected abstract boolean inputIdModified();

    protected abstract String inputId();

    protected abstract boolean inputGroupIdModified();

    protected abstract String inputGroupId();

    protected abstract boolean inputVersionModified();

    protected abstract String inputVersion();

    protected abstract boolean inputExtensionsModified();

    protected abstract String inputExtensions();


    protected abstract void setName(String name);

    protected abstract void setId(String id);

    protected abstract void setGroupId(String groupId);

    protected abstract void setVersion(String version);

    protected abstract void setExtensions(String extensions);


    public void setDefaults() {
        if(!inputGroupIdModified()) {
            setGroupId("org.example");
        }
        if(!inputVersionModified()) {
            setVersion("0.1.0-SNAPSHOT");
        }
    }

    public void distributeProjectName() {
        if(!inputNameModified() || inputName().isEmpty()) {
            final String name = toName(inputProjectName());
            setName(name);
            distributeLanguageName();
        }
        if(!inputIdModified() || inputId().isEmpty()) {
            final String id = toId(inputProjectName());
            setId(id);
        }
    }

    public void distributeLanguageName() {
        if(!inputExtensionsModified() || inputExtensions().isEmpty()) {
            final String extensions = toExtension(inputName());
            setExtensions(extensions);
        }
    }

    public ValidationResult validate() {
        boolean complete = true;
        final List<String> errors = Lists.newArrayList();

        if(!upgrade) {
            final String projectName = inputProjectName();
            if(inputProjectNameModified()) {
                if(projectName.isEmpty()) {
                    errors.add("Project name must be specified");
                } else if(!LanguageIdentifier.validId(projectName)) {
                    errors.add("Project name is invalid; " + LanguageIdentifier.errorDescription);
                }
            } else if(projectName.isEmpty()) {
                complete = false;
            }
        }

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
            } else if(!LanguageVersion.valid(version)) {
                errors.add("Version is invalid; " + LanguageVersion.errorDescription);
            }
        } else if(version.isEmpty()) {
            complete = false;
        }

        if(!upgrade) {
            final String extensions = inputExtensions();
            if(extensions.isEmpty() || extensions().isEmpty()) {
                complete = false;
                if(inputExtensionsModified()) {
                    errors.add("At least one extension must be specified");
                }
            }
        }

        complete = complete && errors.isEmpty();

        return new ValidationResult(complete, errors);
    }


    /**
     * Converts a project name to a language name.
     * 
     * @param name
     *            Project name.
     * @return Language name.
     */
    private static String toName(String name) {
        final char[] input = name.replace(' ', '-').toCharArray();
        final StringBuilder output = new StringBuilder();

        int i = 0;
        while(i < input.length) {
            final char c = input[i++];
            if(Character.isLetter(c) || c == '-' || c == '_') {
                output.append(c);
                break;
            }
        }
        while(i < input.length) {
            final char c = input[i++];
            if(Character.isLetterOrDigit(c) || c == '-' || c == '_') {
                output.append(c);
            }
        }

        return output.toString();
    }

    /**
     * Converts a project name to a language identifier.
     * 
     * @param name
     *            Project name.
     * @return Language identifier.
     */
    private static String toId(String name) {
        final char[] input = name.replace(' ', '-').toCharArray();
        final StringBuilder output = new StringBuilder();

        int i = 0;
        while(i < input.length) {
            final char c = input[i++];
            if(Character.isLetter(c) || c == '.' || c == '_') {
                output.append(c);
                break;
            }
        }
        while(i < input.length) {
            final char c = input[i++];
            if(Character.isLetterOrDigit(c) || c == '.' || c == '_')
                output.append(c);
        }

        final String result = output.toString().replaceAll("\\.(?=\\.|[0-9]|\\Z)", "");
        return result;
    }

    /**
     * Converts a language name to an extension.
     * 
     * @param name
     *            Language name.
     * @return Extension.
     */
    private static String toExtension(String name) {
        final String input = name.toLowerCase().replace("-", "").replace(".", "").replace(" ", "").replace(":", "");
        final String prefix = input.substring(0, Math.min(input.length(), 3));
        if(input.length() == 0) {
            return "";
        }

        for(int i = input.length() - 1;; i--) {
            if(!Character.isDigit(input.charAt(i)) && input.charAt(i) != '.') {
                return prefix + input.substring(Math.max(prefix.length(), Math.min(input.length(), i + 1)));
            } else if(i == prefix.length()) {
                return prefix + input.substring(i);
            }
        }
    }
}
